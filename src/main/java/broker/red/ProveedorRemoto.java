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
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Adaptador de un proveedor que vive en otro proceso.
 *
 * <p>Cada solicitud usa una conexión TCP independiente. El proveedor remoto
 * recibe una solicitud JSON y devuelve una respuesta JSON, ambas en una sola
 * línea.</p>
 */
public final class ProveedorRemoto implements ProveedorServicio {

    private final String host;
    private final int puerto;
    private final int tiempoConexionMs;
    private final int tiempoLecturaMs;
    private final MarshallerJson marshaller;

    public ProveedorRemoto(String host, int puerto, int tiempoConexionMs, int tiempoLecturaMs) {
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
    }

    @Override
    public Respuesta ejecutar(Solicitud solicitud) {
        Objects.requireNonNull(solicitud, "La solicitud es obligatoria");
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
        } catch (IOException | IllegalArgumentException e) {
            return Respuesta.error(solicitud.getId(), EstadoRespuesta.ERROR_PROVEEDOR,
                    e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        }
    }
}
