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
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.AcademicYearRepository;
import com.schoolmanagement.repository.GradeComponentConfigRepository;
import com.schoolmanagement.repository.GradeRecordRepository;
import com.schoolmanagement.repository.SemesterRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.repository.SubjectRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

    public GradeRecordDTO updateGradeRecord(Long id, GradeRecord request) {
        GradeRecord record = gradeRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade record not found with id: " + id));

        record.setStudent(resolveStudent(request.getStudent()));
        record.setSubject(resolveSubject(request.getSubject()));
        record.setSemester(resolveSemester(request.getSemester()));
        record.setComponentType(request.getComponentType());
        record.setScore(request.getScore());
        record.setTeacher(resolveTeacher(request.getTeacher()));
        record.setRemarks(request.getRemarks());

        return mapToDTO(gradeRecordRepository.save(record));
    }

    public GradeRecordDTO getGradeRecordById(Long id) {
        return mapToDTO(gradeRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade record not found with id: " + id)));
    }

    public void deleteGradeRecord(Long id) {
        GradeRecord record = gradeRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Grade record not found with id: " + id));
        gradeRecordRepository.delete(record);
    }

    public List<GradeRecordDTO> getStudentSemesterGrades(Long studentId, Long semesterId) {
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
    public List<SubjectSemesterAverageDTO> getStudentSemesterSummary(Long studentId, Long semesterId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        Semester semester = semesterRepository.findById(semesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id: " + semesterId));

        List<GradeRecord> records = gradeRecordRepository.findByStudentAndSemester(student, semester);
        String academicYearName = semester.getAcademicYear().getName();
        String semesterLabel = academicYearName + " - " + semester.getName();

        Set<Subject> subjects = distinctSubjects(records);

        return subjects.stream()
                .map(subject -> SubjectSemesterAverageDTO.builder()
                        .subjectId(subject.getId())
                        .subjectName(subject.getName())
                        .semesterId(semester.getId())
                        .semesterLabel(semesterLabel)
                        .average(calculateSubjectSemesterAverage(student, subject, semester))
                        .classification(null)
                        .build())
                .collect(Collectors.toList());
    }

    /** Điểm TB môn cả năm = (ĐTB HK1 + ĐTB HK2 × 2) / 3, per subject. */
    public List<SubjectYearAverageDTO> getStudentYearSummary(Long studentId, Long academicYearId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        AcademicYear academicYear = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + academicYearId));

        List<Semester> semesters = semesterRepository.findByAcademicYear(academicYear);
        Semester hk1 = semesters.stream().filter(s -> s.getName() == SemesterName.HK1).findFirst().orElse(null);
        Semester hk2 = semesters.stream().filter(s -> s.getName() == SemesterName.HK2).findFirst().orElse(null);

        Set<Subject> subjects = new LinkedHashSet<>();
        if (hk1 != null) {
            subjects.addAll(distinctSubjects(gradeRecordRepository.findByStudentAndSemester(student, hk1)));
        }
        if (hk2 != null) {
            subjects.addAll(distinctSubjects(gradeRecordRepository.findByStudentAndSemester(student, hk2)));
        }

        return subjects.stream()
                .map(subject -> {
                    Double hk1Average = hk1 != null ? calculateSubjectSemesterAverage(student, subject, hk1) : null;
                    Double hk2Average = hk2 != null ? calculateSubjectSemesterAverage(student, subject, hk2) : null;
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

    /** Điểm TB môn học kỳ = Σ(score × weight) / Σ(weight). Null if there are no records. */
    private Double calculateSubjectSemesterAverage(Student student, Subject subject, Semester semester) {
        List<GradeRecord> records = gradeRecordRepository.findByStudentAndSubjectAndSemester(student, subject, semester);
        if (records.isEmpty()) {
            return null;
        }

        String academicYearName = semester.getAcademicYear().getName();
        double weightedSum = 0;
        double weightSum = 0;
        for (GradeRecord record : records) {
            int weight = resolveWeight(record.getComponentType(), academicYearName);
            weightedSum += record.getScore() * weight;
            weightSum += weight;
        }

        return weightSum > 0 ? round2(weightedSum / weightSum) : null;
    }

    /** Weight in effect for componentType as of academicYearName — the config with the latest appliesFrom <= academicYearName. */
    private int resolveWeight(GradeComponentType componentType, String academicYearName) {
        int targetYear = extractStartYear(academicYearName);

        return gradeComponentConfigRepository.findByComponentType(componentType).stream()
                .filter(config -> extractStartYear(config.getAppliesFrom()) <= targetYear)
                .max(Comparator.comparingInt(config -> extractStartYear(config.getAppliesFrom())))
                .map(GradeComponentConfig::getWeight)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No grade-component weight configured for " + componentType
                                + " applicable to academic year " + academicYearName
                                + " — set one via POST /v1/grade-config"));
    }

    private int extractStartYear(String academicYearLabel) {
        try {
            return Integer.parseInt(academicYearLabel.trim().split("-")[0].trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Cannot parse a starting year out of: " + academicYearLabel);
        }
    }

    private Set<Subject> distinctSubjects(List<GradeRecord> records) {
        return records.stream()
                .map(GradeRecord::getSubject)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private Student resolveStudent(Student reference) {
        if (reference == null || reference.getId() == null) {
            throw new ResourceNotFoundException("A student id is required");
        }
        return studentRepository.findById(reference.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + reference.getId()));
    }

    private Subject resolveSubject(Subject reference) {
        if (reference == null || reference.getId() == null) {
            throw new ResourceNotFoundException("A subject id is required");
        }
        return subjectRepository.findById(reference.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject not found with id: " + reference.getId()));
    }

    private Semester resolveSemester(Semester reference) {
        if (reference == null || reference.getId() == null) {
            throw new ResourceNotFoundException("A semester id is required");
        }
        return semesterRepository.findById(reference.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Semester not found with id: " + reference.getId()));
    }

    private Staff resolveTeacher(Staff reference) {
        if (reference == null || reference.getId() == null) {
            throw new ResourceNotFoundException("A teacher (staff) id is required");
        }
        return staffRepository.findById(reference.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with id: " + reference.getId()));
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
