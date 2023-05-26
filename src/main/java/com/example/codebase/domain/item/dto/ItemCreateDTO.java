package com.example.codebase.domain.item.dto;

import com.example.codebase.domain.item.entity.Item;
import com.example.codebase.domain.item.entity.ItemCategory;
import com.example.codebase.domain.member.entity.Member;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ItemCreateDTO {

    private String name;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String imageUrl;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String username;

    private String category;

    public Item toEntity(Member member) {
        return Item.builder()
                .name(name)
                .imageUrl(imageUrl)
                .member(member)
                .category(ItemCategory.from(category))
                .createdAt(LocalDateTime.now())
                .build();
    }
}

