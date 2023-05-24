package com.example.codebase.domain.meme.dto;

import com.example.codebase.domain.meme.entity.Meme;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MemeResponseDTO {
    private Long memeId;

    private String name;

    private String username;

    private String imageUrl;

    private String type;

    private Boolean publicFlag;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public MemeResponseDTO(Meme save) {
        this.memeId = save.getId();
        this.name = save.getName();
        this.username = save.getUsername();
        this.imageUrl = save.getImageUrl();
        this.type = save.getType().toString();
        this.publicFlag = save.isPublicFlag();
        this.createdAt = save.getCreatedAt();
        this.updatedAt = save.getUpdatedAt();
    }
}
