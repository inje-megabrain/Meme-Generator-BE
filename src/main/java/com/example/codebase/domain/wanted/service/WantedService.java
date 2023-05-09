package com.example.codebase.domain.wanted.service;


import com.example.codebase.controller.dto.PageInfo;
import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.member.repository.MemberRepository;
import com.example.codebase.domain.wanted.dto.WantedCreateDTO;
import com.example.codebase.domain.wanted.dto.WantedPageDTO;
import com.example.codebase.domain.wanted.dto.WantedResponseDTO;
import com.example.codebase.domain.wanted.dto.WantedUpdateDTO;
import com.example.codebase.domain.wanted.entity.Wanted;
import com.example.codebase.domain.wanted.repository.WantedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WantedService {

    private final WantedRepository wantedRepository;

    private final MemberRepository memberRepository;

    @Autowired
    public WantedService(WantedRepository wantedRepository, MemberRepository memberRepository) {
        this.wantedRepository = wantedRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public WantedResponseDTO createWanted(WantedCreateDTO dto) {
        Member member = memberRepository.findByUsername(dto.getUsername()).orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));
        Wanted save = wantedRepository.save(dto.toEntity(member));
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

    public WantedResponseDTO getWanted(Long wantedId) {
        Wanted wanted = wantedRepository.findById(wantedId).orElseThrow(() -> new IllegalArgumentException("해당 수배가 없습니다."));
        return new WantedResponseDTO(wanted);
    }

    public WantedResponseDTO updateWanted(Long wantedId, WantedUpdateDTO dto, String username) {
        Wanted wanted = wantedRepository.findByIdAndMember_Username(wantedId, username).orElseThrow(() -> new IllegalArgumentException("해당 수배가 없거나 작성자가 아닙니다."));
        wanted.update(dto);
        return new WantedResponseDTO(wanted);
    }

    public void deleteWanted(Long wantedId, String username) {
        Wanted wanted = wantedRepository.findByIdAndMember_Username(wantedId, username).orElseThrow(() -> new IllegalArgumentException("해당 수배가 없거나 작성자가 아닙니다."));

        String imageUrl = "." + wanted.getImageUrl();
        boolean delete = new File(imageUrl).delete();

        if (!delete) throw new IllegalArgumentException("이미지 삭제 실패");

        wantedRepository.delete(wanted);
    }
}
