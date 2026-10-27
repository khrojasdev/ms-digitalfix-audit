package cl.duoc.digitalfix.audit.mensajeria.usuarios;

import cl.duoc.digitalfix.audit.domain.AuditEvent;
import cl.duoc.digitalfix.audit.mensajeria.MensajeInvalido;
import cl.duoc.digitalfix.audit.mensajeria.ProcesadorDeMensajes;
import cl.duoc.digitalfix.audit.service.AuditService;
import cl.duoc.digitalfix.audit.service.dto.UsuarioEventoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Dominio usuarios: registra en la bitacora las altas, activaciones, bajas y
 * cambios de rol que publica ms-digitalfix-usuarios. Antes esos eventos se
 * publicaban en un exchange que nadie escuchaba y se perdian.
 */
@Component
public class UsuarioEventoConsumer {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final ProcesadorDeMensajes procesador;
    private final String cola;

    public UsuarioEventoConsumer(AuditService auditService, ObjectMapper objectMapper, ProcesadorDeMensajes procesador,
                                 @Value("${amqp.queue.usuarios}") String cola) {
        this.auditService = auditService;
        this.objectMapper = objectMapper;
        this.procesador = procesador;
        this.cola = cola;
    }

    @RabbitListener(queues = "${amqp.queue.usuarios}")
    public void consumirEvento(String mensajeJson, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        procesador.procesar(cola, channel, tag, () -> {
            UsuarioEventoDto dto = objectMapper.readValue(mensajeJson, UsuarioEventoDto.class);
            auditService.registrarEvento(aEvento(dto));
        });
    }

    static AuditEvent aEvento(UsuarioEventoDto dto) {
        if (dto.tipoEvento() == null || dto.oid() == null || dto.empresaId() == null) {
            throw new MensajeInvalido("evento de usuario incompleto: faltan tipo, oid o empresa");
        }
        LocalDateTime fecha = dto.fecha() == null ? LocalDateTime.now() : dto.fecha();
        String tipo = "USUARIO_" + dto.tipoEvento().trim().toUpperCase();
        // Sin eventKey en el contrato de usuarios: la clave se arma con lo que
        // identifica al evento, asi una entrega repetida no se registra dos veces.
        String clave = "usuario:" + dto.oid() + ":" + tipo + ":" + fecha;
        String detalle = (dto.email() == null ? "" : dto.email())
                + (dto.rol() == null ? "" : " · rol " + dto.rol());
        return new AuditEvent(tipo, null, dto.oid(), dto.empresaId(), fecha, null, clave,
                detalle.isBlank() ? null : detalle.trim());
    }
}
