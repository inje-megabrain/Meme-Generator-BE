package com.example.codebase.controller;

import com.example.codebase.domain.auth.dto.LoginDTO;
import com.example.codebase.domain.member.dto.CreateMemberDTO;
import com.example.codebase.domain.member.entity.Authority;
import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.member.entity.MemberAuthority;
import com.example.codebase.domain.member.repository.MemberAuthorityRepository;
import com.example.codebase.domain.member.repository.MemberRepository;
import com.example.codebase.util.RedisUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisUtil redisUtil;

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

    @DisplayName("ROLE_USER 로그인 API가 작동한다")
    @Test
    void 로그인_시() throws Exception {

        Authority authority = Authority.builder()
                .authorityName("ROLE_USER")
                .build();

        Member member = Member.builder()
                .email("test@test.com")
                .name("test123")
                .username("test123")
                .password(passwordEncoder.encode("password123!"))
                .activated(true)
                .createdTime(LocalDateTime.now())
                .build();

        MemberAuthority memberAuthority = MemberAuthority.builder()
                .authority(authority)
                .member(member)
                .build();
        member.setAuthorities(Set.of(memberAuthority));
        memberRepository.save(member);
        memberAuthorityRepository.save(memberAuthority);

        LoginDTO dto = new LoginDTO();
        dto.setUsername("test123");
        dto.setPassword("password123!");

        mockMvc.perform(
                        post("/api/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("ROLE_GUEST(이메일인증X) 로그인 시")
    @Test
    void 게스트_로그인_시() throws Exception {
        CreateMemberDTO createMemberDTO = new CreateMemberDTO();
        createMemberDTO.setEmail("test@test.com");
        createMemberDTO.setName("testname");
        createMemberDTO.setUsername("testid");
        createMemberDTO.setPassword("password123!");

        mockMvc.perform(
                        post("/api/member")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createMemberDTO))
                )
                .andDo(print())
                .andExpect(status().isCreated());

        LoginDTO dto = new LoginDTO();
        dto.setUsername("testid");
        dto.setPassword("password123!");

        mockMvc.perform(
                        post("/api/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }


    @DisplayName("이메일 인증 코드 후 로그인 시")
    @Test
    public void 인증_코드() throws Exception {
        // given
        Member member = Member.builder()
                .email("test@test.com")
                .name("test123")
                .username("test123")
                .password(passwordEncoder.encode("password123!"))
                .activated(false)
                .createdTime(LocalDateTime.now())
                .build();

        MemberAuthority memberAuthority = MemberAuthority.builder()
                .authority(Authority.of("ROLE_GUEST"))
                .member(member)
                .build();
        member.setAuthorities(Set.of(memberAuthority));
        memberRepository.save(member);
        memberAuthorityRepository.save(memberAuthority);

        String code = "test";
        redisUtil.setDataAndExpire(code, member.getEmail(), 60 * 1000 * 5);

        mockMvc.perform(
                get("/api/auth/email")
                        .param("code", code)
        )
                .andDo(print())
                .andExpect(status().isOk());


        // Login
        LoginDTO dto = new LoginDTO();
        dto.setUsername("test123");
        dto.setPassword("password123!");

        mockMvc.perform(
                        post("/api/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("잘못된 인증 코드로 이메일 인증 요청 시")
    @Test
    public void 잘못된_인증_코드() throws Exception {
        // given
        String code = "test"; // 서버에 저장(캐싱) 되지 않은 코드

        mockMvc.perform(
                        get("/api/auth/email")
                                .param("code", code)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("이미 인증된 계정 이메일 인증 전송 시")
    @Test
    public void 이미_인증된_이메일_전송_요청_시() throws Exception {
        // given
        String email = "admin"; // 서버에 저장(캐싱) 되지 않은 코드

        mockMvc.perform(
                        post("/api/auth/email")
                                .param("email", email)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @DisplayName("등록되지 않은 계정 이메일 인증 전송 시")
    @Test
    public void 잘못된_이메일_인증_시 () throws Exception {
        // given
        String email = "none@email.com"; // 서버에 저장(캐싱) 되지 않은 코드

        mockMvc.perform(
                        post("/api/auth/email")
                                .param("email", email)
                )
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

}