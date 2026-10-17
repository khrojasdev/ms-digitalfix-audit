CREATE INDEX idx_audit_fecha ON audit_event(empresa_id, fecha DESC);
CREATE INDEX idx_audit_filtros ON audit_event(empresa_id, tipo_evento, autor_id);