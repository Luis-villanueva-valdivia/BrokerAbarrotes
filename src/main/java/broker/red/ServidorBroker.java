package broker.red;

import broker.broker.Broker;
import broker.broker.RegistroServicios;
import broker.comun.EstadoRespuesta;
import broker.comun.Respuesta;
import broker.comun.Solicitud;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Punto de entrada del broker distribuido.
 *
 * <p>Un cliente o proveedor envía un JSON por línea y recibe una respuesta por
 * línea. El servidor conserva el registro entre conexiones, por lo que un
 * proveedor puede registrarse después de arrancar el broker.</p>
 */
public final class ServidorBroker {

    public static final int PUERTO = 5000;
    public static final int TIEMPO_CONEXION_MS = 3000;
    public static final int TIEMPO_LECTURA_MS = 10000;
    public static final Path ARCHIVO_BITACORA = Path.of("logs", "broker.log");

    private ServidorBroker() {
    }

    public static void main(String[] args) throws IOException {
        RegistroServicios registro = new RegistroServicios();
        ExecutorService ejecutores = Executors.newCachedThreadPool();
        try (Bitacora bitacora = new Bitacora(ARCHIVO_BITACORA);
             ServerSocket servidor = new ServerSocket(PUERTO)) {
            Broker broker = new Broker(registro, bitacora::escribir);
            System.out.println("ServidorBroker escuchando en el puerto " + PUERTO);
            while (!servidor.isClosed()) {
                Socket conexion = servidor.accept();
                ejecutores.submit(() -> atender(conexion, broker, registro));
            }
        } finally {
            ejecutores.shutdown();
        }
    }

    private static void atender(Socket conexion, Broker broker, RegistroServicios registro) {
        try (Socket socket = conexion;
             BufferedReader entrada = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter salida = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            socket.setSoTimeout(TIEMPO_LECTURA_MS);
            try {
                String linea = entrada.readLine();
                if (linea == null || linea.isBlank()) {
                    escribir(salida, Respuesta.error(UUID.randomUUID(), EstadoRespuesta.PARAMETRO_INVALIDO,
                            "El mensaje está vacío"));
                    return;
                }

                MarshallerJson marshaller = new MarshallerJson();
                if (esRegistro(linea)) {
                    MensajeRegistro mensaje = marshaller.registroDesde(linea);
                    registro.registrar(mensaje.servicio(),
                            new ProveedorRemoto(mensaje.host(), mensaje.puerto(),
                                    TIEMPO_CONEXION_MS, TIEMPO_LECTURA_MS));
                    escribir(salida, Respuesta.exito(UUID.randomUUID(), "Servicio registrado"));
                } else {
                    Solicitud solicitud = marshaller.solicitudDesde(linea);
                    escribir(salida, broker.enviar(solicitud));
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                escribir(salida, Respuesta.error(UUID.randomUUID(), EstadoRespuesta.PARAMETRO_INVALIDO,
                        e.getMessage() == null ? "Mensaje inválido" : e.getMessage()));
            }
        } catch (IOException e) {
            System.err.println("[BROKER] Error de conexión: " + describir(e));
        }
    }

    private static boolean esRegistro(String json) {
        try {
            JsonElement mensaje = JsonParser.parseString(json);
            return mensaje.isJsonObject() && mensaje.getAsJsonObject().has("tipo");
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static void escribir(BufferedWriter salida, Respuesta respuesta) throws IOException {
        salida.write(new MarshallerJson().aJson(respuesta));
        salida.newLine();
        salida.flush();
    }

    private static String describir(Throwable error) {
        return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
    }

    private static final class Bitacora implements AutoCloseable {
        private final java.io.PrintWriter archivo;

        private Bitacora(Path ruta) throws IOException {
            Path padre = ruta.getParent();
            if (padre != null) {
                Files.createDirectories(padre);
            }
            archivo = new java.io.PrintWriter(Files.newBufferedWriter(ruta, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND), true);
        }

        private synchronized void escribir(String linea) {
            System.out.println(linea);
            archivo.println(linea);
            if (archivo.checkError()) {
                System.err.println("[BROKER] No se pudo escribir la bitácora");
            }
        }

        @Override
        public void close() {
            archivo.close();
        }
    }
}
