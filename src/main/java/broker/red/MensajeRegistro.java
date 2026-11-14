package broker.red;

/**
 * Mensaje que un proveedor envía al broker para registrar un servicio de forma dinámica.
 *
 * @param tipo tipo de mensaje; debe ser {@link #TIPO_REGISTRAR}
 * @param servicio nombre del servicio, por ejemplo "productos"
 * @param host dirección donde escucha el proveedor
 * @param puerto puerto donde escucha el proveedor, entre 1 y 65535
 */
public record MensajeRegistro(String tipo, String servicio, String host, int puerto) {

    /** Tipo de mensaje para registrar un servicio. */
    public static final String TIPO_REGISTRAR = "REGISTRAR";

    /** Puerto más bajo permitido. */
    public static final int PUERTO_MINIMO = 1;

    /** Puerto más alto permitido. */
    public static final int PUERTO_MAXIMO = 65535;
}
