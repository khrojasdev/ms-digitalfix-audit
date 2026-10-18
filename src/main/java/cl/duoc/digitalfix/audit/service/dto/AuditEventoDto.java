package cl.duoc.digitalfix.audit.service.dto;

import java.time.LocalDateTime;

public record AuditEventoDto(
        String eventKey,       // Clave única de idempotencia
        String tipoEvento,     // ej: CREADA, ASIGNADA, INICIADA, CERRADA, CANCELADA
        Long ordenId,
        String autorId,        // OID del usuario en Azure AD
        Long empresaId,
        LocalDateTime fecha,
        String correlacionId
) {}