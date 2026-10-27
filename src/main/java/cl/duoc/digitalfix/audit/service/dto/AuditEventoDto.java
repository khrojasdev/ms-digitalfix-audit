package cl.duoc.digitalfix.audit.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * Evento de orden tal como lo publica ms-digitalfix-workorders. Se leen solo
 * los campos que la auditoria necesita; los demas se ignoran para no romperse
 * cuando el productor agregue campos nuevos.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuditEventoDto(
        String eventKey,       // Clave unica de idempotencia
        String tipoEvento,     // CREADA, ASIGNADA, EN_DESPLAZAMIENTO, EN_EJECUCION, CERRADA, CANCELADA
        Long ordenId,
        String autorId,        // OID de quien hizo el cambio
        Long empresaId,
        LocalDateTime fecha,
        String correlacionId,
        String estadoAnterior,
        String tecnicoNombre,
        String nota            // comentario opcional del cambio de estado
) {}
