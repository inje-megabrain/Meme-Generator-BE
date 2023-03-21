package com.example.codebase.config;

import com.example.codebase.auth.oauth.CustomOAuth2UserService;
import com.example.codebase.auth.oauth.OAuth2AuthenticationFailureHandler;
import com.example.codebase.auth.oauth.OAuth2AuthenticationSuccessHandler;
import com.example.codebase.jwt.JwtAuthenticationEntryPoint;
import com.example.codebase.jwt.TokenProvider;
import com.example.codebase.jwt.JwtAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final TokenProvider tokenProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;
    @Autowired
    public SecurityConfig(TokenProvider tokenProvider, JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, JwtAccessDeniedHandler jwtAccessDeniedHandler, OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler, OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler, CustomOAuth2UserService customOAuth2UserService) {
        this.tokenProvider = tokenProvider;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
        this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
        this.customOAuth2UserService = customOAuth2UserService;
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private String[] permitList = {
            "/v2/**",
            "/configuration/**",
            "/swagger*/**",
            "/webjars/**",
            "/swagger-resources/**"
    };

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers("/h2-console/**", "/favicon.ico")
                .antMatchers(permitList);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .formLogin().disable()
                .httpBasic().disable()
                .headers().frameOptions().disable()

                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .authorizeRequests()
                .antMatchers("/api/**").permitAll()
                .antMatchers(permitList).permitAll()

                .anyRequest().authenticated()

                .and()
                .oauth2Login()
                .successHandler(oAuth2AuthenticationSuccessHandler)
                .failureHandler(oAuth2AuthenticationFailureHandler)
                .userInfoEndpoint()
                .userService(customOAuth2UserService);
    }
}

