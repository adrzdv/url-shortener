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
    @Transient
    private Integer ttlDays;
    @Column(nullable = false)
    private LocalDate expiresAt;
    @Column(nullable = false)
    private Integer visitCount;
    @Column
    private Integer maxVisit;
    @Column(nullable = false)
    private Boolean isApproved;
    @Version
    private Long version;

    @PrePersist
    public void prePersist() {
        isApproved = false;
        if (maxVisit == null) maxVisit = 0;
        visitCount = 0;
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
        if (expiresAt == null && ttlDays != null) {
            expiresAt = createdAt.plusDays(ttlDays);
        }
    }
}
