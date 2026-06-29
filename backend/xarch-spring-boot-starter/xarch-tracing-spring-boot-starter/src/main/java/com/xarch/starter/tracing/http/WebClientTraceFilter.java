package com.xarch.starter.tracing.http;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * {@link ExchangeFilterFunction} that propagates the W3C trace context on
 * outgoing {@link org.springframework.web.reactive.function.client.WebClient}
 * requests.
 *
 * <p>Attach it to your {@code WebClient.Builder}:
 *
 * <pre>{@code
 * WebClient.builder()
 *     .filter(webClientTraceFilter)
 *     .build();
 * }</pre>
 */
public class WebClientTraceFilter implements ExchangeFilterFunction {

    private static final TextMapSetter<ClientRequest.Builder> SETTER = new TextMapSetter<>() {
        @Override
        public void set(ClientRequest.Builder carrier, String key, String value) {
            if (carrier != null) {
                carrier.header(key, value);
            }
        }
    };

    private final OpenTelemetry openTelemetry;

    public WebClientTraceFilter(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
    }

    @Override
    public Mono<org.springframework.web.reactive.function.client.ClientResponse> filter(
            ClientRequest request, ExchangeFunction next) {
        ClientRequest.Builder mutated = ClientRequest.from(request);
        openTelemetry.getPropagators()
                .getTextMapPropagator()
                .inject(Context.current(), mutated, SETTER);
        return next.exchange(mutulated.build());
    }
}