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
        createDTO.setType(MemeType.TEMPLATE.toString());

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
                .andDo(print())
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
                    .type(MemeType.TEMPLATE)
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
}