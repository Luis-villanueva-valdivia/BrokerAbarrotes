package broker.red;

import broker.comun.EstadoRespuesta;
import broker.comun.Respuesta;
import broker.comun.Solicitud;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de ida y vuelta y de rechazo de JSON mal formado o con tipos incorrectos para MarshallerJson.
 */
class MarshallerJsonTest {

    private static final String ID_DE_PRUEBA = "123e4567-e89b-12d3-a456-426614174000";

    private MarshallerJson marshaller;

    @BeforeEach
    void setUp() {
        marshaller = new MarshallerJson();
    }

    @Test
    void solicitudIdaYVueltaDaLaMismaSolicitud() {
        Solicitud original = new Solicitud("productos", "consultar", Map.of("codigo", "CC001"));

        Solicitud copia = marshaller.solicitudDesde(marshaller.aJson(original));

        assertEquals(original.getId(), copia.getId());
        assertEquals(original.getServicio(), copia.getServicio());
        assertEquals(original.getOperacion(), copia.getOperacion());
        assertEquals(original.getParametros(), copia.getParametros());
        assertEquals(original.getFechaHora(), copia.getFechaHora());
    }

    @Test
    void solicitudSeEscribeEnUnaSolaLinea() {
        String json = marshaller.aJson(new Solicitud("ventas", "totalDelDia", Map.of("fecha", "2026-05-12")));

        assertFalse(json.contains("\n"));
        assertFalse(json.contains("\r"));
    }

    @Test
    void respuestaExitosaIdaYVueltaConservaEstadoYResultado() {
        UUID id = UUID.randomUUID();
        Respuesta original = Respuesta.exito(id, "Coca Cola");

        Respuesta copia = marshaller.respuestaDesde(marshaller.aJson(original));

        assertEquals(id, copia.getIdSolicitud());
        assertEquals(EstadoRespuesta.EXITO, copia.getEstado());
        assertEquals("Coca Cola", copia.getResultado());
    }

    @Test
    void respuestaDeErrorIdaYVueltaConservaEstadoYMensaje() {
        UUID id = UUID.randomUUID();
        Respuesta original = Respuesta.error(id, EstadoRespuesta.PARAMETRO_INVALIDO, "umbral inválido");

        Respuesta copia = marshaller.respuestaDesde(marshaller.aJson(original));

        assertEquals(id, copia.getIdSolicitud());
        assertEquals(EstadoRespuesta.PARAMETRO_INVALIDO, copia.getEstado());
        assertEquals("umbral inválido", copia.getMensajeError());
    }

    @Test
    void mensajeRegistroIdaYVueltaDaElMismoMensaje() {
        MensajeRegistro original = new MensajeRegistro("REGISTRAR", "productos", "localhost", 6001);

        String json = marshaller.aJson(original);

        assertTrue(json.contains("\"tipo\":\"REGISTRAR\""));
        assertEquals(original, marshaller.registroDesde(json));
    }

    @Test
    void jsonMalformadoDaIllegalArgumentEnSolicitud() {
        assertThrows(IllegalArgumentException.class, () -> marshaller.solicitudDesde("{servicio:"));
    }

