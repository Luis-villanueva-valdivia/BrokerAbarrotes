package broker.proveedores;

import broker.abarrotes.VentaDAO;
import broker.abarrotes.VentaModel;
import broker.comun.EstadoRespuesta;
import broker.comun.Respuesta;
import broker.comun.Solicitud;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del proveedor de ventas con la base de datos del proyecto.
 */
class ServicioVentasTest {

    private static final int VENTAS_EN_LA_BASE = 19;

    private VentaDAO ventaDAO;
    private ServicioVentas servicio;

    /** Prepara el proveedor conectado a la base de datos de pruebas. */
    @BeforeEach
    void setUp() {
        ventaDAO = new VentaDAO();
        servicio = new ServicioVentas(ventaDAO);
    }

    @Test
    void laBaseContieneLasVentasDePruebaEsperadas() {
        assertEquals(VENTAS_EN_LA_BASE, ventaDAO.listarTodos().size());
    }

    @Test
    void consultarDevuelveUnaVentaExistenteConSuFechaYTotal() {
        List<VentaModel> ventas = ventaDAO.listarTodos();
        assertFalse(ventas.isEmpty(), "La base de datos debe contener ventas de prueba");
        VentaModel esperada = ventas.get(0);

        Respuesta respuesta = ejecutar("consultar", Map.of("idVenta", String.valueOf(esperada.getId())));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        VentaModel resultado = assertInstanceOf(VentaModel.class, respuesta.getResultado());
        assertEquals(esperada.getId(), resultado.getId());
        assertEquals(esperada.getFecha(), resultado.getFecha());
        assertEquals(esperada.getTotal(), resultado.getTotal(), 0.0001);
    }

    @Test
    void consultarVentaInexistenteDevuelveError() {
        Respuesta respuesta = ejecutar("consultar", Map.of("idVenta", String.valueOf(Integer.MAX_VALUE)));

        assertEquals(EstadoRespuesta.ERROR_PROVEEDOR, respuesta.getEstado());
        assertEquals("venta no encontrada", respuesta.getMensajeError());
    }

    @Test
    void consultarSinIdDevuelveParametroInvalido() {
        Respuesta respuesta = ejecutar("consultar", Map.of());

        assertEquals(EstadoRespuesta.PARAMETRO_INVALIDO, respuesta.getEstado());
    }

    @Test
    void consultarConIdNoNumericoDevuelveParametroInvalido() {
        Respuesta respuesta = ejecutar("consultar", Map.of("idVenta", "abc"));

        assertEquals(EstadoRespuesta.PARAMETRO_INVALIDO, respuesta.getEstado());
    }

    @Test
    void consultarConIdNoPositivoDevuelveParametroInvalido() {
        Respuesta respuestaCero = ejecutar("consultar", Map.of("idVenta", "0"));
        Respuesta respuestaNegativa = ejecutar("consultar", Map.of("idVenta", "-1"));

        assertEquals(EstadoRespuesta.PARAMETRO_INVALIDO, respuestaCero.getEstado());
        assertEquals(EstadoRespuesta.PARAMETRO_INVALIDO, respuestaNegativa.getEstado());
    }

    @Test
    void totalDelDiaSumaLasVentasDeLaFechaIndicada() {
        List<VentaModel> ventas = ventaDAO.listarTodos();
        assertFalse(ventas.isEmpty(), "La base de datos debe contener ventas de prueba");
        LocalDate fecha = convertirFecha(ventas.get(0).getFecha());
        double totalEsperado = ventas.stream()
                .filter(venta -> venta.getFecha() != null)
                .filter(venta -> convertirFecha(venta.getFecha()).equals(fecha))
                .mapToDouble(VentaModel::getTotal)
                .sum();

        Respuesta respuesta = ejecutar("totalDelDia", Map.of("fecha", fecha.toString()));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        Number totalObtenido = assertInstanceOf(Number.class, respuesta.getResultado());
        assertEquals(totalEsperado, totalObtenido.doubleValue(), 0.0001);
    }

    @Test
    void totalDelDiaSinFechaUtilizaElDiaActual() {
        LocalDate hoy = LocalDate.now();
        double totalEsperado = ventaDAO.listarTodos().stream()
                .filter(venta -> venta.getFecha() != null)
                .filter(venta -> convertirFecha(venta.getFecha()).equals(hoy))
                .mapToDouble(VentaModel::getTotal)
                .sum();

        Respuesta respuesta = ejecutar("totalDelDia", Map.of());

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        Number totalObtenido = assertInstanceOf(Number.class, respuesta.getResultado());
        assertEquals(totalEsperado, totalObtenido.doubleValue(), 0.0001);
    }

    @Test
    void totalDelDiaConFechaMalEscritaDevuelveParametroInvalido() {
        Respuesta respuesta = ejecutar("totalDelDia", Map.of("fecha", "12/05/2026"));

        assertEquals(EstadoRespuesta.PARAMETRO_INVALIDO, respuesta.getEstado());
    }

    @Test
    void totalDelDiaConFechaImposibleDevuelveParametroInvalido() {
        Respuesta respuesta = ejecutar("totalDelDia", Map.of("fecha", "2026-02-30"));

        assertEquals(EstadoRespuesta.PARAMETRO_INVALIDO, respuesta.getEstado());
    }

    @Test
    void operacionDesconocidaDevuelveErrorConMensaje() {
        Respuesta respuesta = ejecutar("eliminar", Map.of());

        assertEquals(EstadoRespuesta.ERROR_PROVEEDOR, respuesta.getEstado());
        assertTrue(respuesta.getMensajeError().contains("eliminar"));
    }

    private Respuesta ejecutar(String operacion, Map<String, String> parametros) {
        return servicio.ejecutar(new Solicitud("ventas", operacion, parametros));
    }

    private static LocalDate convertirFecha(Date fecha) {
        return fecha.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
