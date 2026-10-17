package cl.duoc.digitalfix.audit.repository;

import cl.duoc.digitalfix.audit.domain.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface AuditEventRepository extends Repository<AuditEvent, Long> {

    AuditEvent save(AuditEvent evento);
    Optional<AuditEvent> findById(Long id);
    boolean existsByEventKey(String eventKey);

    // HU-44.1: Consulta paginada con filtros dinámicos (sin exponer metodos de borrado)
    Page<AuditEvent> findAll(Specification<AuditEvent> spec, Pageable pageable);
}