-- =====================================================
-- Public portal (Cổng thông tin công khai): news, events,
-- media assets and contact messages.
-- See KE_HOACH_TRANG_TIN_TUC_CONG_KHAI.md.
-- No backfill - nothing before this represented public content.
-- =====================================================

CREATE TABLE `news_categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(150) NOT NULL,
  `slug` varchar(160) NOT NULL,
  `display_order` int NOT NULL DEFAULT 0,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_news_categories_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `news_articles` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `slug` varchar(280) NOT NULL,
  `summary` varchar(500) DEFAULT NULL,
  `content` longtext,
  `cover_image_url` varchar(500) DEFAULT NULL,
  `category_id` bigint DEFAULT NULL,
  `status` enum('DRAFT','PUBLISHED','ARCHIVED') NOT NULL DEFAULT 'DRAFT',
  `published_at` datetime(6) DEFAULT NULL,
  `is_featured` bit(1) NOT NULL DEFAULT b'0',
  `view_count` bigint NOT NULL DEFAULT 0,
  `author_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_news_articles_slug` (`slug`),
  KEY `idx_news_articles_status_published_at` (`status`, `published_at`),
  KEY `idx_news_articles_category` (`category_id`),
  CONSTRAINT `fk_news_articles_category` FOREIGN KEY (`category_id`) REFERENCES `news_categories` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_news_articles_author` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `school_events` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `slug` varchar(280) NOT NULL,
  `description` longtext,
  `cover_image_url` varchar(500) DEFAULT NULL,
  `location` varchar(255) DEFAULT NULL,
  `start_at` datetime(6) NOT NULL,
  `end_at` datetime(6) DEFAULT NULL,
  `status` enum('DRAFT','PUBLISHED','ARCHIVED') NOT NULL DEFAULT 'DRAFT',
  `published_at` datetime(6) DEFAULT NULL,
  `is_featured` bit(1) NOT NULL DEFAULT b'0',
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_school_events_slug` (`slug`),
  KEY `idx_school_events_status_start_at` (`status`, `start_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `media_assets` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `file_name` varchar(255) NOT NULL,
  `stored_file_name` varchar(255) NOT NULL,
  `content_type` varchar(100) NOT NULL,
  `size_bytes` bigint NOT NULL,
  `uploaded_by_id` bigint DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_media_assets_stored_file_name` (`stored_file_name`),
  CONSTRAINT `fk_media_assets_uploaded_by` FOREIGN KEY (`uploaded_by_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `contact_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `full_name` varchar(150) NOT NULL,
  `email` varchar(150) DEFAULT NULL,
  `phone` varchar(30) DEFAULT NULL,
  `subject` varchar(200) DEFAULT NULL,
  `message` varchar(4000) NOT NULL,
  `handled` bit(1) NOT NULL DEFAULT b'0',
  `created_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_contact_messages_handled_created_at` (`handled`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed the standard news categories a Vietnamese school starts with.
INSERT INTO `news_categories` (`name`, `slug`, `display_order`, `created_at`) VALUES
  ('Tuyển sinh', 'tuyen-sinh', 1, NOW(6)),
  ('Hoạt động', 'hoat-dong', 2, NOW(6)),
  ('Thông báo', 'thong-bao', 3, NOW(6));
