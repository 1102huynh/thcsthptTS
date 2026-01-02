package com.schoolmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public class AnalyticsDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StudentPerformanceDTO {
        private Long studentId;
        private String studentName;
        private String className;
        private Double averageGrade;
        private Double attendanceRate;
        private String performanceTrend; // IMPROVING, DECLINING, STABLE
        private Map<String, Double> subjectAverages;
        private List<ExamScoreDTO> recentExams;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExamScoreDTO {
        private String examName;
        private String subject;
        private Double score;
        private Double maxScore;
        private Double percentage;
        private String date;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClassAnalyticsDTO {
        private String className;
        private Integer totalStudents;
        private Double averageGrade;
        private Double attendanceRate;
        private Map<String, Double> subjectAverages;
        private Map<String, Integer> gradeDistribution;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttendanceAnalyticsDTO {
        private Map<String, Integer> monthlyAttendance;
        private Double overallAttendanceRate;
        private Integer totalPresent;
        private Integer totalAbsent;
        private Integer totalLate;
        private List<AttendanceTrendDTO> trends;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AttendanceTrendDTO {
        private String month;
        private Double attendanceRate;
        private Integer present;
        private Integer absent;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GradeDistributionDTO {
        private Map<String, Integer> distribution;
        private Double mean;
        private Double median;
        private Double standardDeviation;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PerformancePredictionDTO {
        private Long studentId;
        private String studentName;
        private Double currentAverage;
        private Double predictedAverage;
        private String riskLevel; // LOW, MEDIUM, HIGH
        private List<String> recommendations;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CustomReportDTO {
        private String reportName;
        private String reportType;
        private Map<String, Object> data;
        private String generatedAt;
    }
}

