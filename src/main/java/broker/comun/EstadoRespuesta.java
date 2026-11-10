package broker.comun;

/**
 * Estados posibles al procesar una solicitud.
 */
public enum EstadoRespuesta {
    /** La operación terminó correctamente. */
    EXITO,
    /** No existe un proveedor para el servicio solicitado. */
    SERVICIO_DESCONOCIDO,
    /** Los parámetros de la solicitud no son válidos. */
    PARAMETRO_INVALIDO,
    /** El proveedor no pudo completar la operación. */
    ERROR_PROVEEDOR,
    /** El proveedor no respondió dentro del tiempo esperado. */
    TIMEOUT
}
