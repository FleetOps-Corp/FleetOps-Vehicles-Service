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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtValidationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationFilter.class);

    private final TokenJwtConfig tokenJwtConfig;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    public JwtValidationFilter(TokenJwtConfig tokenJwtConfig, JwtAuthenticationEntryPoint authenticationEntryPoint) {
        this.tokenJwtConfig = tokenJwtConfig;
        this.authenticationEntryPoint = authenticationEntryPoint;
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
                List<SimpleGrantedAuthority> grantedAuthorities = resolveAuthorities(claims);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Usuario autenticado: {}", username);
            }

            filterChain.doFilter(request, response);

        } catch (JwtException e) {

            log.warn("JWT inválido o expirado: {}", e.getMessage());

            SecurityContextHolder.clearContext();

            authenticationEntryPoint.commence(
                    request,
                    response,
                    new BadCredentialsException("Token JWT inválido o expirado.", e)
            );
        }
    }

    @SuppressWarnings("unchecked")
    private List<SimpleGrantedAuthority> resolveAuthorities(Claims claims) {
        List<String> authorities = claims.get("authorities", List.class);

        if (authorities == null) {
            String role = claims.get("role", String.class);
            authorities = role != null ? List.of(role) : List.of();
        }

        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}
