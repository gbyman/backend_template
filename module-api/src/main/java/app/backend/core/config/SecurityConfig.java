package app.backend.core.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import app.backend.core.jwt.JwtAccessDeniedHandler;
import app.backend.core.jwt.JwtAuthenticationEntryPoint;
import app.backend.core.jwt.filter.JwtAuthenticationFilter;
import app.backend.core.jwt.filter.JwtExceptionHandlerFilter;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final long CORS_MAX_AGE_SECONDS = 3600L;

    @Value("${cors.allowed-origin}")
    private List<String> allowedOrigin;

    @Value("${security.public-uris}")
    private String[] publicUris;

    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtExceptionHandlerFilter jwtExceptionHandlerFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Spring Security 설정 - CORS: 허용된 Origin에서 API 호출 가능 - CSRF: JWT 기반 인증이므로 비활성화 (REST API에서는 불필요)
     * - Session: Stateless (세션 사용 안 함) - JWT: 토큰 기반 인증/인가
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CORS 설정 활성화
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                // CSRF 비활성화 (JWT 사용 시 불필요)
                .csrf(AbstractHttpConfigurer::disable)
                // HTTP Basic 인증 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                // Form 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                // 세션 정책: Stateless (세션 사용 안 함)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // URL별 인증/인가 설정
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers(publicUris)
                                        .permitAll() // 공개 URI는 인증 불필요
                                        .anyRequest()
                                        .authenticated() // 나머지는 인증 필요
                        )
                // 예외 처리
                .exceptionHandling(
                        ex ->
                                ex.authenticationEntryPoint(jwtAuthenticationEntryPoint) // 인증 실패
                                        .accessDeniedHandler(jwtAccessDeniedHandler) // 인가 실패
                        )
                // JWT 필터 등록
                .addFilterBefore(
                        jwtExceptionHandlerFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(
                        jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /** CORS(Cross-Origin Resource Sharing) 설정 다른 도메인에서 API 호출을 허용하기 위한 설정 */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin 패턴 (application.yml에서 주입)
        configuration.setAllowedOriginPatterns(allowedOrigin);

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(
                Arrays.asList(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.HEAD.name(),
                        HttpMethod.OPTIONS.name()));

        // 허용할 요청 헤더
        configuration.setAllowedHeaders(List.of("*"));

        // 클라이언트에서 접근 가능한 응답 헤더
        configuration.setExposedHeaders(
                Arrays.asList(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, "X-Request-Id"));

        // 쿠키/인증 정보 포함 허용
        configuration.setAllowCredentials(true);

        // Preflight 요청 캐싱 시간 (초 단위)
        configuration.setMaxAge(CORS_MAX_AGE_SECONDS);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder());

        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
