package com.example.codebase.domain.item.dto;

import com.example.codebase.controller.dto.PageInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ItemPageDTO {

    List<ItemResponseDTO> items;

    PageInfo pageInfo;

    public static ItemPageDTO of(List<ItemResponseDTO> items, PageInfo pageInfo) {
        ItemPageDTO dto = new ItemPageDTO();
        dto.setItems(items);
        dto.setPageInfo(pageInfo);
        return dto;
    }
}
