package com.example.codebase.domain.auth.handler;

import com.example.codebase.domain.auth.dto.TokenResponseDTO;
import com.example.codebase.domain.member.repository.MemberRepository;
import com.example.codebase.jwt.JwtFilter;
import com.example.codebase.jwt.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Slf4j
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private TokenProvider tokenProvider;

    @Value("${app.oauth2-redirect-uri}")
    private String redirectUri;

    @Autowired
    public OAuth2AuthenticationSuccessHandler(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    /*
     * OAuth2 로그인 성공 시 (구글, 네이버, 카카오)
     * Authorization Code를 받아서 Access Token을 받아오는 과정을 거친다. (Google은 생략. Login할때 바로 Access Token을 받아옴)
     * 1. 최초 가입한 사람 -> ROLE_GUEST
     * 2. 이미 가입한 사람 -> ROLE_USER
     * 3. 로그인 성공 후 액세스 토큰 발급
     * 4. 토큰을 프론트로 리다이렉트
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        try {
            String authorizationCode = request.getParameter("code");
            SecurityContextHolder.getContext().setAuthentication(authentication);

            TokenResponseDTO token = tokenProvider.generateToken(authentication);

            log.info("authorizationCode: " + authorizationCode);
            log.info("우리가 발급한 refreshToken: " + token.getRefreshToken());

            if (authentication.getAuthorities().contains("ROLE_GUEST") ){ // 최초 가입한 사람 -> ROLE_GUEST
                response.addHeader(JwtFilter.AUTHORIZATION_HEADER, "Bearer " + token.getRefreshToken());
                response.sendRedirect("oauth2/sign-up"); // 프론트의 회원가입 추가 정보 입력 폼으로 리다이렉트
            }
            else {
                loginSuccess(request, response, token.getRefreshToken());
            }
        } catch (RuntimeException e) {
            throw e;
        }
    }

    private void loginSuccess(HttpServletRequest request, HttpServletResponse response, String token) throws IOException {
        // add to refresh token in set-cookie
        String redirect = redirectUri + "?token=" + token;  // state 파라미터를 추가해서 보내줘야 함 (CSRF Attack 방지)

        response.setStatus(HttpServletResponse.SC_OK);
        getRedirectStrategy().sendRedirect(request, response, redirect);
    }
}