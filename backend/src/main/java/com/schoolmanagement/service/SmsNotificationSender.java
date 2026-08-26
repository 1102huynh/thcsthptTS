package com.schoolmanagement.service;

import com.schoolmanagement.entity.NotificationChannel;
import com.schoolmanagement.exception.NotificationChannelUnavailableException;
import org.springframework.stereotype.Component;

/**
 * Not implemented yet — per IMPLEMENTATION_PLAN.md 3.6, which explicitly
 * requires choosing an SMS provider (eSMS/FPT SMS) and budget before this
 * is built for real. Registered as a bean (so the channel resolves and the
 * error is clear) rather than omitted, but every call fails loudly.
 */
@Component
public class SmsNotificationSender implements NotificationSender {

    @Override
    public boolean send(String recipientContact, String title, String content) {
        throw new NotificationChannelUnavailableException(
                "SMS notifications are not yet available — no SMS provider (eSMS/FPT SMS) has been configured. "
                        + "See IMPLEMENTATION_PLAN.md 3.6.");
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }
}
