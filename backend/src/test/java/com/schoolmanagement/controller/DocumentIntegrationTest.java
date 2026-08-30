package com.schoolmanagement.controller;

import com.schoolmanagement.entity.DocumentOwnerType;
import com.schoolmanagement.entity.EmploymentStatus;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.StaffPosition;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.StudentStatus;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.entity.DocumentAttachment;
import com.schoolmanagement.repository.DocumentAttachmentRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for /v1/documents — real Spring context + local MySQL via
 * the "test" profile. FileStorageService writes real files under
 * app.uploads.dir (overridden to ./uploads/test for the whole test profile
 * so this never touches real dev uploads) - deleted per test in @AfterEach-
 * style cleanup isn't needed since DocumentService.delete() removes the file
 * for the tests that call it, and @Transactional rollback handles the DB
 * row either way; any file left behind by a non-delete test is orphaned but
 * harmless test-directory clutter, not a correctness issue.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DocumentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private StaffRepository staffRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DocumentAttachmentRepository documentAttachmentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Value("${app.uploads.dir}")
    private String uploadsDir;

    private Student student;
    private Student otherStudent;
    private Staff staff;
    private User studentUser;
    private User otherStudentUser;
    private User adminUser;
    private User teacherUser;

    @BeforeEach
    void setUp() {
        adminUser = userRepository.save(User.builder()
                .username("itest.doc.admin").email("itest.doc.admin@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Admin").role(Role.ADMIN).enabled(true).build());
        teacherUser = userRepository.save(User.builder()
                .username("itest.doc.teacher").email("itest.doc.teacher@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Teacher").role(Role.TEACHER).enabled(true).build());
        staff = staffRepository.save(Staff.builder()
                .employeeId("ITEST-DOC-EMP").user(teacherUser)
                .position(StaffPosition.TEACHER).status(EmploymentStatus.ACTIVE).build());

        studentUser = userRepository.save(User.builder()
                .username("itest.doc.student").email("itest.doc.student@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("Student").role(Role.STUDENT).enabled(true).build());
        student = studentRepository.save(Student.builder()
                .rollNumber("ITEST-DOC-ROLL").admissionNumber("ITEST-DOC-ADM")
                .user(studentUser).status(StudentStatus.ACTIVE).build());

        otherStudentUser = userRepository.save(User.builder()
                .username("itest.doc.student2").email("itest.doc.student2@school.com")
                .password(passwordEncoder.encode("Str0ngPassw0rd!"))
                .firstName("Integration").lastName("StudentTwo").role(Role.STUDENT).enabled(true).build());
        otherStudent = studentRepository.save(Student.builder()
                .rollNumber("ITEST-DOC-ROLL-2").admissionNumber("ITEST-DOC-ADM-2")
                .user(otherStudentUser).status(StudentStatus.ACTIVE).build());
    }

    private RequestPostProcessor asUser(User user, String role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    private MockMultipartFile pdfFile() {
        return new MockMultipartFile("file", "hoso.pdf", "application/pdf", "fake pdf content".getBytes());
    }

    @Test
    void upload_asAdminForStudent_succeeds() throws Exception {
        mockMvc.perform(multipart("/v1/documents")
                        .file(pdfFile())
                        .param("ownerType", "STUDENT")
                        .param("ownerId", student.getId().toString())
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("hoso.pdf"))
                .andExpect(jsonPath("$.fileType").value("pdf"))
                .andExpect(jsonPath("$.downloadUrl").exists());
    }

    @Test
    void upload_invalidExtension_returns400() throws Exception {
        MockMultipartFile exeFile = new MockMultipartFile("file", "virus.exe", "application/octet-stream", "x".getBytes());
        mockMvc.perform(multipart("/v1/documents")
                        .file(exeFile)
                        .param("ownerType", "STUDENT")
                        .param("ownerId", student.getId().toString())
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upload_nonexistentStudentOwner_returns404() throws Exception {
        mockMvc.perform(multipart("/v1/documents")
                        .file(pdfFile())
                        .param("ownerType", "STUDENT")
                        .param("ownerId", "9999999")
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void upload_asOwningStudent_succeeds() throws Exception {
        mockMvc.perform(multipart("/v1/documents")
                        .file(pdfFile())
                        .param("ownerType", "STUDENT")
                        .param("ownerId", student.getId().toString())
                        .with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isCreated());
    }

    @Test
    void upload_asDifferentStudent_returns403() throws Exception {
        mockMvc.perform(multipart("/v1/documents")
                        .file(pdfFile())
                        .param("ownerType", "STUDENT")
                        .param("ownerId", student.getId().toString())
                        .with(asUser(otherStudentUser, "STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void upload_staffOwnerAsTeacher_returns403() throws Exception {
        // STAFF-owned documents are ADMIN/PRINCIPAL only - even the staff
        // member's own TEACHER account can't upload against their own record.
        mockMvc.perform(multipart("/v1/documents")
                        .file(pdfFile())
                        .param("ownerType", "STAFF")
                        .param("ownerId", staff.getId().toString())
                        .with(asUser(teacherUser, "TEACHER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void upload_staffOwnerAsAdmin_succeeds() throws Exception {
        mockMvc.perform(multipart("/v1/documents")
                        .file(pdfFile())
                        .param("ownerType", "STAFF")
                        .param("ownerId", staff.getId().toString())
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isCreated());
    }

    @Test
    void listByOwner_asOwningStudent_returnsUploaded() throws Exception {
        mockMvc.perform(multipart("/v1/documents")
                        .file(pdfFile())
                        .param("ownerType", "STUDENT")
                        .param("ownerId", student.getId().toString())
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/v1/documents")
                        .param("ownerType", "STUDENT")
                        .param("ownerId", student.getId().toString())
                        .with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fileName").value("hoso.pdf"));
    }

    @Test
    void listByOwner_asDifferentStudent_returns403() throws Exception {
        mockMvc.perform(get("/v1/documents")
                        .param("ownerType", "STUDENT")
                        .param("ownerId", student.getId().toString())
                        .with(asUser(otherStudentUser, "STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void downloadAndDelete_roundTrip() throws Exception {
        String body = mockMvc.perform(multipart("/v1/documents")
                        .file(pdfFile())
                        .param("ownerType", "STUDENT")
                        .param("ownerId", student.getId().toString())
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = Long.valueOf(body.replaceAll(".*\"id\":(\\d+).*", "$1"));

        byte[] downloaded = mockMvc.perform(get("/v1/documents/{id}/download", id)
                        .with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
        Assertions.assertArrayEquals("fake pdf content".getBytes(), downloaded);

        // STUDENT may read/download but not delete - only ADMIN/PRINCIPAL can.
        mockMvc.perform(delete("/v1/documents/{id}", id).with(asUser(studentUser, "STUDENT")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/v1/documents/{id}", id).with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isNoContent());

        Assertions.assertTrue(documentAttachmentRepository.findById(id).isEmpty());
    }

    @Test
    void deleteStudent_alsoDeletesTheirDocumentAttachmentsAndFiles() throws Exception {
        // document_attachments.owner_id has no FK to students (it's
        // polymorphic - see DocumentOwnerType), so nothing at the DB level
        // would catch an orphaned row/file left behind by a naive student
        // delete. Regression test for that self-review finding.
        String body = mockMvc.perform(multipart("/v1/documents")
                        .file(pdfFile())
                        .param("ownerType", "STUDENT")
                        .param("ownerId", student.getId().toString())
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long docId = Long.valueOf(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
        DocumentAttachment attachment = documentAttachmentRepository.findByIdWithUploadedBy(docId).orElseThrow();
        Path storedFile = Paths.get(uploadsDir).resolve(attachment.getStoredFileName());
        Assertions.assertTrue(Files.exists(storedFile), "precondition: the uploaded file must exist on disk");

        mockMvc.perform(delete("/v1/students/{id}", student.getId()).with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isNoContent());

        Assertions.assertTrue(documentAttachmentRepository.findById(docId).isEmpty(),
                "the DocumentAttachment row must be gone, not left orphaned");
        Assertions.assertFalse(Files.exists(storedFile), "the physical file must be deleted too, not just the DB row");
    }

    @Test
    void download_asDifferentStudent_returns403() throws Exception {
        String body = mockMvc.perform(multipart("/v1/documents")
                        .file(pdfFile())
                        .param("ownerType", "STUDENT")
                        .param("ownerId", student.getId().toString())
                        .with(asUser(adminUser, "ADMIN")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long id = Long.valueOf(body.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mockMvc.perform(get("/v1/documents/{id}/download", id).with(asUser(otherStudentUser, "STUDENT")))
                .andExpect(status().isForbidden());
    }
}
