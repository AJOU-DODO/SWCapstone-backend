package com.dodo.dodoserver.event;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationEvent {
	private final Long userId;       // 알림을 받을 대상 유저 식별자이다.
	private final String token;
	private final String title;      // 알림 제목이다.
	private final String body;       // 알림 본문이다.
	private final Map<String, String> data;   // 앱 내 이동 경로(페이로드)이다.
}