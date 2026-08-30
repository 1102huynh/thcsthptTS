package com.schoolmanagement.controller;

import com.schoolmanagement.dto.DocumentAttachmentDTO;
import com.schoolmanagement.entity.DocumentAttachment;
import com.schoolmanagement.entity.DocumentOwnerType;
import com.schoolmanagement.entity.User;
import com.schoolmanagement.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * File attachments (hồ sơ học sinh, hồ sơ nhân sự, giấy tờ tuyển sinh), per
 * IMPLEMENTATION_PLAN.md 3.9. Object-level access (a STUDENT/PARENT only
 * reaching their own/child's documents; STAFF/ADMISSION_APPLICATION
 * restricted to ADMIN/PRINCIPAL) is enforced inside {@link DocumentService} —
 * see its Javadoc.
 */
@RestController
@RequestMapping("/v1/documents")
@AllArgsConstructor
@Tag(name = "Documents (Tệp đính kèm)", description = "Upload/download/xóa file đính kèm cho học sinh, nhân sự, hồ sơ tuyển sinh.")
public class DocumentController {

    private DocumentService documentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','PRINCIPAL','TEACHER','STUDENT','PARENT')")
    @Operation(summary = "Upload a document attachment",
            description = "Max 10MB; accepted types: pdf, jpg, jpeg, png, doc, docx, xls, xlsx.")
    public ResponseEntity<DocumentAttachmentDTO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam DocumentOwnerType ownerType,
            @RequestParam Long ownerId,
            Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        return new ResponseEntity<>(documentService.upload(file, ownerType, ownerId, requester), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PRINCIPAL','TEACHER','STUDENT','PARENT')")
    @Operation(summary = "List documents for one owner")
    public ResponseEntity<List<DocumentAttachmentDTO>> listByOwner(
            @RequestParam DocumentOwnerType ownerType, @RequestParam Long ownerId, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        return new ResponseEntity<>(documentService.listByOwner(ownerType, ownerId, requester), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PRINCIPAL','TEACHER','STUDENT','PARENT')")
    @Operation(summary = "Get one document's metadata")
    public ResponseEntity<DocumentAttachmentDTO> getById(@PathVariable Long id, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        return new ResponseEntity<>(documentService.getById(id, requester), HttpStatus.OK);
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("hasAnyRole('ADMIN','PRINCIPAL','TEACHER','STUDENT','PARENT')")
    @Operation(summary = "Download a document's bytes")
    public ResponseEntity<Resource> download(@PathVariable Long id, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        DocumentAttachment attachment = documentService.loadForDownload(id, requester);
        Resource resource = documentService.loadFileResource(attachment.getStoredFileName());

        HttpHeaders headers = new HttpHeaders();
        // filename(name, charset), not the single-arg overload: a Vietnamese
        // original filename (very plausible on this system - "Học_bạ.pdf" etc.)
        // needs RFC 5987 percent-encoding, which only the 2-arg form applies;
        // the 1-arg form quotes the raw UTF-8 bytes verbatim, which strict
        // HTTP clients can reject or garble.
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(attachment.getFileName(), java.nio.charset.StandardCharsets.UTF_8).build());
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','PRINCIPAL')")
    @Operation(summary = "Delete a document attachment (metadata + the stored file)")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        User requester = (User) authentication.getPrincipal();
        documentService.delete(id, requester);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
