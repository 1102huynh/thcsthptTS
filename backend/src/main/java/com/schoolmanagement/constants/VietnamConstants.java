package com.schoolmanagement.constants;

import java.util.Arrays;
import java.util.List;

/**
 * Vietnam Education Constants
 * Contains standardized data for Vietnamese education system
 */
public class VietnamConstants {

    // ============ 63 TỈNH/THÀNH PHỐ VIỆT NAM ============
    
    public static final List<String> PROVINCES = Arrays.asList(
        "An Giang", "Bà Rịa - Vũng Tàu", "Bạc Liêu", "Bắc Giang", "Bắc Kạn",
        "Bắc Ninh", "Bến Tre", "Bình Dương", "Bình Định", "Bình Phước",
        "Bình Thuận", "Cà Mau", "Cao Bằng", "Cần Thơ", "Đà Nẵng",
        "Đắk Lắk", "Đắk Nông", "Điện Biên", "Đồng Nai", "Đồng Tháp",
        "Gia Lai", "Hà Giang", "Hà Nam", "Hà Nội", "Hà Tĩnh",
        "Hải Dương", "Hải Phòng", "Hậu Giang", "Hòa Bình", "Hưng Yên",
        "Khánh Hòa", "Kiên Giang", "Kon Tum", "Lai Châu", "Lâm Đồng",
        "Lạng Sơn", "Lào Cai", "Long An", "Nam Định", "Nghệ An",
        "Ninh Bình", "Ninh Thuận", "Phú Thọ", "Phú Yên", "Quảng Bình",
        "Quảng Nam", "Quảng Ngãi", "Quảng Ninh", "Quảng Trị", "Sóc Trăng",
        "Sơn La", "Tây Ninh", "Thái Bình", "Thái Nguyên", "Thanh Hóa",
        "Thừa Thiên Huế", "Tiền Giang", "TP Hồ Chí Minh", "Trà Vinh", "Tuyên Quang",
        "Vĩnh Long", "Vĩnh Phúc", "Yên Bái"
    );

    // ============ 54 DÂN TỘC VIỆT NAM ============
    
    public static final List<String> ETHNICITIES = Arrays.asList(
        "Kinh", "Tày", "Thái", "Mường", "Khmer",
        "Hoa", "Nùng", "H'Mông", "Dao", "Gia Rai",
        "Ê Đê", "Ba Na", "Sán Chay", "Chăm", "Cơ Ho",
        "Xơ Đăng", "Sán Dìu", "Hrê", "Raglay", "Mnông",
        "Thổ", "Stiêng", "Bru - Vân Kiều", "Khơ Mú", "Cơ Tu",
        "Giáy", "Tà Ôi", "Mạ", "Giẻ Triêng", "Co",
        "Chơ Ro", "Xinh Mun", "Hà Nhì", "Chu Ru", "Lào",
        "La Chí", "Kháng", "Phù Lá", "La Hủ", "La Ha",
        "Pà Thẻn", "Lự", "Ngái", "Chứt", "Lô Lô",
        "Mảng", "Cống", "Bố Y", "Si La", "Pu Péo",
        "Rơ Măm", "Brâu", "Ơ Đu", "Người nước ngoài"
    );

    // ============ ĐỐI TƯỢNG ƯU TIÊN ============
    
    public static final List<String> PRIORITY_OBJECTS = Arrays.asList(
        "Không",
        "Con liệt sĩ",
        "Con thương binh",
        "Con bệnh binh",
        "Con gia đình chính sách",
        "Con cán bộ, công chức",
        "Con người có công với cách mạng",
        "Học sinh dân tộc thiểu số",
        "Học sinh vùng sâu, vùng xa",
        "Học sinh khuyết tật",
        "Học sinh mồ côi",
        "Học sinh hoàn cảnh đặc biệt khó khăn",
        "Khác"
    );

    // ============ TÔN GIÁO ============
    
    public static final List<String> RELIGIONS = Arrays.asList(
        "Không",
        "Phật giáo",
        "Công giáo",
        "Tin lành",
        "Cao Đài",
        "Hòa Hảo",
        "Hồi giáo",
        "Khác"
    );

    // ============ GIỚI TÍNH ============
    
