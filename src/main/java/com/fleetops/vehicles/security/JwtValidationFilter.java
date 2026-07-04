package com.fleetops.vehicles.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtValidationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationFilter.class);

    private final TokenJwtConfig tokenJwtConfig;

    public JwtValidationFilter(TokenJwtConfig tokenJwtConfig) {
        this.tokenJwtConfig = tokenJwtConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.replace("Bearer ", "");
        log.debug("Token JWT recibido en petición: {}", request.getRequestURI());

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(tokenJwtConfig.getSecretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String username = claims.getSubject();
            if (username != null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Identidad opcional registrada para: {}", username);
            }

            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            log.warn("Token JWT inválido o expirado, la petición continúa sin identidad: {}", e.getMessage());
            filterChain.doFilter(request, response);
        }
    }
}
