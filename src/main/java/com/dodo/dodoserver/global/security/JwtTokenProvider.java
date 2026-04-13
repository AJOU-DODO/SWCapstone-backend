package com.dodo.dodoserver.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;

/**
 * JWT 토큰의 생성, 파싱, 유효성 검증을 담당하는 컴포넌트입니다.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration.access}")
    private long accessTokenExpiration;

    @Value("${jwt.expiration.refresh}")
    private long refreshTokenExpiration;

    private SecretKey key;

    /**
     * 의존성 주입 완료 후 비밀키를 기반으로 서명에 사용할 Key 객체를 생성합니다.
     */
    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * API 호출 시 인증에 사용하는 Access Token을 생성합니다.
     */
    public String createAccessToken(String email, String role) {
        return createToken(email, role, accessTokenExpiration);
    }

    /**
     * Access Token 재발급을 위한 Refresh Token을 생성합니다. (권한 정보 미포함)
     */
    public String createRefreshToken(String email) {
        return createToken(email, null, refreshTokenExpiration);
    }

    /**
     * 실제 JWT 토큰을 빌드하는 공통 로직입니다.
     */
    private String createToken(String email, String role, long expiration) {
        var claimsBuilder = Jwts.claims().subject(email);
        
        if (role != null) {
            claimsBuilder.add("role", role); // Access Token에만 권한 정보를 담습니다.
        }
        
        Claims claims = claimsBuilder.build();
        Date now = new Date();
        
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(key)
                .compact();
    }

    /**
     * 토큰에서 사용자 정보를 추출하여 Spring Security 인증 객체(Authentication)를 생성합니다.
     */
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        String email = claims.getSubject();
        String role = claims.get("role", String.class);
        return new UsernamePasswordAuthenticationToken(email, "", 
                Collections.singleton(new SimpleGrantedAuthority(role)));
    }

    /**
     * 토큰의 유효성을 검사합니다. (서명 일치 여부, 만료 시간 확인 등)
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            log.error("유효하지 않은 JWT 토큰입니다: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 토큰의 Payload(Claims)를 파싱합니다.
     */
    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
