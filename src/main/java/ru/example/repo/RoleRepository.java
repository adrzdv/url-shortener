package ru.example.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.model.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
