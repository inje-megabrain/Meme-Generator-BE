package com.example.codebase.controller;

import com.example.codebase.annotation.WithMockCustomUser;
import com.example.codebase.domain.member.dto.CreateMemberDTO;
import com.example.codebase.domain.member.entity.Member;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    private ObjectMapper objectMapper = new ObjectMapper();

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
        dto.setName("test");
        dto.setUsername("testid");
        dto.setPassword("1234");

        mockMvc.perform(
                        post("/api/member")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
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
                .andExpect(status().isOk()) // then
                .andDo(print()
                );
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
                .andExpect(status().isOk()) // then
                .andDo(print()
                );
    }

    @WithMockCustomUser(username = "user", role = "USER")
    @DisplayName("일반회원이 다른 회원 삭제 시")
    @Test
    void 일반인이_다른회원_삭제 () throws Exception {
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
                .andExpect(status().isBadRequest()) // then
                .andDo(print());
    }

}