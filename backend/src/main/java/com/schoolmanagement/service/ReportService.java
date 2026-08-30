package com.schoolmanagement.service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.schoolmanagement.dto.FeeDTO;
import com.schoolmanagement.dto.SubjectYearAverageDTO;
import com.schoolmanagement.entity.AcademicYear;
import com.schoolmanagement.entity.Attendance;
import com.schoolmanagement.entity.AttendanceStatus;
import com.schoolmanagement.entity.ConductRecord;
import com.schoolmanagement.entity.PromotionRecord;
import com.schoolmanagement.entity.SchoolClass;
import com.schoolmanagement.entity.Semester;
import com.schoolmanagement.entity.SemesterName;
import com.schoolmanagement.entity.Student;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.AcademicYearRepository;
import com.schoolmanagement.repository.AttendanceRepository;
import com.schoolmanagement.repository.ConductRecordRepository;
import com.schoolmanagement.repository.PromotionRecordRepository;
import com.schoolmanagement.repository.SchoolClassRepository;
import com.schoolmanagement.repository.SemesterRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.security.StudentAccessGuard;
import com.schoolmanagement.util.VietnamesePdfFonts;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PDF/Excel exports per IMPLEMENTATION_PLAN.md 3.8. Read-only — every method
 * assembles its document from data that another service/repository already
 * owns, reusing that service's access control rather than re-implementing it
 * (see {@link #generateStudentTranscriptPdf} delegating to
 * {@link GradeRecordService#getStudentYearSummary} and
 * {@link #generateFeeReceiptPdf} delegating to {@link FeeService#getFeeById}).
 *
 * <p>Xếp loại học lực (classification) does not appear anywhere in the
 * transcript — per 3.3, that calculation is deliberately not implemented yet
 * (TT22/58 thresholds need education-domain sign-off). The transcript shows
 * the raw calculated điểm trung bình only, same as the API it's built from.
 */
@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private static final DateTimeFormatter VN_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private GradeRecordService gradeRecordService;
    private FeeService feeService;
    private StudentRepository studentRepository;
    private AcademicYearRepository academicYearRepository;
    private SemesterRepository semesterRepository;
    private ConductRecordRepository conductRecordRepository;
    private PromotionRecordRepository promotionRecordRepository;
    private SchoolClassRepository schoolClassRepository;
    private AttendanceRepository attendanceRepository;
    private StudentAccessGuard studentAccessGuard;
    private VietnamesePdfFonts fonts;

    // ---------------------------------------------------------------
    // 1) Bảng điểm / học bạ (PDF)
    // ---------------------------------------------------------------

    public byte[] generateStudentTranscriptPdf(Long studentId, Long academicYearId, User requester) {
        // Same object-level check every other per-student endpoint uses; also
        // re-enforced (harmlessly) inside getStudentYearSummary below.
        studentAccessGuard.enforceCanAccessStudent(studentId, requester);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        AcademicYear academicYear = academicYearRepository.findById(academicYearId)
                .orElseThrow(() -> new ResourceNotFoundException("Academic year not found with id: " + academicYearId));

        List<SubjectYearAverageDTO> subjectAverages =
                gradeRecordService.getStudentYearSummary(studentId, academicYearId, requester);

        Optional<Semester> hk1 = semesterRepository.findByAcademicYearAndName(academicYear, SemesterName.HK1);
        Optional<Semester> hk2 = semesterRepository.findByAcademicYearAndName(academicYear, SemesterName.HK2);
        Optional<ConductRecord> hk1Conduct = hk1.flatMap(s -> conductRecordRepository.findByStudentAndSemester(student, s));
        Optional<ConductRecord> hk2Conduct = hk2.flatMap(s -> conductRecordRepository.findByStudentAndSemester(student, s));
        Optional<PromotionRecord> promotion = promotionRecordRepository.findByStudentAndAcademicYear(student, academicYear);

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            document.add(new Paragraph("BẢNG ĐIỂM / HỌC BẠ ĐIỆN TỬ", fonts.bold(16)));
            document.add(spacer());

            document.add(studentInfoTable(student, academicYear));
            document.add(spacer());

            document.add(new Paragraph("Điểm trung bình môn", fonts.bold(12)));
            document.add(subjectAveragesTable(subjectAverages));
            document.add(spacer());

            document.add(new Paragraph("Hạnh kiểm", fonts.bold(12)));
            document.add(conductTable(hk1Conduct, hk2Conduct));

            if (promotion.isPresent()) {
                document.add(spacer());
                document.add(new Paragraph("Kết quả xét lên lớp", fonts.bold(12)));
                document.add(promotionParagraph(promotion.get()));
            }

            document.add(spacer());
            document.add(new Paragraph(
                    "Ghi chú: điểm trung bình môn được tính tự động theo công thức Thông tư 22/2021 "
                            + "(và tương thích Thông tư 58); xếp loại học lực chưa được hệ thống tự động hoá — "
                            + "cần xác nhận ngưỡng quy định từ người có chuyên môn giáo dục.",
                    fonts.italic(8)));

            document.close();
            return out.toByteArray();
        } catch (DocumentException ex) {
            throw new IllegalStateException("Failed to generate transcript PDF for student " + studentId, ex);
        }
    }

    private PdfPTable studentInfoTable(Student student, AcademicYear academicYear) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1});

        addPlainRow(table, "Họ tên học sinh", studentDisplayName(student));
        addPlainRow(table, "Mã học sinh", student.getRollNumber());
        addPlainRow(table, "Ngày sinh", student.getDateOfBirth() != null ? VN_DATE.format(student.getDateOfBirth()) : "—");
        addPlainRow(table, "Lớp", (student.getClassName() != null ? student.getClassName() : "—")
                + (student.getSection() != null ? " - " + student.getSection() : ""));
        addPlainRow(table, "Năm học", academicYear.getName());
        return table;
    }

    private PdfPTable subjectAveragesTable(List<SubjectYearAverageDTO> subjectAverages) {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2.5f, 1, 1, 1});

        addHeaderCell(table, "Môn học");
        addHeaderCell(table, "ĐTB HK1");
        addHeaderCell(table, "ĐTB HK2");
        addHeaderCell(table, "ĐTB cả năm");

        if (subjectAverages.isEmpty()) {
            PdfPCell empty = new PdfPCell(new Paragraph("Chưa có điểm nào được ghi nhận trong năm học này", fonts.italic(9)));
            empty.setColspan(4);
            table.addCell(empty);
        }

        for (SubjectYearAverageDTO row : subjectAverages) {
            addPlainCell(table, row.getSubjectName());
            addPlainCell(table, formatScore(row.getSemester1Average()));
            addPlainCell(table, formatScore(row.getSemester2Average()));
            addPlainCell(table, formatScore(row.getYearAverage()));
        }
        return table;
    }

    private PdfPTable conductTable(Optional<ConductRecord> hk1, Optional<ConductRecord> hk2) {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);

        addHeaderCell(table, "Học kỳ 1");
        addHeaderCell(table, "Học kỳ 2");
        addPlainCell(table, hk1.map(c -> vietnameseConductLabel(c.getRating().name())).orElse("Chưa đánh giá"));
        addPlainCell(table, hk2.map(c -> vietnameseConductLabel(c.getRating().name())).orElse("Chưa đánh giá"));
        return table;
    }

    private Paragraph promotionParagraph(PromotionRecord promotion) {
        String decisionLabel = vietnamesePromotionLabel(promotion.getDecision().name());
        String text = decisionLabel
                + (promotion.getDecisionDate() != null ? " — ngày " + VN_DATE.format(promotion.getDecisionDate()) : "");
        Paragraph p = new Paragraph(text, fonts.regular(11));
        if (promotion.getRemarks() != null && !promotion.getRemarks().isBlank()) {
            p.add(Chunk.NEWLINE);
            p.add(new Chunk("Ghi chú: " + promotion.getRemarks(), fonts.italic(9)));
        }
        return p;
    }

    // ---------------------------------------------------------------
    // 2) Điểm danh theo lớp (Excel)
    // ---------------------------------------------------------------

    /**
     * Roster membership is matched on the same (deprecated) className/section
     * pair {@link com.schoolmanagement.service.SchoolClassService#getStudentsInClass}
     * and {@link com.schoolmanagement.service.ConductRecordService#getClassSemesterRoster}
     * already use codebase-wide — not scoped by academic year, so a
     * className/section reused across years would over-match. Pre-existing
     * limitation of that roster convention, not new to this endpoint.
     */
    public byte[] generateClassAttendanceExcel(Long classId, LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) {
            throw new IllegalArgumentException("'from' phải có giá trị và không được sau 'to'");
        }

        SchoolClass schoolClass = schoolClassRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + classId));

        List<Student> roster = studentRepository
                .findByClassNameAndSection(schoolClass.getClassName(), schoolClass.getSection())
                .stream()
                .sorted((a, b) -> a.getRollNumber().compareToIgnoreCase(b.getRollNumber()))
                .toList();

        List<LocalDate> dates = from.datesUntil(to.plusDays(1)).toList();

        // One query for the whole roster instead of one per student - a 40-student
        // class would otherwise mean 40 round trips for what's fundamentally a
        // single (studentId IN (...), date BETWEEN ...) query.
        Map<Long, Map<LocalDate, AttendanceStatus>> attendanceByStudentId = roster.isEmpty()
                ? Map.of()
                : attendanceRepository.findByStudentInAndAttendanceDateBetween(roster, from, to).stream()
                        .collect(Collectors.groupingBy(
                                a -> a.getStudent().getId(),
                                Collectors.toMap(Attendance::getAttendanceDate, Attendance::getStatus,
                                        // Two records on the same day for the same student shouldn't
                                        // happen in practice (nothing enforces it at the DB level
                                        // though) - last one encountered wins, same "don't crash on
                                        // unexpected duplicates" tolerance as the rest of this
                                        // read-only report.
                                        (first, second) -> second)));

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Điểm danh");
            CellStyle headerStyle = headerCellStyle(workbook);
            CellStyle legendStyle = legendCellStyle(workbook);

            // 2 name columns + one per date + 5 summary columns - computed up front
            // so the legend row (below) can be merged across exactly this width
            // instead of living alone in column A, which would otherwise make
            // POI's autoSizeColumn() blow column A out to the legend text's full
            // length (seen live: ~120 characters wide) since it measures every
            // cell in the column, this one included.
            int lastColIndex = dates.size() + 6;

            int rowIdx = 0;
            Row legendRow = sheet.createRow(rowIdx++);
            org.apache.poi.ss.usermodel.Cell legendCell = legendRow.createCell(0);
            legendCell.setCellValue("Chú thích: CM=Có mặt, V=Vắng không phép, T=Trễ giờ, OM=Nghỉ ốm, PA=Nghỉ có phép, CD=Chờ duyệt — ô trống = không có dữ liệu điểm danh ngày đó");
            legendCell.setCellStyle(legendStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, lastColIndex));
            rowIdx++; // blank separator row

            Row header = sheet.createRow(rowIdx++);
            int col = 0;
            header.createCell(col++).setCellValue("Mã HS");
            header.createCell(col++).setCellValue("Họ tên");
            for (LocalDate date : dates) {
                header.createCell(col++).setCellValue(date.format(DateTimeFormatter.ofPattern("dd/MM")));
            }
            header.createCell(col++).setCellValue("Có mặt");
            header.createCell(col++).setCellValue("Vắng");
            header.createCell(col++).setCellValue("Phép/Ốm");
            header.createCell(col++).setCellValue("Tổng ghi nhận");
            header.createCell(col).setCellValue("Chuyên cần (%)");
            for (int c = 0; c <= lastColIndex; c++) {
                header.getCell(c).setCellStyle(headerStyle);
            }

            for (Student student : roster) {
                Map<LocalDate, AttendanceStatus> byDate = attendanceByStudentId
                        .getOrDefault(student.getId(), Map.of());

                Row row = sheet.createRow(rowIdx++);
                col = 0;
                row.createCell(col++).setCellValue(student.getRollNumber());
                row.createCell(col++).setCellValue(studentDisplayName(student));

                long present = 0, absent = 0, leaveOrSick = 0, recorded = 0;
                for (LocalDate date : dates) {
                    AttendanceStatus status = byDate.get(date);
                    row.createCell(col++).setCellValue(attendanceCode(status));
                    // LEAVE_PENDING is an unresolved request - not yet an actual
                    // absence or a confirmed excused day - so it's shown in its own
                    // day cell (code "CD") but deliberately left out of every
                    // summary count below, "recorded" (and thus the % chuyên cần
                    // denominator) included. Counting it either way would bake in
                    // an outcome the school hasn't decided yet.
                    if (status != null && status != AttendanceStatus.LEAVE_PENDING) {
                        recorded++;
                        switch (status) {
                            case PRESENT, LATE -> present++;
                            case ABSENT -> absent++;
                            case SICK_LEAVE, LEAVE_APPROVED -> leaveOrSick++;
                            default -> { /* LEAVE_PENDING excluded above - unreachable here */ }
                        }
                    }
                }
                row.createCell(col++).setCellValue(present);
                row.createCell(col++).setCellValue(absent);
                row.createCell(col++).setCellValue(leaveOrSick);
                row.createCell(col++).setCellValue(recorded);
                row.createCell(col).setCellValue(recorded > 0 ? Math.round(present * 10000.0 / recorded) / 100.0 : 0);
            }

            for (int c = 0; c <= lastColIndex; c++) {
                sheet.autoSizeColumn(c);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("Failed to generate attendance Excel for class " + classId, ex);
        }
    }

    private String attendanceCode(AttendanceStatus status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case PRESENT -> "CM";
            case ABSENT -> "V";
            case LATE -> "T";
            case SICK_LEAVE -> "OM";
            case LEAVE_APPROVED -> "PA";
            case LEAVE_PENDING -> "CD";
        };
    }

    private CellStyle headerCellStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle legendCellStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setItalic(true);
        style.setFont(font);
        return style;
    }

    private String studentDisplayName(Student student) {
        return student.getUser() != null
                ? student.getUser().getFirstName() + " " + student.getUser().getLastName()
                : "(chưa liên kết tài khoản)";
    }

    // ---------------------------------------------------------------
    // 3) Biên lai học phí (PDF)
    // ---------------------------------------------------------------

    public byte[] generateFeeReceiptPdf(Long feeId, User requester) {
        // Reuses FeeService's own access control (ADMIN/ACCOUNTANT/PRINCIPAL
        // unrestricted, STUDENT/PARENT limited to their own/child's fee) rather
        // than re-implementing it here.
        FeeDTO fee = feeService.getFeeById(feeId, requester);

        if (fee.getPaidAmount() == null || fee.getPaidAmount() <= 0) {
            throw new IllegalArgumentException(
                    "Khoản thu #" + feeId + " chưa ghi nhận khoản thanh toán nào — không thể xuất biên lai");
        }

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A5, 36, 36, 54, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph title = new Paragraph("BIÊN LAI THU HỌC PHÍ", fonts.bold(16));
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            Paragraph subtitle = new Paragraph("Biên lai số: " + fee.getId()
                    + "   —   Ngày lập: " + VN_DATE.format(LocalDate.now()), fonts.italic(9));
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);
            document.add(spacer());

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 1.2f});

            addPlainRow(table, "Học sinh", fee.getStudentName() != null ? fee.getStudentName() : "—");
            addPlainRow(table, "Năm học", fee.getAcademicYear());
            addPlainRow(table, "Khoản thu", fee.getFeeType());
            addPlainRow(table, "Tổng số tiền", formatMoney(fee.getAmount()));
            addPlainRow(table, "Đã thu", formatMoney(fee.getPaidAmount()));
            addPlainRow(table, "Còn lại", formatMoney(fee.getRemainingAmount()));
            addPlainRow(table, "Ngày thu", fee.getPaidDate() != null ? VN_DATE.format(fee.getPaidDate()) : "—");
            addPlainRow(table, "Phương thức", fee.getPaymentMethod() != null ? fee.getPaymentMethod() : "—");
            addPlainRow(table, "Mã giao dịch", fee.getTransactionId() != null ? fee.getTransactionId() : "—");
            addPlainRow(table, "Trạng thái", vietnameseFeeStatusLabel(fee.getStatus().name()));
            document.add(table);

            if (fee.getRemarks() != null && !fee.getRemarks().isBlank()) {
                document.add(spacer());
                document.add(new Paragraph("Ghi chú: " + fee.getRemarks(), fonts.italic(9)));
            }

            document.close();
            return out.toByteArray();
        } catch (DocumentException ex) {
            throw new IllegalStateException("Failed to generate fee receipt PDF for fee " + feeId, ex);
        }
    }

    // ---------------------------------------------------------------
    // Shared PDF helpers
    // ---------------------------------------------------------------

    private Paragraph spacer() {
        return new Paragraph(" ", fonts.regular(4));
    }

    private void addPlainRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Paragraph(label, fonts.bold(10)));
        labelCell.setBorder(0);
        labelCell.setPaddingBottom(4);
        PdfPCell valueCell = new PdfPCell(new Paragraph(value != null ? value : "—", fonts.regular(10)));
        valueCell.setBorder(0);
        valueCell.setPaddingBottom(4);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private static final java.awt.Color HEADER_FILL = new java.awt.Color(224, 224, 224);

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, fonts.bold(10)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(HEADER_FILL);
        table.addCell(cell);
    }

    private void addPlainCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Paragraph(text != null ? text : "—", fonts.regular(10)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private String formatScore(Double score) {
        return score != null ? String.format("%.2f", score) : "—";
    }

    private String formatMoney(Double amount) {
        return amount != null ? String.format("%,.0f đ", amount) : "—";
    }

    private String vietnameseConductLabel(String rating) {
        return switch (rating) {
            case "TOT" -> "Tốt";
            case "KHA" -> "Khá";
            case "TRUNG_BINH" -> "Trung bình";
            case "YEU" -> "Yếu";
            default -> rating;
        };
    }

    private String vietnamesePromotionLabel(String decision) {
        return switch (decision) {
            case "LEN_LOP" -> "Được lên lớp";
            case "O_LAI" -> "Ở lại lớp";
            case "TOT_NGHIEP" -> "Tốt nghiệp";
            case "RA_TRUONG" -> "Rời trường";
            default -> decision;
        };
    }

    private String vietnameseFeeStatusLabel(String status) {
        return switch (status) {
            case "PENDING" -> "Chưa thu";
            case "PARTIAL_PAID" -> "Thu một phần";
            case "PAID" -> "Đã thu đủ";
            case "OVERDUE" -> "Quá hạn";
            case "EXEMPTED" -> "Miễn giảm";
            case "CANCELLED" -> "Đã huỷ";
            default -> status;
        };
    }
}
