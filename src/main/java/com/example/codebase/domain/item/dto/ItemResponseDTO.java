package com.example.codebase.domain.item.dto;


import com.example.codebase.domain.item.entity.Item;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemResponseDTO {

    private Long itemId;

    private String name;

    private String imageUrl;

    private String category;

    private String username;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private String createdAt;


    public static ItemResponseDTO from(Item item) {
        ItemResponseDTO dto = new ItemResponseDTO();
        dto.setItemId(item.getId());
        dto.setName(item.getName());
        dto.setImageUrl(item.getImageUrl());
        dto.setCategory(item.getCategory().name());
        dto.setUsername(item.getMember().getUsername());
        dto.setCreatedAt(item.getCreatedAt().toString());
        return dto;
    }
}
