package broker.proveedores;

import broker.abarrotes.ConexionDB;
import broker.comun.Solicitud;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Utilidades compartidas por los proveedores de productos.
 *
 * <p>Centraliza los nombres de operaciones y parámetros, su validación y la
 * comprobación de disponibilidad de la conexión usada por el DAO.</p>
 */
final class ProveedorProductosUtil {

    static final String OPERACION_CONSULTAR = "consultar";
    static final String OPERACION_BUSCAR = "buscar";
    static final String OPERACION_STOCK_BAJO = "stockBajo";

    static final String PARAMETRO_CODIGO = "codigo";
    static final String PARAMETRO_NOMBRE = "nombre";
    static final String PARAMETRO_UMBRAL = "umbral";

    static final String MENSAJE_PRODUCTO_NO_ENCONTRADO = "producto no encontrado";
    static final String MENSAJE_SIN_CONEXION = "No hay conexión disponible con la base de datos de productos";

    private ProveedorProductosUtil() {
        // Clase de utilidades: no requiere instancias.
    }

    /**
     * Obtiene y valida la operación solicitada.
     *
     * @param solicitud solicitud que contiene la operación
     * @return nombre de la operación
     * @throws IllegalArgumentException si la operación es nula o está vacía
     */
    static String obtenerOperacion(Solicitud solicitud) {
        String operacion = solicitud.getOperacion();
        if (operacion == null || operacion.isBlank()) {
            throw new IllegalArgumentException("La operación es obligatoria");
        }
        return operacion;
    }

    /**
     * Obtiene un parámetro obligatorio de una solicitud.
     *
     * @param solicitud solicitud que contiene los parámetros
     * @param nombre nombre del parámetro
     * @return valor sin espacios al inicio ni al final
     * @throws IllegalArgumentException si el parámetro falta o está vacío
     */
    static String obtenerParametro(Solicitud solicitud, String nombre) {
        String valor = solicitud.getParametros().get(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta el parámetro obligatorio: " + nombre);
        }
        return valor.trim();
    }

    /**
     * Obtiene y valida el umbral numérico de stock.
     *
     * @param solicitud solicitud que contiene el parámetro umbral
     * @return umbral entero no negativo
     * @throws IllegalArgumentException si falta, no es numérico o es negativo
     */
    static int obtenerUmbral(Solicitud solicitud) {
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

    /**
     * Comprueba que la conexión compartida de la base de datos esté disponible.
     *
     * @return {@code true} si existe una conexión abierta; en otro caso, {@code false}
     */
    static boolean hayConexionBaseDatos() {
        Connection conexion = ConexionDB.getInstancia().getConexion();
        if (conexion == null) {
            return false;
        }
        try {
            return !conexion.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
