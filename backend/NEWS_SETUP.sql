-- =====================================================
-- News Table Setup for School Management System
-- =====================================================
-- Purpose: Create news table and insert sample data
-- Database: PostgreSQL 12+
-- Date: December 5, 2025
-- =====================================================

-- =====================================================
-- 1. CREATE NEWS TABLE
-- =====================================================

CREATE TABLE IF NOT EXISTS news (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    published_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    category VARCHAR(100) NOT NULL,
    image VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    author_name VARCHAR(255),
    created_by BIGINT NOT NULL,
    view_count INTEGER DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_news_created_by FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_news_status ON news(status);
CREATE INDEX IF NOT EXISTS idx_news_category ON news(category);
CREATE INDEX IF NOT EXISTS idx_news_published_date ON news(published_date);
CREATE INDEX IF NOT EXISTS idx_news_featured ON news(featured);
CREATE INDEX IF NOT EXISTS idx_news_created_by ON news(created_by);

-- =====================================================
-- 2. INSERT SAMPLE NEWS DATA
-- =====================================================

-- News #1: Annual Sports Day (Event)
INSERT INTO news (title, content, published_date, category, image, status, featured, author_name, created_by, view_count, created_at, updated_at)
VALUES (
    'Annual Sports Day 2025',
    'Join us for the exciting Annual Sports Day on December 15, 2025. Various sports competitions and cultural programs will be held throughout the day. Students from all classes will participate in athletic events including track and field, basketball, football, and relay races. Special performances by our cultural team will add to the festivities. Parents and guardians are cordially invited to attend and cheer for their children. Refreshments will be provided. The event will commence at 8:00 AM at the school ground.',
    '2025-11-16 09:00:00',
    'Event',
    '🏆',
    'PUBLISHED',
    TRUE,
    'Principal School',
    2,
    145,
    NOW(),
    NOW()
);

-- News #2: New Computer Lab (Infrastructure)
INSERT INTO news (title, content, published_date, category, image, status, featured, author_name, created_by, view_count, created_at, updated_at)
VALUES (
    'New Computer Lab Inauguration',
    'Our state-of-the-art computer lab with 50 high-end computers is now operational. Students can now use the latest technology for learning programming, web development, and digital skills. The lab is equipped with high-speed internet, modern software, and comfortable seating. Classes will be conducted for all grades to ensure every student gets hands-on experience with technology. Special computer science workshops and coding classes will also be organized regularly.',
    '2025-11-15 10:30:00',
    'Infrastructure',
    '💻',
    'PUBLISHED',
    TRUE,
    'Admin User',
    1,
    98,
    NOW(),
    NOW()
);

-- News #3: Excellence Awards Ceremony (Achievement)
INSERT INTO news (title, content, published_date, category, image, status, featured, author_name, created_by, view_count, created_at, updated_at)
VALUES (
    'Excellence Awards Ceremony',
    'Annual Excellence Awards ceremony will be held on November 30, 2025 to recognize and honor outstanding students and staff members. Awards will be given in various categories including academic excellence, sports achievements, cultural performances, and community service. The ceremony will feature guest speakers and inspiring stories from our achievers. All parents are invited to witness this proud moment. The event will be held in the school auditorium at 4:00 PM.',
    '2025-11-14 14:00:00',
    'Achievement',
    '🎖️',
    'PUBLISHED',
    FALSE,
    'Principal School',
    2,
    67,
    NOW(),
    NOW()
);

-- News #4: School Foundation Day (Event)
INSERT INTO news (title, content, published_date, category, image, status, featured, author_name, created_by, view_count, created_at, updated_at)
VALUES (
    'School Foundation Day Celebration',
    'Join us on November 25, 2025 to celebrate our school''s 25th anniversary with special programs, performances, and prizes. The celebration will include cultural performances, speeches by distinguished alumni, and a photo exhibition showcasing our school''s journey over the years. Special prizes and awards will be distributed. A grand feast will be organized for all students and staff. The event starts at 10:00 AM and will continue throughout the day.',
    '2025-11-13 08:00:00',
    'Event',
    '🎉',
    'PUBLISHED',
    FALSE,
    'Admin User',
    1,
    89,
    NOW(),
    NOW()
);

-- News #5: NEW - Admissions Open for Academic Year 2026-2027
INSERT INTO news (title, content, published_date, category, image, status, featured, author_name, created_by, view_count, created_at, updated_at)
VALUES (
    'Admissions Open for Academic Year 2026-2027',
    'We are pleased to announce that admissions are now open for the academic year 2026-2027 for classes 1 to 11. Tay Son Secondary and High School invites applications from bright and motivated students who wish to be part of our excellent learning environment. Our admission process is designed to identify students with strong academic potential and a passion for learning.

Key Highlights:
• Classes Available: 1 to 11 (All Streams: Science, Commerce, Arts)
• Admission Test Date: January 15, 2026
• Last Date for Application: January 5, 2026
• Application Mode: Online and Offline both available
• Scholarship opportunities for meritorious students
• Limited seats available - First come, first served basis

Required Documents:
- Previous year mark sheets and certificates
- Birth certificate
- Transfer certificate (if applicable)
- Passport size photographs
- Address proof

For more information and to download the application form, please visit our admission office or contact us at admission@taysonsecondary.edu or call +84 (123) 456-7890. Our admission counselors are available Monday to Friday, 9:00 AM to 5:00 PM.',
    '2025-12-05 09:00:00',
    'Admission',
    '📚',
    'PUBLISHED',
    TRUE,
    'Principal School',
    2,
    234,
    NOW(),
    NOW()
);

-- News #6: NEW - Registration for Extracurricular Classes and Clubs
INSERT INTO news (title, content, published_date, category, image, status, featured, author_name, created_by, view_count, created_at, updated_at)
VALUES (
    'Registration Open for Extracurricular Classes and Clubs',
    'We are excited to announce that registration for extracurricular classes and clubs for the semester January-May 2026 is now open! Join us to explore your talents, develop new skills, and make lasting friendships.

🎭 Cultural Activities:
• Drama and Theatre Club - Learn acting, stagecraft, and performance arts
• Music and Band - Vocal training, instrument lessons (guitar, piano, drums)
• Dance Academy - Classical, Contemporary, and Folk dance forms
• Art and Craft Workshop - Painting, sculpture, and creative arts

⚽ Sports Clubs:
• Football Academy - Professional coaching for all skill levels
• Basketball Team - Practice sessions and inter-school tournaments
• Badminton Club - Court training and competitive matches
• Table Tennis - Advanced techniques and tournament preparation
• Athletics Club - Track and field events training

🧪 Academic Enrichment:
• Science Club - Experiments, robotics, and innovation projects
• Mathematics Olympiad Training - Problem solving and competition prep
• Coding and Programming - Python, Java, Web Development
• Debate and Public Speaking - Communication skills development
• English Literature Circle - Creative writing and book discussions

🎨 Special Interest Groups:
• Photography Club - Camera techniques and photo editing
• Environmental Club - Sustainability projects and nature conservation
• Quiz Club - General knowledge and competitive quizzing
• Chess Academy - Strategic thinking and tournament participation

Registration Details:
• Registration Period: December 5, 2025 - December 20, 2025
• Classes Start: January 10, 2026
• Registration Fee: Varies by activity (₫200,000 - ₫500,000)
• Duration: 5 months (January - May 2026)
• Maximum 2 activities per student

How to Register:
1. Visit the school office or online portal
2. Fill out the registration form
3. Select your preferred activities
4. Submit the form with the registration fee
5. Receive confirmation within 3 working days

Special Offers:
• Early bird discount of 10% if registered before December 15, 2025
• Sibling discount of 15% available
• Merit scholarships for outstanding performers

Limited seats available! Register early to secure your spot. For more information, contact our activities coordinator at activities@taysonsecondary.edu or call extension 245.',
    '2025-12-04 10:00:00',
    'Extracurricular',
    '🎯',
    'PUBLISHED',
    TRUE,
    'Admin User',
    1,
    189,
    NOW(),
    NOW()
);

-- News #7: NEW - Science Exhibition 2026
INSERT INTO news (title, content, published_date, category, image, status, featured, author_name, created_by, view_count, created_at, updated_at)
VALUES (
    'Annual Science Exhibition 2026 - Call for Projects',
    'Students are invited to participate in our Annual Science Exhibition scheduled for February 20-22, 2026. This is a wonderful opportunity to showcase your scientific creativity and innovative thinking. Projects will be evaluated by distinguished scientists and educators. Winners will receive certificates, prizes, and the opportunity to represent our school at inter-school competitions.

Categories: Physics, Chemistry, Biology, Environmental Science, Technology & Innovation
Registration Deadline: January 31, 2026
Prize Pool: ₫10,000,000 in total prizes

Register now and let your ideas shine!',
    '2025-12-03 11:00:00',
    'Event',
    '🔬',
    'PUBLISHED',
    FALSE,
    'Admin User',
    1,
    76,
    NOW(),
    NOW()
);

-- News #8: NEW - Parent-Teacher Meeting Schedule
INSERT INTO news (title, content, published_date, category, image, status, featured, author_name, created_by, view_count, created_at, updated_at)
VALUES (
    'Parent-Teacher Meeting - December 2025',
    'Dear Parents and Guardians, we cordially invite you to attend the Parent-Teacher Meeting scheduled for December 18-19, 2025. This is an important opportunity to discuss your child''s academic progress, attendance, and overall development.

Schedule:
• Classes 1-5: December 18, 2025 (2:00 PM - 5:00 PM)
• Classes 6-10: December 19, 2025 (2:00 PM - 5:00 PM)
• Classes 11-12: December 19, 2025 (9:00 AM - 12:00 PM)

Please bring your child''s progress report card. Individual time slots will be allocated to avoid crowding. Your presence is highly appreciated.',
    '2025-12-02 09:30:00',
    'Meeting',
    '👨‍👩‍👧‍👦',
    'PUBLISHED',
    FALSE,
    'Principal School',
    2,
    112,
    NOW(),
    NOW()
);

-- News #9: DRAFT - Winter Camp Planning
INSERT INTO news (title, content, published_date, category, image, status, featured, author_name, created_by, view_count, created_at, updated_at)
VALUES (
    'Winter Adventure Camp 2026 - Planning Phase',
    'We are planning an exciting Winter Adventure Camp for students during the winter break in January 2026. The camp will include outdoor activities, team building exercises, nature trails, and adventure sports. More details will be announced soon. Stay tuned!',
    '2025-12-10 10:00:00',
    'Event',
    '⛺',
    'DRAFT',
    FALSE,
    'Admin User',
    1,
    0,
    NOW(),
    NOW()
);

-- News #10: NEW - Library New Books Arrival
INSERT INTO news (title, content, published_date, category, image, status, featured, author_name, created_by, view_count, created_at, updated_at)
VALUES (
    'New Books Added to School Library',
    'Great news for all book lovers! Our school library has added over 500 new books across various genres including fiction, non-fiction, science, history, and reference materials. The collection includes bestsellers, award-winning literature, and educational resources to support your learning journey.

Featured Collections:
• Contemporary Fiction - 150 titles
• Science & Technology - 120 titles
• History & Biography - 80 titles
• Educational References - 100 titles
• Comics & Graphic Novels - 50 titles

Visit the library to explore the new collection. Library hours: Monday to Friday, 8:00 AM - 4:00 PM.',
    '2025-12-01 14:00:00',
    'Library',
    '📖',
    'PUBLISHED',
    FALSE,
    'Emily Davis',
    6,
    54,
    NOW(),
    NOW()
);

-- =====================================================
-- 3. VERIFY DATA INSERTION
-- =====================================================

-- Check count of news items
SELECT COUNT(*) as total_news FROM news;

-- Check published news
SELECT COUNT(*) as published_news FROM news WHERE status = 'PUBLISHED';

-- Check featured news
SELECT COUNT(*) as featured_news FROM news WHERE status = 'PUBLISHED' AND featured = TRUE;

-- Display all news titles and categories
SELECT id, title, category, status, featured, view_count, published_date
FROM news
ORDER BY published_date DESC;

-- =====================================================
-- END OF SCRIPT
-- =====================================================

