package broker.proveedores;

import broker.abarrotes.ProductoDAO;
import broker.abarrotes.ProductoModel;
import broker.comun.EstadoRespuesta;
import broker.comun.ProveedorServicio;
import broker.comun.Respuesta;
import broker.comun.Solicitud;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Proveedor del servicio de productos conectado a la base de datos SQLite.
 *
 * <p>Recibe el DAO por constructor y resuelve las operaciones de consulta,
 * búsqueda por nombre y listado de productos con stock bajo.</p>
 */
public class ServicioProductos implements ProveedorServicio {

    private static final String OPERACION_CONSULTAR = "consultar";
    private static final String OPERACION_BUSCAR = "buscar";
    private static final String OPERACION_STOCK_BAJO = "stockBajo";
    private static final String PARAMETRO_CODIGO = "codigo";
    private static final String PARAMETRO_NOMBRE = "nombre";
    private static final String PARAMETRO_UMBRAL = "umbral";
    private static final String MENSAJE_PRODUCTO_NO_ENCONTRADO = "producto no encontrado";

    private final ProductoDAO productoDAO;

    /**
     * Crea el proveedor que utiliza el DAO de productos recibido.
     *
     * @param productoDAO DAO que consulta los productos almacenados
     */
    public ServicioProductos(ProductoDAO productoDAO) {
        this.productoDAO = Objects.requireNonNull(productoDAO, "El ProductoDAO es obligatorio");
    }

    /**
     * Ejecuta una operación del servicio de productos.
     *
     * @param solicitud solicitud con la operación y sus parámetros
     * @return respuesta exitosa o de error; los parámetros inválidos se
     *         devuelven con estado PARAMETRO_INVALIDO
     */
    @Override
    public Respuesta ejecutar(Solicitud solicitud) {
        if (solicitud == null) {
            return Respuesta.error(UUID.randomUUID(), EstadoRespuesta.PARAMETRO_INVALIDO,
                    "La solicitud es obligatoria");
        }

        try {
            return switch (solicitud.getOperacion()) {
                case OPERACION_CONSULTAR -> consultar(solicitud);
                case OPERACION_BUSCAR -> buscar(solicitud);
                case OPERACION_STOCK_BAJO -> stockBajo(solicitud);
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
        String codigo = obtenerParametro(solicitud, PARAMETRO_CODIGO);
        List<ProductoModel> productos = productoDAO.getBuscador().buscarPorCodigo(codigo);
        if (productos.isEmpty()) {
            return Respuesta.error(solicitud.getId(), EstadoRespuesta.ERROR_PROVEEDOR,
                    MENSAJE_PRODUCTO_NO_ENCONTRADO);
        }
        return Respuesta.exito(solicitud, productos.get(0));
    }

    private Respuesta buscar(Solicitud solicitud) {
        String nombre = obtenerParametro(solicitud, PARAMETRO_NOMBRE);
        List<ProductoModel> productos = productoDAO.getBuscador().buscarPorNombre(nombre);
        return Respuesta.exito(solicitud, productos);
    }

    private Respuesta stockBajo(Solicitud solicitud) {
        int umbral = obtenerUmbral(solicitud);
        List<ProductoModel> productos = productoDAO.listarTodos().stream()
                .filter(producto -> producto.getCantidad() < umbral)
                .collect(Collectors.toList());
        return Respuesta.exito(solicitud, productos);
    }

    private static String obtenerParametro(Solicitud solicitud, String nombre) {
        String valor = solicitud.getParametros().get(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta el parámetro obligatorio: " + nombre);
        }
        return valor.trim();
    }

    private static int obtenerUmbral(Solicitud solicitud) {
        String valor = obtenerParametro(solicitud, PARAMETRO_UMBRAL);
        try {
            int umbral = Integer.parseInt(valor);
            if (umbral < 0) {
                throw new IllegalArgumentException("El parámetro umbral no puede ser negativo");
            }
            return umbral;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El parámetro umbral debe ser numérico", e);
        }
    }
}
