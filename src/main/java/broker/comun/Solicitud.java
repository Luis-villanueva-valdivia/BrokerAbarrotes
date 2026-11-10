package broker.comun;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Solicitud de un servicio realizada por un cliente.
 *
 * @param id identificador único de la solicitud
 * @param servicio nombre del servicio solicitado
 * @param operacion operación que debe ejecutar el servicio
 * @param parametros parámetros de la operación
 * @param fechaHora fecha y hora de creación de la solicitud
 */
public class Solicitud {
    private final UUID id;
    private final String servicio;
    private final String operacion;
    private final Map<String, String> parametros;
    private final LocalDateTime fechaHora;

    /**
     * Crea una solicitud con identificador y fecha de creación automáticos.
     *
     * @param servicio nombre del servicio
     * @param operacion operación solicitada
     * @param parametros parámetros de la operación
     */
    public Solicitud(String servicio, String operacion, Map<String, String> parametros) {
        this(UUID.randomUUID(), servicio, operacion, parametros, LocalDateTime.now());
    }

    /**
     * Crea una solicitud con todos sus datos.
     *
     * @param id identificador de la solicitud
     * @param servicio nombre del servicio
     * @param operacion operación solicitada
     * @param parametros parámetros de la operación
     * @param fechaHora fecha y hora de creación
     */
    public Solicitud(UUID id, String servicio, String operacion,
                     Map<String, String> parametros, LocalDateTime fechaHora) {
        this.id = Objects.requireNonNull(id, "El id es obligatorio");
        this.servicio = Objects.requireNonNull(servicio, "El servicio es obligatorio");
        this.operacion = Objects.requireNonNull(operacion, "La operación es obligatoria");
        this.parametros = Map.copyOf(Objects.requireNonNull(parametros, "Los parámetros son obligatorios"));
        this.fechaHora = Objects.requireNonNull(fechaHora, "La fecha y hora son obligatorias");
    }

    /** @return identificador único de la solicitud */
    public UUID getId() {
        return id;
    }

    /** @return nombre del servicio */
    public String getServicio() {
        return servicio;
    }

    /** @return operación solicitada */
    public String getOperacion() {
        return operacion;
    }

    /** @return parámetros inmutables de la operación */
    public Map<String, String> getParametros() {
        return parametros;
    }

    /** @return fecha y hora de creación */
    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
}
