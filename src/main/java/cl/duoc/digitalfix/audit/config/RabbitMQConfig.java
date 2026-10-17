package cl.duoc.digitalfix.audit.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        return new TopicExchange(exchangeName);
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
}