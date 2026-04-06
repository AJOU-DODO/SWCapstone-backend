package com.dodo.dodoserver.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.dodo.dodoserver.service.FcmService;

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