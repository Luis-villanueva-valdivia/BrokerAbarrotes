package broker.broker;

import broker.comun.ProveedorServicio;

import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Directorio de servicios del broker: relaciona el nombre de cada servicio
 * con el proveedor que lo atiende.
 *
 * <p>Es seguro para hilos porque en el nivel 2 el servidor del broker
 * atiende varias conexiones a la vez sobre el mismo registro.</p>
 */
public class RegistroServicios {

    private final ConcurrentMap<String, ProveedorServicio> proveedores = new ConcurrentHashMap<>();

    /**
     * Registra un proveedor nuevo para un servicio.
     *
     * @param nombre nombre del servicio, por ejemplo "productos"
     * @param proveedor proveedor que atenderá el servicio
     * @throws IllegalStateException si el servicio ya estaba registrado
     */
    public void registrar(String nombre, ProveedorServicio proveedor) {
        String clave = validarNombre(nombre);
        Objects.requireNonNull(proveedor, "El proveedor es obligatorio");
        ProveedorServicio anterior = proveedores.putIfAbsent(clave, proveedor);
        if (anterior != null) {
            throw new IllegalStateException("El servicio '" + clave + "' ya está registrado");
        }
    }

    /**
     * Busca el proveedor de un servicio.
     *
     * @param nombre nombre del servicio
     * @return proveedor registrado
     * @throws ServicioDesconocidoException si el servicio no está registrado
     */
    public ProveedorServicio buscar(String nombre) {
        String clave = validarNombre(nombre);
        ProveedorServicio proveedor = proveedores.get(clave);
        if (proveedor == null) {
            throw new ServicioDesconocidoException(clave);
        }
        return proveedor;
    }

    private static String validarNombre(String nombre) {
        Objects.requireNonNull(nombre, "El nombre del servicio es obligatorio");
        String limpio = nombre.trim();
        if (limpio.isEmpty()) {
            throw new IllegalArgumentException("El nombre del servicio no puede estar vacío");
        }
        return limpio;
    }

        /**
     * Cambia el proveedor de un servicio ya registrado sin que el cliente se entere.
     *
     * @param nombre nombre del servicio
     * @param proveedor nuevo proveedor
     * @return el proveedor que atendía el servicio antes del cambio
     * @throws ServicioDesconocidoException si el servicio no estaba registrado
     */
    public ProveedorServicio sustituir(String nombre, ProveedorServicio proveedor) {
        String clave = validarNombre(nombre);
        Objects.requireNonNull(proveedor, "El proveedor es obligatorio");
        ProveedorServicio anterior = proveedores.replace(clave, proveedor);
        if (anterior == null) {
            throw new ServicioDesconocidoException(clave);
        }
        return anterior;
    }

    /**
     * Quita un servicio del registro.
     *
     * @param nombre nombre del servicio
     * @return el proveedor que se quitó
     * @throws ServicioDesconocidoException si el servicio no estaba registrado
     */
    public ProveedorServicio eliminar(String nombre) {
        String clave = validarNombre(nombre);
        ProveedorServicio eliminado = proveedores.remove(clave);
        if (eliminado == null) {
            throw new ServicioDesconocidoException(clave);
        }
        return eliminado;
    }

    /**
     * Lista los servicios registrados en orden alfabético.
     *
     * @return copia de los nombres; modificarla no afecta al registro
     */
    public SortedSet<String> listar() {
        return new TreeSet<>(proveedores.keySet());
    }

    /**
     * Indica si un servicio está registrado.
     *
     * @param nombre nombre del servicio
     * @return true si existe un proveedor para ese nombre
     */
    public boolean contiene(String nombre) {
        return nombre != null && proveedores.containsKey(nombre.trim());
    }
}