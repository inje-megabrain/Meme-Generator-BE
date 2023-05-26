package com.example.codebase.controller.dto;

import lombok.Getter;

public enum  ErrorCode {
    NOT_NULL("ERROR_CODE_NOT_NULL", "필수값이 누락되었습니다."),
    MIN_VALUE("ERROR_CODE_MIN_VALUE", "최소값보다 커야 합니다."),
    PATTERN("ERROR_CODE_PATTERN", "값 형식이 다릅니다."),
    NOT_BLANK("ERROR_CODE_BLANK","필수값이 누락되었습니다."),
    EMAIL("ERROR_CODE_EMAIL", "이메일 형식이 아닙니다.");

    @Getter
    private String code;

    @Getter
    private String description;

    ErrorCode(String code, String description) {
        this.code = code;
        this.description = description;
    }
}