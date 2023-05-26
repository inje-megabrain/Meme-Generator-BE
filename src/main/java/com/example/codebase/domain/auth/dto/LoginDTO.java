package com.example.codebase.domain.auth.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class LoginDTO {

    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 4자 이상 20자 이하의 영어 또는 숫자로 입력해주세요.")
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력입니다.")
    private String password;
}
