package com.example.codebase.domain.meme.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemeCountDTO {

    private Long viewTotalCount;
    private Long likeTotalCount;

    public MemeCountDTO(Long viewCount, Long likeCount) {
        this.viewTotalCount = viewCount;
        this.likeTotalCount = likeCount;
    }
}
