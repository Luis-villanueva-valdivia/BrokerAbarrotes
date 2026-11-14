package broker.proveedores;

import broker.abarrotes.ProductoModel;
import broker.comun.EstadoRespuesta;
import broker.comun.Respuesta;
import broker.comun.Solicitud;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioProductosMemoriaTest {

    private ServicioProductosMemoria servicio;

    @BeforeEach
    void setUp() {
        servicio = new ServicioProductosMemoria();
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
        Respuesta respuesta = ejecutar("buscar", Map.of("nombre", "refresco"));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        List<?> productos = assertInstanceOf(List.class, respuesta.getResultado());
        assertEquals(2, productos.size());
    }

    @Test
    void stockBajoFiltraLosProductosEnMemoria() {
        Respuesta respuesta = ejecutar("stockBajo", Map.of("umbral", "10"));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        List<?> productos = assertInstanceOf(List.class, respuesta.getResultado());
        assertEquals(2, productos.size());
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

    @Test
    void modificarElResultadoNoAlteraLosDatosInternos() {
        ProductoModel resultado = (ProductoModel) ejecutar(
                "consultar", Map.of("codigo", "PEP001")).getResultado();
        resultado.setNombre("Nombre modificado");

        ProductoModel siguiente = (ProductoModel) ejecutar(
                "consultar", Map.of("codigo", "PEP001")).getResultado();
        assertFalse("Nombre modificado".equals(siguiente.getNombre()));
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
