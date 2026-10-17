package cl.duoc.digitalfix.audit.service;

import cl.duoc.digitalfix.audit.domain.AuditEvent;
import cl.duoc.digitalfix.audit.service.dto.AuditEventoDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class AuditEventoConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuditEventoConsumer.class);
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public AuditEventoConsumer(AuditService auditService, ObjectMapper objectMapper) {
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(queues = "${amqp.queue.auditoria}")
    public void consumirEvento(String mensajeJson, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            // 1. Deserializar el mensaje de RabbitMQ
            AuditEventoDto dto = objectMapper.readValue(mensajeJson, AuditEventoDto.class);

            // 2. Mapear a la entidad inmutable
            AuditEvent evento = new AuditEvent(
                    dto.tipoEvento(),
                    dto.ordenId(),
                    dto.autorId(),
                    dto.empresaId(),
                    dto.fecha(),
                    dto.correlacionId(),
                    dto.eventKey()
            );

            // 3. Guardar usando el servicio que creamos (que valida idempotencia)
            auditService.registrarEvento(evento);

            // 4. EXIGENCIA DE RÚBRICA: Confirmación manual de éxito
            channel.basicAck(tag, false);

        } catch (Exception e) {
            log.error("HU-45.3: Error procesando evento de auditoría. Enviando a DLQ. Motivo: {}", e.getMessage(), e);
            // EXIGENCIA DE RÚBRICA: NACK manual enviando el mensaje a la cola de muertos
            channel.basicNack(tag, false, false);
        }
    }
}