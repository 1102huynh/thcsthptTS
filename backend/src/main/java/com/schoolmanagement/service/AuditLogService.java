package com.schoolmanagement.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.schoolmanagement.dto.AuditLogDTO;
import com.schoolmanagement.entity.AuditLog;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.repository.AuditLogRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Writes one {@link AuditLog} row per sensitive operation, per
 * IMPLEMENTATION_PLAN.md 3.9. Called manually from the specific service
 * methods that need a trail, NOT via a blanket AOP interceptor around every
 * create/update/delete in the app — that would touch 20+ existing services
 * for one phase, bury genuinely sensitive events under routine ones (e.g.
 * editing a library book's description), and be much harder to review.
 *
 * <p>Currently instrumented call sites:
 * <ul>
 *   <li>{@link GradeRecordService#updateGradeRecord} / {@link GradeRecordService#deleteGradeRecord} — sửa/xóa điểm</li>
 *   <li>{@link StudentService#deleteStudent} — xóa học sinh</li>
 *   <li>{@link AdmissionService#updateStatus} / {@link AdmissionService#approveAndCreate} — duyệt tuyển sinh</li>
 *   <li>{@link AuthenticationService#createUserByAdmin} — cấp tài khoản kèm quyền</li>
 *   <li>{@link PasswordResetService#resetPassword} — đặt lại mật khẩu</li>
 * </ul>
 *
 * <p>Deliberately runs in the SAME transaction as the operation it's
 * logging (no {@code REQUIRES_NEW}) — if that operation rolls back, the log
 * entry must roll back with it; a log claiming something happened that
 * didn't would be worse than no log at all.
 */
@Service
@AllArgsConstructor
@Transactional
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    private AuditLogRepository auditLogRepository;
    private ObjectMapper objectMapper;

    /**
     * @param detail free-form context (e.g. old/new values) - optional, may be null.
     *               Serialization failures are swallowed (logged, detail stored as
     *               null) rather than allowed to fail the real operation being audited.
     */
    public void log(User actor, String action, String entityType, Long entityId, Map<String, Object> detail) {
        String detailJson = null;
        if (detail != null) {
            try {
                detailJson = objectMapper.writeValueAsString(detail);
            } catch (Exception ex) {
                log.warn("Failed to serialize audit detail for {} {} #{} - logging without it: {}",
                        action, entityType, entityId, ex.getMessage());
            }
        }

        auditLogRepository.save(AuditLog.builder()
                .actor(actor)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .detailJson(detailJson)
                .build());
    }

    @Transactional(readOnly = true)
    public Page<AuditLogDTO> search(String entityType, Long actorId, Pageable pageable) {
        return auditLogRepository.search(entityType, actorId, pageable).map(this::mapToDTO);
    }

    private AuditLogDTO mapToDTO(AuditLog entry) {
        User actor = entry.getActor();
        return AuditLogDTO.builder()
                .id(entry.getId())
                .actorId(actor != null ? actor.getId() : null)
                .actorName(actor != null ? actor.getFirstName() + " " + actor.getLastName() : null)
                .action(entry.getAction())
                .entityType(entry.getEntityType())
                .entityId(entry.getEntityId())
                .occurredAt(entry.getOccurredAt())
                .detailJson(entry.getDetailJson())
                .build();
    }
}
