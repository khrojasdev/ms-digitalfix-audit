package cl.duoc.digitalfix.audit.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_event")
@Immutable // Bloquea cualquier intento de UPDATE a nivel de Hibernate
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_evento", nullable = false, updatable = false)
    private String tipoEvento;

    /** Null en los eventos de usuarios, que no tienen orden asociada. */
    @Column(name = "orden_id", updatable = false)
    private Long ordenId;

    @Column(name = "autor_id", nullable = false, updatable = false)
    private String autorId;

    @Column(name = "empresa_id", nullable = false, updatable = false)
    private Long empresaId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @Column(name = "correlacion_id", updatable = false)
    private String correlacionId;

    @Column(name = "event_key", unique = true, updatable = false)
    private String eventKey;

    /** Texto legible: tecnico asignado, comentario del cambio, datos del usuario. */
    @Column(length = 500, updatable = false)
    private String detalle;

    public AuditEvent() {}

    public AuditEvent(String tipoEvento, Long ordenId, String autorId, Long empresaId, LocalDateTime fecha, String correlacionId, String eventKey) {
        this(tipoEvento, ordenId, autorId, empresaId, fecha, correlacionId, eventKey, null);
    }

    public AuditEvent(String tipoEvento, Long ordenId, String autorId, Long empresaId, LocalDateTime fecha,
                      String correlacionId, String eventKey, String detalle) {
        this.tipoEvento = tipoEvento;
        this.ordenId = ordenId;
        this.autorId = autorId;
        this.empresaId = empresaId;
        this.fecha = fecha;
        this.correlacionId = correlacionId;
        this.eventKey = eventKey;
        this.detalle = detalle == null || detalle.length() <= 500 ? detalle : detalle.substring(0, 500);
    }

    // Solo Getters (Sin Setters, garantizando inmutabilidad en el código)
    public Long getId() { return id; }
    public String getTipoEvento() { return tipoEvento; }
    public Long getOrdenId() { return ordenId; }
    public String getAutorId() { return autorId; }
    public Long getEmpresaId() { return empresaId; }
    public LocalDateTime getFecha() { return fecha; }
    public String getCorrelacionId() { return correlacionId; }
    public String getEventKey() { return eventKey; }
    public String getDetalle() { return detalle; }
}