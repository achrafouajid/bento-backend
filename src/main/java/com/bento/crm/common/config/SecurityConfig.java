package com.bento.crm.common.config;

import com.bento.crm.auth.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
@EnableMethodSecurity(prePostEnabled = false) // Disabled for development - allows public API access
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final TenantFilterInterceptor tenantFilterInterceptor;
    // private final RateLimitFilter rateLimitFilter; // Disabled: bucket4j not available

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/auth/login", "/auth/refresh",
                                "/organizations",
                                "/actuator/health",
                                "/swagger-ui.html", "/openapi/**", "/swagger-ui/**",
                                "/users", "/users/**",
                                "/teams", "/teams/**",
                                "/groups", "/groups/**",
                                "/partners", "/partners/**",
                                "/deals", "/deals/**",
                                "/proposals", "/proposals/**",
                                "/tasks", "/tasks/**",
                                "/tickets", "/tickets/**",
                                "/invoices", "/invoices/**",
                                "/purchase-orders", "/purchase-orders/**",
                                "/campaigns", "/campaigns/**",
                                "/automation-rules", "/automation-rules/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                // .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class) // Disabled: bucket4j not available
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(tenantFilterInterceptor, JwtAuthFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
