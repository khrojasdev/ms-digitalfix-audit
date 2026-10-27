package cl.duoc.digitalfix.audit.web;

import cl.duoc.digitalfix.audit.domain.AuditEvent;
import cl.duoc.digitalfix.audit.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** La bitacora contra H2: guardar, filtrar por orden, aislar por empresa y por rol. */
@SpringBootTest
@AutoConfigureMockMvc
class BitacoraTest {

    @Autowired private MockMvc mvc;
    @Autowired private AuditService servicio;

    private final long orden = Math.abs(UUID.randomUUID().getMostSignificantBits() % 100000);

    @BeforeEach
    void eventos() {
        LocalDateTime t = LocalDateTime.of(2026, 10, 27, 9, 0);
        servicio.registrarEvento(new AuditEvent("CREADA", orden, "oid-cli", 1L, t, null, UUID.randomUUID().toString()));
        servicio.registrarEvento(new AuditEvent("ASIGNADA", orden, "oid-sup", 1L, t.plusMinutes(5), "c-1",
                UUID.randomUUID().toString(), "Tecnico: Pedro Soto"));
        servicio.registrarEvento(new AuditEvent("CREADA", orden + 1, "oid-cli", 1L, t, null, UUID.randomUUID().toString()));
        servicio.registrarEvento(new AuditEvent("CREADA", orden, "oid-otra", 2L, t, null, UUID.randomUUID().toString()));
    }

    private ResultActions consultar(String roles, String empresa, String parametros) throws Exception {
        return mvc.perform(get("/api/audit/events" + parametros)
                .header("X-User-Oid", "oid-aud").header("X-Company-Id", empresa).header("X-Roles", roles));
    }

    @Test
    void elTimelineDeUnaOrdenTraeSoloSusEventosYDeSuEmpresa() throws Exception {
        consultar("AUDITOR", "1", "?ordenId=" + orden + "&sort=fecha,asc")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].tipoEvento").value("CREADA"))
                .andExpect(jsonPath("$.content[1].detalle").value("Tecnico: Pedro Soto"));
    }

    @Test
    void unEventoRepetidoNoSeGuardaDosVeces() throws Exception {
        String clave = "repetido-" + orden;
        servicio.registrarEvento(new AuditEvent("CERRADA", orden, "oid-tec", 1L, LocalDateTime.now(), null, clave));
        servicio.registrarEvento(new AuditEvent("CERRADA", orden, "oid-tec", 1L, LocalDateTime.now(), null, clave));
        consultar("ADMIN", "1", "?ordenId=" + orden + "&tipoEvento=CERRADA")
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void elSupervisorNoEntraALaBitacora() throws Exception {
        consultar("SUPERVISOR", "1", "").andExpect(status().isForbidden());
    }
}
