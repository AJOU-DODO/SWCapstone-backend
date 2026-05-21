package com.dodo.dodoserver.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 애플리케이션 전용 설정 프로퍼티 클래스
 * application.yml의 'app' 프리픽스를 가진 설정들을 매핑
 */
@Component
@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {
    private final OAuth2 oauth2 = new OAuth2();

    @Getter
    @Setter
    public static class OAuth2 {
        private List<String> authorizedRedirectUris = new ArrayList<>();
    }
}
