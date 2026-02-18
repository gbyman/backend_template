package app.backend.common.http;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@RequiredArgsConstructor
public class WebClientHelper {

    private final WebClient webClient;

    public <T> Mono<T> getMono(
            @NonNull String url,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            @NonNull Class<T> responseType) {

        return exchangeToBody(url, HttpMethod.GET, null, headers, queryParams, responseType);
    }

    /**
     * GET 요청을 Mono로 반환 (재시도 기능 포함)
     *
     * <p>재시도 전략 예시:
     *
     * <pre>
     * // 최대 3번 재시도, 2초 간격으로 exponential backoff
     * Retry retry = Retry.backoff(3, java.time.Duration.ofSeconds(2));
     *
     * // 고정 간격으로 재시도
     * Retry retry = Retry.fixedDelay(3, java.time.Duration.ofSeconds(1));
     * </pre>
     *
     * @param url 요청 URL
     * @param headers HTTP 헤더
     * @param queryParams 쿼리 파라미터
     * @param responseType 응답 타입
     * @param retrySpec 재시도 전략
     * @param <T> 응답 타입
     * @return Mono 응답
     */
    public <T> Mono<T> getMono(
            @NonNull String url,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            @NonNull Class<T> responseType,
            @NonNull Retry retrySpec) {

        return exchangeToBodyWithRetry(
                url, HttpMethod.GET, null, headers, queryParams, responseType, retrySpec);
    }

    public <T> Mono<ResponseEntity<T>> getEntity(
            @NonNull String url,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            @NonNull Class<T> responseType) {

        return exchangeToEntity(url, HttpMethod.GET, null, headers, queryParams, responseType);
    }

    public <T> Mono<ResponseEntity<T>> postEntity(
            @NonNull String url,
            Object body,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            @NonNull Class<T> responseType) {
        return exchangeToEntity(url, HttpMethod.POST, body, headers, queryParams, responseType);
    }

    public <T> Mono<T> getMono(
            @NonNull String url,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            @NonNull ParameterizedTypeReference<T> responseType) {

        return exchangeToBody(url, HttpMethod.GET, null, headers, queryParams, responseType);
    }

    public <T> Mono<T> postMono(
            @NonNull String url,
            Object body,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            @NonNull Class<T> responseType) {
        return exchangeToBody(url, HttpMethod.POST, body, headers, queryParams, responseType);
    }

    public <T> Mono<T> postMono(
            @NonNull String url,
            Object body,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            @NonNull ParameterizedTypeReference<T> responseType) {
        return exchangeToBody(url, HttpMethod.POST, body, headers, queryParams, responseType);
    }

    /**
     * POST 요청을 Mono로 반환 (재시도 기능 포함)
     *
     * @param url 요청 URL
     * @param body 요청 바디
     * @param headers HTTP 헤더
     * @param queryParams 쿼리 파라미터
     * @param responseType 응답 타입
     * @param retrySpec 재시도 전략
     * @param <T> 응답 타입
     * @return Mono 응답
     */
    public <T> Mono<T> postMono(
            @NonNull String url,
            Object body,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            @NonNull Class<T> responseType,
            @NonNull Retry retrySpec) {
        return exchangeToBodyWithRetry(
                url, HttpMethod.POST, body, headers, queryParams, responseType, retrySpec);
    }

    public <T> Mono<T> putMono(
            @NonNull String url,
            Object body,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            @NonNull Class<T> responseType) {
        return exchangeToBody(url, HttpMethod.PUT, body, headers, queryParams, responseType);
    }

    public <T> Mono<T> putMono(
            @NonNull String url,
            Object body,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            @NonNull ParameterizedTypeReference<T> responseType) {
        return exchangeToBody(url, HttpMethod.PUT, body, headers, queryParams, responseType);
    }

    /**
     * PUT 요청을 Mono로 반환 (재시도 기능 포함)
     *
     * @param url 요청 URL
     * @param body 요청 바디
     * @param headers HTTP 헤더
     * @param queryParams 쿼리 파라미터
     * @param responseType 응답 타입
     * @param retrySpec 재시도 전략
     * @param <T> 응답 타입
     * @return Mono 응답
     */
    public <T> Mono<T> putMono(
            @NonNull String url,
            Object body,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            @NonNull Class<T> responseType,
            @NonNull Retry retrySpec) {
        return exchangeToBodyWithRetry(
                url, HttpMethod.PUT, body, headers, queryParams, responseType, retrySpec);
    }

    public Mono<Void> deleteMono(
            @NonNull String url, HttpHeaders headers, Map<String, ?> queryParams) {
        return exchangeToBody(url, HttpMethod.DELETE, null, headers, queryParams, Void.class);
    }

