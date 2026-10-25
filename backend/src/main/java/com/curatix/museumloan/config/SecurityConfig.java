package com.curatix.museumloan.config;

import com.curatix.museumloan.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                        "/api/public/**",
                        "/api/auth/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/actuator/health",
                        "/actuator/info",
                        "/actuator/prometheus",
                        "/actuator/metrics"
                ).permitAll()
                // Write operations — URL-based role enforcement
                .requestMatchers(HttpMethod.POST, "/api/museums").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/artifacts").hasRole("MUSEUM")
                .requestMatchers(HttpMethod.POST, "/api/loans").hasRole("MUSEUM")
                .requestMatchers(HttpMethod.PATCH, "/api/loans/*/approve").hasRole("REVIEWER")
                .requestMatchers(HttpMethod.PATCH, "/api/loans/*/reject").hasRole("REVIEWER")
                .requestMatchers(HttpMethod.PATCH, "/api/loans/*/ship").hasRole("MUSEUM")
                .requestMatchers(HttpMethod.PATCH, "/api/loans/*/return").hasRole("MUSEUM")
                // Admin endpoints (class-level @PreAuthorize still enforced)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Everything else requires authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
