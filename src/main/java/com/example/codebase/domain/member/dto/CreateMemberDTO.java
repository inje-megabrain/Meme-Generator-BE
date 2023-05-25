package com.example.codebase.domain.member.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.parameters.P;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class CreateMemberDTO {

        @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 4자 이상 20자 이하의 영어 또는 숫자로 입력해주세요.")
        private String username;

        // 특수 문자 포함 비밀번호 regex
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[$@$!%*#?&])[A-Za-z\\d$@$!%*#?&]{8,}$", message = "비밀번호는 최소 8자, 최소 하나의 문자, 하나의 숫자 및 하나의 특수 문자로 구성되어야 합니다.")
        private String password;

        @Pattern(regexp = "^[가-힣a-zA-Z]{2,10}$", message = "이름은 2자 이상 10자 이하의 한글 또는 영어로 입력해주세요.")
        private String name;

        @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$") // 이메일에 대한 유효성
        private String email;
}

