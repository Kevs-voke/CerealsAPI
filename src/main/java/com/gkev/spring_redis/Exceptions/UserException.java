package com.gkev.spring_redis.Exceptions;

import lombok.Getter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class UserException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String type;
    private final List<String> suggestions;


    public UserException(String message, String type) {
        super(message);
        this.type = type;
        this.suggestions = Collections.emptyList();
    }

    public UserException(String message, String type, List<String> suggestions) {
        super(message);
        this.type = type;
        this.suggestions = Collections.unmodifiableList(new ArrayList<>(suggestions));
    }
}
