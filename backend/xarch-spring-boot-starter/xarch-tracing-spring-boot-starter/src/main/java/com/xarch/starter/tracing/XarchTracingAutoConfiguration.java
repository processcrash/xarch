package com.xarch.starter.tracing;

import com.xarch.starter.tracing.http.RestTemplateTraceInterceptor;
import com.xarch.starter.tracing.http.WebClientTraceFilter;
import com.xarch.starter.tracing.servlet.TracingFilter;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import jakarta.servlet.DispatcherType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

/**
 * Entry point for the xarch OpenTelemetry tracing starter.
 *
 * <p>The configuration is master-switched by {@code xarch.tracing.enabled}
 * (default {@code true}). When enabled it:
 * <ol>
 *   <li>Registers an {@link OpenTelemetry} bean — preferring the one already
 *       provided by Spring Boot's OpenTelemetry starter, otherwise building
 *       one from {@link TracingProperties}.</li>
 *   <li>Adds a Servlet {@link TracingFilter} so every inbound HTTP request
 *       opens a server span and propagates W3C headers.</li>
 *   <li>Provides {@link RestTemplateTraceInterceptor} and
 *       {@link WebClientTraceFilter} beans for outbound call propagation.</li>
 * </ol>
 */
@AutoConfiguration(before = {WebMvcAutoConfiguration.class, DataSourceAutoConfiguration.class})
@EnableConfigurationProperties(TracingProperties.class)
@ConditionalOnProperty(prefix = "xarch.tracing", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(OpenTelemetry.class)
public class XarchTracingAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(XarchTracingAutoConfiguration.class);

    /**
     * Resolves the effective service name. Supports the placeholder
     * {@code ${spring.application.name}} which is the default.
     */
    static String resolveServiceName(TracingProperties properties, Environment environment) {
        String raw = properties.getServiceName();
        if (raw == null || raw.isBlank()) {
            return environment.getProperty("spring.application.name", "xarch-service");
        }
        if (raw.startsWith("${") && raw.endsWith("}")) {
            String key = raw.substring(2, raw.length() - 1);
            return environment.getProperty(key, "xarch-service");
        }
        return raw;
    }

    /**
     * Builds the {@link OpenTelemetry} instance. If another configuration
     * (typically Spring Boot's own OpenTelemetry starter) has already
     * produced one we reuse it; otherwise we build a fresh SDK configured
     * from {@link TracingProperties}.
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenTelemetry openTelemetry(TracingProperties properties,
                                        Environment environment) {
        String serviceName = resolveServiceName(properties, environment);
        Resource resource = buildResource(serviceName, properties);

        SpanExporter exporter = buildExporter(properties);
        SdkTracerProviderBuilder providerBuilder = SdkTracerProvider.builder()
                .setResource(resource)
                .setSampler(buildSampler(properties));

        if (exporter instanceof OtlpGrpcSpanExporter || exporter instanceof ZipkinSpanExporter) {
            providerBuilder.addSpanProcessor(BatchSpanProcessor.builder(exporter)
                    .setScheduleDelay(2, TimeUnit.SECONDS)
                    .build());
        } else {
            // Logging exporter — keep spans inline so they appear in real time.
            providerBuilder.addSpanProcessor(SimpleSpanProcessor.create(exporter));
        }

        SdkTracerProvider tracerProvider = providerBuilder.build();
        log.info("xarch tracing initialised: service={}, exporter={}, endpoint={}",
                serviceName, properties.getExporter(), properties.getEndpoint());

        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(buildPropagators())
                .buildAndRegisterGlobal();
    }

    private static Resource buildResource(String serviceName, TracingProperties properties) {
        Resource.Builder builder = Resource.getDefault().toBuilder()
                .put("service.name", serviceName)
                .put("service.version", "1.0.0");
        for (java.util.Map.Entry<String, String> entry : properties.getResourceAttributes().entrySet()) {
            builder.put(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    private static Sampler buildSampler(TracingProperties properties) {
        double rate = Math.max(0.0, Math.min(1.0, properties.getSampleRate()));
        if (rate <= 0.0) {
            return Sampler.alwaysOff();
        }
        if (rate >= 1.0) {
            return Sampler.alwaysOn();
        }
        return Sampler.traceIdRatioBased(rate);
    }

    private static SpanExporter buildExporter(TracingProperties properties) {
        return switch (properties.getExporter()) {
            case LOGGING -> LoggingSpanExporter.create();
            case ZIPKIN -> ZipkinSpanExporter.builder()
                    .setEndpoint(properties.getEndpoint() == null
                            ? TracingProperties.DEFAULT_ZIPKIN_ENDPOINT
                            : properties.getEndpoint())
                    .build();
            case OTLP -> OtlpGrpcSpanExporter.builder()
                    .setEndpoint(properties.getEndpoint() == null
                            ? TracingProperties.DEFAULT_OTLP_ENDPOINT
                            : properties.getEndpoint())
                    .build();
        };
    }

    private static io.opentelemetry.context.propagation.ContextPropagators buildPropagators() {
        return io.opentelemetry.context.propagation.ContextPropagators.create(
                io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator.getInstance());
    }

    /**
     * Servlet filter that opens a server span for every HTTP request.
     */
    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public FilterRegistrationBean<TracingFilter> tracingFilter(OpenTelemetry openTelemetry) {
        TracingFilter filter = new TracingFilter(openTelemetry);
        FilterRegistrationBean<TracingFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setName("xarchTracingFilter");
        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR));
        registration.addUrlPatterns("/*");
        registration.setOrder(Integer.MIN_VALUE + 100);
        return registration;
    }

    /**
     * RestTemplate interceptor that propagates W3C headers on outbound calls.
     */
    @Bean
    @ConditionalOnMissingBean
    public RestTemplateTraceInterceptor restTemplateTraceInterceptor(OpenTelemetry openTelemetry) {
        return new RestTemplateTraceInterceptor(openTelemetry);
    }

    /**
     * WebClient filter that propagates W3C headers on outbound reactive calls.
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.web.reactive.function.client.WebClient")
    @ConditionalOnMissingBean
    public WebClientTraceFilter webClientTraceFilter(OpenTelemetry openTelemetry) {
        return new WebClientTraceFilter(openTelemetry);
    }
}