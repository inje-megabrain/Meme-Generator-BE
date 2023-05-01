package com.example.codebase.domain.wanted.dto;

import com.example.codebase.domain.wanted.entity.Wanted;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class WantedResponseDTO {
    private Long wantedId;
    private String name;
    private String description;
    private String imageUrl;
    private Integer prize;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public WantedResponseDTO(Wanted save) {
        this.wantedId = save.getId();
        this.name = save.getName();
        this.description = save.getDescription();
        this.imageUrl = save.getImageUrl();
        this.prize = save.getPrize();
        this.createdAt = save.getCreatedAt();
        this.updatedAt = save.getUpdatedAt();
    }
}
