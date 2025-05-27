package ru.example.service.shortener;

import ru.example.dto.UrlShortenerDto;
import ru.example.model.ShortUrl;

public interface ShortenerService {

    ShortUrl getShortUrl(UrlShortenerDto urlShortener);

    ShortUrl findByCode(String code) throws RuntimeException;
}
