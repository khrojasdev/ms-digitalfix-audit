package cl.duoc.digitalfix.audit.service;

import cl.duoc.digitalfix.audit.domain.AuditEvent;
import cl.duoc.digitalfix.audit.repository.AuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditEventRepository repository;

    public AuditService(AuditEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void registrarEvento(AuditEvent evento) {
        if (evento.getEventKey() != null && repository.existsByEventKey(evento.getEventKey())) {
            log.info("Evento duplicado ignorado por clave de idempotencia: {}", evento.getEventKey());
            return;
        }

        repository.save(evento);
        log.info("Evento de auditoria registrado con exito. Tipo: {}, Orden: {}", evento.getTipoEvento(), evento.getOrdenId());
    }
}