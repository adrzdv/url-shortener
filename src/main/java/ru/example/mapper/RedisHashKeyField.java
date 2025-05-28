package ru.example.mapper;

/**
 * ENUM with basic redis hash keys
 */
public enum RedisHashKeyField {
    ORIGINAL_URL("originalUrl"),
    SHORT_CODE("shortCode"),
    CREATED_AT("createdAt"),
    EXPIRES_AT("expiresAt"),
    VISIT_COUNT("visitCount"),
    MAX_VISIT("maxVisit"),
    IS_APPROVED("isApproved"),
    REDIS_PREFIX("short:");

    private final String field;

    RedisHashKeyField(String field) {
        this.field = field;
    }

    public String key() {
        return field;
    }
}

