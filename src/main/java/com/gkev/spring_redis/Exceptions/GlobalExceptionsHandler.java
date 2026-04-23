package com.gkev.spring_redis.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionsHandler {

    @ExceptionHandler(UserException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleUserException(UserException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("message", ex.getMessage());
        body.put("suggestions", ex.getSuggestions());

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body));
    }

    @ExceptionHandler(ResourceNotFound.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleResourceNotFoundExceptions(ResourceNotFound e){
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("message", e.getMessage());


        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(body));
    }
    @ExceptionHandler(NoServiceException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleNoServiceException(NoServiceException e){
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now());
        body.put("message", e.getMessage());

        return Mono.just(ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(body));
    }

}
