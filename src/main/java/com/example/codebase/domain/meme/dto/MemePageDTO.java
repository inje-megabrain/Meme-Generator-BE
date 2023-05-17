package com.example.codebase.domain.meme.dto;

import com.example.codebase.controller.dto.PageInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MemePageDTO {

    List<MemeResponseDTO> dtos;

    PageInfo pageInfo;

    public static MemePageDTO of(List<MemeResponseDTO> dtos, PageInfo pageInfo) {
        MemePageDTO memePageDTO = new MemePageDTO();
        memePageDTO.setDtos(dtos);
        memePageDTO.setPageInfo(pageInfo);
        return memePageDTO;
    }
}

