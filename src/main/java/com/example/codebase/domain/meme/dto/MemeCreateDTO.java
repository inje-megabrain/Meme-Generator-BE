package com.example.codebase.domain.meme.dto;

import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.meme.entity.Meme;
import com.example.codebase.domain.meme.entity.MemeType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Getter
@Setter
public class MemeCreateDTO {

    private String name;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String imageUrl;

    private String type;

    // 여러개의 태그를 받을 수 있도록 정규표현식 작성한다
    @Pattern(regexp = "^#[a-zA-Z0-9가-힣]{1,20}( #[a-zA-Z0-9가-힣]{1,20})*$",
            message = "태그는 1~20자의 한글, 영문, 숫자로 이루어진 문자열이며, 공백으로 구분하며 최소 1개 최대 5개 입력해주세요.")
    private String tags;

    private Boolean publicFlag;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String username;

    public Meme toEntity(Member member) {
        return Meme.builder()
                .name(name)
                .imageUrl(imageUrl)
                .type(MemeType.from(type))
                .tags(tags)
                .publicFlag(publicFlag)
                .member(member)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
