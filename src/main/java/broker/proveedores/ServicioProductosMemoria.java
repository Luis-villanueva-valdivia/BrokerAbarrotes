package broker.proveedores;

import broker.abarrotes.ProductoModel;
import broker.comun.EstadoRespuesta;
import broker.comun.ProveedorServicio;
import broker.comun.Respuesta;
import broker.comun.Solicitud;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Sustituto en memoria del proveedor de productos.
 *
 * <p>Utiliza productos fijos para probar el cambio de proveedor sin modificar
 * al cliente ni acceder a la base de datos.</p>
 */
public class ServicioProductosMemoria implements ProveedorServicio {

    private static final String OPERACION_CONSULTAR = "consultar";
    private static final String OPERACION_BUSCAR = "buscar";
    private static final String OPERACION_STOCK_BAJO = "stockBajo";
    private static final String PARAMETRO_CODIGO = "codigo";
    private static final String PARAMETRO_NOMBRE = "nombre";
    private static final String PARAMETRO_UMBRAL = "umbral";
    private static final String MENSAJE_PRODUCTO_NO_ENCONTRADO = "producto no encontrado";

    private final List<ProductoModel> productos = crearProductos();

    /**
     * Crea el proveedor con un conjunto pequeño de productos de prueba.
     */
    public ServicioProductosMemoria() {
        // Los datos se inicializan en crearProductos para mantenerlos privados.
    }

    /**
     * Ejecuta una operación sobre los productos almacenados en memoria.
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
        return productos.stream()
                .filter(producto -> producto.getCodigo().equals(codigo))
                .findFirst()
                .map(producto -> Respuesta.exito(solicitud, copiar(producto)))
                .orElseGet(() -> Respuesta.error(solicitud.getId(), EstadoRespuesta.ERROR_PROVEEDOR,
                        MENSAJE_PRODUCTO_NO_ENCONTRADO));
    }

    private Respuesta buscar(Solicitud solicitud) {
        String nombre = obtenerParametro(solicitud, PARAMETRO_NOMBRE).toLowerCase(Locale.ROOT);
        List<ProductoModel> encontrados = productos.stream()
                .filter(producto -> producto.getNombre().toLowerCase(Locale.ROOT).contains(nombre))
                .map(ServicioProductosMemoria::copiar)
                .toList();
        return Respuesta.exito(solicitud, encontrados);
    }

    private Respuesta stockBajo(Solicitud solicitud) {
        int umbral = obtenerUmbral(solicitud);
        List<ProductoModel> encontrados = productos.stream()
                .filter(producto -> producto.getCantidad() < umbral)
                .map(ServicioProductosMemoria::copiar)
                .toList();
        return Respuesta.exito(solicitud, encontrados);
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

    private static List<ProductoModel> crearProductos() {
        List<ProductoModel> lista = new ArrayList<>();
        lista.add(crearProducto(1, "Refresco cola alternativo", 18.50, 12, "PEP001"));
        lista.add(crearProducto(2, "Refresco cola", 19.00, 5, "CC001"));
        lista.add(crearProducto(3, "Sabritas", 17.00, 8, "SAB001"));
        lista.add(crearProducto(4, "Galletas", 15.50, 20, "GAL001"));
        return List.copyOf(lista);
    }

    private static ProductoModel crearProducto(int id, String nombre, double precio,
                                                int cantidad, String codigo) {
        ProductoModel producto = new ProductoModel();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setCantidad(cantidad);
        producto.setCodigo(codigo);
        producto.setCategoria("Abarrotes");
        producto.setProveedor("Proveedor de prueba");
        return producto;
    }

    private static ProductoModel copiar(ProductoModel original) {
        ProductoModel copia = new ProductoModel();
        copia.setId(original.getId());
        copia.setNombre(original.getNombre());
        copia.setPrecio(original.getPrecio());
        copia.setCantidad(original.getCantidad());
        copia.setVencimiento(original.getVencimiento());
        copia.setDescripcion(original.getDescripcion());
        copia.setCategoria(original.getCategoria());
        copia.setProveedor(original.getProveedor());
        copia.setCodigo(original.getCodigo());
        return copia;
    }
}
