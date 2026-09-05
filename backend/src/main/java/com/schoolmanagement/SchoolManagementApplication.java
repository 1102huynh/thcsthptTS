package com.schoolmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.schoolmanagement.repository")
@ComponentScan(basePackages = "com.schoolmanagement")
// AdmissionRateLimitFilter's periodic stale-entry sweep (3.7) and
// NewsViewCountAggregator's batched-view-count flush (P4).
@EnableScheduling
// Public-portal read caches (P4: NewsService/SchoolEventService/PublicPortalService)
// - no cache library on the classpath, so Spring Boot wires the in-process
// ConcurrentMapCacheManager. Fine for this single-instance deployment; would
// need a shared cache (e.g. Redis) if this app ever runs as multiple nodes.
@EnableCaching
public class SchoolManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(SchoolManagementApplication.class, args);
    }

}

