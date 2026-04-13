package com.dodo.dodoserver.infrastructure.fcm;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
	private final FcmService fcmService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleNotification(NotificationEvent event) {
		fcmService.sendNotification(event);
	}
}