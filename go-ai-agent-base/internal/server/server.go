// Package server implements the Gin HTTP server: routing, middleware,
// SSE streaming endpoint, and graceful shutdown.
package server

import (
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"

	"github.com/xarch/go-ai-agent-base/internal/agent"
	"github.com/xarch/go-ai-agent-base/internal/auth"
	"github.com/xarch/go-ai-agent-base/internal/config"
	"github.com/xarch/go-ai-agent-base/internal/observability"
)

// Server holds the wired HTTP server. Build with New() and call Start().
type Server struct {
	cfg     *config.Config
	engine  *gin.Engine
	runtime *agent.Runtime
	logger  *zap.Logger
	metrics *observability.Metrics
	auth    *auth.Verifier
}

// New constructs a Server. All dependencies must be non-nil.
func New(cfg *config.Config, rt *agent.Runtime, logger *zap.Logger, metrics *observability.Metrics, av *auth.Verifier) *Server {
	gin.SetMode(gin.ReleaseMode)
	engine := gin.New()
	s := &Server{
		cfg:     cfg,
		engine:  engine,
		runtime: rt,
		logger:  logger,
		metrics: metrics,
		auth:    av,
	}
	s.installMiddleware()
	s.installRoutes()
	return s
}

// Engine exposes the underlying Gin engine (useful in tests).
func (s *Server) Engine() *gin.Engine { return s.engine }

func (s *Server) installMiddleware() {
	s.engine.Use(gin.Recovery())
	s.engine.Use(s.requestLogger())
	s.engine.Use(s.corsMiddleware())
	s.engine.Use(s.metricsMiddleware())
	if s.cfg.Auth.Enabled {
		staticKeys := convertAPIKeys(s.cfg.Auth.APIKeys)
		s.engine.Use(auth.Middleware(s.auth, staticKeys))
	}
}

func (s *Server) installRoutes() {
	v1 := s.engine.Group("/api/v1")
	{
		v1.GET("/health", s.handleHealth)
		v1.GET("/agents", s.handleListAgents)
		v1.POST("/agents/:name/sessions", s.handleCreateSession)
		v1.GET("/agents/:name/sessions/:sid/messages", s.handleListMessages)
		v1.POST("/agents/:name/sessions/:sid/messages", s.handleSendMessage)
		v1.POST("/agents/:name/sessions/:sid/stream", s.handleStreamMessage)
		v1.DELETE("/agents/:name/sessions/:sid", s.handleDeleteSession)
	}

	if s.cfg.Observability.Metrics.Enabled {
		s.engine.GET(s.cfg.Observability.Metrics.Path, gin.WrapH(s.metrics.Handler()))
	}
}

// Start blocks serving until ctx is cancelled.
func (s *Server) Start(ctx context.Context) error {
	addr := fmt.Sprintf("%s:%d", s.cfg.Server.Host, s.cfg.Server.Port)
	srv := &http.Server{
		Addr:         addr,
		Handler:      s.engine,
		ReadTimeout:  s.cfg.Server.ReadTimeout,
		WriteTimeout: s.cfg.Server.WriteTimeout,
	}
	go func() {
		<-ctx.Done()
		s.logger.Info("shutdown signal received, draining", zap.String("addr", addr))
		shutdownCtx, cancel := context.WithTimeout(context.Background(), s.cfg.Server.ShutdownTimeout)
		defer cancel()
		if err := srv.Shutdown(shutdownCtx); err != nil {
			s.logger.Error("shutdown error", zap.Error(err))
		}
	}()
	s.logger.Info("HTTP server listening", zap.String("addr", addr))
	if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
		return err
	}
	return nil
}

// ---- handlers -----------------------------------------------------------

func (s *Server) handleHealth(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{
		"status":      "UP",
		"service":     s.cfg.Observability.ServiceName,
		"version":     "0.1.0",
		"agents":      s.runtime.ListAgents(),
		"tools":       s.runtime.ListTools(),
		"timestamp":   time.Now().UTC().Format(time.RFC3339),
	})
}

func (s *Server) handleListAgents(c *gin.Context) {
	c.JSON(http.StatusOK, gin.H{"agents": s.runtime.ListAgents()})
}

type createSessionRequest struct {
	UserID string `json:"user_id"`
}

