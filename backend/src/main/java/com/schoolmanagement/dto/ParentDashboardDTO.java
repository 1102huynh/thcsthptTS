package com.schoolmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentDashboardDTO {
    private ParentDTO parent;
    private DashboardStats stats;
    private java.util.List<AnnouncementDTO> recentAnnouncements;
    private java.util.List<ParentMeetingDTO> upcomingMeetings;
    private java.util.List<ParentTeacherMessageDTO> unreadMessages;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DashboardStats {
        private int totalChildren;
        private int unreadMessages;
        private int upcomingMeetings;
        private int activeAnnouncements;
    }
}

