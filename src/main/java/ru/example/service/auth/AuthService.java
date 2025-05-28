package ru.example.service.auth;

import ru.example.dto.UserRegistrationDto;

/**
 * Basic interface for authentication service
 */
public interface AuthService {

    /**
     * Register new user
     *
     * @param userDto {@link UserRegistrationDto} dto with registering user's data
     */
    void register(UserRegistrationDto userDto);
}
