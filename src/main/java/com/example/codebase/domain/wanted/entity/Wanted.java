package com.example.codebase.domain.wanted.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name = "wanted")
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Wanted {

    @Id
    @Column(name = "wanted_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @Column(name = "prize", columnDefinition = "DECIMAL(10)")
    private Integer prize;

    @Column(name = "image_url")
    private String imageUrl;


    @Column(name = "created_at", columnDefinition = "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;
}
