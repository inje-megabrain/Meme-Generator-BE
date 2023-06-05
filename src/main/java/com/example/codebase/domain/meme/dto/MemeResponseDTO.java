package com.example.codebase.domain.meme.dto;

import com.example.codebase.domain.meme.entity.Meme;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class MemeResponseDTO {
    private Long memeId;

    private String name;

    private String userid;

    private String username;

    private String imageUrl;

    private String type;

    private List<String> tags;

    private Integer viewCount;

    private Integer likeCount;

    private Boolean isLiked = false;

    private Boolean publicFlag;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    public MemeResponseDTO(Meme save) {
        this.memeId = save.getId();
        this.name = save.getName();
        this.userid = save.getUsername();
        this.username = save.getMember().getName();
        this.imageUrl = save.getImageUrl();
        this.type = save.getType().toString();
        this.tags = List.of(save.getTags().split(" "));
        this.viewCount = save.getViewCount();
        this.likeCount = save.getLikeCount();
        this.publicFlag = save.isPublicFlag();
        this.createdAt = save.getCreatedAt();
        this.updatedAt = save.getUpdatedAt();
    }

    public MemeResponseDTO(Meme meme, Boolean isLike) {
        this(meme);
        this.isLiked = isLike;
    }
}
