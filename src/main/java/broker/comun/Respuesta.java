package broker.comun;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Resultado de la ejecución de una {@link Solicitud}.
 */
public class Respuesta {
    private final UUID idSolicitud;
    private final Object resultado;
    private final EstadoRespuesta estado;
    private final String mensajeError;
    private final LocalDateTime fechaHora;

    private Respuesta(UUID idSolicitud, Object resultado, EstadoRespuesta estado,
                      String mensajeError) {
        this.idSolicitud = Objects.requireNonNull(idSolicitud, "El id de solicitud es obligatorio");
        this.resultado = resultado;
        this.estado = Objects.requireNonNull(estado, "El estado es obligatorio");
        if (estado == EstadoRespuesta.EXITO && mensajeError != null) {
            throw new IllegalArgumentException("Una respuesta exitosa no debe tener mensaje de error");
        }
        if (estado != EstadoRespuesta.EXITO
                && (mensajeError == null || mensajeError.isBlank())) {
            throw new IllegalArgumentException("Una respuesta de error debe tener mensaje");
        }
        this.mensajeError = mensajeError;
        this.fechaHora = LocalDateTime.now();
    }

    /**
     * Crea una respuesta exitosa.
     *
     * @param idSolicitud identificador de la solicitud
     * @param resultado resultado de la operación
     * @return respuesta exitosa
     */
    public static Respuesta exito(UUID idSolicitud, Object resultado) {
        return new Respuesta(idSolicitud, resultado, EstadoRespuesta.EXITO, null);
    }

    /**
     * Crea una respuesta exitosa para una solicitud.
     *
     * @param solicitud solicitud atendida
     * @param resultado resultado de la operación
     * @return respuesta exitosa
     */
    public static Respuesta exito(Solicitud solicitud, Object resultado) {
        return exito(solicitud.getId(), resultado);
    }

    /**
     * Crea una respuesta de error.
     *
     * @param idSolicitud identificador de la solicitud
     * @param estado estado del error
     * @param mensajeError descripción del error
     * @return respuesta con error
     */
    public static Respuesta error(UUID idSolicitud, EstadoRespuesta estado, String mensajeError) {
        if (estado == EstadoRespuesta.EXITO) {
            throw new IllegalArgumentException("El estado de error no puede ser EXITO");
        }
        return new Respuesta(idSolicitud, null, estado, mensajeError);
    }

    /**
     * Crea una respuesta de error de proveedor.
     *
     * @param idSolicitud identificador de la solicitud
     * @param mensajeError descripción del error
     * @return respuesta con error
     */
    public static Respuesta error(UUID idSolicitud, String mensajeError) {
        return error(idSolicitud, EstadoRespuesta.ERROR_PROVEEDOR, mensajeError);
    }

    /** @return identificador de la solicitud */
    public UUID getIdSolicitud() {
        return idSolicitud;
    }

    /** @return resultado de la operación */
    public Object getResultado() {
        return resultado;
    }

    /** @return estado de la respuesta */
    public EstadoRespuesta getEstado() {
        return estado;
    }

    /** @return mensaje de error, o {@code null} si fue exitosa */
    public String getMensajeError() {
        return mensajeError;
    }

    /** @return fecha y hora de creación */
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
}
