package broker.red;

import broker.comun.EstadoRespuesta;
import broker.comun.Respuesta;
import broker.comun.Solicitud;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Convierte solicitudes, respuestas y mensajes de registro a JSON y de regreso.
 *
 * <p>Cada mensaje se escribe en una sola línea, para leerlo con {@code readLine()}
 * desde un socket. Un JSON vacío, mal formado, con tipos de datos incorrectos o con
 * datos inválidos se reporta con {@link IllegalArgumentException}. La capa que integra
 * este marshaller con la comunicación del broker debe convertir esa excepción en una
 * respuesta PARAMETRO_INVALIDO.</p>
 */
public class MarshallerJson {

    private static final String MENSAJE_INVALIDO = "El mensaje JSON es inválido: ";
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final Gson gson;

    /**
     * Crea el marshaller con Gson configurado para incluir los campos nulos y
     * para escribir las fechas en formato ISO.
     */
    public MarshallerJson() {
        this.gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new AdaptadorFechaHora())
                .create();
    }

    /**
     * Convierte una solicitud a JSON en una sola línea.
     *
     * @param solicitud solicitud a convertir
     * @return texto JSON de la solicitud
     * @throws NullPointerException si la solicitud es null
     */
    public String aJson(Solicitud solicitud) {
        Objects.requireNonNull(solicitud, "La solicitud es obligatoria");
        SolicitudJson dto = new SolicitudJson();
        dto.id = solicitud.getId();
        dto.servicio = solicitud.getServicio();
        dto.operacion = solicitud.getOperacion();
        dto.parametros = solicitud.getParametros();
        dto.fechaHora = solicitud.getFechaHora();
        return gson.toJson(dto);
    }

    /**
     * Reconstruye una solicitud a partir de su JSON.
     *
     * @param json texto JSON producido por {@link #aJson(Solicitud)}
     * @return solicitud reconstruida con el mismo id, servicio, operación, parámetros y fecha
     * @throws IllegalArgumentException si el JSON es vacío, está mal formado, tiene tipos de datos
     *         incorrectos o le faltan campos obligatorios
     */
    public Solicitud solicitudDesde(String json) {
        SolicitudJson dto = leer(json, SolicitudJson.class);
        try {
            return new Solicitud(dto.id, dto.servicio, dto.operacion, dto.parametros, dto.fechaHora);
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(MENSAJE_INVALIDO + "faltan campos obligatorios", e);
        }
    }

    /**
     * Convierte una respuesta a JSON en una sola línea.
     *
     * @param respuesta respuesta a convertir
     * @return texto JSON de la respuesta
     * @throws NullPointerException si la respuesta es null
     */
    public String aJson(Respuesta respuesta) {
        Objects.requireNonNull(respuesta, "La respuesta es obligatoria");
        RespuestaJson dto = new RespuestaJson();
        dto.idSolicitud = respuesta.getIdSolicitud();
        dto.resultado = respuesta.getResultado();
        dto.estado = respuesta.getEstado();
        dto.mensajeError = respuesta.getMensajeError();
        dto.fechaHora = respuesta.getFechaHora();
        return gson.toJson(dto);
    }

    /**
     * Reconstruye una respuesta a partir de su JSON.
     *
     * <p>El resultado se reconstruye como estructura genérica (texto, número,
     * lista o mapa), porque el JSON no conserva el tipo original de la clase.
     * La fecha de la respuesta se asigna al momento de reconstruirla.</p>
     *
     * @param json texto JSON producido por {@link #aJson(Respuesta)}
     * @return respuesta reconstruida
     * @throws IllegalArgumentException si el JSON es vacío, está mal formado, tiene tipos de datos
     *         incorrectos, tiene un estado desconocido o le faltan campos
     */
    public Respuesta respuestaDesde(String json) {
        RespuestaJson dto = leer(json, RespuestaJson.class);
        try {
            if (dto.estado == EstadoRespuesta.EXITO) {
                return Respuesta.exito(dto.idSolicitud, dto.resultado);
            }
            return Respuesta.error(dto.idSolicitud, dto.estado, dto.mensajeError);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new IllegalArgumentException(MENSAJE_INVALIDO + "estado o campos inválidos", e);
        }
    }

    /**
     * Convierte un mensaje de registro dinámico a JSON en una sola línea.
     *
     * @param mensaje mensaje a convertir
     * @return texto JSON del mensaje
     * @throws NullPointerException si el mensaje es null
     */
    public String aJson(MensajeRegistro mensaje) {
        Objects.requireNonNull(mensaje, "El mensaje de registro es obligatorio");
        return gson.toJson(mensaje);
    }

    /**
     * Reconstruye un mensaje de registro dinámico a partir de su JSON.
     *
     * <p>La validación de tipo, servicio, host y puerto la hace el constructor de
     * {@link MensajeRegistro}, así que aquí solo se traduce su error al formato del marshaller.</p>
     *
     * @param json texto JSON del mensaje
     * @return mensaje validado
     * @throws IllegalArgumentException si el JSON es inválido, tiene tipos de datos incorrectos,
     *         el tipo no es REGISTRAR, faltan campos o el puerto no está entre 1 y 65535
     */
    public MensajeRegistro registroDesde(String json) {
        return leer(json, MensajeRegistro.class);
    }

    /**
     * Lee un JSON y lo convierte al tipo indicado. Cualquier fallo de Gson, de tipos
     * de datos o de validación se traduce a IllegalArgumentException.
     */
    private <T> T leer(String json, Class<T> tipo) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException(MENSAJE_INVALIDO + "el mensaje está vacío");
        }
        T dto;
        try {
            dto = gson.fromJson(json, tipo);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException(MENSAJE_INVALIDO + mensajeDe(e), e);
        }
        if (dto == null) {
            throw new IllegalArgumentException(MENSAJE_INVALIDO + "el mensaje está vacío");
        }
        return dto;
    }

    /** Devuelve el mensaje de la causa más profunda, para dar un error legible. */
    private static String mensajeDe(Throwable error) {
        Throwable causa = error;
        while (causa.getCause() != null && causa.getCause() != causa) {
            causa = causa.getCause();
        }
        return causa.getMessage() != null ? causa.getMessage() : "formato inválido";
    }

    /** Forma en JSON de una solicitud; Gson la llena por reflexión. */
    private static final class SolicitudJson {
        UUID id;
        String servicio;
        String operacion;
        Map<String, String> parametros;
        LocalDateTime fechaHora;
    }

    /** Forma en JSON de una respuesta; Gson la llena por reflexión. */
    private static final class RespuestaJson {
        UUID idSolicitud;
        Object resultado;
        EstadoRespuesta estado;
        String mensajeError;
        LocalDateTime fechaHora;
    }

    /** Escribe y lee las fechas en formato ISO, y reporta fechas mal escritas como error de JSON. */
    private static final class AdaptadorFechaHora extends TypeAdapter<LocalDateTime> {

        @Override
        public void write(JsonWriter salida, LocalDateTime fecha) throws IOException {
            if (fecha == null) {
                salida.nullValue();
            } else {
                salida.value(fecha.format(FORMATO_FECHA_HORA));
            }
        }

        @Override
        public LocalDateTime read(JsonReader entrada) throws IOException {
            if (entrada.peek() == JsonToken.NULL) {
                entrada.nextNull();
                return null;
            }
            String texto = entrada.nextString();
            try {
                return LocalDateTime.parse(texto, FORMATO_FECHA_HORA);
            } catch (DateTimeParseException e) {
                throw new JsonParseException("fecha inválida: " + texto, e);
            }
        }
    }
}
