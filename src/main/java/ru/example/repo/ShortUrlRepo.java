package ru.example.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.example.model.ShortUrl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShortUrlRepo extends JpaRepository<ShortUrl, Long> {

    boolean existsByShortCode(String shortCode);

    Optional<ShortUrl> findByShortCode(String code);

    List<ShortUrl> findAllByExpiresAtBefore(LocalDate date);
}