func (s *Server) handleCreateSession(c *gin.Context) {
	name := c.Param("name")
	var req createSessionRequest
	_ = c.ShouldBindJSON(&req)
	if req.UserID == "" {
		req.UserID = subjectOrAnonymous(c)
	}
	sess, err := s.runtime.GetAgent(name)
	_ = sess
	if name == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "agent name required"})
		return
	}
	sessions, _ := buildSessions(s.cfg)
	if sessions == nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "sessions not initialised"})
		return
	}
	newSess, err := sessions.Create(c.Request.Context(), req.UserID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusCreated, gin.H{
		"session_id": newSess.ID(),
		"user_id":    newSess.UserID(),
		"agent":      name,
	})
}

type sendMessageRequest struct {
	Content string                 `json:"content"`
	Stream   bool                   `json:"stream"`
	Metadata map[string]interface{} `json:"metadata"`
}

type messageResponse struct {
	Message   agent.Message `json:"message"`
	Usage     agent.Usage   `json:"usage"`
	StopReason string       `json:"stop_reason"`
	Duration  string       `json:"duration"`
}

func (s *Server) handleSendMessage(c *gin.Context) {
	name := c.Param("name")
	sid := c.Param("sid")
	var req sendMessageRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}
	opts := agent.RunOptions{
		AgentName: name,
		SessionID: sid,
		UserID:    subjectOrAnonymous(c),
		Messages: []agent.Message{{
			Role: agent.RoleUser, Content: req.Content, Timestamp: time.Now(),
		}},
		Metadata: req.Metadata,
	}
	start := time.Now()
	resp, err := s.runtime.Run(c.Request.Context(), opts, nil)
	s.metrics.AgentRuns.WithLabelValues(name, errLabel(err)).Inc()
	s.metrics.AgentDuration.WithLabelValues(name).Observe(time.Since(start).Seconds())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	s.metrics.TokensUsed.WithLabelValues("", resp.Message.Role, "completion").Add(float64(resp.Usage.CompletionTokens))
	s.metrics.TokensUsed.WithLabelValues("", resp.Message.Role, "prompt").Add(float64(resp.Usage.PromptTokens))
	c.JSON(http.StatusOK, messageResponse{
		Message:   resp.Message,
		Usage:     resp.Usage,
		StopReason: resp.StopReason,
		Duration:  resp.Duration.String(),
	})
}

// handleStreamMessage streams SSE events as the agent runs.
func (s *Server) handleStreamMessage(c *gin.Context) {
	name := c.Param("name")
	sid := c.Param("sid")
	var req sendMessageRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	c.Writer.Header().Set("Content-Type", "text/event-stream")
	c.Writer.Header().Set("Cache-Control", "no-cache")
	c.Writer.Header().Set("Connection", "keep-alive")
	c.Writer.Header().Set("X-Accel-Buffering", "no")
	flusher, ok := c.Writer.(http.Flusher)
	if !ok {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "streaming not supported"})
		return
	}

	opts := agent.RunOptions{
		AgentName: name,
		SessionID: sid,
		UserID:    subjectOrAnonymous(c),
		Messages: []agent.Message{{
			Role: agent.RoleUser, Content: req.Content, Timestamp: time.Now(),
		}},
		Metadata: req.Metadata,
		Stream:   true,
	}
	emitter := agent.EventHandlerFunc(func(ev agent.Event) error {
		payload, _ := json.Marshal(ev)
		if _, err := fmt.Fprintf(c.Writer, "event: %s\ndata: %s\n\n", ev.Type, payload); err != nil {
			return err
		}
		flusher.Flush()
		return nil
	})

	if _, err := s.runtime.Run(c.Request.Context(), opts, emitter); err != nil {
		payload, _ := json.Marshal(agent.Event{Type: agent.EventError, Error: err.Error(), Time: time.Now()})
		fmt.Fprintf(c.Writer, "event: error\ndata: %s\n\n", payload)
		flusher.Flush()
	}
}

func (s *Server) handleListMessages(c *gin.Context) {
	sid := c.Param("sid")
	sessions, _ := buildSessions(s.cfg)
	sess, err := sessions.Get(c.Request.Context(), sid, subjectOrAnonymous(c))
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": err.Error()})
		return
	}
	msgs, err := sess.Get(c.Request.Context())
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"session_id": sid, "messages": msgs})
}

