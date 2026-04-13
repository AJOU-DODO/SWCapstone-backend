package com.dodo.dodoserver.infrastructure.fcm;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.BatchResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {
	private final FirebaseMessaging firebaseMessaging;

	@Async("fcmExecutor")
	public void sendNotification(NotificationEvent event) {
		if (event.tokens() == null || event.tokens().isEmpty()) {
			log.warn("FCM tokens are empty for the event: {}", event.title());
			return;
		}

		MulticastMessage message = MulticastMessage.builder()
			.addAllTokens(event.tokens())
			.setNotification(Notification.builder()
				.setTitle(event.title())
				.setBody(event.body())
				.build())
			.putAllData(event.data())
			.setAndroidConfig(AndroidConfig.builder()
				.setPriority(AndroidConfig.Priority.HIGH)
				.build())
			.build();

		try {
			BatchResponse response = firebaseMessaging.sendEachForMulticast(message);
			log.info("FCM Sent Successfully. Success count: {}, Failure count: {}", 
				response.getSuccessCount(), response.getFailureCount());
		} catch (FirebaseMessagingException e) {
			log.error("FCM Multicast Send Failed: {}", e.getMessage());
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