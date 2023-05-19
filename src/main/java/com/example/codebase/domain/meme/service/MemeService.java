package com.example.codebase.domain.meme.service;


import com.example.codebase.controller.dto.PageInfo;
import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.member.repository.MemberRepository;
import com.example.codebase.domain.meme.dto.MemeCreateDTO;
import com.example.codebase.domain.meme.dto.MemePageDTO;
import com.example.codebase.domain.meme.dto.MemeResponseDTO;
import com.example.codebase.domain.meme.dto.MemeUpdateDTO;
import com.example.codebase.domain.meme.entity.Meme;
import com.example.codebase.domain.meme.repository.MemeRepository;
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
public class MemeService {

    private final MemeRepository memeRepository;

    private final MemberRepository memberRepository;

    @Autowired
    public MemeService(MemeRepository memeRepository, MemberRepository memberRepository) {
        this.memeRepository = memeRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public MemeResponseDTO createMeme(MemeCreateDTO dto) {
        Member member = memberRepository.findByUsername(dto.getUsername()).orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));
        Meme save = memeRepository.save(dto.toEntity(member));
        member.addMeme(save);

        return new MemeResponseDTO(save);
    }

    public MemePageDTO getMemeList(int page, int size, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "createdAt");
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Meme> memePage = memeRepository.findAll(pageRequest);
        PageInfo pageInfo = PageInfo.of(page, size, memePage.getTotalPages(), memePage.getTotalElements());

        List<MemeResponseDTO> all = memePage.stream()
                .map(MemeResponseDTO::new)
                .collect(Collectors.toList());

        return MemePageDTO.of(all, pageInfo);
    }

    public MemeResponseDTO getMeme(Long memeId) {
        Meme meme = memeRepository.findById(memeId).orElseThrow(() -> new IllegalArgumentException("해당 짤이 없습니다."));
        return new MemeResponseDTO(meme);
    }

    @Transactional
    public MemeResponseDTO updateMeme(Long memeId, MemeUpdateDTO dto, String username) {
        Meme meme = memeRepository.findByIdAndMember_Username(memeId, username).orElseThrow(() -> new IllegalArgumentException("해당 짤이 없거나 작성자가 아닙니다."));
        meme.update(dto);
        return new MemeResponseDTO(meme);
    }

    @Transactional
    public MemeResponseDTO updateMeme(Long memeId, MemeUpdateDTO dto) {
        Meme meme = memeRepository.findById(memeId).orElseThrow(() -> new IllegalArgumentException("해당 짤이 없습니다."));
        meme.update(dto);
        return new MemeResponseDTO(meme);
    }


    @Transactional
    public void deleteMeme(Long memeId, String username) {
        Meme meme = memeRepository.findByIdAndMember_Username(memeId, username).orElseThrow(() -> new IllegalArgumentException("해당 짤이 없거나 작성자가 아닙니다."));

        String imageUrl = "." + meme.getImageUrl();
        File target = new File(imageUrl);
        if (!target.exists()) {
            throw new IllegalArgumentException("이미지가 존재하지 않습니다.");
        }

        boolean delete = target.delete();
        if (!delete) throw new IllegalArgumentException("이미지 삭제 실패");

        memeRepository.delete(meme);
    }

    @Transactional
    public void deleteMeme(Long memeId) {
        Meme meme = memeRepository.findById(memeId).orElseThrow(() -> new IllegalArgumentException("해당 밈이 없습니다."));

        String imageUrl = "." + meme.getImageUrl();
        boolean delete = new File(imageUrl).delete();

        if (!delete) throw new IllegalArgumentException("이미지 삭제 실패");

        memeRepository.delete(meme);
    }

    public MemePageDTO getMemberMeme(String username, int page, int size, String sortDirection) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "createdAt");
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<Meme> memePage = memeRepository.findAllByMember_Username(username, pageRequest);
        PageInfo pageInfo = PageInfo.of(page, size, memePage.getTotalPages(), memePage.getTotalElements());

        List<MemeResponseDTO> all = memePage.stream()
                .map(MemeResponseDTO::new)
                .collect(Collectors.toList());

        return MemePageDTO.of(all, pageInfo);
    }
}
