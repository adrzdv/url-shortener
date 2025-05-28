package ru.example.service.schedular;

/**
 * Base service for scheduling
 */
public interface RedisSyncScheduler {

    /**
     * Sync count of visits between cache and db
     */
    void syncVisitCountsToDb();

    /**
     * Clean expired urls from database and cache also
     */
    void cleanExpiredShorts();
}
