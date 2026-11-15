package broker.red;

import broker.comun.EstadoRespuesta;
import broker.comun.ProveedorServicio;
import broker.comun.Respuesta;
import broker.comun.Solicitud;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Adaptador de un proveedor que vive en otro proceso.
 *
 * <p>Cada solicitud usa una conexión TCP independiente. El proveedor remoto
 * recibe una solicitud JSON y devuelve una respuesta JSON, ambas en una sola
 * línea.</p>
 *
 * <p>Si la conexión falla o el proveedor no responde a tiempo, reintenta hasta
 * {@link #MAX_REINTENTOS} veces y anota cada reintento en la bitácora. Al
 * agotarlos responde {@code TIMEOUT} si el último intento se quedó esperando,
 * o {@code ERROR_PROVEEDOR} si hubo una desconexión.</p>
 */
public final class ProveedorRemoto implements ProveedorServicio {

    /** Reintentos máximos después del primer intento. */
    public static final int MAX_REINTENTOS = 2;

    private final String host;
    private final int puerto;
    private final int tiempoConexionMs;
    private final int tiempoLecturaMs;
    private final MarshallerJson marshaller;
    private final Consumer<String> bitacora;

    /**
     * Crea un adaptador para un proveedor TCP remoto sin bitácora.
     *
     * @param host dirección del proveedor
     * @param puerto puerto del proveedor
     * @param tiempoConexionMs tiempo máximo para establecer la conexión
     * @param tiempoLecturaMs tiempo máximo para recibir la respuesta
     * @throws IllegalArgumentException si el host, puerto o tiempos son inválidos
     */
    public ProveedorRemoto(String host, int puerto, int tiempoConexionMs, int tiempoLecturaMs) {
        this(host, puerto, tiempoConexionMs, tiempoLecturaMs, linea -> { });
    }

    /**
     * Crea un adaptador para un proveedor TCP remoto que anota sus reintentos.
     *
     * @param host dirección del proveedor
     * @param puerto puerto del proveedor
     * @param tiempoConexionMs tiempo máximo para establecer la conexión
     * @param tiempoLecturaMs tiempo máximo para recibir la respuesta en cada intento
     * @param bitacora destino de los mensajes de reintento
     * @throws IllegalArgumentException si el host, puerto o tiempos son inválidos
     * @throws NullPointerException si la bitácora es nula
     */
    public ProveedorRemoto(String host, int puerto, int tiempoConexionMs, int tiempoLecturaMs,
                           Consumer<String> bitacora) {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("El host es obligatorio");
        }
        if (puerto < MensajeRegistro.PUERTO_MINIMO || puerto > MensajeRegistro.PUERTO_MAXIMO) {
            throw new IllegalArgumentException("El puerto debe estar entre 1 y 65535");
        }
        if (tiempoConexionMs <= 0 || tiempoLecturaMs <= 0) {
            throw new IllegalArgumentException("Los tiempos deben ser positivos");
        }
        this.host = host;
        this.puerto = puerto;
        this.tiempoConexionMs = tiempoConexionMs;
        this.tiempoLecturaMs = tiempoLecturaMs;
        this.marshaller = new MarshallerJson();
        this.bitacora = Objects.requireNonNull(bitacora, "La bitácora es obligatoria");
    }

    /**
     * Envía una solicitud al proveedor remoto y reconstruye su respuesta,
     * reintentando ante desconexiones o falta de respuesta.
     *
     * @param solicitud solicitud que se enviará
     * @return respuesta del proveedor, o {@code TIMEOUT} / {@code ERROR_PROVEEDOR}
     *         si se agotan los reintentos
     * @throws NullPointerException si la solicitud es nula
     */
    @Override
    public Respuesta ejecutar(Solicitud solicitud) {
        Objects.requireNonNull(solicitud, "La solicitud es obligatoria");
        IOException ultimoError = null;
        for (int intento = 0; intento <= MAX_REINTENTOS; intento++) {
            if (intento > 0) {
                bitacora.accept(mensajeReintento(solicitud, intento, ultimoError));
            }
            try {
                return enviar(solicitud);
            } catch (IOException e) {
                ultimoError = e;
            } catch (IllegalArgumentException e) {
                return Respuesta.error(solicitud.getId(), EstadoRespuesta.ERROR_PROVEEDOR, describir(e));
            }
        }
        return Respuesta.error(solicitud.getId(), estadoDe(ultimoError),
                "Sin respuesta del proveedor después de " + MAX_REINTENTOS + " reintentos: "
                        + describir(ultimoError));
    }

    private Respuesta enviar(Solicitud solicitud) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, puerto), tiempoConexionMs);
            socket.setSoTimeout(tiempoLecturaMs);
            BufferedWriter salida = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            BufferedReader entrada = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            salida.write(marshaller.aJson(solicitud));
            salida.newLine();
            salida.flush();
            String linea = entrada.readLine();
            if (linea == null) {
                throw new IOException("El proveedor cerró la conexión sin responder");
            }
            return marshaller.respuestaDesde(linea);
        }
    }

    private String mensajeReintento(Solicitud solicitud, int intento, IOException error) {
        return "[PROVEEDOR] Reintento " + intento + "/" + MAX_REINTENTOS
                + " " + solicitud.getServicio() + "." + solicitud.getOperacion()
                + " en " + host + ":" + puerto
                + " estado=" + estadoDe(error) + " (" + describir(error) + ")";
    }

    private static EstadoRespuesta estadoDe(IOException error) {
        return error instanceof SocketTimeoutException
                ? EstadoRespuesta.TIMEOUT
                : EstadoRespuesta.ERROR_PROVEEDOR;
    }

    private static String describir(Exception error) {
        return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
    }
}
