package com.schoolmanagement.service;

import com.schoolmanagement.dto.DocumentAttachmentDTO;
import com.schoolmanagement.entity.DocumentAttachment;
import com.schoolmanagement.entity.DocumentOwnerType;
import com.schoolmanagement.entity.Role;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.AdmissionApplicationRepository;
import com.schoolmanagement.repository.DocumentAttachmentRepository;
import com.schoolmanagement.repository.StaffRepository;
import com.schoolmanagement.repository.StudentRepository;
import com.schoolmanagement.security.StudentAccessGuard;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * File attachment metadata + access control, per IMPLEMENTATION_PLAN.md 3.9.
 * The actual bytes live on local disk (see {@link FileStorageService});
 * this service owns the {@link DocumentAttachment} rows and who's allowed to
 * touch them.
 *
 * <p>Object-level access: for {@code ownerType == STUDENT}, delegates to
 * {@link StudentAccessGuard} (same self-access pattern grades/fees/conduct
 * already use) so a STUDENT/PARENT may only reach their own/child's
 * documents; ADMIN/PRINCIPAL/TEACHER are unrestricted. STAFF and
 * ADMISSION_APPLICATION documents are administrative records with no
 * "self-service" caller — ADMIN/PRINCIPAL only, matching how
 * {@code /v1/admissions} itself is ADMIN-only. Deleting a document (any
 * owner type) is ADMIN/PRINCIPAL only.
 */
