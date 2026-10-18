ALTER TABLE audit_event ADD event_key VARCHAR2(150);
ALTER TABLE audit_event ADD CONSTRAINT uk_audit_event_key UNIQUE (event_key);