package cl.duoc.digitalfix.audit.repository;

import cl.duoc.digitalfix.audit.domain.AuditEvent;
import org.springframework.data.repository.Repository;

import java.util.Optional;

// Al heredar de Repository (en vez de JpaRepository), ocultamos todos los métodos DELETE
public interface AuditEventRepository extends Repository<AuditEvent, Long> {

    // Solo permitimos insertar
    AuditEvent save(AuditEvent evento);

    // Solo permitimos leer
    Optional<AuditEvent> findById(Long id);

    // Validar idempotencia
    boolean existsByEventKey(String eventKey);
}