    public <T> Mono<T> deleteMono(
            @NonNull String url, Object body, HttpHeaders headers, Class<T> responseType) {
        return exchangeToBody(url, HttpMethod.DELETE, body, headers, null, responseType);
    }

    public <T> Flux<T> getFlux(
            @NonNull String url,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            @NonNull Class<T> responseType) {

        return executeFluxRequest(url, HttpMethod.GET, null, headers, queryParams, responseType);
    }

    public <T> Flux<T> getFlux(
            @NonNull String url,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            @NonNull ParameterizedTypeReference<T> responseType) {

        return executeFluxRequest(url, HttpMethod.GET, null, headers, queryParams, responseType);
    }

    public <T> Flux<T> postFlux(
            @NonNull String url,
            Object body,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            @NonNull Class<T> responseType) {
        return executeFluxRequest(url, HttpMethod.POST, body, headers, queryParams, responseType);
    }

    public <T> Flux<T> postFlux(
            @NonNull String url,
            Object body,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            @NonNull ParameterizedTypeReference<T> responseType) {
        return executeFluxRequest(url, HttpMethod.POST, body, headers, queryParams, responseType);
    }

    public <T> Flux<T> putFlux(
            @NonNull String url,
            Object body,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            @NonNull Class<T> responseType) {
        return executeFluxRequest(url, HttpMethod.PUT, body, headers, queryParams, responseType);
    }

    public <T> Flux<T> putFlux(
            @NonNull String url,
            Object body,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            @NonNull ParameterizedTypeReference<T> responseType) {
        return executeFluxRequest(url, HttpMethod.PUT, body, headers, queryParams, responseType);
    }

    public Flux<Void> deleteFlux(
            @NonNull String url, HttpHeaders headers, Map<String, ?> queryParams) {
        return executeFluxRequest(url, HttpMethod.DELETE, null, headers, queryParams, Void.class);
    }

    private <T> Mono<T> exchangeToBody(
            String url,
            HttpMethod method,
            Object body,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            Class<T> responseType) {
        WebClient.ResponseSpec responseSpec =
                buildRequestSpec(url, method, body, headers, queryParams);
        return responseSpec.bodyToMono(responseType);
    }

    private <T> Mono<ResponseEntity<T>> exchangeToEntity(
            String url,
            HttpMethod method,
            Object body,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            Class<T> responseType) {
        WebClient.ResponseSpec responseSpec =
                buildRequestSpec(url, method, body, headers, queryParams);
        return responseSpec.toEntity(responseType);
    }

    private <T> Mono<T> exchangeToBody(
            String url,
            HttpMethod method,
            Object body,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            ParameterizedTypeReference<T> responseType) {

        return buildRequestSpec(url, method, body, headers, queryParams).bodyToMono(responseType);
    }

    /**
     * HTTP 요청을 수행하고 재시도 로직을 적용하여 응답을 Mono로 반환
     *
     * @param url 요청 URL
     * @param method HTTP 메서드
     * @param body 요청 바디
     * @param headers HTTP 헤더
     * @param queryParams 쿼리 파라미터
     * @param responseType 응답 타입
     * @param retrySpec 재시도 전략
     * @param <T> 응답 타입
     * @return Mono 응답
     */
    private <T> Mono<T> exchangeToBodyWithRetry(
            String url,
            HttpMethod method,
            Object body,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            Class<T> responseType,
            Retry retrySpec) {

        WebClient.ResponseSpec responseSpec =
                buildRequestSpec(url, method, body, headers, queryParams);

        return responseSpec.bodyToMono(responseType).retryWhen(retrySpec);
    }

    private <T> Flux<T> executeFluxRequest(
            String url,
            HttpMethod method,
            Object body,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            Class<T> responseType) {
        WebClient.ResponseSpec responseSpec =
                buildRequestSpec(url, method, body, headers, queryParams);
        return responseSpec.bodyToFlux(responseType);
    }

    private <T> Flux<T> executeFluxRequest(
            String url,
            HttpMethod method,
            Object body,
            HttpHeaders headers,
            Map<String, ?> queryParams,
            ParameterizedTypeReference<T> responseType) {

        return buildRequestSpec(url, method, body, headers, queryParams).bodyToFlux(responseType);
    }

