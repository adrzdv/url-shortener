package ru.example.service;

import ru.example.dto.UserRegistrationDto;

public interface AuthService {
    void register(UserRegistrationDto userDto);
}
