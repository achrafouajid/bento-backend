package com.bento.crm.auth.filter;

import com.bento.crm.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final com.bento.crm.common.config.JwtProperties jwtProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (jwtService.isTokenValid(token)) {
                try {
                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey(jwtProperties.getSecretKey())
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

                    String userId = claims.getSubject();
                    @SuppressWarnings("unchecked")
                    List<String> authorities = (List<String>) claims.get("authorities");

                    List<SimpleGrantedAuthority> grantedAuthorities = authorities != null ?
                            authorities.stream()
                                    .map(SimpleGrantedAuthority::new)
                                    .collect(Collectors.toList()) :
                            Collections.emptyList();

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userId, null, grantedAuthorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (Exception e) {
                    logger.error("Cannot set user authentication", e);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
