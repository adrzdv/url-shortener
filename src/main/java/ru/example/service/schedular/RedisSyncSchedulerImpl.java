package ru.example.service.schedular;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.example.mapper.RedisHashKeyField;
import ru.example.model.ShortUrl;
import ru.example.repo.ShortUrlRepo;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RedisSyncSchedulerImpl implements RedisSyncScheduler {

    private final RedisTemplate<String, String> redisTemplate;
    private final ShortUrlRepo shortUrlRepo;

    @Autowired
    public RedisSyncSchedulerImpl(RedisTemplate redisTemplate, ShortUrlRepo shortUrlRepo) {
        this.redisTemplate = redisTemplate;
        this.shortUrlRepo = shortUrlRepo;
    }

    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Override
    public void syncVisitCountsToDb() {

        Set<String> keys = redisTemplate.keys(RedisHashKeyField.REDIS_PREFIX.key() + "*");

        if (keys == null) return;

        for (String key : keys) {
            Map<Object, Object> hashValue = redisTemplate.opsForHash().entries(key);
            String code = key.replaceFirst(RedisHashKeyField.REDIS_PREFIX.key(), "");

            Integer visitCount = Integer.parseInt((String) hashValue.get(RedisHashKeyField.VISIT_COUNT.key()));

            shortUrlRepo.findByShortCode(code).ifPresent(shortUrl -> {
                if (shortUrl.getVisitCount() < visitCount) {
                    shortUrl.setVisitCount(visitCount);
                    shortUrlRepo.save(shortUrl);
                }
            });
        }
    }

    @Override
    @Scheduled(fixedRate = 24 * 60 * 60 * 1000L)
    @Transactional
    public void cleanExpiredShorts() {

        List<ShortUrl> expiredUrls = shortUrlRepo.findAllByExpiresAtBefore(LocalDate.now());

        if (expiredUrls.isEmpty()) return;

        shortUrlRepo.deleteAll(expiredUrls);

        Set<String> expiredKeys = expiredUrls.stream()
                .map(shortUrl -> RedisHashKeyField.REDIS_PREFIX.key() + shortUrl.getShortCode())
                .collect(Collectors.toSet());

        redisTemplate.delete(expiredKeys);

    }

}
