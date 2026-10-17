CREATE TABLE audit_event (
                             id NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                             tipo_evento VARCHAR2(50) NOT NULL,
                             orden_id NUMBER NOT NULL,
                             autor_id VARCHAR2(100) NOT NULL, -- Es VARCHAR porque en Azure AD (OID) es un texto
                             empresa_id NUMBER NOT NULL,
                             fecha TIMESTAMP NOT NULL,
                             correlacion_id VARCHAR2(100) NOT NULL
);