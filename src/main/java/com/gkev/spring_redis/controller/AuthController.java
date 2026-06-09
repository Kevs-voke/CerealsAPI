package com.gkev.spring_redis.controller;

import com.gkev.spring_redis.DTO.LoginRequestDTO;
import com.gkev.spring_redis.DTO.RegistrationResponseDTO;
import com.gkev.spring_redis.DTO.UserDTO;
import com.gkev.spring_redis.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    @PostMapping("/register")
    public Mono<ResponseEntity<RegistrationResponseDTO>> register(@Valid @RequestBody UserDTO newUser) {
        return userService.register(newUser)
                .map(authResponse ->
                        ResponseEntity
                                .status(HttpStatus.CREATED)
                                .headers(buildCookieHeaders(authResponse.authToken()))
                                .body(authResponse)
                );
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<RegistrationResponseDTO>> login(@Valid @RequestBody LoginRequestDTO credentials) {
        return userService.login(credentials)
                .map(authResponse ->
                        ResponseEntity
                                .ok()
                                .headers(buildCookieHeaders(authResponse.authToken()))
                                .body(authResponse)
                );
    }


    private HttpHeaders buildCookieHeaders(String token) {
        ResponseCookie cookie = ResponseCookie.from("auth_token", token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofHours(24))
                .sameSite("Lax")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
        return headers;
    }
}