package com.example.codebase.domain.wanted.dto;

import com.example.codebase.controller.dto.PageInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WantedPageDTO {

    List<WantedResponseDTO> dtos;

    PageInfo pageInfo;

    public static WantedPageDTO of(List<WantedResponseDTO> dtos, PageInfo pageInfo) {
        WantedPageDTO wantedPageDTO = new WantedPageDTO();
        wantedPageDTO.setDtos(dtos);
        wantedPageDTO.setPageInfo(pageInfo);
        return wantedPageDTO;
    }
}

