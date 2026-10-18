package cl.duoc.digitalfix.audit.actuator;

import cl.duoc.digitalfix.audit.service.MonitoreoService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("rezagoAuditoria")
public class RezagoHealthIndicator implements HealthIndicator {

    private final MonitoreoService monitoreoService;
    // Umbral para considerar que el sistema está degradado
    private static final int UMBRAL_REZAGO_DEGRADADO = 100;

    public RezagoHealthIndicator(MonitoreoService monitoreoService) {
        this.monitoreoService = monitoreoService;
    }

    @Override
    public Health health() {
        int rezago = monitoreoService.obtenerRezagoAuditoria();

        // HU-46.3: Falla controlada si RabbitMQ está caído
        if (rezago == -1) {
            return Health.down().withDetail("motivo", "No se pudo conectar con el broker de mensajería").build();
        }

        // HU-46.2: Reportar estado degradado ante rezago sostenido
        if (rezago >= UMBRAL_REZAGO_DEGRADADO) {
            return Health.status("DEGRADED")
                    .withDetail("mensajes_pendientes", rezago)
                    .withDetail("mensaje", "La auditoría tiene un alto rezago, los consumidores no dan abasto")
                    .build();
        }

        return Health.up()
                .withDetail("mensajes_pendientes", rezago)
                .build();
    }
}