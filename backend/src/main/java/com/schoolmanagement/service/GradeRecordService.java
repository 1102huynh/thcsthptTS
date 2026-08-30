package com.schoolmanagement.service;

import com.schoolmanagement.dto.GradeRecordDTO;
import com.schoolmanagement.dto.SubjectSemesterAverageDTO;
import com.schoolmanagement.dto.SubjectYearAverageDTO;
import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.entity.GradeComponentConfig;
import com.schoolmanagement.entity.GradeComponentType;
import com.schoolmanagement.entity.GradeRecord;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.entity.SemesterName;
import com.schoolmanagement.entity.Staff;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.Subject;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.AcademicYearRepository;
import com.schoolmanagement.repository.GradeComponentConfigRepository;
import com.schoolmanagement.repository.GradeRecordRepository;
import com.schoolmanagement.repository.SemesterRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.repository.SubjectRepository;
import com.schoolmanagement.security.StudentAccessGuard;
import com.schoolmanagement.util.AcademicYearMatcher;
import com.schoolmanagement.util.EntityResolver;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Điểm trung bình formulas per IMPLEMENTATION_PLAN.md 3.3:
 * - Điểm TB môn học kỳ = Σ(score × weight) / Σ(weight).
 * - Điểm TB môn cả năm = (ĐTB HK1 + ĐTB HK2 × 2) / 3.
 * Xếp loại học lực (classification) is deliberately NOT computed — the
 * TT22/58 score thresholds + môn Toán/Ngữ văn condition need confirmation
 * from someone with education-domain expertise first (see the plan).
 */
@Service
@AllArgsConstructor
@Transactional
public class GradeRecordService {

    private GradeRecordRepository gradeRecordRepository;
    private GradeComponentConfigRepository gradeComponentConfigRepository;
    private StudentRepository studentRepository;
    private SubjectRepository subjectRepository;
    private SemesterRepository semesterRepository;
    private AcademicYearRepository academicYearRepository;
    private StaffRepository staffRepository;
    private StudentAccessGuard studentAccessGuard;
    private AuditLogService auditLogService;

    public GradeRecordDTO createGradeRecord(GradeRecord request) {
        GradeRecord record = GradeRecord.builder()
                .student(resolveStudent(request.getStudent()))
                .subject(resolveSubject(request.getSubject()))
                .semester(resolveSemester(request.getSemester()))
                .componentType(request.getComponentType())
                .score(request.getScore())
                .teacher(resolveTeacher(request.getTeacher()))
                .remarks(request.getRemarks())
                .build();

        return mapToDTO(gradeRecordRepository.save(record));
    }

    public GradeRecordDTO updateGradeRecord(Long id, GradeRecord request, User actor) {
        GradeRecord record = gradeRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade record not found with id: " + id));

        Double previousScore = record.getScore();

        record.setStudent(resolveStudent(request.getStudent()));
        record.setSubject(resolveSubject(request.getSubject()));
        record.setSemester(resolveSemester(request.getSemester()));
        record.setComponentType(request.getComponentType());
        record.setScore(request.getScore());
        record.setTeacher(resolveTeacher(request.getTeacher()));
        record.setRemarks(request.getRemarks());

        GradeRecordDTO result = mapToDTO(gradeRecordRepository.save(record));

        auditLogService.log(actor, "UPDATE", "GradeRecord", id,
                Map.of("previousScore", String.valueOf(previousScore), "newScore", String.valueOf(request.getScore())));

        return result;
    }

    public GradeRecordDTO getGradeRecordById(Long id, User requester) {
        GradeRecord record = gradeRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade record not found with id: " + id));
        studentAccessGuard.enforceCanAccessStudent(record.getStudent().getId(), requester);
        return mapToDTO(record);
    }

    public void deleteGradeRecord(Long id, User actor) {
        GradeRecord record = gradeRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade record not found with id: " + id));
        Long studentId = record.getStudent().getId();
        Double score = record.getScore();

        gradeRecordRepository.delete(record);

        auditLogService.log(actor, "DELETE", "GradeRecord", id,
                Map.of("studentId", String.valueOf(studentId), "score", String.valueOf(score)));
    }

    public List<GradeRecordDTO> getStudentSemesterGrades(Long studentId, Long semesterId, User requester) {
        studentAccessGuard.enforceCanAccessStudent(studentId, requester);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id: " + semesterId));

        return gradeRecordRepository.findByStudentAndSemester(student, semester)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /** Điểm TB môn học kỳ, per subject, for every subject the student has a grade in that semester. */
    public List<SubjectSemesterAverageDTO> getStudentSemesterSummary(Long studentId, Long semesterId, User requester) {
        studentAccessGuard.enforceCanAccessStudent(studentId, requester);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id: " + semesterId));

        List<GradeRecord> records = gradeRecordRepository.findByStudentAndSemester(student, semester);
        String academicYearName = semester.getAcademicYear().getName();
        String semesterLabel = academicYearName + " - " + semester.getName();

        Map<Subject, List<GradeRecord>> bySubject = groupBySubject(records);
        Map<GradeComponentType, Integer> weightCache = new HashMap<>();

        return bySubject.entrySet().stream()
                .map(entry -> SubjectSemesterAverageDTO.builder()
                        .subjectId(entry.getKey().getId())
                        .subjectName(entry.getKey().getName())
                        .semesterId(semester.getId())
                        .semesterLabel(semesterLabel)
                        .average(calculateWeightedAverage(entry.getValue(), academicYearName, weightCache))
                        .classification(null)
                        .build())
                .collect(Collectors.toList());
    }

    /** Điểm TB môn cả năm = (ĐTB HK1 + ĐTB HK2 × 2) / 3, per subject. */
    public List<SubjectYearAverageDTO> getStudentYearSummary(Long studentId, Long academicYearId, User requester) {
        studentAccessGuard.enforceCanAccessStudent(studentId, requester);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        AcademicYear academicYear = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + academicYearId));

