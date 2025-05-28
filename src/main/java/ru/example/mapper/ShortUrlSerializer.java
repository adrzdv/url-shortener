package ru.example.mapper;

import ru.example.model.ShortUrl;

import java.time.LocalDate;
import java.util.Map;

public class ShortUrlSerializer {

    public static Map<String, String> serializeShortUrl(ShortUrl shortUrl) {
        return Map.of(RedisHashKeyField.ORIGINAL_URL.key(), shortUrl.getOriginalUrl().toString(),
                RedisHashKeyField.SHORT_CODE.key(), shortUrl.getShortCode().toString(),
                RedisHashKeyField.CREATED_AT.key(), shortUrl.getCreatedAt().toString(),
                RedisHashKeyField.EXPIRES_AT.key(), shortUrl.getExpiresAt().toString(),
                RedisHashKeyField.VISIT_COUNT.key(), shortUrl.getVisitCount().toString(),
                RedisHashKeyField.MAX_VISIT.key(), shortUrl.getMaxVisit().toString(),
                RedisHashKeyField.IS_APPROVED.key(), shortUrl.getIsApproved().toString());
    }

    public static ShortUrl deserializeFromRedisHash(String code, Map<String, String> hashValue) {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode(code);
        shortUrl.setOriginalUrl(hashValue.get(RedisHashKeyField.ORIGINAL_URL.key()));
        shortUrl.setExpiresAt(LocalDate.parse(hashValue.get(RedisHashKeyField.EXPIRES_AT.key())));
        shortUrl.setVisitCount(Integer.parseInt(hashValue.get(RedisHashKeyField.VISIT_COUNT.key())));
        shortUrl.setMaxVisit(Integer.parseInt(hashValue.get(RedisHashKeyField.MAX_VISIT.key())));
        shortUrl.setIsApproved(Boolean.parseBoolean(hashValue.get(RedisHashKeyField.IS_APPROVED.key())));

        return shortUrl;
    }
}
