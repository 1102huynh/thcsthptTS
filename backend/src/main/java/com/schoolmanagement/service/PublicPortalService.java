package com.schoolmanagement.service;

import com.schoolmanagement.dto.PublicHomeDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Aggregates the public home page ({@code GET /v1/public/home}) so the
 * landing page is one request: featured news, latest news, upcoming events.
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class PublicPortalService {

    private NewsService newsService;
    private SchoolEventService eventService;

    public PublicHomeDTO home() {
        return PublicHomeDTO.builder()
                .featuredNews(newsService.featured(4))
                .latestNews(newsService.latest(6))
                .upcomingEvents(eventService.upcoming(4))
                .build();
    }
}
