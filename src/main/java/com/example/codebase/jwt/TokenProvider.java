package com.example.codebase.jwt;

import com.example.codebase.domain.auth.dto.LoginDTO;
import com.example.codebase.domain.auth.dto.TokenResponseDTO;
import com.example.codebase.util.RedisUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TokenProvider implements InitializingBean {

    private static final String AUTHORITIES_KEY = "auth";
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final String secret;

    private final Long tokenValidityInMilliseconds;
    private final Long refreshTokenValidityInMilliseconds;
    private final RedisUtil redisUtil;
    private Key key;

    public TokenProvider(
            AuthenticationManagerBuilder authenticationManagerBuilder,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.token-validity-in-seconds}") Long tokenValidityInMilliseconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") Long refreshTokenValidityInMilliseconds, RedisUtil redisUtil) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.secret = secret;
        this.tokenValidityInMilliseconds = tokenValidityInMilliseconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds * 1000;
        this.redisUtil = redisUtil;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    private Long getTokenValidityInSeconds() {
        return this.tokenValidityInMilliseconds / 1000;
    }

    private Long getRefreshTokenValidityInSeconds() {
        return this.refreshTokenValidityInMilliseconds / 1000 ;
    }

    public String createToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenValidityInMilliseconds);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    }

    public String createRefreshToken(Authentication authentication) {
        long now = (new Date()).getTime();
        Date validity = new Date(now + this.refreshTokenValidityInMilliseconds);

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(authentication.getName())
                .claim("typ", "refresh")
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();
    }

    public TokenResponseDTO generateToken(LoginDTO loginDTO) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = createToken(authentication);
        String refreshToken = createRefreshToken(authentication);

        // Redis에 Refresh Token 캐싱
        redisUtil.setDataAndExpire(authentication.getName() + "_token", refreshToken, getRefreshTokenValidityInSeconds());

        TokenResponseDTO tokenResponseDTO = new TokenResponseDTO();
        tokenResponseDTO.setAccessToken(accessToken);
        tokenResponseDTO.setExpiresIn(getTokenValidityInSeconds());
        tokenResponseDTO.setRefreshToken(refreshToken);
        tokenResponseDTO.setRefreshExpiresIn(getRefreshTokenValidityInSeconds());
        return tokenResponseDTO;
    }

    public TokenResponseDTO generateToken(Authentication authentication) {
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = createToken(authentication);
         String refreshToken = createRefreshToken(authentication);

        // Redis에 Refresh Token 캐싱
        redisUtil.setDataAndExpire(authentication.getName() + "_token", refreshToken, getRefreshTokenValidityInSeconds());

        TokenResponseDTO tokenResponseDTO = new TokenResponseDTO();
        tokenResponseDTO.setAccessToken(accessToken);
        tokenResponseDTO.setExpiresIn(getTokenValidityInSeconds());
        tokenResponseDTO.setRefreshToken(refreshToken);
        tokenResponseDTO.setRefreshExpiresIn(getRefreshTokenValidityInSeconds());
        return tokenResponseDTO;
    }


    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);            // Token 값

        Collection<? extends GrantedAuthority> authorities = Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    private Claims getClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException exception) {
            log.info("잘못된 JWT 서명입니다");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }
}
