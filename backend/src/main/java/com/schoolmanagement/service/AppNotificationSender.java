package com.schoolmanagement.service;

import com.schoolmanagement.entity.NotificationChannel;
import org.springframework.stereotype.Component;

/**
 * The APP channel has nothing to push externally — the
 * {@code NotificationRecipient} row NotificationService already created
 * before calling send() IS the delivery; the recipient "receives" it by
 * calling GET /v1/notifications/my. Always succeeds.
 */
@Component
public class AppNotificationSender implements NotificationSender {

    @Override
    public boolean send(String recipientContact, String title, String content) {
        return true;
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.APP;
    }
}
