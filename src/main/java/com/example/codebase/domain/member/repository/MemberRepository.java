package com.example.codebase.domain.member.repository;


import com.example.codebase.domain.member.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findOneWithAuthoritiesByUsername(String username);
    Optional<Member> findByUsername(String username);
    Optional<Member> findByOauthProviderId(String oauthProviderId);

    void deleteByUsername(String username);

}