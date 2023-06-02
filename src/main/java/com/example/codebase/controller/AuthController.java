package com.example.codebase.controller;

import com.example.codebase.domain.auth.dto.LoginDTO;
import com.example.codebase.domain.auth.dto.TokenResponseDTO;
import com.example.codebase.domain.auth.service.AuthService;
import com.example.codebase.domain.mail.service.MailService;
import com.example.codebase.jwt.TokenProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(value = "Auth APIs", description = "인증 관련 APIs")
@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;

    private final AuthService authService;
    private final MailService mailService;

    @Autowired
    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, TokenProvider tokenProvider, AuthService authService, MailService mailService) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.tokenProvider = tokenProvider;
        this.authService = authService;
        this.mailService = mailService;
    }

    @PostMapping("/login")
    public ResponseEntity login(@Valid @RequestBody LoginDTO loginDTO) {
        TokenResponseDTO responseDTO = tokenProvider.generateToken(loginDTO);
        return new ResponseEntity(responseDTO, HttpStatus.OK);
    }

    @ApiOperation(value = "토큰 재발급", notes = "토큰 재발급")
    @PostMapping("/refresh")
    public ResponseEntity refresh(@RequestBody String refreshToken) {
        TokenResponseDTO responseDTO = tokenProvider.regenerateToken(refreshToken);
        return new ResponseEntity(responseDTO, HttpStatus.OK);
    }

    @ApiOperation(value = "이메일 인증 코드 API", notes = "이메일 인증 코드 API")
    @GetMapping("/auth/email")
    public ResponseEntity emailAuth(@RequestParam String code) {
        authService.authenticateMail(code);
        return new ResponseEntity("이메일 인증되었습니다", HttpStatus.OK);
    }

    @ApiOperation(value = "이메일 인증 전송 API", notes = "이메일 인증 전송 API")
    @PostMapping("/auth/email")
    public ResponseEntity sendEmailAuth(@RequestParam String email) {
        mailService.sendMail(email);
        return new ResponseEntity(HttpStatus.OK);
    }
}
