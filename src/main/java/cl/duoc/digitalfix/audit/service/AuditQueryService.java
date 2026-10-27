package cl.duoc.digitalfix.audit.service;

import cl.duoc.digitalfix.audit.domain.AuditEvent;
import cl.duoc.digitalfix.audit.repository.AuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditQueryService {

    private final AuditEventRepository repository;

    public AuditQueryService(AuditEventRepository repository) {
        this.repository = repository;
    }

    public Page<AuditEvent> buscarEventos(Long empresaId, String autorId, String tipoEvento, LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        return buscarEventos(empresaId, autorId, tipoEvento, null, desde, hasta, pageable);
    }

    /** Igual que el anterior, y con ordenId arma el timeline de una orden. */
    public Page<AuditEvent> buscarEventos(Long empresaId, String autorId, String tipoEvento, Long ordenId,
                                          LocalDateTime desde, LocalDateTime hasta, Pageable pageable) {
        Specification<AuditEvent> spec = (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();

            // Filtro de aislamiento (obligatorio)
            predicados.add(cb.equal(root.get("empresaId"), empresaId));

            // Filtros opcionales combinables (HU-44.1)
            if (autorId != null && !autorId.isBlank()) {
                predicados.add(cb.equal(root.get("autorId"), autorId));
            }
            if (tipoEvento != null && !tipoEvento.isBlank()) {
                predicados.add(cb.equal(root.get("tipoEvento"), tipoEvento));
            }
            if (ordenId != null) {
                predicados.add(cb.equal(root.get("ordenId"), ordenId));
            }
            if (desde != null) {
                predicados.add(cb.greaterThanOrEqualTo(root.get("fecha"), desde));
            }
            if (hasta != null) {
                predicados.add(cb.lessThanOrEqualTo(root.get("fecha"), hasta));
            }

            return cb.and(predicados.toArray(new Predicate[0]));
        };

        return repository.findAll(spec, pageable);
    }
}