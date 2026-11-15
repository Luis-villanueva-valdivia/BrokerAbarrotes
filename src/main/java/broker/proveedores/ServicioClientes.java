package broker.proveedores;

import broker.abarrotes.ClienteDAO;
import broker.abarrotes.ClienteModel;
import broker.comun.EstadoRespuesta;
import broker.comun.ProveedorServicio;
import broker.comun.Respuesta;
import broker.comun.Solicitud;

import java.util.Objects;
import java.util.UUID;

/**
 * Proveedor del servicio de clientes conectado a ClienteDAO.
 *
 * <p>Permite consultar un cliente por id, buscarlo por teléfono y contar
 * sus ventas del mes actual.</p>
 */
public class ServicioClientes implements ProveedorServicio {

    private static final String OPERACION_CONSULTAR = "consultar";
    private static final String OPERACION_POR_TELEFONO = "porTelefono";
    private static final String OPERACION_VENTAS_DEL_MES = "ventasDelMes";
    private static final String PARAMETRO_ID = "id";
    private static final String PARAMETRO_TELEFONO = "telefono";
    private static final String MENSAJE_CLIENTE_NO_ENCONTRADO = "cliente no encontrado";

    private final ClienteDAO clienteDAO;

    /**
     * Crea el proveedor que utiliza el DAO de clientes recibido.
     *
     * @param clienteDAO DAO utilizado para recuperar los clientes
     */
    public ServicioClientes(ClienteDAO clienteDAO) {
        this.clienteDAO = Objects.requireNonNull(clienteDAO, "El ClienteDAO es obligatorio");
    }

    /**
     * Ejecuta una operación del servicio de clientes.
     *
     * @param solicitud solicitud con la operación y sus parámetros
     * @return respuesta con el resultado o el error correspondiente
     */
    @Override
    public Respuesta ejecutar(Solicitud solicitud) {
        if (solicitud == null) {
            return Respuesta.error(UUID.randomUUID(), EstadoRespuesta.PARAMETRO_INVALIDO,
                    "La solicitud es obligatoria");
        }
        if (solicitud.getOperacion() == null) {
            return Respuesta.error(solicitud.getId(), EstadoRespuesta.PARAMETRO_INVALIDO,
                    "La operación es obligatoria");
        }

        try {
            return switch (solicitud.getOperacion()) {
                case OPERACION_CONSULTAR -> consultar(solicitud);
                case OPERACION_POR_TELEFONO -> porTelefono(solicitud);
                case OPERACION_VENTAS_DEL_MES -> ventasDelMes(solicitud);
                default -> Respuesta.error(solicitud.getId(), EstadoRespuesta.ERROR_PROVEEDOR,
                        "Operación desconocida: " + solicitud.getOperacion());
            };
        } catch (IllegalArgumentException e) {
            return Respuesta.error(solicitud.getId(), EstadoRespuesta.PARAMETRO_INVALIDO, e.getMessage());
        } catch (RuntimeException e) {
            String mensaje = e.getMessage() == null || e.getMessage().isBlank()
                    ? e.getClass().getSimpleName()
                    : e.getMessage();
            return Respuesta.error(solicitud.getId(), EstadoRespuesta.ERROR_PROVEEDOR, mensaje);
        }
    }

    private Respuesta consultar(Solicitud solicitud) {
        int id = obtenerId(solicitud);
        ClienteModel cliente = clienteDAO.buscar(id);
        if (cliente == null) {
            return errorClienteNoEncontrado(solicitud);
        }
        return Respuesta.exito(solicitud, cliente);
    }

    private Respuesta porTelefono(Solicitud solicitud) {
        String telefono = obtenerParametro(solicitud, PARAMETRO_TELEFONO);
        ClienteModel cliente = clienteDAO.buscarPorTelefono(telefono);
        if (cliente == null) {
            return errorClienteNoEncontrado(solicitud);
        }
        return Respuesta.exito(solicitud, cliente);
    }

    private Respuesta ventasDelMes(Solicitud solicitud) {
        int id = obtenerId(solicitud);
        // Se verifica que el cliente exista para no devolver 0 por un id inexistente
        if (clienteDAO.buscar(id) == null) {
            return errorClienteNoEncontrado(solicitud);
        }
        int total = clienteDAO.contarVentasMes(id);
        return Respuesta.exito(solicitud, total);
    }

    private static Respuesta errorClienteNoEncontrado(Solicitud solicitud) {
        return Respuesta.error(solicitud.getId(), EstadoRespuesta.ERROR_PROVEEDOR,
                MENSAJE_CLIENTE_NO_ENCONTRADO);
    }

    private static String obtenerParametro(Solicitud solicitud, String nombre) {
        String valor = solicitud.getParametros().get(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta el parámetro obligatorio: " + nombre);
        }
        return valor.trim();
    }

    private static int obtenerId(Solicitud solicitud) {
        String valor = obtenerParametro(solicitud, PARAMETRO_ID);
        try {
            int id = Integer.parseInt(valor);
            if (id <= 0) {
                throw new IllegalArgumentException("El parámetro id debe ser un entero positivo");
            }
            return id;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El parámetro id debe ser numérico", e);
        }
    }
}
