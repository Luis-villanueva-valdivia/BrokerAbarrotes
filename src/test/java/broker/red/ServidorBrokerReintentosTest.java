package broker.red;

import broker.comun.EstadoRespuesta;
import broker.comun.ProveedorServicio;
import broker.comun.Respuesta;
import broker.comun.Solicitud;
import broker.proveedores.ServicioProductosMemoria;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de los criterios del ISS-11 pasando por el broker real: proveedor
 * apagado, proveedor lento con reintentos en la bitácora y re-registro.
 */
class ServidorBrokerReintentosTest {

    private static final MarshallerJson MARSHALLER = new MarshallerJson();
    private static final int TIEMPO_ESPERA_S = 20;
    private static final int DEMORA_LENTO_MS = ServidorBroker.TIEMPO_RESPUESTA_PROVEEDOR_MS + 1000;

    private static final List<ServerSocket> SERVIDORES = new ArrayList<>();
    private static final ExecutorService EJECUTOR = Executors.newCachedThreadPool();
    private static ServidorBroker broker;

    @BeforeAll
    static void iniciarBroker() throws Exception {
        broker = new ServidorBroker();
        Thread hiloBroker = new Thread(() -> {
            try {
                broker.ejecutar();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
        hiloBroker.setDaemon(true);
        hiloBroker.start();
        esperarBroker();
    }

    @AfterAll
    static void detener() throws IOException {
        if (broker != null) {
            broker.close();
        }
        for (ServerSocket servidor : SERVIDORES) {
            servidor.close();
        }
        EJECUTOR.shutdownNow();
    }

    @Test
    void proveedorApagadoRespondeErrorProveedorYSeRecuperaAlVolverARegistrarse() throws IOException {
        int puertoApagado;
        try (ServerSocket temporal = new ServerSocket(0)) {
            puertoApagado = temporal.getLocalPort();
        }
        registrar("apagado", puertoApagado);

        Respuesta apagado = pedir(new Solicitud("apagado", "buscar", Map.of("nombre", "Refresco")));
        Respuesta brokerActivo = pedir(new Solicitud("facturacion", "consultar", Map.of()));

        assertEquals(EstadoRespuesta.ERROR_PROVEEDOR, apagado.getEstado());
        assertEquals(EstadoRespuesta.SERVICIO_DESCONOCIDO, brokerActivo.getEstado());

        registrar("apagado", levantar(new ServicioProductosMemoria()));
        Respuesta recuperado = pedir(new Solicitud("apagado", "buscar", Map.of("nombre", "Refresco")));

        assertEquals(EstadoRespuesta.EXITO, recuperado.getEstado(), recuperado.getMensajeError());
    }

    @Test
    void proveedorLentoRespondeTimeoutYLaBitacoraMuestraLosReintentos() throws IOException {
        registrar("lentoprueba", levantar(solicitud -> {
            try {
                Thread.sleep(DEMORA_LENTO_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return Respuesta.exito(solicitud, "tarde");
        }));

        Respuesta respuesta = pedir(new Solicitud("lentoprueba", "ejecutar", Map.of()));

        assertEquals(EstadoRespuesta.TIMEOUT, respuesta.getEstado());
        String bitacora = Files.readString(ServidorBroker.ARCHIVO_BITACORA, StandardCharsets.UTF_8);
        assertTrue(bitacora.contains("Reintento 1/2 lentoprueba.ejecutar"));
        assertTrue(bitacora.contains("Reintento 2/2 lentoprueba.ejecutar"));
    }

    private static int levantar(ProveedorServicio proveedor) throws IOException {
        ServerSocket servidor = new ServerSocket(0);
        SERVIDORES.add(servidor);
        Object candado = new Object();
        Thread hilo = new Thread(() -> {
            while (!servidor.isClosed()) {
                try {
                    Socket conexion = servidor.accept();
                    EJECUTOR.submit(() -> NodoProveedores.atender(conexion, proveedor, "prueba", candado));
                } catch (IOException e) {
                    if (!servidor.isClosed()) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        });
        hilo.setDaemon(true);
        hilo.start();
        return servidor.getLocalPort();
    }

    private static void registrar(String servicio, int puerto) throws IOException {
        MensajeRegistro registro = new MensajeRegistro(
                MensajeRegistro.TIPO_REGISTRAR, servicio, "localhost", puerto);
        Respuesta respuesta = enviar(MARSHALLER.aJson(registro));
        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado(), respuesta.getMensajeError());
    }

    private static Respuesta pedir(Solicitud solicitud) throws IOException {
        return enviar(MARSHALLER.aJson(solicitud));
    }

    private static Respuesta enviar(String texto) throws IOException {
        try (Socket socket = new Socket("localhost", ServidorBroker.PUERTO);
             BufferedReader entrada = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter salida = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            socket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(TIEMPO_ESPERA_S));
            salida.write(texto);
            salida.newLine();
            salida.flush();
            String linea = entrada.readLine();
            if (linea == null) {
                throw new IOException("El broker cerró la conexión sin responder");
            }
            return MARSHALLER.respuestaDesde(linea);
        }
    }

    private static void esperarBroker() throws InterruptedException {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(TIEMPO_ESPERA_S);
        while (System.nanoTime() < limite) {
            try (Socket ignorado = new Socket("localhost", ServidorBroker.PUERTO)) {
                return;
            } catch (IOException e) {
                Thread.sleep(50);
            }
        }
        throw new IllegalStateException("El broker no inició a tiempo");
    }
}
