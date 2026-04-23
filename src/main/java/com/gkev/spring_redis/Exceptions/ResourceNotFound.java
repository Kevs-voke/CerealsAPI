package com.gkev.spring_redis.Exceptions;

public class ResourceNotFound extends RuntimeException {
    public ResourceNotFound(String message, Throwable cause) {
        super(message, cause);
    }

}
