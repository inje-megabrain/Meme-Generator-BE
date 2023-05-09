package com.example.codebase.domain.wanted.dto;

import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.wanted.entity.Wanted;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class WantedCreateDTO {

    private String name;

    private String description;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String imageUrl;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String username;

    private Integer prize;

    public Wanted toEntity() {
        return Wanted.builder()
                .name(name)
                .description(description)
                .imageUrl(imageUrl)
                .prize(prize)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public Wanted toEntity(Member member) {
        return Wanted.builder()
                .name(name)
                .description(description)
                .imageUrl(imageUrl)
                .prize(prize)
                .member(member)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
