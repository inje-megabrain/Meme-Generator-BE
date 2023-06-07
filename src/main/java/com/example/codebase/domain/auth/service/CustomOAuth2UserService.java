package com.example.codebase.domain.auth.service;

import com.example.codebase.domain.auth.OAuthAttributes;
import com.example.codebase.domain.member.entity.Authority;
import com.example.codebase.domain.member.entity.Member;
import com.example.codebase.domain.member.entity.MemberAuthority;
import com.example.codebase.domain.member.repository.MemberAuthorityRepository;
import com.example.codebase.domain.member.repository.MemberRepository;
import com.example.codebase.domain.member.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    private MemberRepository memberRepository;
    private MemberAuthorityRepository memberAuthorityRepository;
    private MemberService memberService;

    @Autowired
    public CustomOAuth2UserService(MemberRepository memberRepository, MemberAuthorityRepository memberAuthorityRepository, PasswordEncoder passwordEncoder, MemberService memberService) {
        this.memberRepository = memberRepository;
        this.memberAuthorityRepository = memberAuthorityRepository;
        this.memberService = memberService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();    // 현재 진행중인 서비스 코드(google, naver ..)
        String userNameAttributeName = userRequest.getClientRegistration()  // oauth2 로그인 진행 시 key field 값
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes oAuthAttributes = OAuthAttributes.
                of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        try {
            Member member = saveOrUpdate(oAuthAttributes);

            List<SimpleGrantedAuthority> simpleGrantedAuthorityList = new ArrayList<>();
            for (MemberAuthority memberAuthority : member.getAuthorities()) {
                simpleGrantedAuthorityList.add(new SimpleGrantedAuthority(memberAuthority.getAuthority().getAuthorityName()));
            }
            return new DefaultOAuth2User(simpleGrantedAuthorityList,
                    oAuthAttributes.getAttributes(),
                    oAuthAttributes.getNameAttributeKey());
        } catch (RuntimeException e) {
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_runtime_error"), e.getMessage());
        }
    }

    private Member saveOrUpdate(OAuthAttributes oAuthAttributes) {
        Optional<Member> find = memberRepository.findByOauthProviderId(oAuthAttributes.getOAuthProviderId());

        if (find.isPresent()) { // Update
            Member presentMember = find.get();
            presentMember.update(oAuthAttributes.getName(), oAuthAttributes.getPicture());
            return presentMember;
        }

        return memberService.createOAuthMember(oAuthAttributes);
    }
}