    private WebClient.ResponseSpec buildRequestSpec(
            String url,
            HttpMethod method,
            Object body,
            HttpHeaders headers,
            Map<String, ?> queryParams) {

        WebClient.RequestBodySpec requestSpec =
                webClient
                        .method(method)
                        .uri(buildUrlWithParams(url, queryParams))
                        .accept(MediaType.APPLICATION_JSON)
                        .headers(httpHeaders -> applyHeaders(httpHeaders, headers));

        if (body != null) {
            if (isMultipartBody(body)) {
                requestSpec.contentType(MediaType.MULTIPART_FORM_DATA);
                MultipartBodyBuilder builder = buildMultipartParameter(body);
                return requestSpec
                        .body(BodyInserters.fromMultipartData(builder.build()))
                        .retrieve();
            } else {
                requestSpec.contentType(MediaType.APPLICATION_JSON);
                requestSpec.bodyValue(body);
            }
        }

        return requestSpec.retrieve();
    }

    private boolean isMultipartBody(Object body) {

        // Map 타입 별도 처리
        if (body instanceof Map) {
            return isMultipartMap((Map<?, ?>) body);
        }

        for (Field field : body.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();

            // 단일 MultipartFile 필드
            if (MultipartFile.class.isAssignableFrom(fieldType)) {
                return true;
            }

            // List 또는 Collection 타입
            if (Collection.class.isAssignableFrom(fieldType)) {
                Type genericType = field.getGenericType();
                if (genericType instanceof ParameterizedType pt) {
                    Type[] actualTypes = pt.getActualTypeArguments();
                    if (actualTypes.length == 1 && actualTypes[0] instanceof Class<?> actualClass) {
                        if (MultipartFile.class.isAssignableFrom(actualClass)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean isMultipartMap(Map<?, ?> map) {
        return map.values().stream()
                .anyMatch(
                        value -> {
                            if (value instanceof MultipartFile) {
                                return true;
                            }
                            if (value instanceof Collection) {
                                return ((Collection<?>) value)
                                        .stream().anyMatch(item -> item instanceof MultipartFile);
                            }
                            return false;
                        });
    }

    private MultipartBodyBuilder buildMultipartParameter(Object dto) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        // Map 타입 별도 처리
        if (dto instanceof Map<?, ?> map) {
            return buildMultipartFromMap(builder, map);
        }

        for (Field field : dto.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object value = field.get(dto);

                if (value instanceof MultipartFile multipartFile) {
                    builder.part(field.getName(), multipartFile.getResource());
                } else if (value instanceof List<?> list) {
                    for (Object item : list) {
                        if (item instanceof MultipartFile multipartFileItem) {
                            builder.part(field.getName(), multipartFileItem.getResource());
                        } else {
                            builder.part(field.getName(), item.toString()); // 일반 리스트 값
                        }
                    }
                } else {
                    builder.part(field.getName(), value != null ? value.toString() : "");
                }

            } catch (Exception e) {
                throw new RuntimeException("Multipart 생성 실패", e);
            }
        }

        return builder;
    }

    private MultipartBodyBuilder buildMultipartFromMap(
            MultipartBodyBuilder builder, Map<?, ?> map) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            String key = entry.getKey().toString();
            Object value = entry.getValue();
            addPartToBuilder(builder, key, value);
        }
        return builder;
    }

    private void addPartToBuilder(MultipartBodyBuilder builder, String name, Object value) {
        if (value instanceof MultipartFile multipartFile) {
            builder.part(name, multipartFile.getResource());
        } else if (value instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof MultipartFile multipartFileItem) {
                    builder.part(name, multipartFileItem.getResource());
                } else {
                    builder.part(name, item != null ? item.toString() : "");
                }
            }
        } else {
            builder.part(name, value != null ? value.toString() : "");
        }
    }

    private void applyHeaders(HttpHeaders targetHeaders, HttpHeaders sourceHeaders) {
        if (sourceHeaders != null) {
            targetHeaders.putAll(sourceHeaders);
        }
    }

    private String buildUrlWithParams(String url, Map<String, ?> queryParams) {
        if (queryParams == null) {
            return url;
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);

        queryParams.forEach(
                (key, value) -> {
                    if (value == null) {
                        return;
                    }

                    if (value instanceof List) {
                        ((List<?>) value)
                                .forEach(
                                        listValue -> {
                                            if (listValue == null) {
                                                return;
                                            }

                                            uriBuilder.queryParam(key, listValue);
                                        });

                    } else {
                        uriBuilder.queryParam(key, value);
                    }
                });

        return uriBuilder.build().toUriString();
    }

    public ResponseEntity<Resource> postForFile(@NonNull String url, HttpEntity<?> requestEntity) {
        return webClient
                .post()
                .uri(url)
                .headers(
                        headers -> {
                            HttpHeaders entityHeaders = requestEntity.getHeaders();
                            if (!entityHeaders.isEmpty()) {
                                headers.addAll(entityHeaders);
                            }
                        })
                .bodyValue(Objects.requireNonNull(requestEntity.getBody()))
                .retrieve()
                .toEntity(Resource.class)
                .block();
    }
}
