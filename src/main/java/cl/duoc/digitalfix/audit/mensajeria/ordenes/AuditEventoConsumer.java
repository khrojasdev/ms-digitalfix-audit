package cl.duoc.digitalfix.audit.mensajeria.ordenes;

import cl.duoc.digitalfix.audit.domain.AuditEvent;
import cl.duoc.digitalfix.audit.mensajeria.MensajeInvalido;
import cl.duoc.digitalfix.audit.mensajeria.ProcesadorDeMensajes;
import cl.duoc.digitalfix.audit.service.AuditService;
import cl.duoc.digitalfix.audit.service.dto.AuditEventoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Dominio ordenes: registra en la bitacora cada evento de orden de trabajo
 * (creada, asignada, en desplazamiento, en ejecucion, cerrada, cancelada).
 *
 * ACK, reintento y DLQ los decide {@link ProcesadorDeMensajes}. Un evento sin
 * orden, autor o empresa es invalido y va directo a la DLQ.
 */
@Component
public class AuditEventoConsumer {

    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final ProcesadorDeMensajes procesador;
    private final String cola;

    public AuditEventoConsumer(AuditService auditService, ObjectMapper objectMapper, ProcesadorDeMensajes procesador,
                               @Value("${amqp.queue.auditoria}") String cola) {
        this.auditService = auditService;
        this.objectMapper = objectMapper;
        this.procesador = procesador;
        this.cola = cola;
    }

    @RabbitListener(queues = "${amqp.queue.auditoria}")
    public void consumirEvento(String mensajeJson, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        procesador.procesar(cola, channel, tag, () -> {
            AuditEventoDto dto = objectMapper.readValue(mensajeJson, AuditEventoDto.class);
            auditService.registrarEvento(aEvento(dto));
        });
    }

    static AuditEvent aEvento(AuditEventoDto dto) {
        if (dto.tipoEvento() == null || dto.ordenId() == null || dto.autorId() == null || dto.empresaId() == null) {
            throw new MensajeInvalido("evento de orden incompleto: faltan tipo, orden, autor o empresa");
        }
        return new AuditEvent(
                dto.tipoEvento(),
                dto.ordenId(),
                dto.autorId(),
                dto.empresaId(),
                dto.fecha() == null ? java.time.LocalDateTime.now() : dto.fecha(),
                dto.correlacionId(),
                dto.eventKey(),
                detalle(dto));
    }

    /** "Tecnico: Pedro Soto · Comentario: llegue al lugar" */
    static String detalle(AuditEventoDto dto) {
        List<String> partes = new ArrayList<>();
        boolean asignacion = "ASIGNADA".equalsIgnoreCase(dto.tipoEvento());
        if (asignacion && dto.tecnicoNombre() != null && !dto.tecnicoNombre().isBlank()) {
            boolean reasignacion = "ASIGNADA".equalsIgnoreCase(dto.estadoAnterior());
            partes.add((reasignacion ? "Reasignada a " : "Tecnico: ") + dto.tecnicoNombre().trim());
        }
        if (dto.nota() != null && !dto.nota().isBlank()) {
            partes.add("Comentario: " + dto.nota().trim());
        }
        return partes.isEmpty() ? null : String.join(" · ", partes);
    }
}
