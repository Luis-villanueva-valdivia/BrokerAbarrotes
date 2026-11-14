package broker.proveedores;

import broker.abarrotes.ProductoDAO;
import broker.abarrotes.ProductoModel;
import broker.comun.EstadoRespuesta;
import broker.comun.Respuesta;
import broker.comun.Solicitud;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioProductosTest {

    private ServicioProductos servicio;

    @BeforeEach
    void setUp() {
        servicio = new ServicioProductos(new ProductoDAO());
    }

    @Test
    void consultarDevuelveElProductoPorCodigo() {
        Respuesta respuesta = ejecutar("consultar", Map.of("codigo", "PEP001"));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        ProductoModel producto = assertInstanceOf(ProductoModel.class, respuesta.getResultado());
        assertEquals("PEP001", producto.getCodigo());
        assertTrue(producto.getPrecio() > 0);
    }

    @Test
    void buscarDevuelveProductosPorNombre() {
        Respuesta respuesta = ejecutar("buscar", Map.of("nombre", "Sabritas"));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        assertInstanceOf(java.util.List.class, respuesta.getResultado());
        assertTrue(((java.util.List<?>) respuesta.getResultado()).stream()
                .map(ProductoModel.class::cast)
                .anyMatch(producto -> producto.getNombre().equalsIgnoreCase("Sabritas")));
    }

    @Test
    void stockBajoFiltraLosProductosEnElProveedor() {
        Respuesta respuesta = ejecutar("stockBajo", Map.of("umbral", "100000"));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        assertEquals(4, assertInstanceOf(java.util.List.class, respuesta.getResultado()).size());
    }

    @Test
    void consultarSinCodigoDevuelveParametroInvalido() {
        assertEquals(EstadoRespuesta.PARAMETRO_INVALIDO,
                ejecutar("consultar", Map.of()).getEstado());
    }

    @Test
    void buscarSinNombreDevuelveParametroInvalido() {
        assertEquals(EstadoRespuesta.PARAMETRO_INVALIDO,
                ejecutar("buscar", Map.of()).getEstado());
    }

    @Test
    void stockBajoSinUmbralDevuelveParametroInvalido() {
        assertEquals(EstadoRespuesta.PARAMETRO_INVALIDO,
                ejecutar("stockBajo", Map.of()).getEstado());
    }

    @Test
    void umbralNoNumericoONegativoDevuelveParametroInvalido() {
        assertEquals(EstadoRespuesta.PARAMETRO_INVALIDO,
                ejecutar("stockBajo", Map.of("umbral", "abc")).getEstado());
        assertEquals(EstadoRespuesta.PARAMETRO_INVALIDO,
                ejecutar("stockBajo", Map.of("umbral", "-1")).getEstado());
    }

    @Test
    void codigoInexistenteDevuelveErrorConMensaje() {
        Respuesta respuesta = ejecutar("consultar", Map.of("codigo", "NO-EXISTE"));

        assertEquals(EstadoRespuesta.ERROR_PROVEEDOR, respuesta.getEstado());
        assertEquals(ProveedorProductosUtil.MENSAJE_PRODUCTO_NO_ENCONTRADO, respuesta.getMensajeError());
    }

    @Test
    void operacionNulaDevuelveParametroInvalido() {
        Respuesta respuesta = servicio.ejecutar(new SolicitudOperacionNula());

        assertEquals(EstadoRespuesta.PARAMETRO_INVALIDO, respuesta.getEstado());
        assertEquals("La operación es obligatoria", respuesta.getMensajeError());
    }

    @Test
    void operacionDesconocidaDevuelveErrorConMensaje() {
        Respuesta respuesta = ejecutar("eliminar", Map.of());

        assertEquals(EstadoRespuesta.ERROR_PROVEEDOR, respuesta.getEstado());
        assertTrue(respuesta.getMensajeError().contains("eliminar"));
    }

    private Respuesta ejecutar(String operacion, Map<String, String> parametros) {
        return servicio.ejecutar(new Solicitud("productos", operacion, parametros));
    }

    private static class SolicitudOperacionNula extends Solicitud {
        SolicitudOperacionNula() {
            super("productos", "consultar", Map.of());
        }

        @Override
        public String getOperacion() {
            return null;
        }
    }
}
