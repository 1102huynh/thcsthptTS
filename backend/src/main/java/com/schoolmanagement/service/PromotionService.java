package com.schoolmanagement.service;

import com.schoolmanagement.dto.PromotionPreviewEntryDTO;
import com.schoolmanagement.dto.PromotionRecordDTO;
import com.schoolmanagement.dto.SubjectYearAverageDTO;
import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.entity.Attendance;
import com.schoolmanagement.entity.AttendanceStatus;
import com.schoolmanagement.entity.ConductRating;
import com.schoolmanagement.entity.ConductRecord;
import com.schoolmanagement.entity.PromotionDecision;
import com.schoolmanagement.entity.PromotionRecord;
import com.schoolmanagement.entity.PromotionThresholdConfig;
import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.entity.SemesterName;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.DuplicateResourceException;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.AcademicYearRepository;
import com.schoolmanagement.repository.AttendanceRepository;
import com.schoolmanagement.repository.ConductRecordRepository;
import com.schoolmanagement.repository.PromotionRecordRepository;
import com.schoolmanagement.repository.PromotionThresholdConfigRepository;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.repository.SemesterRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.security.StudentAccessGuard;
import com.schoolmanagement.security.TeacherHomeroomGuard;
import com.schoolmanagement.util.AcademicYearMatcher;
import com.schoolmanagement.util.EntityResolver;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Xét lên lớp/ở lại/tốt nghiệp per IMPLEMENTATION_PLAN.md 3.5.
 *
 * The preview's suggestedDecision is a configurable APPROXIMATION, not the
 * official TT22/58 xếp loại calculation (that classification is deliberately
 * not implemented yet — see GradeClassification, 3.3). It compares the
 * student's lowest per-subject điểm TB năm (not an invented cross-subject
 * average), HK2 hạnh kiểm, and attendance rate against an ADMIN/PRINCIPAL-
 * configured {@link PromotionThresholdConfig} — no defaults are seeded, so
 * with no config, no suggestion is offered. The final decision is always
 * chosen by a human at confirm time; nothing here is authoritative.
 */
@Service
@AllArgsConstructor
@Transactional
public class PromotionService {

    /** Lớp 9 (cuối THCS) và lớp 12 (cuối THPT) — students who meet thresholds here are suggested TOT_NGHIEP, not LEN_LOP. */
    private static final Set<Integer> GRADUATING_GRADE_LEVELS = Set.of(9, 12);

    private PromotionRecordRepository promotionRecordRepository;
    private PromotionThresholdConfigRepository promotionThresholdConfigRepository;
    private StudentRepository studentRepository;
    private AcademicYearRepository academicYearRepository;
    private SchoolClassRepository schoolClassRepository;
    private SemesterRepository semesterRepository;
    private ConductRecordRepository conductRecordRepository;
    private AttendanceRepository attendanceRepository;
    private StaffRepository staffRepository;
    private GradeRecordService gradeRecordService;
    private StudentAccessGuard studentAccessGuard;
    private TeacherHomeroomGuard teacherHomeroomGuard;

