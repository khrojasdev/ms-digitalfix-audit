package cl.duoc.digitalfix.audit.mensajeria;

import cl.duoc.digitalfix.audit.domain.AuditEvent;
import cl.duoc.digitalfix.audit.mensajeria.ordenes.AuditEventoConsumer;
import cl.duoc.digitalfix.audit.mensajeria.usuarios.UsuarioEventoConsumer;
import cl.duoc.digitalfix.audit.service.AuditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Los dos consumidores de la auditoria: que guardan, que confirman y que mandan a la DLQ. */
class ConsumidoresAuditoriaTest {

    private final AuditService servicio = mock(AuditService.class);
    private final Channel canal = mock(Channel.class);
    private final ObjectMapper json = new ObjectMapper().findAndRegisterModules();
    private final ProcesadorDeMensajes procesador = new ProcesadorDeMensajes(3, 1);

    private final AuditEventoConsumer ordenes = new AuditEventoConsumer(servicio, json, procesador, "ordenes.auditoria.queue");
    private final UsuarioEventoConsumer usuarios = new UsuarioEventoConsumer(servicio, json, procesador, "usuarios.auditoria.queue");

    private AuditEvent guardado() {
        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(servicio).registrarEvento(captor.capture());
        return captor.getValue();
    }

    @Test
    void guardaElEventoDeOrdenAunqueNoTraigaCorrelacion() throws Exception {
        // Asi llegaban los eventos en produccion: sin correlacionId y con campos
        // nuevos. Antes el INSERT fallaba y todo terminaba en la DLQ.
        String mensaje = "{\"eventKey\":\"k1\",\"tipoEvento\":\"ASIGNADA\",\"ordenId\":9,\"autorId\":\"oid-sup\","
                + "\"empresaId\":1,\"fecha\":\"2026-10-26T10:00:00\",\"correlacionId\":null,"
                + "\"estadoAnterior\":\"CREADA\",\"tecnicoNombre\":\"Pedro Soto\",\"servicioId\":3}";

        ordenes.consumirEvento(mensaje, canal, 1L);

        AuditEvent evento = guardado();
        assertThat(evento.getOrdenId()).isEqualTo(9L);
        assertThat(evento.getCorrelacionId()).isNull();
        assertThat(evento.getDetalle()).isEqualTo("Tecnico: Pedro Soto");
        verify(canal).basicAck(1L, false);
    }

    @Test
    void elComentarioDelCambioQuedaEnElDetalle() throws Exception {
        String mensaje = "{\"eventKey\":\"k2\",\"tipoEvento\":\"EN_EJECUCION\",\"ordenId\":9,\"autorId\":\"oid-tec\","
                + "\"empresaId\":1,\"nota\":\"Se cambio el diferencial\"}";
        ordenes.consumirEvento(mensaje, canal, 2L);
        assertThat(guardado().getDetalle()).isEqualTo("Comentario: Se cambio el diferencial");
    }

    @Test
    void eventoDeOrdenIncompletoVaALaDlq() throws Exception {
        ordenes.consumirEvento("{\"tipoEvento\":\"CREADA\",\"ordenId\":9}", canal, 3L);
        verify(servicio, never()).registrarEvento(any());
        verify(canal).basicNack(3L, false, false);
    }

    @Test
    void jsonIlegibleVaALaDlq() throws Exception {
        ordenes.consumirEvento("esto no es json", canal, 4L);
        verify(canal).basicNack(4L, false, false);
    }

    @Test
    void guardaLosEventosDeUsuarios() throws Exception {
        String mensaje = "{\"tipoEvento\":\"ROL_CAMBIADO\",\"oid\":\"oid-ana\",\"email\":\"ana@digitalfix.cl\","
                + "\"rol\":\"TECNICO\",\"empresaId\":1,\"activo\":true,\"fecha\":\"2026-10-27T09:00:00\"}";

        usuarios.consumirEvento(mensaje, canal, 5L);

        AuditEvent evento = guardado();
        assertThat(evento.getTipoEvento()).isEqualTo("USUARIO_ROL_CAMBIADO");
        assertThat(evento.getOrdenId()).isNull();
        assertThat(evento.getAutorId()).isEqualTo("oid-ana");
        assertThat(evento.getDetalle()).isEqualTo("ana@digitalfix.cl · rol TECNICO");
        assertThat(evento.getEventKey()).isEqualTo("usuario:oid-ana:USUARIO_ROL_CAMBIADO:2026-10-27T09:00");
        verify(canal).basicAck(5L, false);
    }

    @Test
    void eventoDeUsuarioSinEmpresaVaALaDlq() throws Exception {
        usuarios.consumirEvento("{\"tipoEvento\":\"CREADO\",\"oid\":\"oid-x\"}", canal, 6L);
        verify(canal).basicNack(6L, false, false);
    }
}
