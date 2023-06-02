package com.example.codebase.domain.meme.repository;

import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.meme.entity.MemeLikeMember;
import com.example.codebase.domain.meme.entity.MemeLikeMemberId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MemeLikeMemberRepository extends JpaRepository<MemeLikeMember, MemeLikeMemberId> {

    @Query("SELECT COUNT(mlm) FROM MemeLikeMember mlm WHERE mlm.meme.id = :memeId")
    Integer getLikeCount(Long memeId);

    Integer countByMemeId(Long memeId);

    Page<MemeLikeMember> findByMember(Member member, Pageable pageable);
}
