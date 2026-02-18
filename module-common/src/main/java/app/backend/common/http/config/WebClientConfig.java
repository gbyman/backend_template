package app.backend.common.http.config;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Configuration
public class WebClientConfig {

    private static final int MAX_IN_MEMORY_SIZE_MB = 50;
    private static final int BYTES_PER_KB = 1024;
    private static final int KB_PER_MB = 1024;
    private static final int MILLIS_PER_SECOND = 1000;

    private final int timeoutSeconds;

    public WebClientConfig(@Value("${webclient.timeout.seconds:120}") int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    @Bean
    public WebClient webClient() {

        return WebClient.builder()
                .clientConnector(createClientConnector())
                .codecs(
                        configurer ->
                                configurer
                                        .defaultCodecs()
                                        .maxInMemorySize(
                                                MAX_IN_MEMORY_SIZE_MB * BYTES_PER_KB * KB_PER_MB))
                .filter(logRequest())
                .filter(handleErrors())
                .build();
    }

    private ReactorClientHttpConnector createClientConnector() {
        // HttpClient에 타임아웃 설정 추가
        HttpClient httpClient =
                HttpClient.create()
                        .option(
                                ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                timeoutSeconds * MILLIS_PER_SECOND) // 연결 타임아웃: 120초
                        .responseTimeout(Duration.ofSeconds(timeoutSeconds)) // 응답 타임아웃: 120초
                        .doOnConnected(
                                conn ->
                                        conn.addHandlerLast(
                                                        new ReadTimeoutHandler(
                                                                timeoutSeconds,
                                                                TimeUnit.SECONDS)) // 읽기 타임아웃
                                                .addHandlerLast(
                                                        new WriteTimeoutHandler(
                                                                timeoutSeconds,
                                                                TimeUnit.SECONDS))); // 쓰기 타임아웃
        return new ReactorClientHttpConnector(httpClient);
    }

    private ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            log.info("Request:{} {}", clientRequest.method(), clientRequest.url());
            clientRequest
                    .headers()
                    .forEach(
                            (name, values) ->
                                    values.forEach(value -> log.info("{}={}", name, value)));

            return next.exchange(clientRequest);
        };
    }

    private ExchangeFilterFunction handleErrors() {
        return (clientRequest, next) ->
                next.exchange(clientRequest)
                        .doOnNext(
                                clientResponse -> {
                                    if (clientResponse.statusCode().is4xxClientError()) {
                                        log.error("Client Error: {}", clientResponse.statusCode());
                                    } else if (clientResponse.statusCode().is5xxServerError()) {
                                        log.error("Server Error: {}", clientResponse.statusCode());
                                    }
                                })
                        .doOnError(
                                error ->
                                        log.error(
                                                "Error during request: {}",
                                                error.getMessage(),
                                                error));
    }
}
