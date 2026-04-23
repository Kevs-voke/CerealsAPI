package com.gkev.spring_redis.controller;


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
    public Mono<ResponseEntity<String>> register(@Valid @RequestBody UserDTO newUser) {
        return userService.register(newUser)
                .flatMap(authResponse -> {


                    ResponseCookie authCookie = ResponseCookie.from("authToken", authResponse.authToken())  // or authResponse.authToken()
                            .httpOnly(true)
                            .secure(false)
                            .path("/")
                            .maxAge(Duration.ofHours(24))
                            .sameSite("Strict")
                            .build();

                    HttpHeaders headers = new HttpHeaders();
                    headers.add(HttpHeaders.SET_COOKIE, authCookie.toString());

                    return Mono.just(ResponseEntity
                            .status(HttpStatus.CREATED)
                            .headers(headers)
                            .body("User registered successfully: " + authResponse.email()));
                });
    }
}
