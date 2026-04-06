package com.dodo.dodoserver.event;

import java.util.Map;

import lombok.AllArgsConstructor;

/**
 * @param token fcm 기기 token
 * @param title  알림 제목이다.
 * @param body  알림 본문이다.
 * @param data  앱 내 이동 경로(페이로드)이다. */
public record NotificationEvent(String token, String title, String body, Map<String, String> data) {
}