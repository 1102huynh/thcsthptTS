package com.schoolmanagement.service;

import com.schoolmanagement.dto.AnalyticsDTO;
import com.schoolmanagement.entity.*;
import com.schoolmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final AttendanceRepository attendanceRepository;
    private final ExamResultRepository examResultRepository;
    private final SchoolClassRepository schoolClassRepository;

    public AnalyticsDTO.StudentPerformanceDTO getStudentPerformance(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Get all grades for the student
        List<Grade> grades = gradeRepository.findByStudentId(studentId);

        // Calculate average grade
        Double averageGrade = grades.stream()
                .mapToDouble(Grade::getMarksObtained)
                .average()
                .orElse(0.0);

        // Get attendance rate
        Double attendanceRate = calculateAttendanceRate(studentId);

        // Get subject averages
        Map<String, Double> subjectAverages = grades.stream()
                .collect(Collectors.groupingBy(
                        Grade::getSubject,
                        Collectors.averagingDouble(Grade::getMarksObtained)
                ));

        // Get recent exam results
        List<AnalyticsDTO.ExamScoreDTO> recentExams = examResultRepository
                .findByStudentIdOrderByExam_ExamDateDesc(studentId).stream()
                .limit(5)
                .map(this::mapToExamScoreDTO)
                .collect(Collectors.toList());

        // Calculate performance trend
        String performanceTrend = calculatePerformanceTrend(grades);

        return AnalyticsDTO.StudentPerformanceDTO.builder()
                .studentId(studentId)
                .studentName(student.getUser().getFirstName() + " " + student.getUser().getLastName())
                .className(student.getSchoolClass() != null ? student.getSchoolClass().getClassName() : "N/A")
                .averageGrade(averageGrade)
                .attendanceRate(attendanceRate)
                .performanceTrend(performanceTrend)
                .subjectAverages(subjectAverages)
                .recentExams(recentExams)
                .build();
    }

    public AnalyticsDTO.ClassAnalyticsDTO getClassAnalytics(Long classId) {
        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new RuntimeException("Class not found"));

        List<Student> students = studentRepository.findBySchoolClassId(classId);

        // Get all grades for the class
        List<Grade> classGrades = students.stream()
                .flatMap(student -> gradeRepository.findByStudentId(student.getId()).stream())
                .collect(Collectors.toList());

        // Calculate class average
        Double averageGrade = classGrades.stream()
                .mapToDouble(Grade::getMarksObtained)
                .average()
                .orElse(0.0);

        // Calculate attendance rate
        Double attendanceRate = students.stream()
                .mapToDouble(student -> calculateAttendanceRate(student.getId()))
                .average()
                .orElse(0.0);

        // Get subject averages
        Map<String, Double> subjectAverages = classGrades.stream()
                .collect(Collectors.groupingBy(
                        Grade::getSubject,
                        Collectors.averagingDouble(Grade::getMarksObtained)
                ));

        // Grade distribution
        Map<String, Integer> gradeDistribution = calculateGradeDistribution(classGrades);

        return AnalyticsDTO.ClassAnalyticsDTO.builder()
                .className(schoolClass.getClassName())
                .totalStudents(students.size())
                .averageGrade(averageGrade)
                .attendanceRate(attendanceRate)
                .subjectAverages(subjectAverages)
                .gradeDistribution(gradeDistribution)
                .build();
    }

    public AnalyticsDTO.AttendanceAnalyticsDTO getAttendanceAnalytics(Long studentId, int months) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);

        List<Attendance> attendanceRecords = attendanceRepository
                .findByStudentIdAndAttendanceDateBetween(studentId, startDate, endDate);

        // Monthly attendance
        Map<String, Integer> monthlyAttendance = new LinkedHashMap<>();
        List<AnalyticsDTO.AttendanceTrendDTO> trends = new ArrayList<>();

        // Group by month
        Map<String, List<Attendance>> monthlyGroups = attendanceRecords.stream()
                .collect(Collectors.groupingBy(
                        att -> att.getAttendanceDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))
                ));

        monthlyGroups.forEach((month, records) -> {
            int present = (int) records.stream()
                    .filter(att -> att.getStatus() == AttendanceStatus.PRESENT)
                    .count();
            int absent = (int) records.stream()
                    .filter(att -> att.getStatus() == AttendanceStatus.ABSENT)
                    .count();

            monthlyAttendance.put(month, present);

            double rate = records.isEmpty() ? 0 : (present * 100.0 / records.size());
            trends.add(AnalyticsDTO.AttendanceTrendDTO.builder()
                    .month(month)
                    .attendanceRate(rate)
                    .present(present)
                    .absent(absent)
                    .build());
        });

        // Overall stats
        int totalPresent = (int) attendanceRecords.stream()
                .filter(att -> att.getStatus() == AttendanceStatus.PRESENT)
                .count();
        int totalAbsent = (int) attendanceRecords.stream()
                .filter(att -> att.getStatus() == AttendanceStatus.ABSENT)
                .count();
        int totalLate = (int) attendanceRecords.stream()
                .filter(att -> att.getStatus() == AttendanceStatus.LATE)
                .count();

        double overallRate = attendanceRecords.isEmpty() ? 0 :
                (totalPresent * 100.0 / attendanceRecords.size());

        return AnalyticsDTO.AttendanceAnalyticsDTO.builder()
                .monthlyAttendance(monthlyAttendance)
                .overallAttendanceRate(overallRate)
                .totalPresent(totalPresent)
                .totalAbsent(totalAbsent)
                .totalLate(totalLate)
                .trends(trends)
                .build();
    }

    public AnalyticsDTO.GradeDistributionDTO getGradeDistribution(Long classId) {
        List<Student> students = studentRepository.findBySchoolClassId(classId);
        List<Grade> grades = students.stream()
                .flatMap(student -> gradeRepository.findByStudentId(student.getId()).stream())
                .collect(Collectors.toList());

        Map<String, Integer> distribution = calculateGradeDistribution(grades);

        // Calculate statistics
        List<Double> marks = grades.stream()
                .map(Grade::getMarksObtained)
                .sorted()
                .collect(Collectors.toList());

        double mean = marks.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double median = calculateMedian(marks);
        double stdDev = calculateStandardDeviation(marks, mean);

        return AnalyticsDTO.GradeDistributionDTO.builder()
                .distribution(distribution)
                .mean(mean)
                .median(median)
                .standardDeviation(stdDev)
                .build();
    }

    public AnalyticsDTO.PerformancePredictionDTO predictPerformance(Long studentId) {
        List<Grade> grades = gradeRepository.findByStudentId(studentId);

        if (grades.size() < 3) {
            return null; // Not enough data for prediction
        }

        // Sort by date
        grades.sort(Comparator.comparing(Grade::getCreatedAt));

        // Get current average
        double currentAverage = grades.stream()
                .mapToDouble(Grade::getMarksObtained)
                .average()
                .orElse(0.0);

        // Simple linear regression for prediction
        List<Double> recentMarks = grades.stream()
                .skip(Math.max(0, grades.size() - 5))
                .map(Grade::getMarksObtained)
                .collect(Collectors.toList());

        double predictedAverage = calculateTrendPrediction(recentMarks);

        // Determine risk level
        String riskLevel;
        if (predictedAverage >= 75) {
            riskLevel = "LOW";
        } else if (predictedAverage >= 60) {
            riskLevel = "MEDIUM";
        } else {
            riskLevel = "HIGH";
        }

        // Generate recommendations
        List<String> recommendations = generateRecommendations(currentAverage, predictedAverage, grades);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return AnalyticsDTO.PerformancePredictionDTO.builder()
                .studentId(studentId)
                .studentName(student.getUser().getFirstName() + " " + student.getUser().getLastName())
                .currentAverage(currentAverage)
                .predictedAverage(predictedAverage)
                .riskLevel(riskLevel)
                .recommendations(recommendations)
                .build();
    }

    // Helper methods
    private Double calculateAttendanceRate(Long studentId) {
        List<Attendance> attendanceList = attendanceRepository.findByStudentId(studentId);
        if (attendanceList.isEmpty()) {
            return 0.0;
        }

        long presentCount = attendanceList.stream()
                .filter(att -> att.getStatus() == AttendanceStatus.PRESENT)
                .count();

        return (presentCount * 100.0) / attendanceList.size();
    }

    private String calculatePerformanceTrend(List<Grade> grades) {
        if (grades.size() < 2) {
            return "STABLE";
        }

        grades.sort(Comparator.comparing(Grade::getCreatedAt));

        int recentCount = Math.min(5, grades.size());
        List<Grade> recentGrades = grades.subList(grades.size() - recentCount, grades.size());

        double recentAvg = recentGrades.stream()
                .mapToDouble(Grade::getMarksObtained)
                .average()
                .orElse(0.0);

        List<Grade> previousGrades = grades.size() > recentCount ?
                grades.subList(Math.max(0, grades.size() - recentCount * 2), grades.size() - recentCount) :
                grades.subList(0, Math.max(1, grades.size() - recentCount));

        double previousAvg = previousGrades.stream()
                .mapToDouble(Grade::getMarksObtained)
                .average()
                .orElse(recentAvg);

        double difference = recentAvg - previousAvg;

        if (difference > 5) {
            return "IMPROVING";
        } else if (difference < -5) {
            return "DECLINING";
        } else {
            return "STABLE";
        }
    }

    private Map<String, Integer> calculateGradeDistribution(List<Grade> grades) {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("A (90-100)", 0);
        distribution.put("B (80-89)", 0);
        distribution.put("C (70-79)", 0);
        distribution.put("D (60-69)", 0);
        distribution.put("F (0-59)", 0);

        grades.forEach(grade -> {
            double marks = grade.getMarksObtained();
            if (marks >= 90) {
                distribution.merge("A (90-100)", 1, Integer::sum);
            } else if (marks >= 80) {
                distribution.merge("B (80-89)", 1, Integer::sum);
            } else if (marks >= 70) {
                distribution.merge("C (70-79)", 1, Integer::sum);
            } else if (marks >= 60) {
                distribution.merge("D (60-69)", 1, Integer::sum);
            } else {
                distribution.merge("F (0-59)", 1, Integer::sum);
            }
        });

        return distribution;
    }

    private double calculateMedian(List<Double> sortedMarks) {
        if (sortedMarks.isEmpty()) {
            return 0.0;
        }

        int size = sortedMarks.size();
        if (size % 2 == 0) {
            return (sortedMarks.get(size / 2 - 1) + sortedMarks.get(size / 2)) / 2.0;
        } else {
            return sortedMarks.get(size / 2);
        }
    }

    private double calculateStandardDeviation(List<Double> marks, double mean) {
        if (marks.size() < 2) {
            return 0.0;
        }

        double variance = marks.stream()
                .mapToDouble(mark -> Math.pow(mark - mean, 2))
                .average()
                .orElse(0.0);

        return Math.sqrt(variance);
    }

    private double calculateTrendPrediction(List<Double> recentMarks) {
        if (recentMarks.size() < 2) {
            return recentMarks.isEmpty() ? 0.0 : recentMarks.get(0);
        }

        // Simple moving average with trend
        double sum = 0;
        double weightedSum = 0;
        double weightSum = 0;

        for (int i = 0; i < recentMarks.size(); i++) {
            double weight = i + 1; // More recent marks have higher weight
            sum += recentMarks.get(i);
            weightedSum += recentMarks.get(i) * weight;
            weightSum += weight;
        }

        double average = sum / recentMarks.size();
        double weightedAverage = weightedSum / weightSum;

        // Predict next value with trend consideration
        double trend = weightedAverage - average;
        return weightedAverage + trend;
    }

    private List<String> generateRecommendations(double current, double predicted, List<Grade> grades) {
        List<String> recommendations = new ArrayList<>();

        if (predicted < current) {
            recommendations.add("Performance showing declining trend - increased attention needed");
            recommendations.add("Schedule parent-teacher meeting to discuss support strategies");
        }

        // Find weak subjects
        Map<String, Double> subjectAvg = grades.stream()
                .collect(Collectors.groupingBy(
                        Grade::getSubject,
                        Collectors.averagingDouble(Grade::getMarksObtained)
                ));

        subjectAvg.entrySet().stream()
                .filter(e -> e.getValue() < 65)
                .forEach(e -> recommendations.add("Focus on improving " + e.getKey() +
                        " (current average: " + String.format("%.1f", e.getValue()) + ")"));

        if (current >= 75) {
            recommendations.add("Maintain good performance through consistent study habits");
        } else {
            recommendations.add("Consider additional tutoring or study groups");
        }

        return recommendations;
    }

    private AnalyticsDTO.ExamScoreDTO mapToExamScoreDTO(ExamResult result) {
        return AnalyticsDTO.ExamScoreDTO.builder()
                .examName(result.getExam().getExamName())
                .subject(result.getExam().getSubject().getSubjectName())
                .score(result.getMarksObtained())
                .maxScore(result.getExam().getTotalMarks().doubleValue())
                .percentage((result.getMarksObtained() / result.getExam().getTotalMarks()) * 100)
                .date(result.getExam().getExamDate().toString())
                .build();
    }
}

