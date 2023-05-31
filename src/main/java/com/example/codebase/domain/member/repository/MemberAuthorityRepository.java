package com.example.codebase.domain.member.repository;

import com.example.codebase.domain.member.entity.Authority;
import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.member.entity.MemberAuthority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberAuthorityRepository extends JpaRepository<MemberAuthority, Long> {

    Optional<MemberAuthority> findByMember(Member member);

    void deleteByMemberAndAuthority_AuthorityName(Member member, String authority);
}
