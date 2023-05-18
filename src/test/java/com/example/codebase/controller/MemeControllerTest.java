package com.example.codebase.controller;

import com.example.codebase.annotation.WithMockCustomUser;
import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.member.repository.MemberRepository;
import com.example.codebase.domain.meme.dto.MemeCreateDTO;
import com.example.codebase.domain.meme.entity.MemeType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

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
    void 밈_생성 () throws Exception {
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

}