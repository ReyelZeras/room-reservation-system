package com.roomres.audit_service.repository;

import com.roomres.audit_service.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface AuditRepository extends JpaRepository<AuditLog, UUID> {

    // Método utilizado pelo ItemReader do Spring Batch para buscar logs antigos
    Page<AuditLog> findByTimestampBefore(LocalDateTime timestamp, Pageable pageable);
}