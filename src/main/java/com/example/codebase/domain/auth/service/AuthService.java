package com.example.codebase.domain.auth.service;

import com.example.codebase.domain.member.entity.Authority;
import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.member.entity.MemberAuthority;
import com.example.codebase.domain.member.repository.MemberAuthorityRepository;
import com.example.codebase.domain.member.repository.MemberRepository;
import com.example.codebase.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Set;

@Service
public class AuthService {

    private final RedisUtil redisUtil;
    private final MemberAuthorityRepository memberAuthorityRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public AuthService(RedisUtil redisUtil, MemberAuthorityRepository memberAuthorityRepository, MemberRepository memberRepository) {
        this.redisUtil = redisUtil;
        this.memberAuthorityRepository = memberAuthorityRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public void authenticateMail(String code) {
        String email = redisUtil.getData(code)
                .orElseThrow(() -> new RuntimeException("인증 코드가 유효하지 않습니다."));
        redisUtil.deleteData(code);

        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회원입니다."));
        // ROLE_GUEST 삭제
        memberAuthorityRepository.deleteByMemberAndAuthority_AuthorityName(member, "ROLE_GUEST");

        // ROLE_USER 부여
        MemberAuthority memberAuthority = MemberAuthority.builder()
                .member(member)
                .authority(Authority.of("ROLE_USER"))
                .build();
        member.setAuthorities(Set.of(memberAuthority));
        memberAuthorityRepository.save(memberAuthority);
    }
}
