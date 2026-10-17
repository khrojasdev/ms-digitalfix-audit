package cl.duoc.digitalfix.audit.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_event")
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento;

    @Column(name = "orden_id", nullable = false)
    private Long ordenId;

    @Column(name = "autor_id", nullable = false)
    private String autorId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(name = "correlacion_id", nullable = false)
    private String correlacionId;

    // Nueva clave de idempotencia
    @Column(name = "event_key", unique = true)
    private String eventKey;

    public AuditEvent() {}

    public AuditEvent(String tipoEvento, Long ordenId, String autorId, Long empresaId, LocalDateTime fecha, String correlacionId, String eventKey) {
        this.tipoEvento = tipoEvento;
        this.ordenId = ordenId;
        this.autorId = autorId;
        this.empresaId = empresaId;
        this.fecha = fecha;
        this.correlacionId = correlacionId;
        this.eventKey = eventKey;
    }

    // Getters
    public Long getId() { return id; }
    public String getTipoEvento() { return tipoEvento; }
    public Long getOrdenId() { return ordenId; }
    public String getAutorId() { return autorId; }
    public Long getEmpresaId() { return empresaId; }
    public LocalDateTime getFecha() { return fecha; }
    public String getCorrelacionId() { return correlacionId; }
    public String getEventKey() { return eventKey; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public void setOrdenId(Long ordenId) { this.ordenId = ordenId; }
    public void setAutorId(String autorId) { this.autorId = autorId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public void setCorrelacionId(String correlacionId) { this.correlacionId = correlacionId; }
    public void setEventKey(String eventKey) { this.eventKey = eventKey; }
}