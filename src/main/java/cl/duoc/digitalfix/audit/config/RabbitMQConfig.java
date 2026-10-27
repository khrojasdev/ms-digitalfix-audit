package cl.duoc.digitalfix.audit.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologia de la auditoria. Dos rutas de mensajeria, cada una con su cola y
 * su DLQ (por el exchange por defecto):
 *
 *  - ordenes:  ordenes.negocio.exchange  --ordenes.eventos.*-->  ordenes.auditoria.queue
 *  - usuarios: usuarios.negocio.exchange --usuarios.eventos.#--> usuarios.auditoria.queue
 *
 * Los nombres se leen de application.yml (amqp.*).
 */
@Configuration
public class RabbitMQConfig {

    @Value("${amqp.exchange.negocio}")
    private String exchangeName;

    @Value("${amqp.queue.auditoria}")
    private String queueName;

    @Value("${amqp.queue.auditoria-dlq}")
    private String dlqName;

    @Value("${amqp.routing-key.auditoria}")
    private String routingKey;

    // 1. Exchange Principal
    @Bean
    public TopicExchange negocioExchange() {
        return new TopicExchange(exchangeName, true, false);
    }

    // 2. Dead Letter Queue (DLQ) para apartar eventos con error (HU-45.3)
    @Bean
    public Queue auditoriaDlq() {
        return QueueBuilder.durable(dlqName).build();
    }

    // 3. Cola Principal de Auditoría (con enlace directo a la DLQ)
    @Bean
    public Queue auditoriaQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", dlqName)
                .build();
    }

    // 4. Binding: Enlaza la cola al exchange
    @Bean
    public Binding auditoriaBinding(Queue auditoriaQueue, TopicExchange negocioExchange) {
        return BindingBuilder.bind(auditoriaQueue).to(negocioExchange).with(routingKey);
    }

    // ---- Ruta usuarios ----

    @Value("${amqp.exchange.usuarios}")
    private String usuariosExchangeName;

    @Value("${amqp.queue.usuarios}")
    private String usuariosQueueName;

    @Value("${amqp.queue.usuarios-dlq}")
    private String usuariosDlqName;

    @Value("${amqp.routing-key.usuarios}")
    private String usuariosRoutingKey;

    /** Mismas propiedades con que lo declara ms-digitalfix-usuarios (topic, durable). */
    @Bean
    public TopicExchange usuariosExchange() {
        return new TopicExchange(usuariosExchangeName, true, false);
    }

    @Bean
    public Queue usuariosAuditoriaDlq() {
        return QueueBuilder.durable(usuariosDlqName).build();
    }

    @Bean
    public Queue usuariosAuditoriaQueue() {
        return QueueBuilder.durable(usuariosQueueName)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", usuariosDlqName)
                .build();
    }

    @Bean
    public Binding usuariosAuditoriaBinding(Queue usuariosAuditoriaQueue, TopicExchange usuariosExchange) {
        return BindingBuilder.bind(usuariosAuditoriaQueue).to(usuariosExchange).with(usuariosRoutingKey);
    }

    // Administrador para poder consultar el estado y profundidad de las colas
    @Bean
    public org.springframework.amqp.rabbit.core.RabbitAdmin rabbitAdmin(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
        return new org.springframework.amqp.rabbit.core.RabbitAdmin(connectionFactory);
    }
}