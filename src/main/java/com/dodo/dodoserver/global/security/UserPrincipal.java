package com.dodo.dodoserver.global.security;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Spring Security 컨텍스트에서 사용될 커스텀 인증 객체
 * DB 조회 없이 컨트롤러에서 즉시 유저 ID와 이메일을 사용할 수 있도록 지원합니다.
 */
@Getter
public class UserPrincipal implements UserDetails, OAuth2User {

    private final Long id;
    private final String email;
    private final String role;
    private final Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    @Builder
    public UserPrincipal(Long id, String email, String role, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.authorities = authorities;
    }

    public static UserPrincipal create(Long id, String email, String role) {
        return UserPrincipal.builder()
                .id(id)
                .email(email)
                .role(role)
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(role)))
                .build();
    }

    public static UserPrincipal create(Long id, String email, String role, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(id, email, role);
        userPrincipal.setAttributes(attributes);
        return userPrincipal;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // OAuth2 로그인이므로 패스워드 미사용
    }

    @Override
    public String getUsername() {
        return email; // Username으로 이메일 사용
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
