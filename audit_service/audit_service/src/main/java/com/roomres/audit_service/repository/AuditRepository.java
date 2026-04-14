    package com.roomres.audit_service.repository;

    import com.roomres.audit_service.model.AuditLog;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    import java.util.UUID;

    @Repository
    public interface AuditRepository extends JpaRepository<AuditLog, UUID> {
        // Apenas o padrão para salvar os logs de auditoria
    }