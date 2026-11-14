package broker.red;

import broker.comun.Respuesta;
import broker.comun.Solicitud;
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
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServidorBrokerTest {

    private static final MarshallerJson MARSHALLER = new MarshallerJson();
    private static Thread hiloBroker;
    private static Thread hiloProveedor;
    private static ServerSocket servidorProveedor;
    private static ExecutorService ejecutorProveedor;

    @BeforeAll
    static void iniciarProcesos() throws Exception {
        servidorProveedor = new ServerSocket(0);
        ejecutorProveedor = Executors.newCachedThreadPool();
        hiloProveedor = new Thread(ServidorBrokerTest::aceptarProveedores);
        hiloProveedor.setDaemon(true);
        hiloProveedor.start();
        hiloBroker = new Thread(() -> {
            try {
                ServidorBroker.main(new String[0]);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
        hiloBroker.setDaemon(true);
        hiloBroker.start();
        esperarBroker();
    }

    @AfterAll
    static void detenerProveedor() throws IOException {
        if (servidorProveedor != null) {
            servidorProveedor.close();
        }
        if (ejecutorProveedor != null) {
            ejecutorProveedor.shutdownNow();
        }
    }

    @Test
    void registraProveedorDespuesDeArrancarYAtiendeDosClientesEnParalelo() throws Exception {
        registrarProveedor();
        Future<Respuesta> respuestaClienteUno = enviarEnSegundoPlano(
                solicitud("productos", "buscar", "arroz"));
        Future<Respuesta> respuestaClienteDos = enviarEnSegundoPlano(
                solicitud("productos", "consultar", "azucar"));

        Respuesta respuestaUno = respuestaClienteUno.get(5, TimeUnit.SECONDS);
        Respuesta respuestaDos = respuestaClienteDos.get(5, TimeUnit.SECONDS);

        assertEquals("productos.buscar", respuestaUno.getResultado());
        assertEquals("productos.consultar", respuestaDos.getResultado());
    }

    private static Future<Respuesta> enviarEnSegundoPlano(Solicitud solicitud) {
        return ejecutorProveedor.submit(() -> enviarSolicitudAlBroker(solicitud));
    }

    private static Respuesta enviarSolicitudAlBroker(Solicitud solicitud) throws IOException {
        try (Socket socket = new Socket("localhost", ServidorBroker.PUERTO);
             BufferedReader entrada = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter salida = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            salida.write(MARSHALLER.aJson(solicitud));
            salida.newLine();
            salida.flush();
            return MARSHALLER.respuestaDesde(entrada.readLine());
        }
    }

    private static Solicitud solicitud(String servicio, String operacion, String valor) {
        return new Solicitud(servicio, operacion, Map.of("valor", valor));
    }

    private static void registrarProveedor() throws IOException {
        try (Socket socket = new Socket("localhost", ServidorBroker.PUERTO);
             BufferedReader entrada = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter salida = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            MensajeRegistro registro = new MensajeRegistro(
                    MensajeRegistro.TIPO_REGISTRAR, "productos", "localhost", servidorProveedor.getLocalPort());
            salida.write(MARSHALLER.aJson(registro));
            salida.newLine();
            salida.flush();
            assertTrue(MARSHALLER.respuestaDesde(entrada.readLine()).getMensajeError() == null);
        }
    }

    private static void esperarBroker() throws InterruptedException {
        long limite = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < limite) {
            try (Socket ignored = new Socket("localhost", ServidorBroker.PUERTO)) {
                return;
            } catch (IOException e) {
                Thread.sleep(50);
            }
        }
        throw new IllegalStateException("El broker no inició a tiempo");
    }

    private static void aceptarProveedores() {
        try {
            while (!servidorProveedor.isClosed()) {
                Socket conexion = servidorProveedor.accept();
                ejecutorProveedor.submit(() -> atenderProveedor(conexion));
            }
        } catch (IOException e) {
            if (!servidorProveedor.isClosed()) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static void atenderProveedor(Socket conexion) {
        try (Socket socket = conexion;
             BufferedReader entrada = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter salida = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            Solicitud solicitud = MARSHALLER.solicitudDesde(entrada.readLine());
            Respuesta respuesta = Respuesta.exito(solicitud,
                    solicitud.getServicio() + "." + solicitud.getOperacion());
            salida.write(MARSHALLER.aJson(respuesta));
            salida.newLine();
            salida.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
