package com.schoolmanagement.service;

import com.schoolmanagement.entity.NotificationChannel;
import com.schoolmanagement.exception.NotificationChannelUnavailableException;

/**
 * Strategy interface for delivering a Notification through one channel —
 * per IMPLEMENTATION_PLAN.md 3.6. NotificationService autowires every Spring
 * bean implementing this interface and picks the one whose getChannel()
 * matches the requested {@link NotificationChannel}, then calls send() once
 * per resolved recipient.
 */
public interface NotificationSender {

    /**
     * A normal, expected per-recipient delivery failure (bad address, SMTP
     * hiccup) must be caught inside the implementation and reported by
     * returning {@code false} — never let it throw and abort delivery to
     * every other recipient. The one exception (literally): if the whole
     * channel doesn't exist/isn't configured at all (not a per-recipient
     * problem), throw {@link NotificationChannelUnavailableException} — see
     * Sms/ZaloOaNotificationSender — which NotificationService deliberately
     * does NOT catch, so it rolls back the whole send instead of recording a
     * partial failure for a channel that could never have delivered anything.
     *
     * @param recipientContact the channel-appropriate address (email for
     *                          EMAIL, phone number for SMS, ...) — for APP,
     *                          unused (there's nothing to deliver to
     *                          externally; the NotificationRecipient row
     *                          itself is the delivery).
     * @return true if delivered successfully, false on an ordinary per-recipient failure.
     */
    boolean send(String recipientContact, String title, String content);

    NotificationChannel getChannel();
}
