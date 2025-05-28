package ru.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import ru.example.dto.UrlShortenerDto;
import ru.example.exception.VisitLimitExceedException;
import ru.example.model.ShortUrl;
import ru.example.repo.ShortUrlRepo;
import ru.example.service.shortener.ShortenerServiceImpl;

import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortenerServiceImplTest {

    @Mock
    private ShortUrlRepo shortUrlRepo;

    @Mock
    private RedisTemplate<String, ShortUrl> redisTemplate;

    @InjectMocks
    private ShortenerServiceImpl shortenerService;

    @Mock
    private ValueOperations<String, ShortUrl> valueOperations;

    @BeforeEach
    void setUp() {
        //lenient - типа ошибки есть, но ты тут не ори
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(shortUrlRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldGenerateShortUrlWithValidData() {
        UrlShortenerDto dto = new UrlShortenerDto("https://example.com", 3, 2);

        when(shortUrlRepo.existsByShortCode(anyString())).thenReturn(false);
        when(shortUrlRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        ShortUrl result = shortenerService.getShortUrl(dto);

        assertNotNull(result.getShortCode());
        assertEquals(dto.getOriginalUrl(), result.getOriginalUrl());
        //0 мы тут не получим, потому что PrePersist тут не работает и надо поднимать H2 как минимум, а это лучше
        //мы сделаем в интеграционном тестировании
        assertEquals(null, result.getVisitCount());
        assertNotNull(result.getExpiresAt());
    }

    @Test
    void shouldThrowExceptionIfNotFoundInDbAndCache() {
        String code = "abcdef";

        when(redisTemplate.opsForValue().get(code)).thenReturn(null);
        when(shortUrlRepo.findByShortCode(code)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> shortenerService.findByCode(code));
    }

    @Test
    void shouldReturnFromCacheIfExists() {
        String code = "cached123";
        ShortUrl cached = new ShortUrl();
        cached.setOriginalUrl("https://cached.com");
        cached.setShortCode(code);
        cached.setVisitCount(5);
        cached.setIsApproved(true);
        cached.setExpiresAt(LocalDate.now().plusDays(1L));

        when(redisTemplate.opsForValue().get(code)).thenReturn(cached);

        verify(shortUrlRepo, never()).findByShortCode(any());

        ShortUrl result = shortenerService.findByCode(code);

        assertEquals("https://cached.com", result.getOriginalUrl());
        assertEquals(6, result.getVisitCount());
    }

    @Test
    void shouldCacheIfFoundInDb() {
        String code = "db123";
        ShortUrl dbResult = new ShortUrl();
        dbResult.setOriginalUrl("https://db.com");
        dbResult.setShortCode(code);
        dbResult.setVisitCount(0);
        dbResult.setIsApproved(true);
        dbResult.setExpiresAt(LocalDate.now().plusDays(1L));

        when(redisTemplate.opsForValue().get(code)).thenReturn(null);
        when(shortUrlRepo.findByShortCode(code)).thenReturn(Optional.of(dbResult));

        ShortUrl result = shortenerService.findByCode(code);

        assertEquals("https://db.com", result.getOriginalUrl());
        verify(valueOperations).set(eq(code), eq(dbResult), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    void shouldThrowExceptionWhenVisitLimitExceeded() {
        String code = "limitCode";
        ShortUrl cached = new ShortUrl();
        cached.setShortCode(code);
        cached.setOriginalUrl("https://limited.com");
        cached.setVisitCount(5);
        cached.setMaxVisit(5);
        cached.setIsApproved(true);
        cached.setExpiresAt(LocalDate.now().plusDays(1));

        when(redisTemplate.opsForValue().get(code)).thenReturn(cached);

        assertThrows(VisitLimitExceedException.class, () -> shortenerService.findByCode(code));

        verify(redisTemplate).delete(code);
    }
}