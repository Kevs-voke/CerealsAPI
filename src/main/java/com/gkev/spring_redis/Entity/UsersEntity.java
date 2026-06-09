package com.gkev.spring_redis.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("users")
public class UsersEntity {

    @Id
    @Column("user_id")
    private Integer userId;
    private String username;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    private String email;

    @Column("phone_number")
    private String phoneNumber;

    private String passwrd;

    @Column("account_non_expired")
    private Boolean accountNonExpired;

    @Column("account_non_locked")
    private Boolean accountNonLocked;

    @Column("credentials_non_expired")
    private Boolean credentialsNonExpired;

    private Boolean enabled;
}