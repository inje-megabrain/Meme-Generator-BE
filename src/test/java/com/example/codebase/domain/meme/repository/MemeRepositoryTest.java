package com.example.codebase.domain.meme.repository;

import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.member.repository.MemberRepository;
import com.example.codebase.domain.meme.entity.Meme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class MemeRepositoryTest {

    @Autowired
    private MemeRepository memeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("해당 멤버 삭제 시 밈도 같이 삭제되는지 확인")
    @Test
    void 멤버_삭제_시 () throws Exception {
        Member member = Member.builder()
                .username("test")
                .password("test")
                .name("이름")
                .createdTime(LocalDateTime.now())
                .email("test@test.com")
                .build();
        Member saved = memberRepository.save(member);

        List<Meme> memes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Meme meme = Meme.builder()
                    .member(member)
                    .name("밈 제목")
                    .imageUrl("이미지 URL")
                    .createdAt(LocalDateTime.now())
                    .build();
            memes.add(meme);
            member.addMeme(meme);
        }
        memeRepository.saveAll(memes);

        // when
        memberRepository.delete(saved);

        // then
        assertEquals(0, memeRepository.findAll().size());
    }

    @DisplayName("해당 멤버 삭제 시 다른 멤버의 밈은 삭제되지 않는지 확인")
    @Test
    void 멤버_삭제_다른밈존재 () throws Exception {
        Member member = Member.builder()
                .username("test")
                .password("test")
                .name("이름")
                .createdTime(LocalDateTime.now())
                .email("test@test.com")
                .build();
        Member saved = memberRepository.save(member);

        Member member2 = Member.builder()
                .username("test2")
                .password("test")
                .name("이름2")
                .createdTime(LocalDateTime.now())
                .email("test2@test.com")
                .build();
        memberRepository.save(member2);
        Meme meme2 = Meme.builder()
                .member(member2)
                .name("밈 제목")
                .imageUrl("이미지 URL")
                .createdAt(LocalDateTime.now())
                .build();
        memeRepository.save(meme2);

        List<Meme> memes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Meme meme = Meme.builder()
                    .member(member)
                    .name("밈 제목")
                    .imageUrl("이미지 URL")
                    .createdAt(LocalDateTime.now())
                    .build();
            memes.add(meme);
            member.addMeme(meme);
        }
        memeRepository.saveAll(memes);

        // when
        memberRepository.delete(saved);

        // then
        assertEquals(1, memeRepository.findAll().size());
    }

    @DisplayName("밈 삭제가 작동한다")
    @Test
    void 밈_삭제 () throws Exception {
        Member member = Member.builder()
                .username("test")
                .password("test")
                .name("이름")
                .createdTime(LocalDateTime.now())
                .email("test@test.com")
                .build();
        memberRepository.save(member);

        List<Meme> memes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Meme meme = Meme.builder()
                    .member(member)
                    .name("밈 제목")
                    .imageUrl("이미지 URL")
                    .createdAt(LocalDateTime.now())
                    .build();
            memes.add(meme);
            member.addMeme(meme);
        }
        memeRepository.saveAll(memes);

        Meme save = memeRepository.save(Meme.builder()
                .member(member)
                .name("밈 제목")
                .imageUrl("이미지 URL")
                .createdAt(LocalDateTime.now())
                .build());

        // when
        assertEquals(6, memeRepository.findAll().size());

        memeRepository.delete(save);

        // then
        assertEquals(5, memeRepository.findAll().size());
    }

}