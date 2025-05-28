package ru.example.service.shortener;

import ru.example.dto.UrlShortenerDto;
import ru.example.exception.NotFoundShortUrlException;
import ru.example.exception.VisitLimitExceedException;
import ru.example.model.ShortUrl;

import java.util.function.Consumer;

/**
 * Service for create and get short urls
 */
public interface ShortenerService {

    /**
     * Method for make short urls
     *
     * @param urlShortener dto from request in {@link UrlShortenerDto} format
     * @return {@link ShortUrl}
     */
    ShortUrl getShortUrl(UrlShortenerDto urlShortener);

    /**
     * Method for search urls by short code
     *
     * @param code short code of url
     * @return {@link Short}
     * @throws VisitLimitExceedException
     * @throws NotFoundShortUrlException
     */
    ShortUrl findByCode(String code) throws VisitLimitExceedException, NotFoundShortUrlException;

    /**
     * Method for approve existing short url
     *
     * @param code short code of url
     * @return boolean value
     */
    boolean approve(String code);

    /**
     * Thread safety method for approve url
     *
     * @param code    short code of url
     * @param updater consumer-function for update
     */
    void updateShortUrlWithRetry(String code, Consumer<ShortUrl> updater) throws InterruptedException;
}
