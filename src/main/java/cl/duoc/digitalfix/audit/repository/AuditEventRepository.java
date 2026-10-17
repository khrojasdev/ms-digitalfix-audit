package cl.duoc.digitalfix.audit.repository;

import cl.duoc.digitalfix.audit.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {

    // Validar idempotencia rápidamente sin traer toda la entidad
    boolean existsByEventKey(String eventKey);
}