package jwt.project.config;

import jwt.project.filter.JwtFilter;
import jwt.project.filter.RequestLoggingFilter;
import jwt.project.handler.SocialLoginHandler;
import jwt.project.handler.CustomAccessDeniedHandler;
import jwt.project.handler.StateValidatingSuccessHandler;
import jwt.project.handler.resolver.CustomAuthorizationRequestResolver;
import jwt.project.utils.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true) // ✅ 핵심!
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtFilter jwtFilter;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final RequestLoggingFilter requestLoggingFilter;
    private final CustomAuthorizationRequestResolver customAuthorizationRequestResolver;
    private final StateValidatingSuccessHandler stateValidatingSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint) // 🔥 인증 실패
                        .accessDeniedHandler(customAccessDeniedHandler)           // 🔥 권한 실패
                )
                .authorizeHttpRequests(auth -> auth
                        // ① 인증 없이 허용할 엔드포인트만 명시
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/verify-email",
                                "/api/pass/**",
                                "/login**",
                                "/api/auth/register/**",
                                "/api/auth/social-register",
                                "/api/auth/social-login",
                                "/oauth2/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/pass/reset-password").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/pass/reset-password").permitAll()
                        // ② 로그인 후에만 접근 허용
                        .requestMatchers(
                                "/api/auth/refresh",
                                "/api/auth/logout"
                        ).authenticated()
                        // ③ 역할별 보호
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                        // ④ 나머지는 전부 인증 필요
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .failureUrl("/login?error")
                        .authorizationEndpoint(ep -> ep
                                .authorizationRequestResolver(customAuthorizationRequestResolver)   // ★
                        )
                        .successHandler(stateValidatingSuccessHandler)                           // ★
                )
                .addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)  // 순서대로 정상 삽입
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
