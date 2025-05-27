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
import java.time.LocalDate;

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

        try {
            ShortUrl shortUrl = shortenerService.findByCode(code);

            if (shortUrl == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else if (shortUrl.getExpiresAt().isBefore(LocalDate.now())) {
                ResponseEntity.status(HttpStatus.GONE);
            }

            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(shortUrl.getOriginalUrl()))
                    .build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }


    }
}
