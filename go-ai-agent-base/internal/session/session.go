// Package session stores conversation state. Two backends:
// in-memory (default) and Redis (production).
package session

import (
	"context"
	"errors"
	"fmt"
	"sync"
	"time"

	"github.com/xarch/go-ai-agent-base/internal/agent"
	"github.com/google/uuid"
)

// Factory builds a SessionStore from configuration.
func Factory(backend string, ttl time.Duration, maxMessages int, redisURL string) (agent.SessionStore, error) {
	switch backend {
	case "memory", "":
		return NewInMemoryStore(ttl, maxMessages), nil
	case "redis":
		return NewRedisStore(redisURL, ttl, maxMessages)
	default:
		return nil, fmt.Errorf("session: unknown backend %s", backend)
	}
}

// ---- in-memory ----------------------------------------------------------

type inMemSession struct {
	id        string
	userID    string
	mu        sync.RWMutex
	messages  []agent.Message
	expiresAt time.Time
}

func (s *inMemSession) ID() string { return s.id }
func (s *inMemSession) UserID() string { return s.userID }

func (s *inMemSession) Get(_ context.Context) ([]agent.Message, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	if !s.expiresAt.IsZero() && time.Now().After(s.expiresAt) {
		return nil, nil
	}
	out := make([]agent.Message, len(s.messages))
	copy(out, s.messages)
	return out, nil
}

func (s *inMemSession) Append(_ context.Context, msg agent.Message) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.messages = append(s.messages, msg)
	return nil
}

func (s *inMemSession) Clear(_ context.Context) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.messages = nil
	return nil
}

func (s *inMemSession) Close() error { return nil }

// InMemoryStore is the default SessionStore.
type InMemoryStore struct {
	ttl         time.Duration
	maxMessages int
	mu          sync.RWMutex
	sessions    map[string]*inMemSession
}

// NewInMemoryStore creates a new in-memory session store.
func NewInMemoryStore(ttl time.Duration, maxMessages int) *InMemoryStore {
	if ttl == 0 {
		ttl = 24 * time.Hour
	}
	if maxMessages == 0 {
		maxMessages = 100
	}
	return &InMemoryStore{
		ttl:         ttl,
		maxMessages: maxMessages,
		sessions:    map[string]*inMemSession{},
	}
}

// Get implements agent.SessionStore.
func (s *InMemoryStore) Get(_ context.Context, sessionID, userID string) (agent.Session, error) {
	s.mu.RLock()
	sess, ok := s.sessions[sessionID]
	s.mu.RUnlock()
	if ok {
		if !sess.expiresAt.IsZero() && time.Now().After(sess.expiresAt) {
			sess = nil
		} else {
			return sess, nil
		}
	}
	if userID == "" {
		return nil, errors.New("session not found and no user_id to create a new one")
	}
	return s.Create(context.Background(), userID)
}

// Create implements agent.SessionStore.
func (s *InMemoryStore) Create(_ context.Context, userID string) (agent.Session, error) {
	sess := &inMemSession{
		id:        uuid.NewString(),
		userID:    userID,
		messages:  []agent.Message{},
		expiresAt: time.Now().Add(s.ttl),
	}
	s.mu.Lock()
	s.sessions[sess.id] = sess
	s.mu.Unlock()
	return sess, nil
}

// Delete implements agent.SessionStore.
func (s *InMemoryStore) Delete(_ context.Context, sessionID string) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	delete(s.sessions, sessionID)
	return nil
}

// Close implements agent.SessionStore.
func (s *InMemoryStore) Close() error { return nil }

// ---- Redis stub ---------------------------------------------------------

// NewRedisStore returns a placeholder; production callers should replace
// with a real Redis-backed implementation.
func NewRedisStore(url string, ttl time.Duration, maxMessages int) (agent.SessionStore, error) {
	if url == "" {
		return nil, errors.New("session.redis.url required")
	}
	_ = ttl
	_ = maxMessages
	return nil, errors.New("redis session backend not yet implemented in this scaffold")
}