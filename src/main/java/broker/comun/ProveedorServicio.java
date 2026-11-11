package broker.comun;

/**
 * Contrato común para cualquier componente que ejecute servicios.
 */
public interface ProveedorServicio {

    /**
     * Ejecuta la operación indicada en una solicitud.
     *
     * @param solicitud solicitud que debe procesarse
     * @return respuesta de la ejecución
     */
    Respuesta ejecutar(Solicitud solicitud);
}
