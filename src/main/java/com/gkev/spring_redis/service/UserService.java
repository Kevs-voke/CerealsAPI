package com.gkev.spring_redis.service;

import com.gkev.spring_redis.DTO.LoginRequestDTO;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final UserRoleRepo userRoleRepo;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationResponseDTOMapper registrationResponseDTOMapper;
    private final JwtService jwtService;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public Mono<RegistrationResponseDTO> register(UserDTO userDTO) {
        logger.info("Registering user with email: {}", userDTO.email());

        return transactionalOperator.transactional(
                validateEmailAndUsrNameAndPhoneNo(userDTO)
                        .then(Mono.defer(() -> {
                            UsersEntity user = userMapper.toUsersEntity(userDTO);
                            user.setPasswrd(passwordEncoder.encode(userDTO.password()));
                            return usersRepo.save(user);
                        }))
                        .flatMap(savedUser ->
                                saveRoles(savedUser.getUserId(), userDTO.roles())
                                        .thenReturn(savedUser)
                        )
                        .flatMap(savedUser -> {
                            String jwtToken = jwtService.generateToken(savedUser);
                            RegistrationResponseDTO response = registrationResponseDTOMapper.toResponse(savedUser, jwtToken);

                            logger.info("User registered successfully: {}", savedUser.getEmail());

                            return Mono.just(response);
                        })
                        .doOnError(e -> logger.error("Registration failed for email {}: {}", userDTO.email(), e.getMessage()))
        );
    }


    private Mono<Void> validateEmailAndUsrNameAndPhoneNo(UserDTO usrDto) {
        logger.info("Validating email: {}", usrDto.email());

        return usersRepo.existsByEmail(usrDto.email())
                .flatMap(emailExists -> {
                    if (emailExists) {
                        return Mono.error(new UserException("Email already in use", "EMAIL_ALREADY_EXISTS"));
                    }
                    logger.info("Email validated");

                    logger.info("Validating username for user with email {}", usrDto.email());
                    return usersRepo.existsByUsername(usrDto.username());
                })
                .flatMap(usernameExists -> {
                    if (usernameExists) {
                        logger.info("Username already exists for user with email {}", usrDto.email());
                        return UsernameGenerator.generateSuggestions(
                                        usrDto.firstName(),
                                        usrDto.email(),
                                        usrDto.lastName(),
                                        5
                                ).collectList()
                                .flatMap(suggestions -> Mono.error(new UserException(
                                        "Username already taken",
                                        "USERNAME_TAKEN",
                                        suggestions
                                )));
                    }
                    logger.info("Username validated for user with email {}", usrDto.email());

                    logger.info("Validating phone number for user with email {}", usrDto.email());
                    return usersRepo.existsByPhoneNumber(usrDto.phoneNumber());
                })
                .flatMap(phoneExists -> {
                    if (phoneExists) {
                        return Mono.error(new UserException("Phone Number already in use", "PHONE_NUMBER_ALREADY_EXISTS"));
                    }
                    logger.info("Phone number validated for user with email {}", usrDto.email());
                    return Mono.empty();
                });
    }

    private Mono<Void> saveRoles(int userId, Iterable<String> roles) {
        logger.info("Saving roles ");

        return Flux.fromIterable(roles)
                .flatMap(roleName ->
                        roleRepo.findByName(roleName.toUpperCase())
                                .switchIfEmpty(Mono.error(new UserException(
                                        "Invalid role: " + roleName,
                                        "INVALID_ROLE"
                                )))
                                .map(RolesEntity::getId)
                                .map(roleId -> {
                                    UserRoleEntity entity = new UserRoleEntity();
                                    entity.setUserId(userId);
                                    entity.setRoleId(roleId);
                                    return entity;
                                })
                                .flatMap(userRoleRepo::save)
                )
                .collectList()
                .doOnSuccess(saved -> logger.info("Roles successfully saved"))
                .then();
    }
    public Mono<RegistrationResponseDTO> login(LoginRequestDTO loginRequest) {

        logger.info("Login attempt for email: {}", loginRequest.email());

        return usersRepo.findByEmail(loginRequest.email())
                .switchIfEmpty(Mono.error(
                        new UserException(
                                "Invalid email or password",
                                "INVALID_CREDENTIALS"
                        )
                ))
                .flatMap(user -> {

                    boolean passwordMatches = passwordEncoder.matches(
                            loginRequest.password(),
                            user.getPasswrd()
                    );

                    if (!passwordMatches) {
                        return Mono.error(
                                new UserException(
                                        "Invalid email or password",
                                        "INVALID_CREDENTIALS"
                                )
                        );
                    }

                    String jwtToken = jwtService.generateToken(user);

                    RegistrationResponseDTO response =
                            registrationResponseDTOMapper.toResponse(user, jwtToken);

                    logger.info("User logged in successfully: {}", user.getEmail());

                    return Mono.just(response);
                })
                .doOnError(error ->
                        logger.error(
                                "Login failed for email {}: {}",
                                loginRequest.email(),
                                error.getMessage()
                        )
                );
    }
}