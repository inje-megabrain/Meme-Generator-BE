package com.example.codebase.domain.meme.repository;

import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.meme.entity.Meme;
import com.example.codebase.domain.meme.entity.MemeType;
import com.example.codebase.domain.meme.entity.MemeWithIsLike;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MemeRepository extends JpaRepository<Meme, Long> {

    Optional<Meme> findByIdAndMember_Username(Long id, String username);

    Page<Meme> findAllByMember_Username(String username, Pageable pageable);

    Page<Meme> findAllByTypeAndPublicFlagIsTrue(MemeType type, Pageable pageable);

    @Query("SELECT m AS meme, CASE WHEN m = mlm.meme THEN true ELSE false END AS isLike " +
            "FROM Meme m LEFT JOIN MemeLikeMember mlm ON m = mlm.meme AND mlm.member = :member" +
            " WHERE m.type = :type AND m.publicFlag = true")
    Page<MemeWithIsLike> findAllByTypeAndPublicFlagIsTrueAndMemberLiked(MemeType type, Member member, Pageable pageable);

    @Query("SELECT m AS meme, CASE WHEN m = mlm.meme THEN true ELSE false END AS isLike" +
            " FROM Meme m LEFT JOIN MemeLikeMember mlm ON m = mlm.meme" +
            " WHERE m.id = :memeId AND mlm.member = :member")
    Optional<MemeWithIsLike> findMemeWithIsLikeById(Long memeId, Member member);

    @Query("SELECT m FROM Meme m WHERE m.publicFlag = true AND (m.name LIKE %:keyword% OR m.member.name LIKE %:keyword%)")
    Page<Meme> findAllByKeyword(String keyword, Pageable pageable);
}
