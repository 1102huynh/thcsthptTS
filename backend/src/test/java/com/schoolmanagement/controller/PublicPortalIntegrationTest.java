package com.schoolmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.entity.ContentStatus;
import com.schoolmanagement.entity.NewsArticle;
import com.schoolmanagement.entity.NewsCategory;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.SchoolEvent;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.NewsArticleRepository;
import com.schoolmanagement.repository.NewsCategoryRepository;
import com.schoolmanagement.repository.SchoolEventRepository;
import com.schoolmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for the public portal module
 * (KE_HOACH_TRANG_TIN_TUC_CONG_KHAI.md): the public API only ever exposes
 * PUBLISHED content, the CMS write endpoints require ADMIN/PRINCIPAL, and
 * authored HTML is sanitized before it reaches a reader.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PublicPortalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private NewsCategoryRepository categoryRepository;
    @Autowired
    private NewsArticleRepository articleRepository;
    @Autowired
    private SchoolEventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private User admin;
    private User teacher;
    private NewsCategory category;

    @BeforeEach
    void setUp() {
        admin = userRepository.save(User.builder()
                .username("itest.portal.admin").email("itest.portal.admin@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Portal").lastName("Admin").role(Role.ADMIN).enabled(true).build());
        teacher = userRepository.save(User.builder()
                .username("itest.portal.teacher").email("itest.portal.teacher@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Portal").lastName("Teacher").role(Role.TEACHER).enabled(true).build());
        category = categoryRepository.save(NewsCategory.builder()
                .name("Tuyển sinh ITEST").slug("tuyen-sinh-itest").displayOrder(1).build());
    }

    private RequestPostProcessor asUser(User user, String role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    private NewsArticle savePublished(String title, String slug) {
        return articleRepository.save(NewsArticle.builder()
                .title(title).slug(slug).summary("tóm tắt").content("<p>nội dung</p>")
                .category(category).status(ContentStatus.PUBLISHED)
                .publishedAt(LocalDateTime.now().minusHours(1)).author(admin).build());
    }

    // ---- public: only published content leaves the CMS ----

    @Test
    void publicNews_listsPublishedNotDraft() throws Exception {
        savePublished("Bài đã đăng", "itest-bai-da-dang");
        articleRepository.save(NewsArticle.builder()
                .title("Bài nháp").slug("itest-bai-nhap").status(ContentStatus.DRAFT).author(admin).build());

        mockMvc.perform(get("/v1/public/news"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Total-Count"))
                .andExpect(jsonPath("$[?(@.slug == 'itest-bai-da-dang')]").exists())
                .andExpect(jsonPath("$[?(@.slug == 'itest-bai-nhap')]").doesNotExist());
    }

    @Test
    void publicNews_scheduledInFuture_stillHidden() throws Exception {
        articleRepository.save(NewsArticle.builder()
                .title("Hẹn giờ").slug("itest-hen-gio").status(ContentStatus.PUBLISHED)
                .publishedAt(LocalDateTime.now().plusDays(1)).author(admin).build());

        mockMvc.perform(get("/v1/public/news/{slug}", "itest-hen-gio"))
                .andExpect(status().isNotFound());
    }

    @Test
    void publicNewsDetail_incrementsViewCount() throws Exception {
        savePublished("Chi tiết", "itest-chi-tiet");

        mockMvc.perform(get("/v1/public/news/{slug}", "itest-chi-tiet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("<p>nội dung</p>"));
        mockMvc.perform(get("/v1/public/news/{slug}", "itest-chi-tiet")).andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertTrue(
                articleRepository.findBySlug("itest-chi-tiet").orElseThrow().getViewCount() >= 2);
    }

    @Test
    void publicEvents_listsPublishedOnly() throws Exception {
        eventRepository.save(SchoolEvent.builder()
                .title("Lễ khai giảng").slug("itest-khai-giang").status(ContentStatus.PUBLISHED)
                .publishedAt(LocalDateTime.now().minusHours(1)).startAt(LocalDateTime.now().plusDays(3)).build());
        eventRepository.save(SchoolEvent.builder()
                .title("Nháp sự kiện").slug("itest-nhap-sk").status(ContentStatus.DRAFT)
                .startAt(LocalDateTime.now().plusDays(3)).build());

        mockMvc.perform(get("/v1/public/events").param("when", "upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.slug == 'itest-khai-giang')]").exists())
                .andExpect(jsonPath("$[?(@.slug == 'itest-nhap-sk')]").doesNotExist());
    }

    // ---- CMS: auth + sanitize ----

    @Test
    void createNews_asTeacher_returns403() throws Exception {
        mockMvc.perform(post("/v1/news")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("title", "x")))
                        .with(asUser(teacher, "TEACHER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void createThenPublishNews_asAdmin_sanitizesContentAndGoesLive() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "title", "Thông báo có script",
                "summary", "tóm tắt",
                "content", "<p>An toàn</p><script>alert('xss')</script><a href=\"javascript:evil()\">x</a>",
                "categoryId", category.getId()));

        String created = mockMvc.perform(post("/v1/news")
                        .contentType(MediaType.APPLICATION_JSON).content(body)
                        .with(asUser(admin, "ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.slug").value("thong-bao-co-script"))
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("<script"))))
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("javascript:"))))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        // not visible publicly while DRAFT
        mockMvc.perform(get("/v1/public/news/{slug}", "thong-bao-co-script")).andExpect(status().isNotFound());

        mockMvc.perform(put("/v1/news/{id}/publish", id).with(asUser(admin, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        mockMvc.perform(get("/v1/public/news/{slug}", "thong-bao-co-script"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", org.hamcrest.Matchers.containsString("An toàn")));
    }

    @Test
    void cmsNewsList_includesDrafts_forAdmin() throws Exception {
        articleRepository.save(NewsArticle.builder()
                .title("Nháp cho CMS").slug("itest-nhap-cms").status(ContentStatus.DRAFT).author(admin).build());

        mockMvc.perform(get("/v1/news").with(asUser(admin, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.slug == 'itest-nhap-cms')]").exists());
    }

    // ---- contact form ----

    @Test
    void contactForm_submitsPublicly_andAdminCanList() throws Exception {
        mockMvc.perform(post("/v1/public/contact")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "fullName", "Phụ Huynh A", "email", "ph@example.com",
                                "message", "Cho hỏi lịch tuyển sinh?"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/contact-messages").with(asUser(admin, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.fullName == 'Phụ Huynh A')]").exists());
    }

    @Test
    void publicHome_returnsAggregate() throws Exception {
        savePublished("Tin nổi bật", "itest-noi-bat");
        articleRepository.findBySlug("itest-noi-bat").ifPresent(a -> {
            a.setIsFeatured(true);
            articleRepository.save(a);
        });

        mockMvc.perform(get("/v1/public/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.latestNews").isArray())
                .andExpect(jsonPath("$.featuredNews[?(@.slug == 'itest-noi-bat')]").exists());
    }
}
