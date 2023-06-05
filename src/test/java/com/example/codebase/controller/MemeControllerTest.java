package com.example.codebase.controller;

import com.example.codebase.annotation.WithMockCustomUser;
import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.member.repository.MemberRepository;
import com.example.codebase.domain.meme.dto.MemeCreateDTO;
import com.example.codebase.domain.meme.dto.MemeUpdateDTO;
import com.example.codebase.domain.meme.entity.Meme;
import com.example.codebase.domain.meme.entity.MemeType;
import com.example.codebase.domain.meme.repository.MemeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemeControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MemberRepository memberRepository;

    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MemeRepository memeRepository;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @WithMockCustomUser(username = "testid")
    @DisplayName("밈 생성 API가 작동한다")
    @Test
    void 밈_생성() throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);

        MemeCreateDTO createDTO = new MemeCreateDTO();
        createDTO.setUsername(member.getUsername());
        createDTO.setName("test");
        createDTO.setType(MemeType.MEME.toString());
        createDTO.setTags("#태그1 #태그2 #태그3");
        createDTO.setPublicFlag(true);

        MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());

        // when
        mockMvc.perform(
                        multipart("/api/meme")
                                .file(file)
                                .file(new MockMultipartFile("dto", "", "application/json", objectMapper.writeValueAsString(createDTO).getBytes())
                                ).contentType("multipart/form-data")
                                .accept("application/json")
                                .characterEncoding("UTF-8")
                )
                .andDo(print()) // then
                .andExpect(status().isCreated());
    }

    @WithMockCustomUser(username = "testid")
    @DisplayName("밈 템플릿 생성 API가 작동한다")
    @Test
    void 밈_템플릿_생성() throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);

        MemeCreateDTO createDTO = new MemeCreateDTO();
        createDTO.setUsername(member.getUsername());
        createDTO.setName("test");
        createDTO.setType(MemeType.TEMPLATE.toString());
        createDTO.setPublicFlag(true);

        MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());

        // when
        mockMvc.perform(
                        multipart("/api/meme")
                                .file(file)
                                .file(new MockMultipartFile("dto", "", "application/json", objectMapper.writeValueAsString(createDTO).getBytes())
                                ).contentType("multipart/form-data")
                                .accept("application/json")
                                .characterEncoding("UTF-8")
                )
                .andDo(print()) // then
                .andExpect(status().isCreated());
    }

    @DisplayName("밈 전체 조회 API가 작동한다")
    @Test
    void 밈_전체_조회() throws Exception {
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);
        // given
        List<Meme> memes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Meme meme = Meme.builder()
                    .name("test" + i)
                    .member(member)
                    .imageUrl("test" + i)
                    .type(MemeType.MEME)
                    .tags("#TAG1 #TAG2")
                    .publicFlag(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            memes.add(meme);
        }
        memeRepository.saveAll(memes);

        // when
        mockMvc.perform(
                        get("/api/meme")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("밈 전체 공개만 조회 API가 작동한다")
    @Test
    void 밈_공개_전체_조회() throws Exception {
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);
        // given
        List<Meme> memes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Meme meme = Meme.builder()
                    .name("test" + i)
                    .member(member)
                    .imageUrl("test" + i)
                    .type(MemeType.MEME)
                    .publicFlag(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            memes.add(meme);
        }
        for (int i = 5; i <= 10; i++) {
            Meme meme = Meme.builder()
                    .name("test" + i)
                    .member(member)
                    .imageUrl("test" + i)
                    .type(MemeType.MEME)
                    .tags("#TAG1 #TAG2")
                    .publicFlag(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            memes.add(meme);
        }
        memeRepository.saveAll(memes);

        // when
        mockMvc.perform(
                        get("/api/meme")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("밈 템플릿 전체 조회 API가 작동한다")
    @Test
    void 밈_템플릿_전체_조회 () throws Exception {
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);
        // given
        List<Meme> memes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Meme meme = Meme.builder()
                    .name("test" + i)
                    .member(member)
                    .imageUrl("test" + i)
                    .type(MemeType.TEMPLATE)
                    .publicFlag(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            memes.add(meme);
        }
        for (int i = 5; i <= 10; i++) {
            Meme meme = Meme.builder()
                    .name("test" + i)
                    .member(member)
                    .imageUrl("test" + i)
                    .type(MemeType.MEME)
                    .publicFlag(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            memes.add(meme);
        }

        memeRepository.saveAll(memes);

        // when
        mockMvc.perform(
                        get("/api/meme?type=TEMPLATE")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }


    @DisplayName("밈 단일 조회 API가 작동한다")
    @Test
    void 밈_단일_조회() throws Exception {
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);
        // given
        List<Meme> memes = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Meme meme = Meme.builder()
                    .name("test" + i)
                    .member(member)
                    .imageUrl("test" + i)
                    .type(MemeType.TEMPLATE)
                    .tags("#TAG1 #TAG2")
                    .publicFlag(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            memes.add(meme);
        }
        memeRepository.saveAll(memes);

        // when
        mockMvc.perform(
                        get("/api/meme/{memeId}", memes.get(0).getId())
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("비공개 밈 단일 조회 API가 작동한다")
    @Test
    void 비공개밈_단일_조회() throws Exception {
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);
        // given
        List<Meme> memes = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Meme meme = Meme.builder()
                    .name("test" + i)
                    .member(member)
                    .imageUrl("test" + i)
                    .type(MemeType.TEMPLATE)
                    .publicFlag(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            memes.add(meme);
        }
        memeRepository.saveAll(memes);

        // when
        mockMvc.perform(
                        get("/api/meme/{memeId}", memes.get(0).getId())
                )
                .andDo(print())
                .andExpect(status().isOk());
    }


    @WithMockCustomUser(username = "testid")
    @DisplayName("밈 수정 API가 작동한다")
    @Test
    void 밈_수정() throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);

        Meme meme = Meme.builder()
                .name("test")
                .member(member)
                .imageUrl("imageurl")
                .type(MemeType.TEMPLATE)
                .createdAt(LocalDateTime.now())
                .build();
        memeRepository.save(meme);


        MemeUpdateDTO updateDTO = new MemeUpdateDTO();
        updateDTO.setName("제목 수정");
        // when
        mockMvc.perform(
                        put("/api/meme/{memeId}", meme.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithMockCustomUser(username = "testid2")
    @DisplayName("작성자가 아닌데 밈 수정 시")
    @Test
    void 다른사람_밈_수정() throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);

        Meme meme = Meme.builder()
                .name("test")
                .member(member)
                .imageUrl("imageurl")
                .type(MemeType.TEMPLATE)
                .createdAt(LocalDateTime.now())
                .build();
        memeRepository.save(meme);


        MemeUpdateDTO updateDTO = new MemeUpdateDTO();
        updateDTO.setName("제목 수정");
        // when
        mockMvc.perform(
                        put("/api/meme/{memeId}", meme.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    @WithMockCustomUser(username = "admin", role = "ADMIN")
    @DisplayName("관리자가 밈 수정 시")
    @Test
    void 관리자_밈_수정() throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);

        Meme meme = Meme.builder()
                .name("test")
                .member(member)
                .imageUrl("imageurl")
                .type(MemeType.TEMPLATE)
                .createdAt(LocalDateTime.now())
                .build();
        memeRepository.save(meme);

        MemeUpdateDTO updateDTO = new MemeUpdateDTO();
        updateDTO.setName("관리자가 수정함");
        // when
        mockMvc.perform(
                        put("/api/meme/{memeId}", meme.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDTO))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("해당 회원의 밈들 조회")
    @Test
    void 해당_회원_밈_조회() throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);
        // given
        List<Meme> memes = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Meme meme = Meme.builder()
                    .name("test" + i)
                    .member(member)
                    .imageUrl("test" + i)
                    .type(MemeType.TEMPLATE)
                    .publicFlag(true)
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .build();
            memes.add(meme);
        }
        memeRepository.saveAll(memes);

        mockMvc.perform(
                        get("/api/meme/member/{username}?page=0&size=10", member.getUsername())
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithMockCustomUser(username = "testid")
    @DisplayName("밈 삭제 API가 작동한다")
    @Test
    void 밈_삭제() throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);


        // 이미지 저장
        String savePath = "./images/";
        String storeFileName = UUID.randomUUID() + "." + "jpg";
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String key = savePath + now + "/" + storeFileName; // /images/시간/파일명
        File temp = new File(savePath + now + "/");

        if (!temp.exists()) {
            temp.mkdirs();
        }

        FileOutputStream fileOutputStream = new FileOutputStream(key);
        fileOutputStream.write("image".getBytes());
        fileOutputStream.close();

        // given
        List<Meme> memes = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Meme meme = Meme.builder()
                    .name("test" + i)
                    .member(member)
                    .imageUrl("/images/" + now + "/" + storeFileName)
                    .type(MemeType.TEMPLATE)
                    .publicFlag(true)
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .build();
            memes.add(meme);
        }
        memeRepository.saveAll(memes);

        mockMvc.perform(
                        delete("/api/meme/{memeId}", memes.get(0).getId())
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithMockCustomUser(username = "admin", role = "ADMIN")
    @Test
    @DisplayName("어드민이 밈 삭제")
    void 어드민이_밈_삭제 () throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);


        // 이미지 저장
        String savePath = "./images/";
        String storeFileName = UUID.randomUUID() + "." + "jpg";
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String key = savePath + now + "/" + storeFileName; // /images/시간/파일명
        File temp = new File(savePath + now + "/");

        if (!temp.exists()) {
            temp.mkdirs();
        }

        FileOutputStream fileOutputStream = new FileOutputStream(key);
        fileOutputStream.write("image".getBytes());
        fileOutputStream.close();

        // given
        List<Meme> memes = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Meme meme = Meme.builder()
                    .name("test" + i)
                    .member(member)
                    .imageUrl("/images/" + now + "/" + storeFileName)
                    .type(MemeType.TEMPLATE)
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .build();
            memes.add(meme);
        }
        memeRepository.saveAll(memes);

        mockMvc.perform(
                        delete("/api/meme/{memeId}", memes.get(0).getId())
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithMockCustomUser(username = "another", role = "USER")
    @Test
    @DisplayName("작성자가 아닌데 밈 삭제 시")
    void 다른사람이_밈_삭제 () throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);


        // 이미지 저장
        String savePath = "./images/";
        String storeFileName = UUID.randomUUID() + "." + "jpg";
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String key = savePath + now + "/" + storeFileName; // /images/시간/파일명
        File temp = new File(savePath + now + "/");

        if (!temp.exists()) {
            temp.mkdirs();
        }

        FileOutputStream fileOutputStream = new FileOutputStream(key);
        fileOutputStream.write("image".getBytes());
        fileOutputStream.close();

        // given
        List<Meme> memes = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Meme meme = Meme.builder()
                    .name("test" + i)
                    .member(member)
                    .imageUrl("/images/" + now + "/" + storeFileName)
                    .type(MemeType.TEMPLATE)
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .build();
            memes.add(meme);
        }
        memeRepository.saveAll(memes);

        mockMvc.perform(
                        delete("/api/meme/{memeId}", memes.get(0).getId())
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @WithMockCustomUser(username = "testid")
    @DisplayName("밈 공개여부 수정 시")
    @Test
    void 밈_공개여부_수정() throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);

        Meme meme = Meme.builder()
                .name("test")
                .member(member)
                .imageUrl("imageurl")
                .type(MemeType.TEMPLATE)
                .tags("#TAG1 #TAG2")
                .publicFlag(false)
                .createdAt(LocalDateTime.now())
                .build();
        memeRepository.save(meme);

        // when
        mockMvc.perform(
                        put("/api/meme/{memeId}/public", meme.getId())
                                .param("flag", "true")
                )
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(
                        put("/api/meme/{memeId}/public", meme.getId())
                                .param("flag", "false")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithMockCustomUser(username = "testid")
    @DisplayName("밈 좋아요, 좋아요 취소 시")
    @Test
    void 밈_좋아요() throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);

        Meme meme = Meme.builder()
                .name("test")
                .member(member)
                .imageUrl("imageurl")
                .type(MemeType.MEME)
                .publicFlag(true)
                .createdAt(LocalDateTime.now())
                .build();
        memeRepository.save(meme);

        // when
        mockMvc.perform(
                        post("/api/meme/{memeId}/like", meme.getId())
                )
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(
                        post("/api/meme/{memeId}/like", meme.getId())
                )
                .andDo(print())
                .andExpect(status().isNoContent());

    }

    @WithMockCustomUser(username = "testid")
    @DisplayName("로그인 사용자이면 좋아요 여부와 함께 밈 전체 조회")
    @Test
    void 로그인사용자_밈_여부와_함께_전체조회 () throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);

        // given
        List<Meme> memes = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Meme meme = Meme.builder()
                    .name("test" + i)
                    .member(member)
                    .imageUrl("test_url")
                    .type(MemeType.MEME)
                    .publicFlag(true)
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .build();
            memes.add(meme);
        }
        memeRepository.saveAll(memes);


        // when
        mockMvc.perform(
                        post("/api/meme/{memeId}/like", memes.get(0).getId())
                )
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/meme")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithMockCustomUser(username = "testid")
    @DisplayName("로그인 사용자이면 좋아요 여부와 함께 밈 단일 조회")
    @Test
    void 로그인사용자_밈_여부와_함께_단일조회 () throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);

        // given
        List<Meme> memes = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Meme meme = Meme.builder()
                    .name("test" + i)
                    .member(member)
                    .imageUrl("test_url")
                    .type(MemeType.MEME)
                    .publicFlag(true)
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .build();
            memes.add(meme);
        }
        memeRepository.saveAll(memes);


        // when
        mockMvc.perform(
                        post("/api/meme/{memeId}/like", memes.get(0).getId())
                )
                .andDo(print())
                .andExpect(status().isOk());

        // 좋아요한 밈
        mockMvc.perform(
                        get("/api/meme/{memeId}", memes.get(0).getId())
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithMockCustomUser(username = "testid")
    @DisplayName("로그인 사용자이면 좋아요 여부와 함께 밈 단일 조회 (좋아요 안한 밈)")
    @Test
    void 로그인사용자_밈_여부와_함께_단일조회2 () throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);

        Meme meme = Meme.builder()
                .name("test")
                .member(member)
                .imageUrl("imageurl")
                .type(MemeType.MEME)
                .tags("#TAG1 #TAG2")
                .publicFlag(true)
                .createdAt(LocalDateTime.now())
                .build();
        memeRepository.save(meme);

        // 좋아요 안한 밈
        mockMvc.perform(
                        get("/api/meme/{memeId}", meme.getId())
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithMockCustomUser(username = "testid")
    @DisplayName("로그인 사용자가 좋아요 표시한 밈 전체 조회")
    @Test
    void 로그인사용자_좋아요한밈_전체조회 () throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);

        // given
        List<Meme> memes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Meme meme = Meme.builder()
                    .name("test" + i)
                    .member(member)
                    .imageUrl("test_url")
                    .type(MemeType.MEME)
                    .tags("#TAG1 #TAG2")
                    .publicFlag(true)
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .build();
            memes.add(meme);
        }
        memeRepository.saveAll(memes);


        // when
        mockMvc.perform(
                        post("/api/meme/{memeId}/like", memes.get(0).getId())
                )
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(
                        post("/api/meme/{memeId}/like", memes.get(1).getId())
                )
                .andDo(print())
                .andExpect(status().isOk());


        // 좋아요한 밈
        mockMvc.perform(
                        get("/api/meme/likes")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }


    @DisplayName("좋아요순 밈 전체 조회")
    @Test
    void 좋아요순_전체조회 () throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);

        // given
        List<Meme> memes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Meme meme = Meme.builder()
                    .name("test" + i)
                    .member(member)
                    .imageUrl("test_url")
                    .type(MemeType.MEME)
                    .publicFlag(true)
                    .likeCount(i + 5)
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .build();
            memes.add(meme);
        }
        memeRepository.saveAll(memes);


        // when
        mockMvc.perform(
                        get("/api/meme")
                                .param("sort_type", "likeCount")

                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("조회수 순 밈 전체 조회")
    @Test
    void 조회수순_전체조회 () throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);

        // given
        List<Meme> memes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Meme meme = Meme.builder()
                    .name("test" + i)
                    .member(member)
                    .imageUrl("test_url")
                    .type(MemeType.MEME)
                    .publicFlag(true)
                    .viewCount(i + 5)
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .build();
            memes.add(meme);
        }
        memeRepository.saveAll(memes);


        // when
        mockMvc.perform(
                        get("/api/meme")
                                .param("sort_type", "viewCount")
                                .param("sort_direction", "desc")

                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("밈 검색 ")
    @Test
    void 밈_검색 () throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);

        // given
        List<Meme> memes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Meme meme = Meme.builder()
                    .name("test" + i)
                    .member(member)
                    .imageUrl("test_url")
                    .type(MemeType.MEME)
                    .publicFlag(true)
                    .viewCount(i + 5)
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .build();
            memes.add(meme);
        }
        memeRepository.saveAll(memes);


        // when
        mockMvc.perform(
                        get("/api/meme/search")
                                .param("keyword", "test")

                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithMockCustomUser(username = "testid")
    @DisplayName("나의 총 조회수와 좋아요수 조회 ")
    @Test
    void 내_조회_좋아요_수_조회 () throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);

        // given
        List<Meme> memes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Meme meme = Meme.builder()
                    .name("test" + i)
                    .member(member)
                    .imageUrl("test_url")
                    .type(MemeType.MEME)
                    .publicFlag(true)
                    .viewCount(1)
                    .likeCount(1)
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .build();
            memes.add(meme);
        }
        memeRepository.saveAll(memes);


        // when
        mockMvc.perform(
                        get("/api/meme/counts")

                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithMockCustomUser(username = "testid")
    @DisplayName("밈 생성 시 태그 검증이 작동한다")
    @Test
    void 밈_태그_제한_생성() throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("testid")
                .password("1234")
                .build();
        memberRepository.save(member);

        MemeCreateDTO createDTO = new MemeCreateDTO();
        createDTO.setUsername(member.getUsername());
        createDTO.setName("test");
        createDTO.setType(MemeType.MEME.toString());
        createDTO.setTags("#태그1 #태그2 #태그3 #태그4 #태그5 #태그6");
        createDTO.setPublicFlag(true);

        MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpeg", "test".getBytes());

        // when
        mockMvc.perform(
                        multipart("/api/meme")
                                .file(file)
                                .file(new MockMultipartFile("dto", "", "application/json", objectMapper.writeValueAsString(createDTO).getBytes())
                                ).contentType("multipart/form-data")
                                .accept("application/json")
                                .characterEncoding("UTF-8")
                )
                .andDo(print()) // then
                .andExpect(status().isBadRequest());

        createDTO.setTags("");
        mockMvc.perform(
                        multipart("/api/meme")
                                .file(file)
                                .file(new MockMultipartFile("dto", "", "application/json", objectMapper.writeValueAsString(createDTO).getBytes())
                                ).contentType("multipart/form-data")
                                .accept("application/json")
                                .characterEncoding("UTF-8")
                )
                .andDo(print()) // then
                .andExpect(status().isBadRequest());

        createDTO.setTags("#wewe wsewew");
        mockMvc.perform(
                        multipart("/api/meme")
                                .file(file)
                                .file(new MockMultipartFile("dto", "", "application/json", objectMapper.writeValueAsString(createDTO).getBytes())
                                ).contentType("multipart/form-data")
                                .accept("application/json")
                                .characterEncoding("UTF-8")
                )
                .andDo(print()) // then
                .andExpect(status().isBadRequest());

        createDTO.setTags("          ");
        mockMvc.perform(
                        multipart("/api/meme")
                                .file(file)
                                .file(new MockMultipartFile("dto", "", "application/json", objectMapper.writeValueAsString(createDTO).getBytes())
                                ).contentType("multipart/form-data")
                                .accept("application/json")
                                .characterEncoding("UTF-8")
                )
                .andDo(print()) // then
                .andExpect(status().isBadRequest());


    }
}