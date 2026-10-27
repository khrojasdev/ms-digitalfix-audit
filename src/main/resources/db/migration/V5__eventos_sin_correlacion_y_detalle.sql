-- Los eventos llegaban sin id de correlacion (el BFF no lo propagaba) y la
-- columna lo exigia: el INSERT fallaba y cada evento terminaba en la DLQ. La
-- correlacion es un dato de trazabilidad util pero no obligatorio.
ALTER TABLE audit_event MODIFY correlacion_id NULL;

-- Los eventos de usuarios (alta, activacion, baja, cambio de rol) no tienen
-- orden asociada.
ALTER TABLE audit_event MODIFY orden_id NULL;

-- Texto legible del evento: tecnico asignado, comentario del cambio de estado,
-- correo y rol del usuario, etc.
ALTER TABLE audit_event ADD detalle VARCHAR2(500);

CREATE INDEX idx_audit_orden ON audit_event (empresa_id, orden_id, fecha);