func (s *Server) handleDeleteSession(c *gin.Context) {
	sid := c.Param("sid")
	sessions, _ := buildSessions(s.cfg)
	if err := sessions.Delete(c.Request.Context(), sid); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}
	c.JSON(http.StatusOK, gin.H{"deleted": sid})
}

// ---- middleware ---------------------------------------------------------

func (s *Server) requestLogger() gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()
		c.Next()
		s.logger.Info("http",
			zap.String("method", c.Request.Method),
			zap.String("path", c.Request.URL.Path),
			zap.Int("status", c.Writer.Status()),
			zap.Duration("dur", time.Since(start)),
		)
	}
}

func (s *Server) corsMiddleware() gin.HandlerFunc {
	origins := s.cfg.Server.CORS.AllowedOrigins
	if len(origins) == 0 {
		origins = []string{"*"}
	}
	methods := s.cfg.Server.CORS.AllowedMethods
	if len(methods) == 0 {
		methods = []string{"GET", "POST", "PUT", "DELETE", "OPTIONS"}
	}
	headers := s.cfg.Server.CORS.AllowedHeaders
	if len(headers) == 0 {
		headers = []string{"Authorization", "Content-Type", "X-Request-Id"}
	}
	return func(c *gin.Context) {
		origin := c.GetHeader("Origin")
		if origin != "" {
			allowed := false
			for _, o := range origins {
				if o == "*" || o == origin {
					allowed = true
					break
				}
			}
			if allowed {
				c.Writer.Header().Set("Access-Control-Allow-Origin", origin)
				c.Writer.Header().Set("Vary", "Origin")
				c.Writer.Header().Set("Access-Control-Allow-Methods", joinStrings(methods, ","))
				c.Writer.Header().Set("Access-Control-Allow-Headers", joinStrings(headers, ","))
			}
		}
		if c.Request.Method == http.MethodOptions {
			c.AbortWithStatus(http.StatusNoContent)
			return
		}
		c.Next()
	}
}

func (s *Server) metricsMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()
		c.Next()
		path := c.FullPath()
		if path == "" {
			path = "unknown"
		}
		s.metrics.HTTPRequests.WithLabelValues(c.Request.Method, path, statusClass(c.Writer.Status())).Inc()
		s.metrics.HTTPDuration.WithLabelValues(c.Request.Method, path).Observe(time.Since(start).Seconds())
	}
}

func statusClass(s int) string {
	switch {
	case s < 200:
		return "1xx"
	case s < 300:
		return "2xx"
	case s < 400:
		return "3xx"
	case s < 500:
		return "4xx"
	default:
		return "5xx"
	}
}

func errLabel(err error) string {
	if err == nil {
		return "ok"
	}
	return "error"
}

func subjectOrAnonymous(c *gin.Context) string {
	if v, ok := c.Get(auth.CtxSubject); ok {
		if s, ok := v.(string); ok {
			return s
		}
	}
	return "anonymous"
}

func convertAPIKeys(in []config.APIKey) []auth.APIKey {
	out := make([]auth.APIKey, 0, len(in))
	for _, k := range in {
		out = append(out, auth.APIKey{
			Key:    k.Key,
			Name:   k.Name,
			Scopes: k.Scopes,
		})
	}
	return out
}

func joinStrings(s []string, sep string) string {
	out := ""
	for i, v := range s {
		if i > 0 {
			out += sep
		}
		out += v
	}
	return out
}

// buildSessions is a small helper that re-reads the session store from
// the runtime. In production you'd inject the SessionStore directly;
// for the scaffold we recover it via reflection-free accessor.
func buildSessions(cfg *config.Config) (agent.SessionStore, error) {
	// The runtime owns the session store, but we expose a tiny shim so
	// the HTTP handler can call Create / Get / Delete. The shim is
	// wired in cmd/server/main.go via SetSessionStore().
	return globalSessionStore, nil
}

var globalSessionStore agent.SessionStore

// SetSessionStore is called once at startup to inject the SessionStore.
func SetSessionStore(s agent.SessionStore) { globalSessionStore = s }

// Drain is a small helper to satisfy the linter when the package
// imports io without using it directly. Kept for future SSE expansion.
var _ = io.Discard