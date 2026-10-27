package cl.duoc.digitalfix.audit.web;

import cl.duoc.digitalfix.audit.config.ContextoEmpresa;
import cl.duoc.digitalfix.audit.config.ContextoEmpresaHolder;
import cl.duoc.digitalfix.audit.domain.AuditEvent;
import cl.duoc.digitalfix.audit.error.AccesoDenegado;
import cl.duoc.digitalfix.audit.service.AuditQueryService;
import cl.duoc.digitalfix.audit.service.MonitoreoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditQueryService queryService;
    private final MonitoreoService monitoreoService;

    public AuditController(AuditQueryService queryService, MonitoreoService monitoreoService) {
        this.queryService = queryService;
        this.monitoreoService = monitoreoService;
    }

    @GetMapping("/events")
    public ResponseEntity<Page<AuditEvent>> obtenerEventos(
            @RequestParam(required = false) String autorId,
            @RequestParam(required = false) String tipoEvento,
            @RequestParam(required = false) Long ordenId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            Pageable pageable
    ) {
        ContextoEmpresa contexto = ContextoEmpresaHolder.obtener()
                .orElseThrow(() -> new AccesoDenegado("Falta la cabecera de empresa"));

        // HU-44.3: Validar que el rol sea AUDITOR o ADMIN (Bloquea Supervisor y Cliente con HTTP 403)
        if (!contexto.tieneAccesoAuditoria()) {
            throw new AccesoDenegado("Acceso denegado. Se requiere rol AUDITOR o ADMIN.");
        }

        Page<AuditEvent> resultado = queryService.buscarEventos(
                contexto.companyId(), autorId, tipoEvento, ordenId, desde, hasta, pageable
        );

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/lag")
    public ResponseEntity<Map<String, Object>> obtenerRezago() {
        ContextoEmpresa contexto = ContextoEmpresaHolder.obtener()
                .orElseThrow(() -> new AccesoDenegado("Falta la cabecera de empresa"));

        // Validar explícitamente que el usuario sea ADMIN para ver el estado interno
        boolean esAdmin = contexto.roles() != null && contexto.roles().stream()
                .anyMatch(r -> r.equalsIgnoreCase("ADMIN") || r.equalsIgnoreCase("ROLE_ADMIN"));

        if (!esAdmin) {
            throw new AccesoDenegado("Acceso restringido exclusivamente al rol ADMIN");
        }

        int rezago = monitoreoService.obtenerRezagoAuditoria();
        return ResponseEntity.ok(Map.of(
                "componente", "ms-digitalfix-audit",
                "mensajesPendientes", rezago,
                "estado", rezago == -1 ? "CAIDO" : (rezago >= 100 ? "DEGRADADO" : "OPERATIVO")
        ));
    }
}