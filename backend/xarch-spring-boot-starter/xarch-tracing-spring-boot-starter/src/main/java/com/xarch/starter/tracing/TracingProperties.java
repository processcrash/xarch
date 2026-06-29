package com.xarch.starter.tracing;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for the xarch distributed tracing starter.
 *
 * <p>All settings live under the {@code xarch.tracing} prefix in {@code application.yml}.
 */
@ConfigurationProperties(prefix = "xarch.tracing")
public class TracingProperties {

    /** Default OTLP gRPC collector endpoint. */
    public static final String DEFAULT_OTLP_ENDPOINT = "http://localhost:4317";
    /** Default Zipkin HTTP collector endpoint. */
    public static final String DEFAULT_ZIPKIN_ENDPOINT = "http://localhost:9411/api/v2/spans";

    /**
     * Master switch for the tracing auto-configuration. When {@code false} the
     * starter registers no beans at all and OpenTelemetry is left in its
     * no-op state.
     */
    private boolean enabled = true;

    /**
     * Logical service name reported on every span. Defaults to the Spring
     * application name (resolved at runtime by the auto-configuration).
     */
    private String serviceName = "${spring.application.name}";

    /** Span exporter strategy. */
    private Exporter exporter = Exporter.OTLP;

    /**
     * Collector endpoint. The default depends on the configured exporter
     * (OTLP gRPC → {@value #DEFAULT_OTLP_ENDPOINT},
     * Zipkin → {@value #DEFAULT_ZIPKIN_ENDPOINT}).
     */
    private String endpoint = DEFAULT_OTLP_ENDPOINT;

    /**
     * Probability with which a new root trace is sampled. {@code 1.0} means
     * keep everything, {@code 0.0} disables tracing entirely.
     */
    private double sampleRate = 1.0;

    /**
     * Ordered list of W3C propagators. Defaults to {@code tracecontext} and
     * {@code baggage} which covers 99% of use-cases.
     */
    private List<String> propagators = List.of("tracecontext", "baggage");

    /**
     * Additional {@code Resource} attributes attached to every span. Useful
     * for tagging deployment environment, service version, region, etc.
     */
    private Map<String, String> resourceAttributes = new LinkedHashMap<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Exporter getExporter() {
        return exporter;
    }

    public void setExporter(Exporter exporter) {
        this.exporter = exporter;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public double getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(double sampleRate) {
        this.sampleRate = sampleRate;
    }

    public List<String> getPropagators() {
        return propagators == null ? Collections.emptyList() : propagators;
    }

    public void setPropagators(List<String> propagators) {
        this.propagators = propagators;
    }

    public Map<String, String> getResourceAttributes() {
        return resourceAttributes == null ? Collections.emptyMap() : resourceAttributes;
    }

    public void setResourceAttributes(Map<String, String> resourceAttributes) {
        this.resourceAttributes = resourceAttributes;
    }

    /**
     * Supported span exporter types.
     */
    public enum Exporter {
        /** OTLP gRPC — works with Jaeger, Tempo, SigNoz, Honeycomb, etc. */
        OTLP,
        /** Writes spans to {@code System.err} — handy for local development. */
        LOGGING,
        /** Zipkin v2 JSON over HTTP. */
        ZIPKIN
    }
}
