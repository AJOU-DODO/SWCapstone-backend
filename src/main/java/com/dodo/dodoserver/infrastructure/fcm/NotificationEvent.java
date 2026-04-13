package com.dodo.dodoserver.infrastructure.fcm;

import java.util.List;
import java.util.Map;

/**
 * @param tokens fcm 기기 token 목록
 * @param title  알림 제목이다.
 * @param body  알림 본문이다.
 * @param data  앱 내 이동 경로(페이로드)이다. */
public record NotificationEvent(List<String> tokens, String title, String body, Map<String, String> data) {
}