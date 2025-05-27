package ru.example.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UrlShortenerDto {
    @NotBlank(message = "URL required")
    private String originalUrl;
    @Min(1)
    @Max(365)
    private Integer ttlDays;
}
