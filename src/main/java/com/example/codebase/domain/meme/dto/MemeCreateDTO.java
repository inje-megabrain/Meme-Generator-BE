package com.example.codebase.domain.meme.dto;

import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.meme.entity.Meme;
import com.example.codebase.domain.meme.entity.MemeType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Getter
@Setter
public class MemeCreateDTO {

    private String name;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String imageUrl;

    private String type;

    private String tags;

    private Boolean publicFlag;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String username;

    public Meme toEntity(Member member) {
        return Meme.builder()
                .name(name)
                .imageUrl(imageUrl)
                .type(MemeType.from(type))
                .tags(tags)
                .publicFlag(publicFlag)
                .member(member)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
