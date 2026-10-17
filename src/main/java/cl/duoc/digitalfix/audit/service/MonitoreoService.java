package cl.duoc.digitalfix.audit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class MonitoreoService {

    private static final Logger log = LoggerFactory.getLogger(MonitoreoService.class);
    private final RabbitAdmin rabbitAdmin;

    @Value("${amqp.queue.auditoria}")
    private String queueName;

    public MonitoreoService(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    // HU-46.1: Calcula el rezago (lag) basado en la profundidad de la cola
    public int obtenerRezagoAuditoria() {
        try {
            Properties propiedadesCola = rabbitAdmin.getQueueProperties(queueName);
            if (propiedadesCola != null && propiedadesCola.containsKey(RabbitAdmin.QUEUE_MESSAGE_COUNT)) {
                return (int) propiedadesCola.get(RabbitAdmin.QUEUE_MESSAGE_COUNT);
            }
            return 0; // Si no hay mensajes, el rezago es cero
        } catch (Exception e) {
            log.error("HU-46.3: Error al consultar RabbitMQ. Respuesta controlada para evitar caída.", e);
            return -1; // -1 indica que no se pudo medir (falla controlada)
        }
    }
}