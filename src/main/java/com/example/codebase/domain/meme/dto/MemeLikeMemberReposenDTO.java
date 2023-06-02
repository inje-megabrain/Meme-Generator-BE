package com.example.codebase.domain.meme.dto;

import com.example.codebase.domain.member.dto.MemberResponseDTO;
import com.example.codebase.domain.meme.entity.Meme;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemeLikeMemberReposenDTO {

    private MemeResponseDTO meme;

    private Boolean isLiked;

    private String likeStatus;

    public MemeLikeMemberReposenDTO(Meme meme, Boolean isLiked, String likeStatus) {
        this.meme = new MemeResponseDTO(meme);
        this.isLiked = isLiked;
        this.likeStatus = likeStatus;
    }
}
