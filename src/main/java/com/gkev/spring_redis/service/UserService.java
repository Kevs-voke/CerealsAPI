package com.gkev.spring_redis.service;

import com.gkev.spring_redis.DTO.RegistrationResponseDTO;
import com.gkev.spring_redis.DTO.UserDTO;
import com.gkev.spring_redis.Entity.RolesEntity;
import com.gkev.spring_redis.Entity.UserRoleEntity;
import com.gkev.spring_redis.Entity.UsersEntity;
import com.gkev.spring_redis.Exceptions.UserException;
import com.gkev.spring_redis.Mapper.RegistrationResponseDTOMapper;
import com.gkev.spring_redis.Mapper.UserMapper;
import com.gkev.spring_redis.Utilities.UsernameGenerator;
import com.gkev.spring_redis.repository.RoleRepo;
import com.gkev.spring_redis.repository.UserRoleRepo;
import com.gkev.spring_redis.repository.UsersRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@Service
@RequiredArgsConstructor
public class UserService {
    private final TransactionalOperator transactionalOperator;
    private final UsersRepo usersRepo;
    private final RoleRepo roleRepo;
    private  final UserRoleRepo userRoleRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;



    public Mono<RegistrationResponseDTO> register(UserDTO userDTO) {
        return transactionalOperator.transactional(
                validateEmailAndUsrNameAndPhoneNo(userDTO)
                        .then(Mono.defer(()->{
                            UsersEntity user = userMapper.toUsersEntity(userDTO);
                            user.setPasswrd(passwordEncoder.encode(userDTO.password))

                            return usersRepo.save(user);
                        }))
                        .flatMap(savedUser ->
                                saveRoles(savedUser.getId(), userDTO.roles()).thenReturn(savedUser)
                        )
                        .map(RegistrationResponseDTOMapper::toResponse)
        );
    }

    private Mono<Void> validateEmailAndUsrNameAndPhoneNo(UserDTO usrDto){
        return usersRepo.existsByEmail(usrDto.email())
                .flatMap(emailExists ->{
                    if(emailExists){
                        return Mono.error(new UserException("Email already in use", "EMAIL_ALREADY_EXISTS"));
                    }
                    return usersRepo.existsByUsername(usrDto.username());
                })
                .flatMap(usernameExists -> {
                    if(usernameExists){
                    return UsernameGenerator.generateSuggestions(
                            usrDto.firstName(),
                            usrDto.email(),
                            usrDto.lastName(),
                            5
                    )
                            .collectList()
                            .flatMap(suggestions -> Mono.error(new UserException(
                                    "Username already taken",
                                    "USERNAME_TAKEN",
                                    suggestions
                            )));
                }
                return usersRepo.existsByPhoneNumber(usrDto.phoneNumber());

         })
                .flatMap(phoneNumberExists ->{
                    if (phoneNumberExists){
                        return Mono.error(new UserException("Phone Number already in use", "PHONE_NUMBER_ALREADY_EXISTS"));
                    }
                    return Mono.empty();
                });
    }
    private Mono<Void> saveRoles(int userId, Iterable<String> roles) {
        return Flux.fromIterable(roles)
                .flatMap(roleName ->
                        roleRepo.findByName(roleName.toUpperCase())
                                .switchIfEmpty(Mono.error(
                                        new UserException("Invalid role: " + roleName, "INVALID_ROLE")
                                ))
                                .map(RolesEntity::getId)
                                .map(roleId -> {
                                    UserRoleEntity entity = new UserRoleEntity();
                                    entity.setUserId(userId);
                                    entity.setRoleId(roleId);
                                    return entity;
                                })
                )
                .flatMap(userRoleRepo::save)
                .then();
    }

}
