package broker.broker;

import broker.comun.EstadoRespuesta;
import broker.comun.Respuesta;
import broker.comun.Solicitud;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BrokerTest {

    private RegistroServicios registro;
    private List<String> lineas;
    private Broker broker;

    @BeforeEach
    void setUp() {
        registro = new RegistroServicios();
        lineas = new ArrayList<>();
        broker = new Broker(registro, lineas::add);
    }

    private static Solicitud solicitud(String servicio, String operacion) {
        return new Solicitud(servicio, operacion, Map.of());
    }

    @Test
    void reenviaLaSolicitudAlProveedorRegistrado() {
        ProveedorFalso proveedor = new ProveedorFalso("arroz");
        registro.registrar("productos", proveedor);
        Solicitud solicitud = solicitud("productos", "consultar");

        Respuesta respuesta = broker.enviar(solicitud);

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        assertEquals("arroz", respuesta.getResultado());
        assertEquals(solicitud.getId(), respuesta.getIdSolicitud());
        assertEquals(1, proveedor.getLlamadas());
    }

    @Test
    void servicioDesconocidoDevuelveRespuestaDeError() {
        Solicitud solicitud = solicitud("inventario", "consultar");

        Respuesta respuesta = broker.enviar(solicitud);

        assertEquals(EstadoRespuesta.SERVICIO_DESCONOCIDO, respuesta.getEstado());
        assertEquals(solicitud.getId(), respuesta.getIdSolicitud());
        assertTrue(respuesta.getMensajeError().contains("inventario"));
        assertNull(respuesta.getResultado());
    }

    @Test
    void sustituirProveedorEsTransparenteParaElCliente() {
        ProveedorFalso original = new ProveedorFalso("desde sqlite");
        ProveedorFalso sustituto = new ProveedorFalso("desde memoria");
        registro.registrar("productos", original);
        Solicitud solicitud = solicitud("productos", "consultar");

        broker.enviar(solicitud);
        registro.sustituir("productos", sustituto);
        Respuesta respuesta = broker.enviar(solicitud);

        assertEquals("desde memoria", respuesta.getResultado());
        assertEquals(1, original.getLlamadas());
        assertEquals(1, sustituto.getLlamadas());
    }

    @Test
    void proveedorQueLanzaExcepcionDevuelveErrorProveedor() {
        registro.registrar("ventas", s -> {
            throw new IllegalStateException("se cayó la base");
        });
        Solicitud solicitud = solicitud("ventas", "registrar");

        Respuesta respuesta = assertDoesNotThrow(() -> broker.enviar(solicitud));

        assertEquals(EstadoRespuesta.ERROR_PROVEEDOR, respuesta.getEstado());
        assertEquals(solicitud.getId(), respuesta.getIdSolicitud());
        assertTrue(respuesta.getMensajeError().contains("se cayó la base"));
    }

    @Test
    void proveedorQueLanzaExcepcionSinMensajeTambienSeReporta() {
        registro.registrar("ventas", s -> {
            throw new NullPointerException();
        });

        Respuesta respuesta = broker.enviar(solicitud("ventas", "registrar"));

        assertEquals(EstadoRespuesta.ERROR_PROVEEDOR, respuesta.getEstado());
        assertEquals("NullPointerException", respuesta.getMensajeError());
    }

    @Test
    void proveedorQueDevuelveNullSeReportaComoError() {
        registro.registrar("clientes", s -> null);

        Respuesta respuesta = broker.enviar(solicitud("clientes", "buscar"));

        assertEquals(EstadoRespuesta.ERROR_PROVEEDOR, respuesta.getEstado());
    }

    @Test
    void solicitudNulaDevuelveParametroInvalido() {
        Respuesta respuesta = assertDoesNotThrow(() -> broker.enviar(null));

        assertNotNull(respuesta);
        assertEquals(EstadoRespuesta.PARAMETRO_INVALIDO, respuesta.getEstado());
    }

    @Test
    void fallaEnLaBitacoraNoRompeLaRespuesta() {
        Broker brokerConBitacoraRota = new Broker(registro, linea -> {
            throw new IllegalStateException("disco lleno");
        });
        registro.registrar("productos", new ProveedorFalso("ok"));

        Respuesta respuesta = assertDoesNotThrow(
                () -> brokerConBitacoraRota.enviar(solicitud("productos", "consultar")));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
    }

    @Test
    void escribeUnaLineaDeBitacoraPorSolicitud() {
        registro.registrar("productos", new ProveedorFalso("ok"));
        Solicitud solicitud = solicitud("productos", "consultar");

        broker.enviar(solicitud);
        broker.enviar(solicitud("inventario", "consultar"));

        assertEquals(2, lineas.size());
        String idCorto = solicitud.getId().toString().substring(0, 8);
        assertTrue(lineas.get(0).matches(
                "\\[BROKER] \\d{2}:\\d{2}:\\d{2} id=" + idCorto
                        + " productos\\.consultar → EXITO \\(\\d+ ms\\)"),
                lineas.get(0));
        assertTrue(lineas.get(1).contains("inventario.consultar → SERVICIO_DESCONOCIDO"), lineas.get(1));
    }
}