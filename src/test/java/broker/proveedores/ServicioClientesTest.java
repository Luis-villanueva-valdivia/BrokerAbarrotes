package broker.proveedores;

import broker.abarrotes.ClienteDAO;
import broker.abarrotes.ClienteModel;
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

/**
 * Pruebas del proveedor de clientes con la base de datos del proyecto.
 */
class ServicioClientesTest {

    private static final int CLIENTES_EN_LA_BASE = 5;

    private ClienteDAO clienteDAO;
    private ServicioClientes servicio;

    @BeforeEach
    void setUp() {
        clienteDAO = new ClienteDAO();
        servicio = new ServicioClientes(clienteDAO);
    }

    @Test
    void laBaseContieneLosClientesDePruebaEsperados() {
        assertEquals(CLIENTES_EN_LA_BASE, clienteDAO.listarTodos().size());
    }

    @Test
    void consultarDevuelveNombreYTelefonoDeClienteExistente() {
        ClienteModel esperado = primerCliente();

        Respuesta respuesta = ejecutar("consultar", Map.of("id", String.valueOf(esperado.getId())));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        ClienteModel resultado = assertInstanceOf(ClienteModel.class, respuesta.getResultado());
        assertEquals(esperado.getNombre(), resultado.getNombre());
        assertEquals(esperado.getTelefono(), resultado.getTelefono());
    }

    @Test
    void consultarClienteInexistenteDevuelveError() {
        Respuesta respuesta = ejecutar("consultar", Map.of("id", String.valueOf(Integer.MAX_VALUE)));

        assertEquals(EstadoRespuesta.ERROR_PROVEEDOR, respuesta.getEstado());
        assertEquals("cliente no encontrado", respuesta.getMensajeError());
    }

    @Test
    void consultarSinIdDevuelveParametroInvalido() {
        Respuesta respuesta = ejecutar("consultar", Map.of());

        assertEquals(EstadoRespuesta.PARAMETRO_INVALIDO, respuesta.getEstado());
    }

    @Test
    void consultarConIdNoNumericoDevuelveParametroInvalido() {
        Respuesta respuesta = ejecutar("consultar", Map.of("id", "abc"));

        assertEquals(EstadoRespuesta.PARAMETRO_INVALIDO, respuesta.getEstado());
    }

    @Test
    void porTelefonoDevuelveClienteExistente() {
        ClienteModel esperado = primerClienteConTelefono();

        Respuesta respuesta = ejecutar("porTelefono", Map.of("telefono", esperado.getTelefono()));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        ClienteModel resultado = assertInstanceOf(ClienteModel.class, respuesta.getResultado());
        assertEquals(esperado.getId(), resultado.getId());
    }

    @Test
    void porTelefonoInexistenteDevuelveError() {
        Respuesta respuesta = ejecutar("porTelefono", Map.of("telefono", "0000000000"));

        assertEquals(EstadoRespuesta.ERROR_PROVEEDOR, respuesta.getEstado());
        assertEquals("cliente no encontrado", respuesta.getMensajeError());
    }

    @Test
    void porTelefonoSinParametroDevuelveParametroInvalido() {
        Respuesta respuesta = ejecutar("porTelefono", Map.of());

        assertEquals(EstadoRespuesta.PARAMETRO_INVALIDO, respuesta.getEstado());
    }

    @Test
    void ventasDelMesDevuelveElConteoDeClienteExistente() {
        int id = primerCliente().getId();

        Respuesta respuesta = ejecutar("ventasDelMes", Map.of("id", String.valueOf(id)));

        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado());
        assertEquals(clienteDAO.contarVentasMes(id), respuesta.getResultado());
    }

    @Test
    void ventasDelMesClienteInexistenteDevuelveError() {
        Respuesta respuesta = ejecutar("ventasDelMes", Map.of("id", String.valueOf(Integer.MAX_VALUE)));

        assertEquals(EstadoRespuesta.ERROR_PROVEEDOR, respuesta.getEstado());
        assertEquals("cliente no encontrado", respuesta.getMensajeError());
    }

  

    @Test
    void operacionDesconocidaDevuelveErrorConMensaje() {
        Respuesta respuesta = ejecutar("eliminar", Map.of());

        assertEquals(EstadoRespuesta.ERROR_PROVEEDOR, respuesta.getEstado());
        assertFalse(respuesta.getMensajeError().isBlank());
    }

    private Respuesta ejecutar(String operacion, Map<String, String> parametros) {
        return servicio.ejecutar(new Solicitud("clientes", operacion, parametros));
    }

    private ClienteModel primerCliente() {
        List<ClienteModel> clientes = clienteDAO.listarTodos();
        assertFalse(clientes.isEmpty(), "La base de datos debe contener clientes de prueba");
        return clientes.get(0);
    }

    private ClienteModel primerClienteConTelefono() {
        return clienteDAO.listarTodos().stream()
                .filter(c -> c.getTelefono() != null && !c.getTelefono().isBlank())
                .findFirst()
                .orElseThrow(() -> new AssertionError("Debe haber un cliente con teléfono"));
    }
}
