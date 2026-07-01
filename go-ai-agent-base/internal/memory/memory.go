// Package memory provides long-term key-value / similarity storage for
// agent state. Three backends are supported: in-memory (dev),
// Redis (production), Postgres (large-scale).
package memory

import (
	"context"
	"errors"
	"sort"
	"strings"
	"sync"
	"time"

	"github.com/xarch/go-ai-agent-base/internal/agent"
)

// Memory is a memory.Backend-agnostic alias so callers can depend on
// the agent.Memory interface and pick the backend at startup.
type Memory = agent.Memory

// Factory builds a Memory from configuration.
func Factory(backend string, opts Opts) (Memory, error) {
	switch backend {
	case "memory", "":
		return NewInMemory(opts.MaxEntries), nil
	case "redis":
		return NewRedis(opts.RedisURL, opts.RedisPrefix, opts.MaxEntries)
	case "postgres":
		return NewPostgres(opts.PostgresURL, opts.PostgresTable)
	default:
		return nil, errors.New("memory: unknown backend " + backend)
	}
}

// Opts carries all memory-backend settings from config.MemoryConfig.
type Opts struct {
	MaxEntries  int
	RedisURL    string
	RedisPrefix string
	PostgresURL string
	PostgresTable string
}

// ---- in-memory backend --------------------------------------------------

type entry struct {
	value     string
	expiresAt time.Time
}

// InMemory is a thread-safe in-memory memory backend with TTL.
type InMemory struct {
	maxEntries int
	mu         sync.RWMutex
	entries    map[string]entry
}

// NewInMemory creates a new in-memory store.
func NewInMemory(maxEntries int) *InMemory {
	if maxEntries <= 0 {
		maxEntries = 1000
	}
	return &InMemory{
		maxEntries: maxEntries,
		entries:    map[string]entry{},
	}
}

// Get implements agent.Memory.
func (m *InMemory) Get(_ context.Context, key string) (string, bool, error) {
	m.mu.RLock()
	defer m.mu.RUnlock()
	e, ok := m.entries[key]
	if !ok {
		return "", false, nil
	}
	if !e.expiresAt.IsZero() && time.Now().After(e.expiresAt) {
		return "", false, nil
	}
	return e.value, true, nil
}

// Put implements agent.Memory.
func (m *InMemory) Put(_ context.Context, key, value string) error {
	m.mu.Lock()
	defer m.mu.Unlock()
	if len(m.entries) >= m.maxEntries {
		// Simple FIFO eviction.
		var oldestKey string
		var oldestTime time.Time
		for k, e := range m.entries {
			if oldestKey == "" || e.expiresAt.Before(oldestTime) {
				oldestKey = k
				oldestTime = e.expiresAt
			}
		}
		delete(m.entries, oldestKey)
	}
	m.entries[key] = entry{value: value}
	return nil
}

// Delete implements agent.Memory.
func (m *InMemory) Delete(_ context.Context, key string) error {
	m.mu.Lock()
	defer m.mu.Unlock()
	delete(m.entries, key)
	return nil
}

// Search implements agent.Memory with a simple token-overlap score.
func (m *InMemory) Search(_ context.Context, query string, limit int) ([]agent.MemoryHit, error) {
	m.mu.RLock()
	defer m.mu.RUnlock()
	qTokens := tokenize(query)
	if len(qTokens) == 0 {
		return nil, nil
	}
	var hits []agent.MemoryHit
	for k, e := range m.entries {
		if !e.expiresAt.IsZero() && time.Now().After(e.expiresAt) {
			continue
		}
		score := jaccard(qTokens, tokenize(e.value))
		if score > 0 {
			hits = append(hits, agent.MemoryHit{Key: k, Value: e.value, Score: score})
		}
	}
	sort.Slice(hits, func(i, j int) bool { return hits[i].Score > hits[j].Score })
	if limit > 0 && len(hits) > limit {
		hits = hits[:limit]
	}
	return hits, nil
}

// Close implements agent.Memory.
func (m *InMemory) Close() error { return nil }

func tokenize(s string) map[string]struct{} {
	out := map[string]struct{}{}
	for _, w := range strings.Fields(strings.ToLower(s)) {
		w = strings.Trim(w, ",.;:!?()[]{}\"'")
		if len(w) >= 2 {
			out[w] = struct{}{}
		}
	}
	return out
}

func jaccard(a, b map[string]struct{}) float64 {
	if len(a) == 0 || len(b) == 0 {
		return 0
	}
	intersect := 0
	for k := range a {
		if _, ok := b[k]; ok {
			intersect++
		}
	}
	union := len(a) + len(b) - intersect
	if union == 0 {
		return 0
	}
	return float64(intersect) / float64(union)
}

// ---- Redis & Postgres stubs ----------------------------------------------
//
// These are intentionally not implemented in the scaffold — production
// use should plug in a Redis (go-redis) and Postgres (pgx) backed
// implementation matching the agent.Memory interface. The stub returns
// an error so misconfiguration fails fast.

type notImplMemory struct {
	backend string
}

func (n *notImplMemory) Get(context.Context, string) (string, bool, error) {
	return "", false, errors.New(n.backend + " memory backend not yet implemented in this scaffold")
}
func (n *notImplMemory) Put(context.Context, string, string) error {
	return errors.New(n.backend + " memory backend not yet implemented in this scaffold")
}
func (n *notImplMemory) Delete(context.Context, string) error {
	return errors.New(n.backend + " memory backend not yet implemented in this scaffold")
}
func (n *notImplMemory) Search(context.Context, string, int) ([]agent.MemoryHit, error) {
	return nil, errors.New(n.backend + " memory backend not yet implemented in this scaffold")
}
func (n *notImplMemory) Close() error { return nil }

// NewRedis returns a placeholder; production callers should replace
// with a real Redis-backed implementation.
func NewRedis(url, prefix string, maxEntries int) (Memory, error) {
	if url == "" {
		return nil, errors.New("memory.redis.url required")
	}
	_ = prefix
	_ = maxEntries
	return &notImplMemory{backend: "redis"}, nil
}

// NewPostgres returns a placeholder; production callers should replace
// with a real Postgres-backed implementation.
func NewPostgres(url, table string) (Memory, error) {
	if url == "" {
		return nil, errors.New("memory.postgres.url required")
	}
	_ = table
	return &notImplMemory{backend: "postgres"}, nil
}