@Service
@Transactional
public class DocumentService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "jpg", "jpeg", "png", "doc", "docx", "xls", "xlsx");
    private static final long MAX_FILE_SIZE_BYTES = 10L * 1024 * 1024; // 10 MB

    private final DocumentAttachmentRepository documentAttachmentRepository;
    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;
    private final AdmissionApplicationRepository admissionApplicationRepository;
    private final StudentAccessGuard studentAccessGuard;
    private final FileStorageService fileStorageService;
    private final String contextPath;

    // Not @AllArgsConstructor: needs @Value on contextPath, which a
    // Lombok-generated constructor's parameter wouldn't carry - see
    // PasswordResetService's Javadoc for the same gotcha (and the startup
    // failure it caused before being caught there).
    public DocumentService(DocumentAttachmentRepository documentAttachmentRepository,
                            StudentRepository studentRepository,
                            StaffRepository staffRepository,
                            AdmissionApplicationRepository admissionApplicationRepository,
                            StudentAccessGuard studentAccessGuard,
                            FileStorageService fileStorageService,
                            @Value("${server.servlet.context-path:}") String contextPath) {
        this.documentAttachmentRepository = documentAttachmentRepository;
        this.studentRepository = studentRepository;
        this.staffRepository = staffRepository;
        this.admissionApplicationRepository = admissionApplicationRepository;
        this.studentAccessGuard = studentAccessGuard;
        this.fileStorageService = fileStorageService;
        this.contextPath = contextPath;
    }

    public DocumentAttachmentDTO upload(MultipartFile file, DocumentOwnerType ownerType, Long ownerId, User uploader) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is required and must not be empty");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("File exceeds the " + (MAX_FILE_SIZE_BYTES / (1024 * 1024)) + "MB limit");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "File type not allowed - accepted: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        enforceOwnerAccess(ownerType, ownerId, uploader, true);
        resolveOwnerOrThrow(ownerType, ownerId);

        String storedFileName = fileStorageService.store(file);
        DocumentAttachment attachment = DocumentAttachment.builder()
                .ownerType(ownerType)
                .ownerId(ownerId)
                .fileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : storedFileName)
                .storedFileName(storedFileName)
                .fileType(extension)
                .fileSize(file.getSize())
                .uploadedBy(uploader)
                .build();

        try {
            return mapToDTO(documentAttachmentRepository.save(attachment));
        } catch (RuntimeException ex) {
            // The file was already written to disk above; if the DB row never
            // makes it (DB outage, unexpected constraint violation...) the
            // @Transactional rollback undoes the insert but has no idea the
            // file exists - clean it up here instead of leaking disk space.
            fileStorageService.delete(storedFileName);
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public DocumentAttachmentDTO getById(Long id, User requester) {
        DocumentAttachment attachment = findOrThrow(id);
        enforceOwnerAccess(attachment.getOwnerType(), attachment.getOwnerId(), requester, false);
        return mapToDTO(attachment);
    }

    @Transactional(readOnly = true)
    public List<DocumentAttachmentDTO> listByOwner(DocumentOwnerType ownerType, Long ownerId, User requester) {
        enforceOwnerAccess(ownerType, ownerId, requester, false);
        return documentAttachmentRepository.findByOwnerTypeAndOwnerIdOrderByUploadedAtDesc(ownerType, ownerId)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /** @return the file bytes; caller (controller) is responsible for the Content-Disposition/type headers. */
    @Transactional(readOnly = true)
    public DocumentAttachment loadForDownload(Long id, User requester) {
        DocumentAttachment attachment = findOrThrow(id);
        enforceOwnerAccess(attachment.getOwnerType(), attachment.getOwnerId(), requester, false);
        return attachment;
    }

    public Resource loadFileResource(String storedFileName) {
        return fileStorageService.load(storedFileName);
    }

    public void delete(Long id, User requester) {
        // Fails closed on a null requester, same as enforceOwnerAccess() below -
        // the original `requester != null && ...` form let a null caller
        // through with no check at all instead of being denied.
        if (requester == null || (requester.getRole() != Role.ADMIN && requester.getRole() != Role.PRINCIPAL)) {
            throw new AccessDeniedException("Only ADMIN/PRINCIPAL may delete a document attachment");
        }
        DocumentAttachment attachment = findOrThrow(id);
        documentAttachmentRepository.delete(attachment);
        fileStorageService.delete(attachment.getStoredFileName());
    }

    private DocumentAttachment findOrThrow(Long id) {
        return documentAttachmentRepository.findByIdWithUploadedBy(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document attachment not found with id: " + id));
    }

    private void resolveOwnerOrThrow(DocumentOwnerType ownerType, Long ownerId) {
        boolean exists = switch (ownerType) {
            case STUDENT -> studentRepository.existsById(ownerId);
            case STAFF -> staffRepository.existsById(ownerId);
            case ADMISSION_APPLICATION -> admissionApplicationRepository.existsById(ownerId);
        };
        if (!exists) {
            throw new ResourceNotFoundException(ownerType + " not found with id: " + ownerId);
        }
    }

    private void enforceOwnerAccess(DocumentOwnerType ownerType, Long ownerId, User requester, boolean isWrite) {
        if (ownerType == DocumentOwnerType.STUDENT) {
            studentAccessGuard.enforceCanAccessStudent(ownerId, requester);
            return;
        }
        // STAFF / ADMISSION_APPLICATION: administrative records, no self-service
        // caller - ADMIN/PRINCIPAL only (same restriction for read and write).
        if (requester == null || (requester.getRole() != Role.ADMIN && requester.getRole() != Role.PRINCIPAL)) {
            throw new AccessDeniedException(
                    (isWrite ? "Uploading" : "Accessing") + " " + ownerType + " documents requires ADMIN or PRINCIPAL");
        }
    }

    // Mirrors FileStorageService.extensionOf() exactly (including the
    // non-alphanumeric strip) so the extension this whitelist check accepts
    // and the extension actually written to the stored filename can never
    // silently diverge.
    private String extensionOf(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(dot + 1).toLowerCase().replaceAll("[^a-z0-9]", "");
    }

    private DocumentAttachmentDTO mapToDTO(DocumentAttachment attachment) {
        User uploadedBy = attachment.getUploadedBy();
        return DocumentAttachmentDTO.builder()
                .id(attachment.getId())
                .ownerType(attachment.getOwnerType())
                .ownerId(attachment.getOwnerId())
                .fileName(attachment.getFileName())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .uploadedById(uploadedBy != null ? uploadedBy.getId() : null)
                .uploadedByName(uploadedBy != null ? uploadedBy.getFirstName() + " " + uploadedBy.getLastName() : null)
                .uploadedAt(attachment.getUploadedAt())
                .downloadUrl(contextPath + "/v1/documents/" + attachment.getId() + "/download")
                .build();
    }
}
