package ru.example.service.schedular;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.example.mapper.RedisHashKeyField;
import ru.example.repo.ShortUrlRepo;

import java.util.Map;
import java.util.Set;

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
}
