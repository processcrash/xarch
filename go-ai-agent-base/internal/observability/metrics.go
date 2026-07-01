package observability

import (
	"net/http"

	"github.com/prometheus/client_golang/prometheus"
	"github.com/prometheus/client_golang/prometheus/promauto"
	"github.com/prometheus/client_golang/prometheus/promhttp"
)

// Metrics holds the platform-wide Prometheus collectors. Single
// instance, safe for concurrent use. Mount at /metrics via MetricsHandler.
type Metrics struct {
	registry        *prometheus.Registry
	HTTPRequests    *prometheus.CounterVec
	HTTPDuration    *prometheus.HistogramVec
	AgentRuns       *prometheus.CounterVec
	AgentDuration   *prometheus.HistogramVec
	TokensUsed      *prometheus.CounterVec
	ToolCalls       *prometheus.CounterVec
	Errors          *prometheus.CounterVec
	ActiveSessions  prometheus.Gauge
	LLMLatency      *prometheus.HistogramVec
}

// NewMetrics constructs the platform metrics. Returns the registry too
// so callers can mount a /metrics handler scoped to just these.
func NewMetrics() *Metrics {
	reg := prometheus.NewRegistry()
	factory := promauto.With(reg)
	return &Metrics{
		registry: reg,
		HTTPRequests: factory.NewCounterVec(
			prometheus.CounterOpts{Name: "xarch_http_requests_total", Help: "Total HTTP requests"},
			[]string{"method", "path", "status"},
		),
		HTTPDuration: factory.NewHistogramVec(
			prometheus.HistogramOpts{
				Name:    "xarch_http_request_duration_seconds",
				Help:    "HTTP request duration",
				Buckets: prometheus.ExponentialBuckets(0.001, 2, 14),
			},
			[]string{"method", "path"},
		),
		AgentRuns: factory.NewCounterVec(
			prometheus.CounterOpts{Name: "xarch_agent_runs_total", Help: "Agent invocations"},
			[]string{"agent", "status"},
		),
		AgentDuration: factory.NewHistogramVec(
			prometheus.HistogramOpts{
				Name:    "xarch_agent_run_duration_seconds",
				Help:    "Agent run latency",
				Buckets: prometheus.ExponentialBuckets(0.01, 2, 12),
			},
			[]string{"agent"},
		),
		TokensUsed: factory.NewCounterVec(
			prometheus.CounterOpts{Name: "xarch_llm_tokens_total", Help: "LLM tokens consumed"},
			[]string{"provider", "model", "kind"}, // kind = prompt|completion
		),
		ToolCalls: factory.NewCounterVec(
			prometheus.CounterOpts{Name: "xarch_tool_calls_total", Help: "Tool invocations"},
			[]string{"tool", "status"},
		),
		Errors: factory.NewCounterVec(
			prometheus.CounterOpts{Name: "xarch_errors_total", Help: "Errors by subsystem"},
			[]string{"subsystem", "kind"},
		),
		ActiveSessions: factory.NewGauge(prometheus.GaugeOpts{
			Name: "xarch_active_sessions", Help: "Currently active sessions",
		}),
		LLMLatency: factory.NewHistogramVec(
			prometheus.HistogramOpts{
				Name:    "xarch_llm_call_duration_seconds",
				Help:    "Per-LLM-call latency",
				Buckets: prometheus.ExponentialBuckets(0.05, 2, 12),
			},
			[]string{"provider", "model"},
		),
	}
}

// Registry exposes the underlying registry so callers can add their own
// collectors.
func (m *Metrics) Registry() *prometheus.Registry { return m.registry }

// Handler returns an HTTP handler that exposes the /metrics endpoint.
func (m *Metrics) Handler() http.Handler {
	return promhttp.HandlerFor(m.registry, promhttp.HandlerOpts{})
}