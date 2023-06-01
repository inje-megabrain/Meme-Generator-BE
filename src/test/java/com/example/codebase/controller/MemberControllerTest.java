package com.example.codebase.controller;

import com.example.codebase.annotation.WithMockCustomUser;
import com.example.codebase.domain.auth.dto.LoginDTO;
import com.example.codebase.domain.member.dto.CreateMemberDTO;
import com.example.codebase.domain.member.entity.Authority;
import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.member.entity.MemberAuthority;
import com.example.codebase.domain.member.repository.MemberAuthorityRepository;
import com.example.codebase.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MemberAuthorityRepository memberAuthorityRepository;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @DisplayName("회원가입 API가 작동한다")
    @Test
    void test1() throws Exception {

        CreateMemberDTO dto = new CreateMemberDTO();
        dto.setEmail("test@test.com");
        dto.setName("박성훈");
        dto.setUsername("testid");
        dto.setPassword("password123!");

        mockMvc.perform(
                        post("/api/member")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @DisplayName("이미 있는 아이디로 회원가입 시")
    @Test
    void 이미있는_아이디_가입 () throws Exception {
        Member member = Member.builder()
                .email("new@new.com")
                .name("test")
                .username("test123")
                .password("1234")
                .build();
        memberRepository.save(member);

        CreateMemberDTO dto = new CreateMemberDTO();
        dto.setEmail("test@test.com");
        dto.setName("박성훈");
        dto.setUsername("test123");
        dto.setPassword("password123!");

        mockMvc.perform(
                        post("/api/member")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("이미 있는 이메일로 회원가입 시")
    @Test
    void 이미있는_이메일_가입 () throws Exception {
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("test123")
                .password("1234")
                .build();
        memberRepository.save(member);

        CreateMemberDTO dto = new CreateMemberDTO();
        dto.setEmail("test@test.com");
        dto.setName("박성훈");
        dto.setUsername("newid123");
        dto.setPassword("password123!");

        mockMvc.perform(
                        post("/api/member")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }



    @DisplayName("회원가입 시 이메일 유효성 검증이 작동한다")
    @Test
    void test_123() throws Exception {

        CreateMemberDTO dto = new CreateMemberDTO();
        dto.setEmail("test");
        dto.setName("test");
        dto.setUsername("testid");
        dto.setPassword("password123!");

        mockMvc.perform(
                        post("/api/member")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    @DisplayName("회원가입 시 이름 최대 갯수 제한 작동한다")
    @Test
    void test_1233() throws Exception {

        CreateMemberDTO dto = new CreateMemberDTO();
        dto.setEmail("test");
        dto.setName("asdasdasdasdasdassadasdsasdasdasdasdasadasasdsa");
        dto.setUsername("testid");
        dto.setPassword("password123!");

        mockMvc.perform(
                        post("/api/member")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("회원가입시 아이디 검증이 작동한다")
    @Test
    void 회원가입시_아이디_검증() throws Exception {

        CreateMemberDTO dto = new CreateMemberDTO();
        dto.setEmail("test@test.com");
        dto.setName("test");
        dto.setUsername("???"); // 아이디
        dto.setPassword("password123!");

        mockMvc.perform(
                        post("/api/member")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @WithMockCustomUser(username = "test123", role = "USER")
    @DisplayName("회원탈퇴 API가 작동한다")
    @Test
    void 탈퇴_API() throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("test123")
                .password("1234")
                .build();
        memberRepository.save(member);

        // when
        mockMvc.perform(
                        delete("/api/member/{username}", member.getUsername())
                )
                .andDo(print())
                .andExpect(status().isOk()); // then
    }

    @WithMockCustomUser(username = "admin", role = "ADMIN")
    @DisplayName("어드민이 다른 회원 삭제 시")
    @Test
    void 어드민_유저탈퇴_API() throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("test123")
                .password("1234")
                .build();
        memberRepository.save(member);

        // when
        mockMvc.perform(
                        delete("/api/member/{username}", member.getUsername())
                )
                .andDo(print())
                .andExpect(status().isOk()); // then
    }

    @WithMockCustomUser(username = "user", role = "USER")
    @DisplayName("일반회원이 다른 회원 삭제 시")
    @Test
    void 일반인이_다른회원_삭제() throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("test123")
                .password("1234")
                .build();
        memberRepository.save(member);

        // when
        mockMvc.perform(
                        delete("/api/member/{username}", member.getUsername())
                )
                .andDo(print())
                .andExpect(status().isBadRequest()); // then
    }

    @DisplayName("아이디가 최소 1자 이상이어야 한다")
    @Test
    void 아이디_최소_API () throws Exception {

        CreateMemberDTO dto = new CreateMemberDTO();
        dto.setEmail("test@test.com");
        dto.setName("test");
        dto.setUsername("a");
        dto.setPassword("password123!");

        mockMvc.perform(
                        post("/api/member")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("아이디의 최대 길이를 제한한다")
    @Test
    void 아이디_최댸_길이_회원가입_API () throws Exception {

        CreateMemberDTO dto = new CreateMemberDTO();
        dto.setEmail("test@test.com");
        dto.setName("test");
        dto.setUsername("aasdadasdasdasdasdaasd");
        dto.setPassword("password123!");

        mockMvc.perform(
                        post("/api/member")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @WithMockCustomUser(username = "test123", role = "USER")
    @DisplayName("이름 수정 시 ")
    @Test
    void 이름_수정_API () throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("test123")
                .password("1234")
                .build();
        memberRepository.save(member);

        mockMvc.perform(
                        put("/api/member/name")
                                .param("newName", "새로운이름")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @WithMockCustomUser(username = "test123", role = "USER")
    @DisplayName("이름 수정 유효성 검증 처리 ")
    @Test
    void 이름_수정_유효성검증_API () throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test")
                .username("test123")
                .password("1234")
                .build();
        memberRepository.save(member);

        mockMvc.perform(
                        put("/api/member/name")
                                .param("newName", "a")
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        put("/api/member/name")
                                .param("newName", "가")
                )
                .andDo(print())
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        put("/api/member/name")
                                .param("newName", "asdasdasdasdsad")
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}