package broker.red;

import broker.abarrotes.ClienteDAO;
import broker.abarrotes.ProductoDAO;
import broker.abarrotes.VentaDAO;
import broker.comun.EstadoRespuesta;
import broker.comun.ProveedorServicio;
import broker.comun.Respuesta;
import broker.comun.Solicitud;
import broker.proveedores.ServicioClientes;
import broker.proveedores.ServicioProductos;
import broker.proveedores.ServicioVentas;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de {@link NodoProveedores} con el broker real, los proveedores reales
 * y la base {@code data/tienda.db}.
 */
class NodoProveedoresTest {

    private static final MarshallerJson MARSHALLER = new MarshallerJson();
    private static final int CLIENTES_SIMULTANEOS = 2;
    private static final int TIEMPO_ESPERA_S = 10;
    private static final double TOTAL_VENTAS_12_MAYO = 440.5;

    private static final Object ACCESO_BASE_DATOS = new Object();
    private static final List<ServerSocket> SERVIDORES = new ArrayList<>();
    private static final ExecutorService EJECUTOR = Executors.newCachedThreadPool();
    private static ServidorBroker broker;

    @BeforeAll
    static void iniciarBrokerYProveedores() throws Exception {
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

        levantarYRegistrar("productos", new ServicioProductos(new ProductoDAO()));
        levantarYRegistrar("ventas", new ServicioVentas(new VentaDAO()));
        levantarYRegistrar("clientes", new ServicioClientes(new ClienteDAO()));
        levantarYRegistrar("prueba", solicitud -> {
            throw new IllegalStateException("fallo simulado del proveedor de prueba");
        });
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
    void dosClientesSimultaneosNoProducenErroresDeBaseDeDatos() throws Exception {
        CountDownLatch clientesListos = new CountDownLatch(CLIENTES_SIMULTANEOS);
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<List<Respuesta>>> clientes = new ArrayList<>();
        for (int i = 0; i < CLIENTES_SIMULTANEOS; i++) {
            clientes.add(EJECUTOR.submit(() -> {
                clientesListos.countDown();
                if (!salida.await(TIEMPO_ESPERA_S, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Los clientes no arrancaron a tiempo");
                }
                return pedirOperacionesDeLosTresServicios();
            }));
        }
        assertTrue(clientesListos.await(TIEMPO_ESPERA_S, TimeUnit.SECONDS));
        salida.countDown();

        for (Future<List<Respuesta>> cliente : clientes) {
            List<Respuesta> respuestas = cliente.get(TIEMPO_ESPERA_S, TimeUnit.SECONDS);
            for (Respuesta respuesta : respuestas) {
                assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado(), respuesta.getMensajeError());
            }
            assertTrue(String.valueOf(respuestas.get(0).getResultado()).contains("Coca Cola"));
            assertEquals(TOTAL_VENTAS_12_MAYO, ((Number) respuestas.get(1).getResultado()).doubleValue());
            assertTrue(String.valueOf(respuestas.get(2).getResultado()).contains("Juan"));
        }
    }

    @Test
    void solicitudMalFormadaRespondeParametroInvalido() throws IOException {
        int puertoProductos = SERVIDORES.get(0).getLocalPort();

        Respuesta respuesta = enviarTexto(puertoProductos, "esto no es json");

        assertEquals(EstadoRespuesta.PARAMETRO_INVALIDO, respuesta.getEstado());
    }

    @Test
    void proveedorQueLanzaExcepcionRespondeErrorProveedor() throws IOException {
        Respuesta respuesta = pedirAlBroker(new Solicitud("prueba", "ejecutar", Map.of()));

        assertEquals(EstadoRespuesta.ERROR_PROVEEDOR, respuesta.getEstado());
        assertEquals("fallo simulado del proveedor de prueba", respuesta.getMensajeError());
    }

    private static List<Respuesta> pedirOperacionesDeLosTresServicios() throws IOException {
        return List.of(
                pedirAlBroker(new Solicitud("productos", "consultar", Map.of("codigo", "CC001"))),
                pedirAlBroker(new Solicitud("ventas", "totalDelDia", Map.of("fecha", "2026-05-12"))),
                pedirAlBroker(new Solicitud("clientes", "consultar", Map.of("id", "1"))));
    }

    private static void levantarYRegistrar(String servicio, ProveedorServicio proveedor) throws IOException {
        ServerSocket servidor = new ServerSocket(0);
        SERVIDORES.add(servidor);
        Thread hilo = new Thread(() -> {
            while (!servidor.isClosed()) {
                try {
                    Socket conexion = servidor.accept();
                    EJECUTOR.submit(() -> NodoProveedores.atender(
                            conexion, proveedor, servicio, ACCESO_BASE_DATOS));
                } catch (IOException e) {
                    if (!servidor.isClosed()) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        });
        hilo.setDaemon(true);
        hilo.start();

        MensajeRegistro registro = new MensajeRegistro(
                MensajeRegistro.TIPO_REGISTRAR, servicio, "localhost", servidor.getLocalPort());
        Respuesta respuesta = enviarTexto(ServidorBroker.PUERTO, MARSHALLER.aJson(registro));
        assertEquals(EstadoRespuesta.EXITO, respuesta.getEstado(), respuesta.getMensajeError());
    }

    private static Respuesta pedirAlBroker(Solicitud solicitud) throws IOException {
        return enviarTexto(ServidorBroker.PUERTO, MARSHALLER.aJson(solicitud));
    }

    private static Respuesta enviarTexto(int puerto, String texto) throws IOException {
        try (Socket socket = new Socket("localhost", puerto);
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
                throw new IOException("Se cerró la conexión sin respuesta");
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
