package com.example.codebase.controller;

import com.example.codebase.annotation.WithMockCustomUser;
import com.example.codebase.domain.item.dto.ItemCreateDTO;
import com.example.codebase.domain.item.dto.ItemResponseDTO;
import com.example.codebase.domain.item.entity.Item;
import com.example.codebase.domain.item.entity.ItemCategory;
import com.example.codebase.domain.item.repository.ItemRepository;
import com.example.codebase.domain.member.entity.Authority;
import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.member.entity.MemberAuthority;
import com.example.codebase.domain.member.repository.MemberAuthorityRepository;
import com.example.codebase.domain.member.repository.MemberRepository;
import com.example.codebase.domain.meme.repository.MemeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MemberRepository memberRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private MemberAuthorityRepository memberAuthorityRepository;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @WithMockCustomUser(username = "admin", role = "ADMIN")
    @DisplayName("아이템 등록 API가 작동한다")
    @Test
    public void 아이템_등록 () throws Exception {

        Authority authority = Authority.builder()
                .authorityName("ROLE_ADMIN")
                .build();
        Member member = Member.builder()
                .email("admin@admin.com")
                .name("admin")
                .username("admin")
                .password("testpassword")
                .createdTime(LocalDateTime.now())
                .build();
        memberRepository.save(member);

        MemberAuthority memberAuthority = MemberAuthority
                .builder()
                .member(member)
                .authority(authority)
                .build();
        memberAuthorityRepository.save(memberAuthority);

        ItemCreateDTO itemCreateDTO = new ItemCreateDTO();
        itemCreateDTO.setName("아이템1");
        itemCreateDTO.setUsername(member.getUsername());
        itemCreateDTO.setCategory(ItemCategory.도구.toString());
        itemCreateDTO.setImageUrl("testurl");

        MockMultipartFile file = new MockMultipartFile("image", "test.jpg", "image/jpg", "test".getBytes());

        mockMvc.perform(
                        multipart("/api/items")
                                .file(file)
                                .file(new MockMultipartFile("dto", "", "application/json", objectMapper.writeValueAsString(itemCreateDTO).getBytes()))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @DisplayName("도구 아이템 전체 조회 시")
    @Test
    public void 아이템_전체_조회 () throws Exception {
        Authority authority = Authority.builder()
                .authorityName("ROLE_ADMIN")
                .build();
        Member member = Member.builder()
                .email("admin@admin.com")
                .name("admin")
                .username("admin")
                .password("testpassword")
                .createdTime(LocalDateTime.now())
                .build();
        memberRepository.save(member);

        MemberAuthority memberAuthority = MemberAuthority
                .builder()
                .member(member)
                .authority(authority)
                .build();
        memberAuthorityRepository.save(memberAuthority);

        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Item item = Item.builder()
                    .name("아이템" + i)
                    .member(member)
                    .category(ItemCategory.도구)
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .imageUrl("testurl")
                    .build();
            items.add(item);
        }
        for (int i = 5; i <= 10; i++) {
            Item item = Item.builder()
                    .name("아이템" + i)
                    .member(member)
                    .category(ItemCategory.악세서리)
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .imageUrl("testurl")
                    .build();
            items.add(item);
        }
        itemRepository.saveAll(items);

        mockMvc.perform(
                        get("/api/items")
                                .param("category", "도구")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("단일 아이템 조회 시")
    @Test
    public void 단일_아이템_조회 () throws Exception {
        Authority authority = Authority.builder()
                .authorityName("ROLE_ADMIN")
                .build();
        Member member = Member.builder()
                .email("admin@admin.com")
                .name("admin")
                .username("admin")
                .password("testpassword")
                .createdTime(LocalDateTime.now())
                .build();
        memberRepository.save(member);

        MemberAuthority memberAuthority = MemberAuthority
                .builder()
                .member(member)
                .authority(authority)
                .build();
        memberAuthorityRepository.save(memberAuthority);

        Item item = Item.builder()
                .name("아이템1")
                .member(member)
                .category(ItemCategory.이모티콘)
                .createdAt(LocalDateTime.now())
                .imageUrl("testurl")
                .build();
        itemRepository.save(item);

        mockMvc.perform(
                        get("/api/items/" + item.getId())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());
    }


    @WithMockCustomUser(username = "admin", role = "ADMIN")
    @DisplayName("짤 아이템 삭제 시")
    @Test
    public void 짤_아이템_삭제 () throws Exception {
        Authority authority = Authority.builder()
                .authorityName("ROLE_ADMIN")
                .build();
        Member member = Member.builder()
                .email("admin@admin.com")
                .name("admin")
                .username("admin")
                .password("testpassword")
                .createdTime(LocalDateTime.now())
                .build();
        memberRepository.save(member);

        MemberAuthority memberAuthority = MemberAuthority
                .builder()
                .member(member)
                .authority(authority)
                .build();
        memberAuthorityRepository.save(memberAuthority);

        Item item = Item.builder()
                .name("아이템1")
                .member(member)
                .category(ItemCategory.이모티콘)
                .createdAt(LocalDateTime.now())
                .imageUrl("testurl")
                .build();
        itemRepository.save(item);

        mockMvc.perform(
                        delete("/api/items/" + item.getId())
                )
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}