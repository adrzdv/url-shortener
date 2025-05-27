package ru.example.service.auth;

import ru.example.dto.UserRegistrationDto;

public interface AuthService {
    void register(UserRegistrationDto userDto);
}
