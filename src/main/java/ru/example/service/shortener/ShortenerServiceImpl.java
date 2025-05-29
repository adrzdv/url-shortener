package ru.example.service.shortener;

import jakarta.persistence.OptimisticLockException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.dto.UrlModerationRequest;
import ru.example.dto.UrlShortenerDto;
import ru.example.exception.LinkExpiredException;
import ru.example.exception.NotApprovedException;
import ru.example.exception.NotFoundShortUrlException;
import ru.example.exception.VisitLimitExceedException;
import ru.example.mapper.RedisHashKeyField;
import ru.example.mapper.ShortUrlSerializer;
import ru.example.model.ShortUrl;
import ru.example.rabbitmq.RabbitSender;
import ru.example.repo.ShortUrlRepo;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

@Service
public class ShortenerServiceImpl implements ShortenerService {

    private static final int SHORT_CODE_LENGTH = 6;
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final Random random = new SecureRandom();
    private final ShortUrlRepo shortUrlRepo;
    private final RedisTemplate<String, String> redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitSender rabbitSender;

    @Autowired
    public ShortenerServiceImpl(ShortUrlRepo shortUrlRepo,
                                RedisTemplate<String, String> redisTemplate,
                                RabbitTemplate rabbitTemplate,
                                RabbitSender rabbitSender) {
        this.shortUrlRepo = shortUrlRepo;
        this.redisTemplate = redisTemplate;
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitSender = rabbitSender;
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

        if (urlShortener.getMaxVisit() != null) {
            shortUrl.setMaxVisit(urlShortener.getMaxVisit());
        } else {
            shortUrl.setMaxVisit(0);
        }

        shortUrl.setOriginalUrl(urlShortener.getOriginalUrl());
        shortUrl.setShortCode(generateUniqueCode());

        ShortUrl saved = shortUrlRepo.save(shortUrl);

        rabbitSender.sendToModerationQueue(new UrlModerationRequest(saved.getShortCode()));

        return saved;
    }

    @Override
    @Transactional
    public ShortUrl findByCode(String code) throws VisitLimitExceedException, NotFoundShortUrlException {

        Map<Object, Object> hashValue = redisTemplate.opsForHash()
                .entries(RedisHashKeyField.REDIS_PREFIX.key() + code);
        String redisKey = RedisHashKeyField.REDIS_PREFIX.key() + code;

        if (!hashValue.isEmpty()) {

            ShortUrl cached = ShortUrlSerializer.deserializeFromRedisHash(code, hashValue);

            validateLinkOnConstraints(code, true, cached);
            cached.setVisitCount(cached.getVisitCount() + 1);

            redisTemplate.opsForHash().increment(redisKey,
                    RedisHashKeyField.VISIT_COUNT.key(),
                    1);

            return cached;
        }

        ShortUrl fromDb = shortUrlRepo.findByShortCode(code)
                .orElseThrow(() -> new NotFoundShortUrlException("Not found"));

        validateLinkOnConstraints(code, false, fromDb);

        fromDb.setVisitCount(fromDb.getVisitCount() + 1);

        redisTemplate.opsForHash().increment(redisKey,
                RedisHashKeyField.VISIT_COUNT.key(),
                1);

        return fromDb;
    }

    @Override
    @Transactional
    public boolean approve(String code) {

        ShortUrl shortUrl = shortUrlRepo.findByShortCode(code)
                .orElseThrow(() ->
                        new NotFoundShortUrlException("Not found"));

        shortUrl.setIsApproved(true);

        redisTemplate.opsForHash().put(RedisHashKeyField.REDIS_PREFIX.key() + code,
                RedisHashKeyField.IS_APPROVED.key(),
                "true");

        return true;
    }

    @Override
    @Transactional
    public void updateShortUrlWithRetry(String code, Consumer<ShortUrl> updater) throws InterruptedException {
        int retries = 5;
        while (retries > 0) {
            try {
                ShortUrl shortUrl = shortUrlRepo.findByShortCode(code)
                        .orElseThrow(() -> new NotFoundShortUrlException("Url not found"));
                updater.accept(shortUrl);
                ShortUrl saved = shortUrlRepo.save(shortUrl);

                Duration ttl = Duration.between(LocalDate.now().atStartOfDay(), saved.getExpiresAt().atTime(LocalTime.MAX));
                String redisKey = RedisHashKeyField.REDIS_PREFIX.key() + saved.getShortCode();
                Map<String, String> hashValue = ShortUrlSerializer.serializeShortUrl(saved);

                redisTemplate.opsForHash().putAll(redisKey,
                        hashValue);

                if (!ttl.isNegative() && !ttl.isZero()) {
                    redisTemplate.expire(redisKey, ttl);
                }
                return;

            } catch (OptimisticLockException e) {
                retries--;
                if (retries == 0) throw e;
            } catch (Exception e) {
                return;
            }

            //Using exponential backoff for:
            //More chances to unlock object
            //Industrial standard
            //Low capacity on DB
            Thread.sleep(100L * (6 - retries));
        }
    }

    private void validateLinkOnConstraints(String code, boolean isCached, ShortUrl shortUrl) {

        if (!shortUrl.getIsApproved()) {
            throw new NotApprovedException("Link is not approved yet");
        } else if (!shortUrl.getExpiresAt().isAfter(LocalDate.now())) {
            if (isCached) redisTemplate.delete(code);
            throw new LinkExpiredException("Link has expired");
        } else if (shortUrl.getMaxVisit() != null && shortUrl.getMaxVisit() > 0
                && shortUrl.getMaxVisit() <= shortUrl.getVisitCount()) {
            if (isCached) redisTemplate.delete(code);
            throw new VisitLimitExceedException("Visit limit exceed");
        }
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
