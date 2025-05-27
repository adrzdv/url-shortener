package ru.example.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.example.dto.UserRegistrationDto;
import ru.example.model.Role;
import ru.example.model.User;
import ru.example.repo.RoleRepository;
import ru.example.repo.UserRepo;

import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepo userRepo;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServiceImpl(UserRepo userRepo,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public void register(UserRegistrationDto userDto) {

        if (userRepo.existsByUserName(userDto.getUserName())) {
            throw new RuntimeException("User already exists");
        }

        String encodedPassword = passwordEncoder.encode(userDto.getPassword());

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = new User();
        user.setUserName(userDto.getUserName());
        user.setPassword(encodedPassword);
        user.setRoles(Set.of(userRole));

        userRepo.save(user);
    }
}
