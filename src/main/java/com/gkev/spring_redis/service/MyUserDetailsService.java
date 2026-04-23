package com.gkev.spring_redis.service;

import com.gkev.spring_redis.DTO.UserWithRolesDTO;
import com.gkev.spring_redis.Model.UserPrincipal;
import com.gkev.spring_redis.repository.RoleRepo;
import com.gkev.spring_redis.repository.UserRoleRepo;
import com.gkev.spring_redis.repository.UsersRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class MyUserDetailsService implements ReactiveUserDetailsService {

    private final UsersRepo usersRepo;
    private final UserRoleRepo userRoleRepo;
    private final RoleRepo roleRepo;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return getUserWithRoles(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User not found")))
                .map(u -> new UserPrincipal(u.user(), u.roles()));
    }

    private Mono<UserWithRolesDTO> getUserWithRoles(String username) {
        return usersRepo.findByUsername(username)
                .flatMap(user ->
                        userRoleRepo.findByUserId(user.getUserId())
                                .flatMap(userRoles ->
                                       roleRepo.findById(userRoles.getRoleId())
                                )
                                .collectList()
                                .map(roles -> new UserWithRolesDTO(user,roles))

                );
    }
}
