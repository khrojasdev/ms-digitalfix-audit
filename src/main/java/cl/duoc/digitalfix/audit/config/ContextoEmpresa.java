package cl.duoc.digitalfix.audit.config;
import java.util.Set;
public record ContextoEmpresa(String userOid, Long companyId, Set<String> roles, String correlacion) {
    public boolean tieneAccesoAuditoria() {
        return roles != null && roles.stream()
                .anyMatch(r -> r.equalsIgnoreCase("ADMIN") || r.equalsIgnoreCase("ROLE_ADMIN")
                        || r.equalsIgnoreCase("AUDITOR") || r.equalsIgnoreCase("ROLE_AUDITOR"));
    }
}