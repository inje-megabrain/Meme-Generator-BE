package com.example.codebase.domain.meme.entity;

import com.example.codebase.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meme_like_member")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(MemeLikeMemberId.class)
public class MemeLikeMember {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meme_id")
    private Meme meme;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "liked_time")
    private LocalDateTime likedTime;

    public static MemeLikeMember of (Meme meme, Member member) {
        return MemeLikeMember.builder()
                .meme(meme)
                .member(member)
                .likedTime(LocalDateTime.now())
                .build();
    }
}
