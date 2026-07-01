// Package observability provides logging, tracing, and metrics setup.
// All three are configured from config.ObservabilityConfig.
package observability

import (
	"context"
	"fmt"

	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/attribute"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace"
	"go.opentelemetry.io/otel/exporters/otlp/otlptrace/otlptracehttp"
	"go.opentelemetry.io/otel/exporters/stdout/stdouttrace"
	"go.opentelemetry.io/otel/propagation"
	"go.opentelemetry.io/otel/sdk/resource"
	sdktrace "go.opentelemetry.io/otel/sdk/trace"
	semconv "go.opentelemetry.io/otel/semconv/v1.26.0"
	"go.opentelemetry.io/otel/trace"
	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"

	"github.com/xarch/go-ai-agent-base/internal/config"
)

// NewLogger builds a Zap logger configured for the platform.
func NewLogger(cfg config.ObservabilityConfig) (*zap.Logger, error) {
	level := zapcore.InfoLevel
	if err := level.UnmarshalText([]byte(cfg.LogLevel)); err != nil {
		return nil, fmt.Errorf("parse log_level %q: %w", cfg.LogLevel, err)
	}
	var cfgZap zap.Config
	switch cfg.LogFormat {
	case "text", "":
		cfgZap = zap.NewDevelopmentConfig()
	case "json":
		cfgZap = zap.NewProductionConfig()
	default:
		return nil, fmt.Errorf("unknown log_format: %s", cfg.LogFormat)
	}
	cfgZap.Level = zap.NewAtomicLevelAt(level)
	cfgZap.EncoderConfig.TimeKey = "ts"
	cfgZap.EncoderConfig.EncodeTime = zapcore.RFC3339NanoTimeEncoder
	return cfgZap.Build()
}

// SetupTracing wires OpenTelemetry according to the configured exporter
// (otlp / stdout / jaeger-stub via otlp). The returned shutdown func
// must be called at process exit.
func SetupTracing(ctx context.Context, cfg config.ObservabilityConfig) (func(context.Context) error, error) {
	if !cfg.Tracing.Enabled {
		return func(context.Context) error { return nil }, nil
	}
	res, err := resource.New(ctx,
		resource.WithAttributes(
			semconv.ServiceName(cfg.ServiceName),
			semconv.ServiceVersion("0.1.0"),
		),
	)
	if err != nil {
		return nil, fmt.Errorf("create trace resource: %w", err)
	}

	var exporter sdktrace.SpanExporter
	switch cfg.Tracing.Exporter {
	case "otlp":
		exporter, err = otlptrace.New(ctx, otlptracehttp.NewClient(
			otlptracehttp.WithEndpoint(cfg.Tracing.Endpoint),
			otlptracehttp.WithInsecure(),
		))
		if err != nil {
			return nil, fmt.Errorf("create otlp exporter: %w", err)
		}
	case "stdout", "":
		exporter, err = stdouttrace.New(stdouttrace.WithPrettyPrint())
		if err != nil {
			return nil, fmt.Errorf("create stdout exporter: %w", err)
		}
	default:
		return nil, fmt.Errorf("unknown tracing.exporter: %s", cfg.Tracing.Exporter)
	}

	tp := sdktrace.NewTracerProvider(
		sdktrace.WithBatcher(exporter),
		sdktrace.WithResource(res),
		sdktrace.WithSampler(sdktrace.TraceIDRatioBased(cfg.Tracing.SampleRate)),
	)
	otel.SetTracerProvider(tp)
	otel.SetTextMapPropagator(propagation.NewCompositeTextMapPropagator(
		propagation.TraceContext{}, propagation.Baggage{},
	))
	return tp.Shutdown, nil
}

// Tracer is a convenience wrapper that returns a tracer for the
// platform's service name. Reuse otel.Tracer for production code.
func Tracer(name string) trace.Tracer { return otel.Tracer(name) }

// SpanFromContext returns the current span.
func SpanFromContext(ctx context.Context) trace.Span { return trace.SpanFromContext(ctx) }

// AddAgentAttributes decorates the current span with agent context.
func AddAgentAttributes(ctx context.Context, agentName, model, userID, sessionID string) {
	span := SpanFromContext(ctx)
	span.SetAttributes(
		attribute.String("agent.name", agentName),
		attribute.String("agent.model", model),
		attribute.String("user.id", userID),
		attribute.String("session.id", sessionID),
	)
}

// AddTokenUsage decorates the current span with token counts.
func AddTokenUsage(ctx context.Context, prompt, completion, total int) {
	span := SpanFromContext(ctx)
	span.SetAttributes(
		attribute.Int("llm.usage.prompt_tokens", prompt),
		attribute.Int("llm.usage.completion_tokens", completion),
		attribute.Int("llm.usage.total_tokens", total),
	)
}