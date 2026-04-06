package com.gkev.spring_redis.DTO;

import jakarta.validation.constraints.*;

import java.util.List;

public record UserDTO(
        @NotBlank(message = "Username is required")
        String username,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @Email(message = "Email should be valid")
        @NotBlank(message = "Email is required")
        String email,

        @Pattern(regexp = "\\+?[0-9]{7,15}", message = "Phone number must be valid")
        String phoneNumber,

        @NotNull(message = "Roles list cannot be null")
        @Size(min = 1, message = "At least one role must be assigned")
        List<@NotBlank String> roles,

        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters long")
        String password
) {
}
