package com.dodo.dodoserver.infrastructure.fcm;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.SendResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {
	private final FirebaseMessaging firebaseMessaging;

	@Async("fcmExecutor")
	public void sendNotification(NotificationEvent event) {
		sendNotificationSync(event);
	}

	public void sendNotificationSync(NotificationEvent event) {
		if (event.tokens() == null || event.tokens().isEmpty()) {
			log.warn("FCM tokens are empty for the event: {}", event.title());
			return;
		}

		// event.data()가 불변 객체일 수 있으므로 수정 가능한 HashMap으로 복사 (null 체크 포함)
		java.util.Map<String, String> dynamicData = event.data() != null 
			? new java.util.HashMap<>(event.data()) 
			: new java.util.HashMap<>();

		if (event.title() != null) {
			dynamicData.put("title", event.title());
		}
		if (event.body() != null) {
			dynamicData.put("body", event.body());
		}

		MulticastMessage message = MulticastMessage.builder()
			.addAllTokens(event.tokens())
			.putAllData(dynamicData) // title, body가 포함된 통합 데이터 맵 전송
			.setAndroidConfig(AndroidConfig.builder()
				.setPriority(AndroidConfig.Priority.HIGH)
				.build())
			.build();

		try {
			BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
			log.info("[NOTIFICATION] FCM Sent Successfully (Pure Data Message). Success count: {}, Failure count: {}",
				response.getSuccessCount(), response.getFailureCount());
			
			if (response.getFailureCount() > 0) {
				log.warn("[NOTIFICATION] FCM Partial Failures detected. Details:");
				List<SendResponse> responses = response.getResponses();
				for (int i = 0; i < responses.size(); i++) {
					SendResponse sr = responses.get(i);
					if (!sr.isSuccessful()) {
						log.warn("[NOTIFICATION] Failure at index {}: Token={}, Error={}",
							i, event.tokens().get(i), sr.getException().getMessage());
					}
				}
			}
		} catch (FirebaseMessagingException e) {
			log.error("[NOTIFICATION] FCM Multicast Send Failed: {}", e.getMessage());
			throw new RuntimeException("FCM 전송 중 오류 발생", e);
		}
	}

	// public void sendNotification(String title, String body, String fcmToken) {
	// 	log.info("Attempting to send Notification (title: {}, body: {}, fcmToken: {})", title, body, fcmToken);
	// 	send(createMessage(title, body, fcmToken));
	// }
	//
	// private void send(Message message) {
	// 	try {
	// 		String response = firebaseMessaging.send(message);
	// 		log.info("Successfully send Notification: {}", response);
	// 	} catch (FirebaseMessagingException e) {
	// 		log.error("Fail to send Notification : {}", e.getMessage());
	// 	}
	// }

	private Message createMessage(String title, String body, String fcmToken) {
		return Message.builder()
			.putData("title", title)
			.putData("body", body)
			.setToken(fcmToken)
			.build();
	}
}