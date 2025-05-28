package ru.example.mapper;

import ru.example.model.ShortUrl;

import java.time.LocalDate;
import java.util.Map;

public class ShortUrlSerializer {

    public static final String ORIGINAL_URL_KEY = "originalUrl";
    public static final String SHORT_CODE_KEY = "shortCode";
    public static final String CREATED_AT_KEY = "createdAt";
    public static final String EXPIRES_AT_KEY = "expiresAt";
    public static final String VISIT_COUNT_KEY = "visitCount";
    public static final String MAX_VISIT_KEY = "maxVisit";
    public static final String IS_APPROVED_KEY = "isApproved";

    public static Map<String, String> serializeShortUrl(ShortUrl shortUrl) {
        return Map.of(ORIGINAL_URL_KEY, shortUrl.getOriginalUrl().toString(),
                SHORT_CODE_KEY, shortUrl.getShortCode().toString(),
                CREATED_AT_KEY, shortUrl.getCreatedAt().toString(),
                EXPIRES_AT_KEY, shortUrl.getExpiresAt().toString(),
                VISIT_COUNT_KEY, shortUrl.getVisitCount().toString(),
                MAX_VISIT_KEY, shortUrl.getMaxVisit().toString(),
                IS_APPROVED_KEY, shortUrl.getIsApproved().toString());
    }

    public static ShortUrl deserializeFromRedisHash(String code, Map<String, String> hashValue) {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode(code);
        shortUrl.setOriginalUrl(hashValue.get(ORIGINAL_URL_KEY));
        shortUrl.setExpiresAt(LocalDate.parse(hashValue.get(EXPIRES_AT_KEY)));
        shortUrl.setVisitCount(Integer.parseInt(hashValue.get(VISIT_COUNT_KEY)));
        shortUrl.setMaxVisit(Integer.parseInt(hashValue.get(MAX_VISIT_KEY)));
        shortUrl.setIsApproved(Boolean.parseBoolean(hashValue.get(IS_APPROVED_KEY)));

        return shortUrl;
    }
}
