package com.example.codebase.domain.meme.service;


import com.example.codebase.controller.dto.PageInfo;
import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.member.repository.MemberRepository;
import com.example.codebase.domain.meme.dto.*;
import com.example.codebase.domain.meme.entity.*;
import com.example.codebase.domain.meme.repository.MemeLikeMemberRepository;
import com.example.codebase.domain.meme.repository.MemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.constraints.PositiveOrZero;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MemeService {

    private final MemeRepository memeRepository;

    private final MemberRepository memberRepository;

    private final MemeLikeMemberRepository memeLikeMemberRepository;

    @Autowired
    public MemeService(MemeRepository memeRepository, MemberRepository memberRepository, MemeLikeMemberRepository memeLikeMemberRepository) {
        this.memeRepository = memeRepository;
        this.memberRepository = memberRepository;
        this.memeLikeMemberRepository = memeLikeMemberRepository;
    }

    @Transactional
    public MemeResponseDTO createMeme(MemeCreateDTO dto) {
        Member member = memberRepository.findByUsername(dto.getUsername()).orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));
        Meme save = memeRepository.save(dto.toEntity(member));
        member.addMeme(save);

        return new MemeResponseDTO(save);
    }

    public MemePageDTO getMemeList(MemeType type, int page, int size, String sortDirection, Optional<String> loginUsername) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "createdAt");
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        if (loginUsername.isPresent()) {
            Member member = memberRepository.findByUsername(loginUsername.get())
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));
            Page<MemeWithIsLike> memesWithIsLiked = memeRepository.findAllByTypeAndPublicFlagIsTrueAndMemberLiked(type, member, pageRequest);
            PageInfo pageInfo = PageInfo.of(page, size, memesWithIsLiked.getTotalPages(), memesWithIsLiked.getTotalElements());

            List<MemeResponseDTO> all = memesWithIsLiked.stream()
                    .map(memeWithIsLike -> new MemeResponseDTO(memeWithIsLike.getMeme(), memeWithIsLike.getIsLike()))
                    .collect(Collectors.toList());

            return MemePageDTO.of(all, pageInfo);
        }

        Page<Meme> memePage = memeRepository.findAllByTypeAndPublicFlagIsTrue(type, pageRequest);
        PageInfo pageInfo = PageInfo.of(page, size, memePage.getTotalPages(), memePage.getTotalElements());

        List<MemeResponseDTO> all = memePage.stream()
                .map(MemeResponseDTO::new)
                .collect(Collectors.toList());

        return MemePageDTO.of(all, pageInfo);
    }

    @Transactional
    public MemeResponseDTO getMeme(Long memeId, Optional<String> loginUsername) {
        boolean existsById = false;
        if (loginUsername.isPresent()) {
            Member member = memberRepository.findByUsername(loginUsername.get())
                    .orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));
            existsById = memeLikeMemberRepository.existsById(new MemeLikeMemberId(memeId, member.getId()));
        }
        Meme meme = memeRepository.findById(memeId).orElseThrow(() -> new IllegalArgumentException("해당 짤이 없습니다."));
        meme.incressViewCount();

        return new MemeResponseDTO(meme, existsById);
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
        Meme meme = memeRepository.findById(memeId).orElseThrow(() -> new IllegalArgumentException("해당 짤이 없습니다."));

        if (!username.equals("admin") && !meme.getMember().getUsername().equals(username)) {
            throw new IllegalArgumentException("작성자가 아닙니다.");
        }

        String imageUrl = "." + meme.getImageUrl();
        File target = new File(imageUrl);
        if (target.exists()) {
            boolean delete = target.delete();
            if (!delete) throw new IllegalArgumentException("이미지 삭제 실패");
        }

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

    @Transactional
    public MemeResponseDTO updateMemePublicFlag(Long memeId, boolean flag, String loginUsername) {
        Meme meme = memeRepository.findById(memeId).orElseThrow(() -> new IllegalArgumentException("해당 짤이 없습니다."));
        if (!meme.getMember().getUsername().equals(loginUsername)) {
            throw new IllegalArgumentException("작성자가 아닙니다.");
        }
        meme.updatePublicFlag(flag);
        return new MemeResponseDTO(meme);
    }

    @Transactional
    public MemeLikeMemberReposenDTO likeMeme(Long memeId, String loginUsername) {
        Meme meme = memeRepository.findById(memeId).orElseThrow(() -> new IllegalArgumentException("해당 짤이 없습니다."));

        Member member = memberRepository.findByUsername(loginUsername).orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));

        Optional<MemeLikeMember> memeLikeMember = memeLikeMemberRepository.findById(new MemeLikeMemberId(memeId, member.getId()));
        String status = "좋아요";
        if (memeLikeMember.isPresent()) {
            // 좋아요 취소
            memeLikeMemberRepository.delete(memeLikeMember.get());
            status = "좋아요 취소";
        } else {
            MemeLikeMember save = MemeLikeMember.of(meme, member);
            memeLikeMemberRepository.save(save);
        }

        Integer LikeCount = memeLikeMemberRepository.countByMemeId(memeId);
        meme.setLikeCount(LikeCount);

        return new MemeLikeMemberReposenDTO(meme, !memeLikeMember.isPresent(), status);
    }

    public MemePageDTO getLikeMemes(String loginUsername, int page, int size, String sortDirection) {
        Member member = memberRepository.findByUsername(loginUsername).orElseThrow(() -> new IllegalArgumentException("해당 유저가 없습니다."));

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), "likedTime");
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Page<MemeLikeMember> memes = memeLikeMemberRepository.findByMember(member, pageRequest);
        PageInfo pageInfo = PageInfo.of(page, size, memes.getTotalPages(), memes.getTotalElements());

        List<MemeResponseDTO> all = memes.stream()
                .map(memeLikeMember -> new MemeResponseDTO(memeLikeMember.getMeme(), true))
                .collect(Collectors.toList());

        return MemePageDTO.of(all, pageInfo);
    }
}