    public static final List<String> GENDERS = Arrays.asList(
        "Nam",
        "Nữ"
    );

    // ============ NHÓM MÁU ============
    
    public static final List<String> BLOOD_TYPES = Arrays.asList(
        "A",
        "B",
        "AB",
        "O",
        "A+",
        "A-",
        "B+",
        "B-",
        "AB+",
        "AB-",
        "O+",
        "O-"
    );

    // ============ XẾP LOẠI HỌC LỰC ============
    
    public static final List<String> ACADEMIC_RANKS = Arrays.asList(
        "Xuất sắc",      // ≥ 9.0, không có môn nào < 8.0
        "Giỏi",          // ≥ 8.0, không có môn nào < 6.5
        "Khá",           // ≥ 6.5, không có môn nào < 5.0
        "Trung bình",    // ≥ 5.0, không có môn nào < 3.5
        "Yếu"            // < 5.0 hoặc có môn < 3.5
    );

    // ============ XẾP LOẠI HẠNH KIỂM ============
    
    public static final List<String> CONDUCT_RANKS = Arrays.asList(
        "Tốt",
        "Khá",
        "Trung bình",
        "Yếu"
    );

    // ============ TRẠNG THÁI HỌC SINH ============
    
    public enum StudentStatus {
        ACTIVE("Đang học"),
        ON_LEAVE("Bảo lưu"),
        TRANSFERRED("Chuyển trường"),
        DROPPED("Thôi học"),
        GRADUATED("Tốt nghiệp");

        private final String vietnameseName;

        StudentStatus(String vietnameseName) {
            this.vietnameseName = vietnameseName;
        }

        public String getVietnameseName() {
            return vietnameseName;
        }
    }

    // ============ DANH HIỆU ============
    
    public static final List<String> AWARDS = Arrays.asList(
        "Học sinh Giỏi",
        "Học sinh Tiên tiến",
        "Học sinh Xuất sắc",
        "Giải Nhất môn ...",
        "Giải Nhì môn ...",
        "Giải Ba môn ...",
        "Giải Khuyến khích",
        "Học sinh 3 Tốt",
        "Cháu ngoan Bác Hồ"
    );

    // ============ QUAN HỆ VỚI HỌC SINH (Người giám hộ) ============
    
    public static final List<String> GUARDIAN_RELATIONSHIPS = Arrays.asList(
        "Ông",
        "Bà",
        "Cô",
        "Dì",
        "Chú",
        "Bác",
        "Anh",
        "Chị",
        "Khác"
    );

    // ============ KHỐI HỌC ============
    
    public static final List<Integer> GRADE_LEVELS = Arrays.asList(
        6, 7, 8, 9,      // THCS
        10, 11, 12       // THPT
    );

    // ============ BAN (THPT) ============
    
    public static final List<String> THPT_TRACKS = Arrays.asList(
        "Không",
        "Ban Khoa học Tự nhiên (A)",
        "Ban Khoa học Xã hội (C)",
        "Ban Khoa học Cơ bản (D)"
    );

    // ============ HELPER METHODS ============
    
    public static boolean isValidProvince(String province) {
        return PROVINCES.contains(province);
    }

    public static boolean isValidEthnicity(String ethnicity) {
        return ETHNICITIES.contains(ethnicity);
    }

    public static boolean isValidGradeLevel(Integer gradeLevel) {
        return GRADE_LEVELS.contains(gradeLevel);
    }

    /**
     * Generate student code
     * Format: XXYYZZNNNN
     * XX = School code (01-99)
     * YY = Admission year (24, 25, 26...)
     * ZZ = Grade level (06-12)
     * NNNN = Sequential number (0001-9999)
     */
    public static String generateStudentCode(int schoolCode, int admissionYear, int gradeLevel, int sequentialNumber) {
        return String.format("%02d%02d%02d%04d", 
            schoolCode, 
            admissionYear % 100, 
            gradeLevel, 
            sequentialNumber);
    }

    /**
     * Validate student code format
     */
    public static boolean isValidStudentCode(String code) {
        if (code == null || code.length() != 10) {
            return false;
        }
        return code.matches("\\d{10}");
    }
}
