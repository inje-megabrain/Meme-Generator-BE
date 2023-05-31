package com.example.codebase.domain.auth.service;

import com.example.codebase.domain.member.repository.MemberRepository;
import com.example.codebase.util.RedisUtil;
import com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final RedisUtil redisUtil;


    @Autowired
    public AuthService(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    public void authenticateMail(String code) {
        String email = redisUtil.getData(code)
                .orElseThrow(() -> new RuntimeException("인증 코드가 유효하지 않습니다."));
        redisUtil.deleteData(code);

        // ROLE_USER 부여
    }
}
