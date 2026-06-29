package com.xarch.starter.tracing.http;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * {@link ClientHttpRequestInterceptor} that propagates the W3C
 * {@code traceparent} header on outgoing {@link org.springframework.web.client.RestTemplate}
 * requests so that downstream services can stitch their spans into the same
 * distributed trace.
 *
 * <p>The interceptor is registered automatically by the xarch tracing
 * auto-configuration. To inject it into an existing {@code RestTemplate}
 * add it to the list of interceptors:
 *
 * <pre>{@code
 * restTemplate.setInterceptors(List.of(restTemplateTraceInterceptor));
 * }</pre>
 */
public class RestTemplateTraceInterceptor implements ClientHttpRequestInterceptor {

    private static final TextMapSetter<HttpRequest> SETTER = new TextMapSetter<>() {
        @Override
        public void set(HttpRequest carrier, String key, String value) {
            if (carrier != null && carrier.getHeaders() != null) {
                carrier.getHeaders().set(key, value);
            }
        }
    };

    private final OpenTelemetry openTelemetry;

    public RestTemplateTraceInterceptor(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        openTelemetry.getPropagators()
                .getTextMapPropagator()
                .inject(Context.current(), request, SETTER);
        return execution.execute(request, body);
    }
}