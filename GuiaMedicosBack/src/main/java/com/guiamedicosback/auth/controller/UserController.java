package com.guiamedicosback.auth.controller;

import com.guiamedicosback.auth.entity.UserRequest;
import com.guiamedicosback.auth.service.UserService;
import com.guiamedicosback.auth.service.utils.JwtRequestFilter;
import com.guiamedicosback.auth.service.utils.JwtTokenUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserRequest userRequest, HttpServletResponse response) {
        try {
            log.info("Realizando login");

            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userRequest.username(), userRequest.password()));

            final UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String acessToken = jwtTokenUtil.generateToken(userRequest.username());

            boolean isSecure = false;

            JwtRequestFilter.addCookie(response, JwtRequestFilter.JWT_COOKIE_NAME, acessToken, Math.toIntExact(jwtTokenUtil.getExpirationTime()), true, isSecure);
            assert userDetails != null;
            Map<String, String> responseBody = Map.of(
                    "message", "Login successful",
                    "username", userDetails.getUsername()
            );
            log.info("Login successful: {}", responseBody);

            return ResponseEntity.ok(responseBody);
        }catch (Exception e) {
            return ResponseEntity.status(HttpServletResponse.SC_UNAUTHORIZED).body(Map.of("message", "Invalid username or password"));
        }
    }
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody UserRequest userRequest) {
        if (userService.validateCredentials(userRequest.username())) {
            return ResponseEntity.status(HttpServletResponse.SC_CONFLICT).body(Map.of("message", "User already exists"));
        }else {
            String username = userService.registerUser(userRequest.username(), userRequest.password());
            return ResponseEntity.ok(Map.of("message", "User registered successfully", "username", username));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Remover cookies
        JwtRequestFilter.removeCookie(response, JwtRequestFilter.JWT_COOKIE_NAME);
        JwtRequestFilter.removeCookie(response, JwtRequestFilter.REFRESH_COOKIE_NAME);

        return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso"));
    }
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        String token = getCookieValue(request);

        if (token == null) {
            return ResponseEntity.ok(Map.of("valid", false, "message", "Token não encontrado"));
        }

        boolean isValid = jwtTokenUtil.validateToken(token);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);

        if (isValid) {
            try {
                String username = jwtTokenUtil.getUsernameFromToken(token);
                response.put("username", username);
            } catch (Exception e) {
                // Ignorar
            }
        }

        return ResponseEntity.ok(response);
    }
    private String getCookieValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (JwtRequestFilter.JWT_COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
