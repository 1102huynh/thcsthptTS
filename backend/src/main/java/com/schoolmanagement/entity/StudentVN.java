package com.schoolmanagement.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "students_vn")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentVN {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ============ THÔNG TIN CƠ BẢN ============
    
    @Column(name = "student_code", unique = true, length = 10)
    private String studentCode; // Mã HS: 0124061234 (Trường-Năm-Khối-STT)

    @Column(name = "student_id", unique = true, nullable = false, length = 20)
    private String studentId; // Legacy admission number

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Links to User (has email, username, password)

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName; // Họ và tên đệm

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName; // Tên

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "gender", nullable = false, length = 10)
    private String gender; // Nam/Nữ

    @Column(name = "place_of_birth", length = 100)
    private String placeOfBirth; // Nơi sinh

    @Column(name = "id_number", length = 20)
    private String idNumber; // CMND/CCCD

    @Column(name = "id_issue_date")
    private LocalDate idIssueDate;

    @Column(name = "id_issue_place", length = 100)
    private String idIssuePlace;

    // ============ HỘ KHẨU THƯỜNG TRÚ ============

    @Column(name = "province", length = 100)
    private String province; // Tỉnh/Thành phố

    @Column(name = "district", length = 100)
    private String district; // Quận/Huyện

    @Column(name = "ward", length = 100)
    private String ward; // Phường/Xã

    @Column(name = "detailed_address", length = 255)
    private String detailedAddress; // Số nhà, đường

    @Column(name = "phone_number", length = 20)
    private String phoneNumber; // SĐT học sinh

    // ============ DÂN TỘC & TÔN GIÁO ============

    @Column(name = "ethnicity", length = 50)
    private String ethnicity; // Dân tộc

    @Column(name = "religion", length = 50)
    private String religion; // Tôn giáo

    @Column(name = "priority_object", length = 100)
    private String priorityObject; // Đối tượng ưu tiên

    // ============ THÔNG TIN HỌC TẬP ============

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_level_id")
    private GradeLevel gradeLevel; // Khối 6-12

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private SchoolClass schoolClass; // Lớp học hiện tại

    @Column(name = "academic_year", length = 20)
    private String academicYear; // Năm học: 2024-2025

    @Column(name = "admission_year")
    private Integer admissionYear; // Năm nhập học: 2024

    @Column(name = "expected_graduation_year")
    private Integer expectedGraduationYear; // Năm dự kiến tốt nghiệp

    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, ON_LEAVE, TRANSFERRED, DROPPED, GRADUATED

    // ============ THÔNG TIN PHỤ HUYNH - CHA ============

    @Column(name = "father_name", length = 100)
    private String fatherName;

    @Column(name = "father_year_of_birth")
    private Integer fatherYearOfBirth;

    @Column(name = "father_occupation", length = 100)
    private String fatherOccupation;

    @Column(name = "father_workplace", length = 200)
    private String fatherWorkplace;

    @Column(name = "father_phone", length = 20)
    private String fatherPhone;

    @Column(name = "father_email", length = 100)
    private String fatherEmail;

    // ============ THÔNG TIN PHỤ HUYNH - MẸ ============

    @Column(name = "mother_name", length = 100)
    private String motherName;

    @Column(name = "mother_year_of_birth")
    private Integer motherYearOfBirth;

    @Column(name = "mother_occupation", length = 100)
    private String motherOccupation;

    @Column(name = "mother_workplace", length = 200)
    private String motherWorkplace;

    @Column(name = "mother_phone", length = 20)
    private String motherPhone;

    @Column(name = "mother_email", length = 100)
    private String motherEmail;

    // ============ NGƯỜI GIÁM HỘ ============

    @Column(name = "guardian_name", length = 100)
    private String guardianName;

    @Column(name = "guardian_relationship", length = 50)
    private String guardianRelationship; // Ông/Bà/Cô/Chú

    @Column(name = "guardian_phone", length = 20)
    private String guardianPhone;

    @Column(name = "guardian_address", length = 255)
    private String guardianAddress;

    // ============ LÝ LỊCH HỌC TẬP ============

    @Column(name = "previous_school", length = 200)
    private String previousSchool;

    @Column(name = "previous_school_address", length = 255)
    private String previousSchoolAddress;

    @Column(name = "previous_school_from")
    private LocalDate previousSchoolFrom;

    @Column(name = "previous_school_to")
    private LocalDate previousSchoolTo;

    @Column(name = "transfer_reason", length = 500)
    private String transferReason;

    @Column(name = "previous_academic_rank", length = 50)
    private String previousAcademicRank; // Giỏi/Khá/Trung bình/Yếu

    @Column(name = "previous_conduct_rank", length = 50)
    private String previousConductRank; // Tốt/Khá/Trung bình/Yếu

    @Column(name = "awards", length = 500)
    private String awards; // Danh sách khen thưởng (JSON hoặc text)

    // ============ THÔNG TIN SỨC KHỎE ============

    @Column(name = "height")
    private Integer height; // cm

    @Column(name = "weight")
    private Integer weight; // kg

    @Column(name = "blood_type", length = 10)
    private String bloodType; // A, B, AB, O

    @Column(name = "diseases", length = 500)
    private String diseases; // Bệnh lý

    @Column(name = "allergies", length = 500)
    private String allergies; // Dị ứng

    // ============ TÀI LIỆU ĐÍNH KÈM ============

    @Column(name = "photo_url", length = 500)
    private String photoUrl; // Ảnh 3x4

    @Column(name = "birth_certificate_url", length = 500)
    private String birthCertificateUrl; // Giấy khai sinh

    @Column(name = "household_book_url", length = 500)
    private String householdBookUrl; // Sổ hộ khẩu

    @Column(name = "other_documents_url", length = 1000)
    private String otherDocumentsUrl; // Các tài liệu khác (JSON array)

    // ============ GHI CHÚ & METADATA ============

    @Column(name = "notes", length = 1000)
    private String notes; // Ghi chú

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "created_by")
    private String createdBy; // Who created this student

    @Column(name = "updated_by")
    private String updatedBy; // Who last updated

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ============ HELPER METHODS ============

    public String getFullName() {
        return (lastName != null ? lastName + " " : "") + (firstName != null ? firstName : "");
    }

    public Integer getAge() {
        if (dateOfBirth == null) return null;
        return LocalDate.now().getYear() - dateOfBirth.getYear();
    }
}
