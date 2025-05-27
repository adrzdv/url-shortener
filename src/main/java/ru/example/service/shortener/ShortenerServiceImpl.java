package ru.example.service.shortener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.controller.ShortUrlController;
import ru.example.dto.UrlShortenerDto;
import ru.example.model.ShortUrl;
import ru.example.repo.ShortUrlRepo;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class ShortenerServiceImpl implements ShortenerService {

    private static final int SHORT_CODE_LENGTH = 6;
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random random = new SecureRandom();
    private final ShortUrlRepo shortUrlRepo;
    private final RedisTemplate<String, ShortUrl> redisTemplate;

    @Autowired
    public ShortenerServiceImpl(ShortUrlRepo shortUrlRepo,
                                RedisTemplate redisTemplate) {
        this.shortUrlRepo = shortUrlRepo;
        this.redisTemplate = redisTemplate;
    }


    @Override
    public ShortUrl getShortUrl(UrlShortenerDto urlShortener) {

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setCreatedAt(LocalDate.now());

        if (urlShortener.getTtlDays() != null) {
            shortUrl.setExpiresAt(shortUrl.getCreatedAt().plusDays(urlShortener.getTtlDays()));
        } else {
            shortUrl.setExpiresAt(shortUrl.getCreatedAt().plusDays(1L));
        }

        shortUrl.setOriginalUrl(urlShortener.getOriginalUrl());
        shortUrl.setShortCode(generateUniqueCode());

        ShortUrl saved = shortUrlRepo.save(shortUrl);

        long ttl = Duration.between(LocalDateTime.now(), saved.getExpiresAt().atStartOfDay()).getSeconds();

        redisTemplate.opsForValue().set(saved.getShortCode(), saved, ttl, TimeUnit.SECONDS);

        return saved;
    }

    @Override
    @Transactional
    public ShortUrl findByCode(String code) throws RuntimeException {

        ShortUrl cached = redisTemplate.opsForValue().get(code);

        if (cached != null) {
            cached.setVisitCount(cached.getVisitCount() + 1);
            return cached;
        }

        ShortUrl fromDb = shortUrlRepo.findByShortCode(code)
                .orElseThrow(() -> new RuntimeException("Not found"));

        fromDb.setVisitCount(fromDb.getVisitCount() + 1);

        return fromDb;
    }

    private String generateRandomString(int length) {

        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }

        return sb.toString();
    }

    private String generateUniqueCode() {

        String code;

        do {
            code = generateRandomString(SHORT_CODE_LENGTH);
        } while (shortUrlRepo.existsByShortCode(code));

        return code;

    }
}