        List<Semester> semesters = semesterRepository.findByAcademicYear(academicYear);
        Semester hk1 = semesters.stream().filter(s -> s.getName() == SemesterName.HK1).findFirst().orElse(null);
        Semester hk2 = semesters.stream().filter(s -> s.getName() == SemesterName.HK2).findFirst().orElse(null);

        Map<Subject, List<GradeRecord>> hk1BySubject = hk1 != null
                ? groupBySubject(gradeRecordRepository.findByStudentAndSemester(student, hk1))
                : Map.of();
        Map<Subject, List<GradeRecord>> hk2BySubject = hk2 != null
                ? groupBySubject(gradeRecordRepository.findByStudentAndSemester(student, hk2))
                : Map.of();

        // Both semesters belong to the same academicYear (fetched by id above), so one
        // weight cache — keyed only by componentType — is valid for both halves.
        Map<GradeComponentType, Integer> weightCache = new HashMap<>();

        Map<Subject, Subject> distinctSubjects = new LinkedHashMap<>();
        hk1BySubject.keySet().forEach(s -> distinctSubjects.put(s, s));
        hk2BySubject.keySet().forEach(s -> distinctSubjects.put(s, s));

        return distinctSubjects.keySet().stream()
                .map(subject -> {
                    Double hk1Average = hk1BySubject.containsKey(subject)
                            ? calculateWeightedAverage(hk1BySubject.get(subject), academicYear.getName(), weightCache)
                            : null;
                    Double hk2Average = hk2BySubject.containsKey(subject)
                            ? calculateWeightedAverage(hk2BySubject.get(subject), academicYear.getName(), weightCache)
                            : null;
                    Double yearAverage = (hk1Average != null && hk2Average != null)
                            ? round2((hk1Average + hk2Average * 2) / 3.0)
                            : null;

                    return SubjectYearAverageDTO.builder()
                            .subjectId(subject.getId())
                            .subjectName(subject.getName())
                            .academicYearId(academicYear.getId())
                            .academicYearName(academicYear.getName())
                            .semester1Average(hk1Average)
                            .semester2Average(hk2Average)
                            .yearAverage(yearAverage)
                            .classification(null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Map<Subject, List<GradeRecord>> groupBySubject(List<GradeRecord> records) {
        return records.stream()
                .collect(Collectors.groupingBy(GradeRecord::getSubject, LinkedHashMap::new, Collectors.toList()));
    }

    /** Σ(score × weight) / Σ(weight) over an already-fetched list of records for one subject. */
    private Double calculateWeightedAverage(List<GradeRecord> records, String academicYearName,
                                             Map<GradeComponentType, Integer> weightCache) {
        if (records.isEmpty()) {
            return null;
        }

        double weightedSum = 0;
        double weightSum = 0;
        for (GradeRecord record : records) {
            int weight = weightCache.computeIfAbsent(record.getComponentType(),
                    type -> resolveWeight(type, academicYearName));
            weightedSum += record.getScore() * weight;
            weightSum += weight;
        }

        return weightSum > 0 ? round2(weightedSum / weightSum) : null;
    }

    /** Weight in effect for componentType as of academicYearName — the config with the latest appliesFrom <= academicYearName. */
    private int resolveWeight(GradeComponentType componentType, String academicYearName) {
        int targetYear = AcademicYearMatcher.extractStartYear(academicYearName);

        return gradeComponentConfigRepository.findByComponentType(componentType).stream()
                .filter(config -> AcademicYearMatcher.extractStartYear(config.getAppliesFrom()) <= targetYear)
                .max(Comparator.comparingInt(config -> AcademicYearMatcher.extractStartYear(config.getAppliesFrom())))
                .map(GradeComponentConfig::getWeight)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No grade-component weight configured for " + componentType
                                + " applicable to academic year " + academicYearName
                                + " — set one via POST /v1/grade-config"));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private Student resolveStudent(Student reference) {
        return EntityResolver.resolveOrThrow(studentRepository, reference != null ? reference.getId() : null, "Student");
    }

    private Subject resolveSubject(Subject reference) {
        return EntityResolver.resolveOrThrow(subjectRepository, reference != null ? reference.getId() : null, "Subject");
    }

    private Semester resolveSemester(Semester reference) {
        return EntityResolver.resolveOrThrow(semesterRepository, reference != null ? reference.getId() : null, "Semester");
    }

    private Staff resolveTeacher(Staff reference) {
        return EntityResolver.resolveOrThrow(staffRepository, reference != null ? reference.getId() : null, "Teacher (staff)");
    }

    private GradeRecordDTO mapToDTO(GradeRecord record) {
        Student student = record.getStudent();
        Subject subject = record.getSubject();
        Semester semester = record.getSemester();
        Staff teacher = record.getTeacher();

        return GradeRecordDTO.builder()
                .id(record.getId())
                .studentId(student.getId())
                .studentName(student.getUser() != null
                        ? student.getUser().getFirstName() + " " + student.getUser().getLastName()
                        : null)
                .subjectId(subject.getId())
                .subjectName(subject.getName())
                .semesterId(semester.getId())
                .semesterLabel(semester.getAcademicYear().getName() + " - " + semester.getName())
                .componentType(record.getComponentType())
                .score(record.getScore())
                .teacherId(teacher.getId())
                .teacherName(teacher.getUser() != null
                        ? teacher.getUser().getFirstName() + " " + teacher.getUser().getLastName()
                        : null)
                .remarks(record.getRemarks())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
