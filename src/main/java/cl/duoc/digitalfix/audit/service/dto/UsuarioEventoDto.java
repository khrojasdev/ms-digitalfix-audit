package cl.duoc.digitalfix.audit.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

/**
 * Evento de usuario que publica ms-digitalfix-usuarios por
 * usuarios.negocio.exchange. tipoEvento: CREADO, ACTIVADO, DESACTIVADO,
 * ROL_CAMBIADO.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UsuarioEventoDto(
        String tipoEvento,
        String oid,
        String email,
        String rol,
        Long empresaId,
        Boolean activo,
        LocalDateTime fecha
) {}