    /**
     * Bảng xét lên lớp cho cả lớp — computed live, nothing here is saved yet.
     * Does one grade/conduct/attendance lookup per roster student (no
     * findByStudentIn-style batching) — acceptable for a class-sized roster
     * (tens of students), but would need batching to scale to a whole-school
     * report across many classes at once.
     */
    public List<PromotionPreviewEntryDTO> previewClassPromotions(Long classId, Long academicYearId, User requester) {
        teacherHomeroomGuard.enforceHomeroomClassId(classId, requester);
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));
        AcademicYear academicYear = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + academicYearId));

        // The same className/section can exist across multiple academic years
        // (SchoolClass's own unique constraint is on all three together) - without
        // this check, a mismatched classId/academicYearId pair would silently mix
        // one year's roster with another year's grading/attendance window.
        if (!academicYear.getName().equals(schoolClass.getAcademicYear())) {
            throw new IllegalArgumentException("Class " + classId + " (" + schoolClass.getClassName() + "-"
                    + schoolClass.getSection() + ", năm học " + schoolClass.getAcademicYear()
                    + ") does not belong to academic year " + academicYear.getName());
        }

        List<Student> roster = studentRepository.findByClassNameAndSection(
                schoolClass.getClassName(), schoolClass.getSection());
        Semester hk2 = resolveHk2(academicYear);
        PromotionThresholdConfig config = resolveThresholdConfig(academicYear.getName()).orElse(null);

        return roster.stream()
                .map(student -> buildPreviewEntry(student, academicYear, hk2, schoolClass.getGradeLevel(), config))
                .collect(Collectors.toList());
    }

    public List<PromotionRecordDTO> confirmPromotions(List<PromotionRecord> requests) {
        return requests.stream().map(this::confirmOne).collect(Collectors.toList());
    }

    public List<PromotionRecordDTO> getStudentPromotionHistory(Long studentId, User requester) {
        studentAccessGuard.enforceCanAccessStudent(studentId, requester);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        return promotionRecordRepository.findByStudent(student)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private PromotionRecordDTO confirmOne(PromotionRecord request) {
        Student student = resolveStudent(request.getStudent());
        AcademicYear academicYear = resolveAcademicYear(request.getAcademicYear());
        // Unlike ConductRecordService's evaluatedBy (pinned to the caller's own
        // staff profile for TEACHER writes, since /v1/conduct is open to any
        // TEACHER), decidedBy here is accepted as given: /v1/promotions/confirm
        // is already ADMIN/PRINCIPAL-only, and a xét lên lớp decision is
        // recorded "on behalf of" a Hội đồng — the ADMIN/PRINCIPAL confirming
        // it often isn't the staff member of record themselves (and ADMIN
        // accounts aren't required to have a Staff profile at all).
        Staff decidedBy = resolveStaff(request.getDecidedBy());
        if (request.getDecision() == null) {
            throw new IllegalArgumentException("decision is required");
        }

        Double lowestSubjectAverage = computeLowestSubjectAverage(student, academicYear);
        Semester hk2 = resolveHk2(academicYear);
        ConductRating conduct = resolveConduct(student, hk2);
        Double attendanceRate = computeAttendanceRate(student, academicYear);

        // Ghi đè hàng loạt: confirming again for the same (student, academicYear)
        // updates the existing decision rather than erroring.
        PromotionRecord record = promotionRecordRepository
                .findByStudentAndAcademicYear(student, academicYear)
                .orElseGet(PromotionRecord::new);

        record.setStudent(student);
        record.setAcademicYear(academicYear);
        record.setLowestSubjectAverageSnapshot(lowestSubjectAverage);
        record.setConductSnapshot(conduct);
        record.setAttendanceRateSnapshot(attendanceRate);
        record.setDecision(request.getDecision());
        record.setDecisionDate(LocalDate.now());
        record.setDecidedBy(decidedBy);
        record.setRemarks(request.getRemarks());

        try {
            return mapToDTO(promotionRecordRepository.save(record));
        } catch (DataIntegrityViolationException ex) {
            // Two concurrent confirms for the same brand-new (student, year) pair
            // can both miss the findByStudentAndAcademicYear lookup above before
            // either commits; surface that race as 409, not a masked 500.
            throw new DuplicateResourceException(
                    "A promotion decision for this student and academic year was just confirmed by another request — retry");
        }
    }

    private PromotionPreviewEntryDTO buildPreviewEntry(Student student, AcademicYear academicYear, Semester hk2,
                                                         Integer gradeLevel, PromotionThresholdConfig config) {
        Double lowestSubjectAverage = computeLowestSubjectAverage(student, academicYear);
        ConductRating conduct = resolveConduct(student, hk2);
        Double attendanceRate = computeAttendanceRate(student, academicYear);

        List<String> reasons = new ArrayList<>();
        Boolean meetsThresholds = null;
        PromotionDecision suggestedDecision = null;

        if (config == null) {
            reasons.add("Chưa cấu hình ngưỡng xét lên lớp cho năm học này — xem POST /v1/promotion-thresholds");
        } else {
            meetsThresholds = true;

            if (lowestSubjectAverage == null) {
                meetsThresholds = false;
                reasons.add("Chưa có điểm môn nào để xét");
            } else if (lowestSubjectAverage < config.getMinSubjectAverage()) {
                meetsThresholds = false;
                reasons.add("Có môn điểm TB năm (" + lowestSubjectAverage + ") dưới ngưỡng " + config.getMinSubjectAverage());
            }

            if (conduct == null) {
                meetsThresholds = false;
                reasons.add("Chưa có đánh giá hạnh kiểm học kỳ 2");
            } else if (conduct.ordinal() > config.getMinConduct().ordinal()) {
                meetsThresholds = false;
                reasons.add("Hạnh kiểm (" + conduct + ") dưới ngưỡng " + config.getMinConduct());
            }

            if (attendanceRate == null) {
                meetsThresholds = false;
                reasons.add("Chưa có dữ liệu điểm danh");
            } else if ((100.0 - attendanceRate) > config.getMaxAbsenceRate()) {
                meetsThresholds = false;
                reasons.add(String.format("Tỷ lệ nghỉ (%.1f%%) vượt ngưỡng %.1f%%",
                        100.0 - attendanceRate, config.getMaxAbsenceRate()));
            }

            if (meetsThresholds) {
                suggestedDecision = (gradeLevel != null && GRADUATING_GRADE_LEVELS.contains(gradeLevel))
                        ? PromotionDecision.TOT_NGHIEP
                        : PromotionDecision.LEN_LOP;
            } else {
                suggestedDecision = PromotionDecision.O_LAI;
            }
        }

        return PromotionPreviewEntryDTO.builder()
                .studentId(student.getId())
                .studentName(studentName(student))
                .rollNumber(student.getRollNumber())
                .lowestSubjectAverage(lowestSubjectAverage)
                .conduct(conduct)
                .attendanceRate(attendanceRate)
                .meetsThresholds(meetsThresholds)
                .suggestedDecision(suggestedDecision)
                .reasons(reasons)
                .build();
    }

    /** The lowest of the student's per-subject điểm TB năm — null if there are none. */
    private Double computeLowestSubjectAverage(Student student, AcademicYear academicYear) {
        // requester=null: this is an internal, already-authorized call (the
        // controller's own @PreAuthorize already restricted the caller to
        // ADMIN/PRINCIPAL/TEACHER) — GradeRecordService's own STUDENT-only
        // ownership check is a no-op for anyone else, so null is safe here.
        List<SubjectYearAverageDTO> yearSummary =
                gradeRecordService.getStudentYearSummary(student.getId(), academicYear.getId(), null);

        return yearSummary.stream()
                .map(SubjectYearAverageDTO::getYearAverage)
                .filter(Objects::nonNull)
                .min(Double::compareTo)
                .orElse(null);
    }

    private ConductRating resolveConduct(Student student, Semester hk2) {
        if (hk2 == null) {
            return null;
        }
        return conductRecordRepository.findByStudentAndSemester(student, hk2)
                .map(ConductRecord::getRating)
                .orElse(null);
    }

    /** % of recorded school days NOT marked ABSENT, for the year — null if nothing was recorded. */
    private Double computeAttendanceRate(Student student, AcademicYear academicYear) {
        List<Attendance> records = attendanceRepository.findByStudentAndAttendanceDateBetween(
                student, academicYear.getStartDate(), academicYear.getEndDate());
        if (records.isEmpty()) {
            return null;
        }
        long absentCount = records.stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count();
        return round2(100.0 * (records.size() - absentCount) / records.size());
    }

    private Semester resolveHk2(AcademicYear academicYear) {
        return semesterRepository.findByAcademicYear(academicYear).stream()
                .filter(s -> s.getName() == SemesterName.HK2)
                .findFirst()
                .orElse(null);
    }

    /** The threshold config with the latest appliesFrom <= academicYearName, if any. */
    private Optional<PromotionThresholdConfig> resolveThresholdConfig(String academicYearName) {
        int targetYear = AcademicYearMatcher.extractStartYear(academicYearName);
        return promotionThresholdConfigRepository.findAll().stream()
                .filter(config -> AcademicYearMatcher.extractStartYear(config.getAppliesFrom()) <= targetYear)
                .max(Comparator.comparingInt(config -> AcademicYearMatcher.extractStartYear(config.getAppliesFrom())));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private Student resolveStudent(Student reference) {
        return EntityResolver.resolveOrThrow(studentRepository, reference != null ? reference.getId() : null, "Student");
    }

    private AcademicYear resolveAcademicYear(AcademicYear reference) {
        return EntityResolver.resolveOrThrow(academicYearRepository, reference != null ? reference.getId() : null, "Academic year");
    }

    private Staff resolveStaff(Staff reference) {
        return EntityResolver.resolveOrThrow(staffRepository, reference != null ? reference.getId() : null, "Staff (decidedBy)");
    }

    private String studentName(Student student) {
        return student.getUser() != null ? student.getUser().getFirstName() + " " + student.getUser().getLastName() : null;
    }

    private String staffName(Staff staff) {
        return staff.getUser() != null ? staff.getUser().getFirstName() + " " + staff.getUser().getLastName() : null;
    }

    private PromotionRecordDTO mapToDTO(PromotionRecord record) {
        Student student = record.getStudent();
        AcademicYear academicYear = record.getAcademicYear();
        Staff decidedBy = record.getDecidedBy();

        return PromotionRecordDTO.builder()
                .id(record.getId())
                .studentId(student.getId())
                .studentName(studentName(student))
                .academicYearId(academicYear.getId())
                .academicYearName(academicYear.getName())
                .lowestSubjectAverageSnapshot(record.getLowestSubjectAverageSnapshot())
                .conductSnapshot(record.getConductSnapshot())
                .attendanceRateSnapshot(record.getAttendanceRateSnapshot())
                .decision(record.getDecision())
                .decisionDate(record.getDecisionDate())
                .decidedById(decidedBy.getId())
                .decidedByName(staffName(decidedBy))
                .remarks(record.getRemarks())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
