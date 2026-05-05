package com.dodo.dodoserver.infrastructure.fcm;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {
	private final FcmService fcmService;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
	public void handleNotification(NotificationEvent event) {
		log.info("Notification event received: title={}, tokenCount={}", event.title(), event.tokens().size());
		fcmService.sendNotification(event);
	}
}