    @Test
    void jsonVacioDaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> marshaller.solicitudDesde(null));
        assertThrows(IllegalArgumentException.class, () -> marshaller.solicitudDesde(""));
        assertThrows(IllegalArgumentException.class, () -> marshaller.respuestaDesde("   "));
    }

    @Test
    void solicitudSinCamposObligatoriosDaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> marshaller.solicitudDesde("{}"));
    }

    @Test
    void fechaMalEscritaDaIllegalArgument() {
        String json = "{\"id\":\"" + ID_DE_PRUEBA + "\",\"servicio\":\"productos\","
                + "\"operacion\":\"consultar\",\"parametros\":{},\"fechaHora\":\"ayer\"}";

        assertThrows(IllegalArgumentException.class, () -> marshaller.solicitudDesde(json));
    }

    @Test
    void estadoDesconocidoEnRespuestaDaIllegalArgument() {
        String json = "{\"idSolicitud\":\"" + ID_DE_PRUEBA + "\",\"estado\":\"NO_EXISTE\"}";

        assertThrows(IllegalArgumentException.class, () -> marshaller.respuestaDesde(json));
    }

    @Test
    void registroConTipoIncorrectoDaIllegalArgument() {
        String json = "{\"tipo\":\"BORRAR\",\"servicio\":\"productos\",\"host\":\"localhost\",\"puerto\":6001}";

        assertThrows(IllegalArgumentException.class, () -> marshaller.registroDesde(json));
    }

    @Test
    void registroConPuertoFueraDeRangoDaIllegalArgument() {
        String json = "{\"tipo\":\"REGISTRAR\",\"servicio\":\"productos\",\"host\":\"localhost\",\"puerto\":70000}";

        assertThrows(IllegalArgumentException.class, () -> marshaller.registroDesde(json));
    }

    @Test
    void registroSinServicioDaIllegalArgument() {
        String json = "{\"tipo\":\"REGISTRAR\",\"host\":\"localhost\",\"puerto\":6001}";

        assertThrows(IllegalArgumentException.class, () -> marshaller.registroDesde(json));
    }

    @Test
    void solicitudConIdNumericoDaIllegalArgument() {
        String json = "{\"id\":123,\"servicio\":\"productos\",\"operacion\":\"consultar\","
                + "\"parametros\":{},\"fechaHora\":\"2026-05-12T10:30:00\"}";

        assertThrows(IllegalArgumentException.class, () -> marshaller.solicitudDesde(json));
    }

    @Test
    void solicitudConParametrosComoTextoDaIllegalArgument() {
        String json = "{\"id\":\"" + ID_DE_PRUEBA + "\",\"servicio\":\"productos\","
                + "\"operacion\":\"consultar\",\"parametros\":\"codigo=CC001\",\"fechaHora\":\"2026-05-12T10:30:00\"}";

        assertThrows(IllegalArgumentException.class, () -> marshaller.solicitudDesde(json));
    }

    @Test
    void jsonQueNoEsObjetoDaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> marshaller.solicitudDesde("[1,2,3]"));
        assertThrows(IllegalArgumentException.class, () -> marshaller.solicitudDesde("\"texto\""));
    }

    @Test
    void fechaConNumeroDaIllegalArgument() {
        String json = "{\"id\":\"" + ID_DE_PRUEBA + "\",\"servicio\":\"productos\","
                + "\"operacion\":\"consultar\",\"parametros\":{},\"fechaHora\":12}";

        assertThrows(IllegalArgumentException.class, () -> marshaller.solicitudDesde(json));
    }

    @Test
    void respuestaConIdNumericoDaIllegalArgument() {
        String json = "{\"idSolicitud\":42,\"estado\":\"EXITO\"}";

        assertThrows(IllegalArgumentException.class, () -> marshaller.respuestaDesde(json));
    }

    @Test
    void registroConPuertoComoTextoDaIllegalArgument() {
        String json = "{\"tipo\":\"REGISTRAR\",\"servicio\":\"productos\",\"host\":\"localhost\",\"puerto\":\"seis\"}";

        assertThrows(IllegalArgumentException.class, () -> marshaller.registroDesde(json));
    }

    @Test
    void registroConPuertoDecimalDaIllegalArgument() {
        String json = "{\"tipo\":\"REGISTRAR\",\"servicio\":\"productos\",\"host\":\"localhost\",\"puerto\":6001.5}";

        assertThrows(IllegalArgumentException.class, () -> marshaller.registroDesde(json));
    }

    @Test
    void registroQueNoEsObjetoDaIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> marshaller.registroDesde("[\"REGISTRAR\"]"));
    }
}
