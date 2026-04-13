package com.dodo.dodoserver.global.security;

import com.dodo.dodoserver.domain.user.entity.Role;
import com.dodo.dodoserver.domain.user.entity.User;
import com.dodo.dodoserver.domain.user.dao.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 구글 등 소셜 서버로부터 사용자 정보를 전달받아 처리하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 기본 OAuth2UserService를 통해 소셜 사용자 정보(Attributes)를 가져옵니다.
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 서비스 구분(google, kakao 등) 및 필수 정보 추출
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oAuth2User.getAttribute("sub"); // 구글 사용자 식별 pk
        String email = oAuth2User.getAttribute("email");
        String nickname = oAuth2User.getAttribute("name");

        // 사용자 정보를 DB에 저장하거나 업데이트합니다 (최초 로그인 시 회원가입 처리).
        User user = saveOrUpdate(provider, providerId, email, nickname);

        // Spring Security에서 사용할 인증 객체를 생성하여 반환합니다.
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().getKey())),
                oAuth2User.getAttributes(),
                "sub" // PK로 사용할 속성명 지정
        );
    }

    /**
     * 기존 회원이면 정보를 업데이트하고, 신규 회원이면 새로운 User 엔티티를 생성하여 저장합니다.
     */
    private User saveOrUpdate(String provider, String providerId, String email, String nickname) {
        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .map(entity -> {
                    entity.setNickname(nickname); // 이름이 변경되었을 수 있으므로 업데이트
                    return entity;
                })
                .orElse(User.builder()
                        .email(email)
                        .nickname(nickname)
                        .provider(provider)
                        .providerId(providerId)
                        .role(Role.USER) // 기본 권한 부여
                        .build());

        return userRepository.save(user);
    }
}
