package com.gkev.spring_redis.Exceptions;

public class NoServiceException  extends RuntimeException{


    public NoServiceException(String message, Throwable cause) {
        super(message, cause);
    }

}
