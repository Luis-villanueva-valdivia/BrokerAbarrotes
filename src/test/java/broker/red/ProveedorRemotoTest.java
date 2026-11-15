package broker.red;

import broker.comun.EstadoRespuesta;
import broker.comun.Respuesta;
import broker.comun.Solicitud;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProveedorRemotoTest {

    private static final int MAX_REINTENTOS = 2;
    private static final int TIEMPO_CONEXION_MS = 1000;
    private static final int TIEMPO_LECTURA_MS = 100;
    private static final int DEMORA_PROVEEDOR_MS = 350;

    @Test
    void devuelveErrorProveedorCuandoAgotaReintentosPorDesconexion() throws IOException {
        int puerto;
        try (ServerSocket temporal = new ServerSocket(0)) {
            puerto = temporal.getLocalPort();
        }
        List<String> eventos = new ArrayList<>();
        ProveedorRemoto proveedor = new ProveedorRemoto("localhost", puerto,
                TIEMPO_CONEXION_MS, TIEMPO_LECTURA_MS, eventos::add);

        Respuesta respuesta = proveedor.ejecutar(solicitud());

        assertEquals(EstadoRespuesta.ERROR_PROVEEDOR, respuesta.getEstado());
        assertEquals(MAX_REINTENTOS, eventos.size());
        assertTrue(eventos.get(0).contains("Reintento 1/2"));
        assertTrue(eventos.get(1).contains("Reintento 2/2"));
    }

    @Test
    void devuelveTimeoutCuandoProveedorTardaMasDelLimiteYRegistraReintentos() throws Exception {
        List<String> eventos = new ArrayList<>();
        try (ServerSocket servidorLento = new ServerSocket(0)) {
            ExecutorService ejecutor = Executors.newSingleThreadExecutor();
            try {
                Future<?> aceptarSolicitudes = ejecutor.submit(() -> aceptarSinResponder(
                        servidorLento, MAX_REINTENTOS + 1));
                ProveedorRemoto proveedor = new ProveedorRemoto("localhost",
                        servidorLento.getLocalPort(), TIEMPO_CONEXION_MS,
                        TIEMPO_LECTURA_MS, eventos::add);

                Respuesta respuesta = proveedor.ejecutar(solicitud());

                assertEquals(EstadoRespuesta.TIMEOUT, respuesta.getEstado());
                assertEquals(MAX_REINTENTOS, eventos.size());
                assertTrue(eventos.stream().allMatch(evento -> evento.contains("estado=TIMEOUT")));
                aceptarSolicitudes.get(3, TimeUnit.SECONDS);
            } finally {
                ejecutor.shutdownNow();
            }
        }
    }

    private static Solicitud solicitud() {
        return new Solicitud("productos", "consultar", Map.of("codigo", "CC001"));
    }

    private static void aceptarSinResponder(ServerSocket servidor, int cantidad) {
        for (int i = 0; i < cantidad; i++) {
            try (Socket socket = servidor.accept();
                 BufferedReader entrada = new BufferedReader(
                         new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
                entrada.readLine();
                Thread.sleep(DEMORA_PROVEEDOR_MS);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }
    }
}
