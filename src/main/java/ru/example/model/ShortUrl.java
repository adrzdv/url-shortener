package ru.example.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "short_url")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ShortUrl {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String originalUrl;
    @Column(nullable = false, unique = true)
    private String shortCode;
    @Column(nullable = false)
    private LocalDate createdAt;
    //@Column(nullable = false)
    @Transient
    private Integer ttlDays;
    @Column(nullable = false)
    private LocalDate expiresAt;
    @Column(nullable = false)
    private Integer visitCount;
    @Column
    private Integer maxVisit;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
        if (expiresAt == null && ttlDays != null) {
            expiresAt = createdAt.plusDays(ttlDays);
        }
    }
}
