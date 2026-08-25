package com.schoolmanagement.repository;

import com.schoolmanagement.entity.NotificationRecipient;
import com.schoolmanagement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRecipientRepository extends JpaRepository<NotificationRecipient, Long> {
    List<NotificationRecipient> findByRecipientOrderByCreatedAtDesc(User recipient);
}
