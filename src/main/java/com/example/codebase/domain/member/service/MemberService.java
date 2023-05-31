package com.example.codebase.domain.member.service;

import com.example.codebase.domain.auth.OAuthAttributes;
import com.example.codebase.domain.member.dto.CreateMemberDTO;
import com.example.codebase.domain.member.dto.MemberResponseDTO;
import com.example.codebase.domain.member.entity.Authority;
import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.member.entity.MemberAuthority;
import com.example.codebase.domain.member.repository.MemberAuthorityRepository;
import com.example.codebase.domain.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final MemberAuthorityRepository memberAuthorityRepository;

    @Autowired
    public MemberService(PasswordEncoder passwordEncoder, MemberRepository memberRepository, MemberAuthorityRepository memberAuthorityRepository) {
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
        this.memberAuthorityRepository = memberAuthorityRepository;
    }

    @Transactional
    public MemberResponseDTO createMember(CreateMemberDTO member) {
        if (memberRepository.findOneWithAuthoritiesByUsername(member.getUsername()).isPresent()) {
            throw new RuntimeException("이미 존재하는 회원입니다.");
        }

        Authority authority = Authority.builder()
                .authorityName("ROLE_USER")
                .build();


        Member newMember = Member.builder()
                .username(member.getUsername())
                .password(passwordEncoder.encode(member.getPassword()))
                .name(member.getName())
                .email(member.getEmail())
                .createdTime(LocalDateTime.now())
                .activated(true)
                .build();

        MemberAuthority memberAuthority = MemberAuthority.builder()
                .authority(authority)
                .member(newMember)
                .build();
        newMember.setAuthorities(Collections.singleton(memberAuthority));

        Member save = memberRepository.save(newMember);
        memberAuthorityRepository.save(memberAuthority);

        return MemberResponseDTO.from(save);
    }

    @Transactional
    public Member createOAuthMember(OAuthAttributes oAuthAttributes) {
        // New Save
        Authority authority = new Authority();
        authority.setAuthorityName("ROLE_USER");

        Member newMember = oAuthAttributes.toEntity(passwordEncoder);
        MemberAuthority memberAuthority = MemberAuthority.builder()
                .authority(authority)
                .member(newMember)
                .build();
        newMember.setAuthorities(Collections.singleton(memberAuthority));

        Member save = memberRepository.save(newMember);
        memberAuthorityRepository.save(memberAuthority);

        return save;
    }

    public List<MemberResponseDTO> getAllMember() {
        return memberRepository.findAll().stream()
                .map(MemberResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteMember(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));

        memberRepository.delete(member);
    }

    public MemberResponseDTO getMember(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));

        return MemberResponseDTO.from(member);
    }

    public void updateName(String loginUesrname, String newName) {
        if (loginUesrname.equals(newName)) {
            throw new RuntimeException("이름이 같습니다.");
        }

        Member member = memberRepository.findByUsername(loginUesrname)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));
        member.updateName(newName);
    }
}
