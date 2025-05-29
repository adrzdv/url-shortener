package ru.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.example.dto.UrlShortenerDto;
import ru.example.model.ShortUrl;
import ru.example.service.shortener.ShortenerService;

import java.net.URI;

@Tag(name = "Generating short url")
@RestController
@RequestMapping("/makeurl")
@AllArgsConstructor
public class ShortUrlController {

    private final ShortenerService shortenerService;

    @PostMapping
    public ResponseEntity<String> makeShortUrl(@Valid @RequestBody UrlShortenerDto urlShortenerDto) {

        ShortUrl shortUrl = shortenerService.getShortUrl(urlShortenerDto);
        return ResponseEntity.status(HttpStatus.OK).body(shortUrl.getShortCode());
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirectToOriginal(@PathVariable String code) {

        ShortUrl shortUrl = shortenerService.findByCode(code);
        String originalUrl = shortUrl.getOriginalUrl();

        if (!originalUrl.startsWith("http://") && !originalUrl.startsWith("https://")) {
            originalUrl = "http://" + originalUrl;
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();

    }

    @PostMapping("/{code}/approve")
    public ResponseEntity<Void> approveUrl(@PathVariable String code) {
        if (shortenerService.approve(code)) {
            return ResponseEntity.status(HttpStatus.OK).build();
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
