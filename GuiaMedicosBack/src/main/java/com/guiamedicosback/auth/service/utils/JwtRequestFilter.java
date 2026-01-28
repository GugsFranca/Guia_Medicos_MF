package com.guiamedicosback.auth.service.utils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    public static final String JWT_COOKIE_NAME = "auth_token";
    public static final String REFRESH_COOKIE_NAME = "refresh_token";

    // Lista de rotas que NÃO devem passar pelo filtro de autenticação
    private static final List<String> PUBLIC_ROUTES = Arrays.asList(
            "/api/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api/clinicas/**"  // Apenas GET será público, mas o filtro ignora
    );

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        // Verifica se a rota atual corresponde a alguma rota pública
        for (String pattern : PUBLIC_ROUTES) {
            if (antPathMatcher.match(pattern, requestURI)) {
                // Se for rota de clinicas, verifica se é GET
                if (pattern.equals("/api/clinicas/**")) {
                    String method = request.getMethod();
                    // Apenas GET é público para clinicas
                    return "GET".equalsIgnoreCase(method);
                }
                return true; // Para outras rotas públicas, sempre ignora o filtro
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        // Agora só executa para rotas que não são públicas
        String jwt = null;
        String username = null;

        // 1️⃣ Busca no header Authorization
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            jwt = authHeader.substring(7);
        }

        // 2️⃣ fallback para cookie
        if (jwt == null) {
            jwt = getCookieValue(request);
        }

        // Se encontrou um token, tenta autenticar
        if (jwt != null) {
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwt);
            } catch (Exception e) {
                logger.debug("Token JWT inválido ou expirado: " + e.getMessage());
                // Não faz nada, apenas continua sem autenticação
            }
        }

        // Se tem username e ainda não está autenticado no contexto
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtTokenUtil.validateToken(jwt)) {
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (Exception e) {
                logger.debug("Erro ao carregar usuário: " + e.getMessage());
                // Limpa qualquer autenticação prévia em caso de erro
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }

    private String getCookieValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (JWT_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // Métodos utilitários para manipulação de cookies (mantidos como estão)
    public static void addCookie(HttpServletResponse response, String name,
                                 String value, int maxAge, boolean httpOnly, boolean secure) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(httpOnly);
        cookie.setSecure(secure);
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
    }

    public static void removeCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}