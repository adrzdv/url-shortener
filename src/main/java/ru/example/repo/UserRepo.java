package ru.example.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.model.User;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String username);

    boolean existsByUserName(String name);
}
