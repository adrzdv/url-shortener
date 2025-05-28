package ru.example.service.schedular;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import ru.example.repo.ShortUrlRepo;

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
    public void sync() {

    }
}
