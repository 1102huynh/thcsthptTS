package com.schoolmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.entity.AcademicYearStatus;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.entity.SemesterName;
import com.schoolmanagement.entity.Subject;
import com.schoolmanagement.entity.SubjectCategory;
import com.schoolmanagement.repository.AcademicYearRepository;
import com.schoolmanagement.repository.SemesterRepository;
import com.schoolmanagement.repository.SubjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for /v1/academic-years, /v1/semesters, /v1/subjects —
 * real Spring context + local MySQL via the "test" profile. Each test rolls
 * back (@Transactional), so it never depends on — or pollutes — the
 * migration-backfilled 2024-2025 academic year / HK1 / subjects.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AcademicStructureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AcademicYearRepository academicYearRepository;
    @Autowired
    private SemesterRepository semesterRepository;
    @Autowired
    private SubjectRepository subjectRepository;

    private AcademicYear newYear(String name) {
        return AcademicYear.builder()
                .name(name)
                .startDate(LocalDate.of(2030, 9, 1))
                .endDate(LocalDate.of(2031, 5, 31))
                .status(AcademicYearStatus.ACTIVE)
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAcademicYear_persistsAndReturnsIt() throws Exception {
        mockMvc.perform(post("/v1/academic-years")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newYear("ITEST-2030-2031"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("ITEST-2030-2031"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAcademicYear_duplicateName_returns409() throws Exception {
        academicYearRepository.save(newYear("ITEST-DUP"));

        mockMvc.perform(post("/v1/academic-years")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newYear("ITEST-DUP"))))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAcademicYear_endDateBeforeStartDate_returns400() throws Exception {
        AcademicYear invalid = AcademicYear.builder()
                .name("ITEST-BAD-RANGE")
                .startDate(LocalDate.of(2030, 9, 1))
                .endDate(LocalDate.of(2020, 5, 31))
                .build();

        mockMvc.perform(post("/v1/academic-years")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void createAcademicYear_asTeacher_returns403() throws Exception {
        mockMvc.perform(post("/v1/academic-years")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newYear("ITEST-RBAC"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void closeAcademicYear_setsStatusClosed() throws Exception {
        AcademicYear saved = academicYearRepository.save(newYear("ITEST-CLOSE"));

        mockMvc.perform(put("/v1/academic-years/{id}/close", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAcademicYear_withSemesters_returns409() throws Exception {
        AcademicYear saved = academicYearRepository.save(newYear("ITEST-INUSE"));
        semesterRepository.save(Semester.builder()
                .academicYear(saved)
                .name(SemesterName.HK1)
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .build());

        mockMvc.perform(delete("/v1/academic-years/{id}", saved.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAcademicYear_empty_returns204() throws Exception {
        AcademicYear saved = academicYearRepository.save(newYear("ITEST-EMPTY"));

        mockMvc.perform(delete("/v1/academic-years/{id}", saved.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "ACCOUNTANT")
    void getAcademicYears_asAccountant_returns200() throws Exception {
        // Regression test: FeeManagement.jsx's fee list is gated on this call
        // succeeding (`enabled: Boolean(academicYear)`, derived from GET
        // /v1/academic-years) - found live that it 403'd for ACCOUNTANT,
        // silently zeroing out the entire fee list with no error shown, even
        // though ACCOUNTANT could already read GET /v1/fees/year/{year} fine.
        mockMvc.perform(get("/v1/academic-years"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ACCOUNTANT")
    void createAcademicYear_asAccountant_returns403() throws Exception {
        // Read-only for ACCOUNTANT - the write endpoints stay ADMIN/PRINCIPAL.
        mockMvc.perform(post("/v1/academic-years")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newYear("ITEST-ACCOUNTANT-DENIED"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSemester_forExistingAcademicYear_persists() throws Exception {
        AcademicYear saved = academicYearRepository.save(newYear("ITEST-SEM"));
        Semester semester = Semester.builder()
                .academicYear(AcademicYear.builder().id(saved.getId()).build())
                .name(SemesterName.HK1)
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .build();

        mockMvc.perform(post("/v1/semesters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(semester)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.academicYearId").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("HK1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSemester_duplicateNameForSameYear_returns409() throws Exception {
        AcademicYear saved = academicYearRepository.save(newYear("ITEST-SEM-DUP"));
        semesterRepository.save(Semester.builder()
                .academicYear(saved)
                .name(SemesterName.HK1)
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .build());

        Semester duplicate = Semester.builder()
                .academicYear(AcademicYear.builder().id(saved.getId()).build())
                .name(SemesterName.HK1)
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .build();

        mockMvc.perform(post("/v1/semesters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSubject_persistsAndReturnsIt() throws Exception {
        Subject subject = Subject.builder()
                .code("ITEST-LY")
                .name("Vật lý")
                .gradeLevels("10,11,12")
                .category(SubjectCategory.BAT_BUOC)
                .build();

        mockMvc.perform(post("/v1/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subject)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("ITEST-LY"))
                .andExpect(jsonPath("$.name").value("Vật lý"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createSubject_duplicateCode_returns409() throws Exception {
        subjectRepository.save(Subject.builder()
                .code("ITEST-HOA")
                .name("Hoá học")
                .category(SubjectCategory.BAT_BUOC)
                .build());

        Subject duplicate = Subject.builder()
                .code("ITEST-HOA")
                .name("Hoá học (khác)")
                .category(SubjectCategory.TU_CHON)
                .build();

        mockMvc.perform(post("/v1/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }
}
