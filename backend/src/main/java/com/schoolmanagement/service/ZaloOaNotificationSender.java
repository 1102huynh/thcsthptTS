package com.schoolmanagement.service;

import com.schoolmanagement.entity.NotificationChannel;
import com.schoolmanagement.exception.NotificationChannelUnavailableException;
import org.springframework.stereotype.Component;

/**
 * Not implemented yet — per IMPLEMENTATION_PLAN.md 3.6, which explicitly
 * requires a registered Zalo OA (Official Account) before this is built for
 * real. Registered as a bean (so the channel resolves and the error is
 * clear) rather than omitted, but every call fails loudly.
 */
@Component
public class ZaloOaNotificationSender implements NotificationSender {

    @Override
    public boolean send(String recipientContact, String title, String content) {
        throw new NotificationChannelUnavailableException(
                "Zalo notifications are not yet available — no Zalo OA (Official Account) has been registered. "
                        + "See IMPLEMENTATION_PLAN.md 3.6.");
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.ZALO;
    }
}
