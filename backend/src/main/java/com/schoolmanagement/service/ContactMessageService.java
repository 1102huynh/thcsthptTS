package com.schoolmanagement.service;

import com.schoolmanagement.dto.ContactMessageDTO;
import com.schoolmanagement.dto.ContactRequest;
import com.schoolmanagement.entity.ContactMessage;
import com.schoolmanagement.exception.ResourceNotFoundException;
import com.schoolmanagement.repository.ContactMessageRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Transactional
public class ContactMessageService {

    private ContactMessageRepository repository;

    /** Public submission (rate-limited by ContactRateLimitFilter). */
    public void submit(ContactRequest request) {
        repository.save(ContactMessage.builder()
                .fullName(request.getFullName().trim())
                .email(trimToNull(request.getEmail()))
                .phone(trimToNull(request.getPhone()))
                .subject(trimToNull(request.getSubject()))
                .message(request.getMessage().trim())
                .handled(false)
                .build());
    }

    @Transactional(readOnly = true)
    public Page<ContactMessageDTO> list(Pageable pageable) {
        return repository.findAllByOrderByHandledAscCreatedAtDesc(pageable).map(this::toDTO);
    }

    public ContactMessageDTO markHandled(Long id, boolean handled) {
        ContactMessage m = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message not found with id: " + id));
        m.setHandled(handled);
        return toDTO(repository.save(m));
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private ContactMessageDTO toDTO(ContactMessage m) {
        return ContactMessageDTO.builder()
                .id(m.getId()).fullName(m.getFullName()).email(m.getEmail()).phone(m.getPhone())
                .subject(m.getSubject()).message(m.getMessage()).handled(m.getHandled())
                .createdAt(m.getCreatedAt())
                .build();
    }
}
