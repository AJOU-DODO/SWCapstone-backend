package com.dodo.dodoserver.global.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
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
 * JWT 토큰 생성, 파싱, 유효성 검증 담당 컴포넌트
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Getter
	@Value("${jwt.expiration.access}")
    private long accessTokenExpiration;

	@Value("${jwt.expiration.refresh}")
    private long refreshTokenExpiration;

    private SecretKey key;

    /**
     * 의존성 주입 후 비밀키 기반 서명용 Key 객체 생성
     */
    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * API 호출 인증용 Access Token 생성
     */
    public String createAccessToken(String email, String role) {
        return createToken(email, role, accessTokenExpiration);
    }

    /**
     * Access Token 재발급용 Refresh Token 생성 (권한 정보 미포함)
     */
    public String createRefreshToken(String email) {
        return createToken(email, null, refreshTokenExpiration);
    }

    /**
     * JWT 토큰 빌드 공통 로직
     */
    private String createToken(String email, String role, long expiration) {
        var claimsBuilder = Jwts.claims().subject(email);
        
        if (role != null) {
            claimsBuilder.add("role", role); // Access Token에만 권한 정보 포함
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
     * 토큰 사용자 정보 추출 및 Spring Security 인증 객체(Authentication) 생성
     */
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        String email = claims.getSubject();
        String role = claims.get("role", String.class);
        return new UsernamePasswordAuthenticationToken(email, "", 
                Collections.singleton(new SimpleGrantedAuthority(role)));
    }

    /**
     * 토큰 유효성 검사 (서명 일치 여부, 만료 시간 확인 등)
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
     * 토큰 Payload(Claims) 파싱
     */
    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
