package broker.red;

/**
 * Mensaje que un proveedor envía al broker para registrar un servicio de forma dinámica.
 *
 * <p>El constructor compacto valida los datos, así que no es posible crear un mensaje
 * inválido ni por código ni al reconstruirlo desde JSON.</p>
 *
 * @param tipo tipo de mensaje; debe ser {@link #TIPO_REGISTRAR}
 * @param servicio nombre del servicio, por ejemplo "productos"; no puede estar vacío
 * @param host dirección donde escucha el proveedor; no puede estar vacía
 * @param puerto puerto donde escucha el proveedor, entre 1 y 65535
 * @throws IllegalArgumentException si alguno de los datos es inválido
 */
public record MensajeRegistro(String tipo, String servicio, String host, int puerto) {

    /** Tipo de mensaje para registrar un servicio. */
    public static final String TIPO_REGISTRAR = "REGISTRAR";

    /** Puerto más bajo permitido. */
    public static final int PUERTO_MINIMO = 1;

    /** Puerto más alto permitido. */
    public static final int PUERTO_MAXIMO = 65535;

    /**
     * Valida los datos del mensaje antes de crearlo.
     *
     * @throws IllegalArgumentException si el tipo no es REGISTRAR, el servicio o el host están vacíos
     *         o el puerto está fuera de rango
     */
    public MensajeRegistro {
        if (!TIPO_REGISTRAR.equals(tipo)) {
            throw new IllegalArgumentException("El tipo de mensaje debe ser REGISTRAR");
        }
        if (servicio == null || servicio.isBlank()) {
            throw new IllegalArgumentException("El servicio es obligatorio");
        }
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("El host es obligatorio");
        }
        if (puerto < PUERTO_MINIMO || puerto > PUERTO_MAXIMO) {
            throw new IllegalArgumentException("El puerto debe estar entre 1 y 65535");
        }
    }
}
