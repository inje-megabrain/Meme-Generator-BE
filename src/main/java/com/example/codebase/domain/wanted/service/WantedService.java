package com.example.codebase.domain.wanted.service;


import com.example.codebase.controller.dto.PageInfo;
import com.example.codebase.domain.wanted.dto.WantedCreateDTO;
import com.example.codebase.domain.wanted.dto.WantedPageDTO;
import com.example.codebase.domain.wanted.dto.WantedResponseDTO;
import com.example.codebase.domain.wanted.entity.Wanted;
import com.example.codebase.domain.wanted.repository.WantedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WantedService {

    private final WantedRepository wantedRepository;

    @Autowired
    public WantedService(WantedRepository wantedRepository) {
        this.wantedRepository = wantedRepository;
    }

    @Transactional
    public WantedResponseDTO createWanted(WantedCreateDTO dto) {
        Wanted save = wantedRepository.save(dto.toEntity());
        return new WantedResponseDTO(save);
    }

    public WantedPageDTO getWantedList(int page, int size, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "createdAt");
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Wanted> artworksPage = wantedRepository.findAll(pageRequest);
        PageInfo pageInfo = PageInfo.of(page, size, artworksPage.getTotalPages(), artworksPage.getTotalElements());

        List<WantedResponseDTO> all = artworksPage.stream()
                .map(WantedResponseDTO::new)
                .collect(Collectors.toList());


        return WantedPageDTO.of(all, pageInfo);
    }
}
