package broker.broker;

/**
 * Indica que se pidió un servicio que no está registrado en el RegistroServicios.
 */
public class ServicioDesconocidoException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    private final String nombreServicio;

    /**
     * Crea la excepción para un servicio que no existe en el registro.
     *
     * @param nombreServicio nombre del servicio que se buscó
     */
    public ServicioDesconocidoException(String nombreServicio) {
        super("El servicio '" + nombreServicio + "' no está registrado");
        this.nombreServicio = nombreServicio;
    }

    /** @return nombre del servicio que no se encontró */
    public String getNombreServicio() {
        return nombreServicio;
    }
}