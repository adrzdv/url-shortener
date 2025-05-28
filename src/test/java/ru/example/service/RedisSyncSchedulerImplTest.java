package ru.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import ru.example.mapper.RedisHashKeyField;
import ru.example.model.ShortUrl;
import ru.example.repo.ShortUrlRepo;
import ru.example.service.schedular.RedisSyncSchedulerImpl;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RedisSyncSchedulerImplTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ShortUrlRepo shortUrlRepo;

    @InjectMocks
    private RedisSyncSchedulerImpl scheduler;

    private static final String REDIS_PREFIX = RedisHashKeyField.REDIS_PREFIX.key();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    void syncVisitCountsToDb_noKeys_noSave() {
        when(redisTemplate.keys(REDIS_PREFIX + "*")).thenReturn(Collections.emptySet());

        scheduler.syncVisitCountsToDb();

        verify(redisTemplate).keys(REDIS_PREFIX + "*");
        verifyNoMoreInteractions(shortUrlRepo, hashOperations);
    }

    @Test
    void syncVisitCountsToDb_visitCountGreater_saveUpdated() {
        String code = "xyz789";
        String redisKey = REDIS_PREFIX + code;

        Set<String> keys = Set.of(redisKey);
        when(redisTemplate.keys(REDIS_PREFIX + "*")).thenReturn(keys);

        Map<Object, Object> hashValue = new HashMap<>();
        hashValue.put("visitCount", "15");
        when(hashOperations.entries(redisKey)).thenReturn(hashValue);

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode(code);
        shortUrl.setVisitCount(10);

        when(shortUrlRepo.findByShortCode(code)).thenReturn(Optional.of(shortUrl));

        scheduler.syncVisitCountsToDb();

        assertEquals(15, shortUrl.getVisitCount());

        verify(redisTemplate).keys(REDIS_PREFIX + "*");
        verify(redisTemplate).opsForHash();
        verify(hashOperations).entries(redisKey);
        verify(shortUrlRepo).findByShortCode(code);
        verify(shortUrlRepo).save(shortUrl);
    }

    @Test
    void syncVisitCountsToDb_visitCountLess_noSave() {
        String code = "abc123";
        String redisKey = REDIS_PREFIX + code;

        Set<String> keys = Set.of(redisKey);
        when(redisTemplate.keys(REDIS_PREFIX + "*")).thenReturn(keys);

        Map<Object, Object> hashValue = new HashMap<>();
        hashValue.put("visitCount", "3");
        when(hashOperations.entries(redisKey)).thenReturn(hashValue);

        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode(code);
        shortUrl.setVisitCount(5);

        when(shortUrlRepo.findByShortCode(code)).thenReturn(Optional.of(shortUrl));

        scheduler.syncVisitCountsToDb();

        assertEquals(5, shortUrl.getVisitCount());

        verify(shortUrlRepo, never()).save(any());
    }

    @Test
    void cleanExpiredShorts_emptyList_nothingDeleted() {
        when(shortUrlRepo.findAllByExpiresAtBefore(any(LocalDate.class))).thenReturn(Collections.emptyList());

        scheduler.cleanExpiredShorts();

        verify(shortUrlRepo).findAllByExpiresAtBefore(any(LocalDate.class));
        verifyNoMoreInteractions(shortUrlRepo);
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void cleanExpiredShorts_expiredUrls_deletedFromDbAndRedis() {
        ShortUrl url1 = new ShortUrl();
        url1.setShortCode("abc123");

        ShortUrl url2 = new ShortUrl();
        url2.setShortCode("def456");

        List<ShortUrl> expiredUrls = List.of(url1, url2);
        when(shortUrlRepo.findAllByExpiresAtBefore(any(LocalDate.class))).thenReturn(expiredUrls);

        doNothing().when(shortUrlRepo).deleteAll(expiredUrls);
        when(redisTemplate.delete(anySet())).thenReturn(1L);

        scheduler.cleanExpiredShorts();

        verify(shortUrlRepo).findAllByExpiresAtBefore(any(LocalDate.class));
        verify(shortUrlRepo).deleteAll(expiredUrls);

        Set<String> expectedKeys = expiredUrls.stream()
                .map(url -> REDIS_PREFIX + url.getShortCode())
                .collect(Collectors.toSet());

        verify(redisTemplate).delete(expectedKeys);
    }
}