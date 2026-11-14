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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Levanta los tres proveedores remotos de la demo del nivel 2.
 *
 * <p>Cada proveedor escucha en su propio puerto y se registra dinámicamente
 * con el broker al iniciar. El acceso a los DAO se serializa porque todos
 * comparten la conexión SQLite del proceso.</p>
 */
public final class NodoProveedores {

    public static final int PUERTO_PRODUCTOS = 6001;
    public static final int PUERTO_VENTAS = 6002;
    public static final int PUERTO_CLIENTES = 6003;
    public static final int TIEMPO_LECTURA_MS = 10000;

    private static final String HOST_POR_DEFECTO = "localhost";
    private static final int PUERTO_BROKER = ServidorBroker.PUERTO;

    private NodoProveedores() {
    }

    /**
     * Inicia los tres servidores y los anuncia al broker.
     *
     * @param args opcionalmente {@code args[0]} es el host del broker
     * @throws IOException si no puede abrir un puerto o registrarse
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        String hostBroker = args.length == 0 ? HOST_POR_DEFECTO : args[0];
        ExecutorService ejecutores = Executors.newCachedThreadPool();
        Object accesoBaseDatos = new Object();

        try (ServerSocket productos = new ServerSocket(PUERTO_PRODUCTOS);
             ServerSocket ventas = new ServerSocket(PUERTO_VENTAS);
             ServerSocket clientes = new ServerSocket(PUERTO_CLIENTES)) {
            iniciarServidor(productos, new ServicioProductos(new ProductoDAO()),
                    "productos", PUERTO_PRODUCTOS, hostBroker, accesoBaseDatos, ejecutores);
            iniciarServidor(ventas, new ServicioVentas(new VentaDAO()),
                    "ventas", PUERTO_VENTAS, hostBroker, accesoBaseDatos, ejecutores);
            iniciarServidor(clientes, new ServicioClientes(new ClienteDAO()),
                    "clientes", PUERTO_CLIENTES, hostBroker, accesoBaseDatos, ejecutores);

            registrar(hostBroker, "productos", PUERTO_PRODUCTOS);
            registrar(hostBroker, "ventas", PUERTO_VENTAS);
            registrar(hostBroker, "clientes", PUERTO_CLIENTES);
            System.out.println("Proveedores registrados en el broker.");
            new CountDownLatch(1).await();
        } finally {
            ejecutores.shutdownNow();
        }
    }

    private static void iniciarServidor(ServerSocket servidor, ProveedorServicio proveedor,
                                       String servicio, int puerto, String hostBroker,
                                       Object accesoBaseDatos, ExecutorService ejecutores) {
        Thread hilo = new Thread(() -> aceptar(servidor, proveedor, servicio, accesoBaseDatos,
                ejecutores), "proveedor-" + servicio);
        hilo.setDaemon(false);
        hilo.start();
        System.out.println("Proveedor " + servicio + " escuchando en " + puerto
                + " (broker " + hostBroker + ")");
    }

    private static void aceptar(ServerSocket servidor, ProveedorServicio proveedor,
                                String servicio, Object accesoBaseDatos,
                                ExecutorService ejecutores) {
        while (!servidor.isClosed()) {
            try {
                Socket conexion = servidor.accept();
                ejecutores.submit(() -> atender(conexion, proveedor, servicio, accesoBaseDatos));
            } catch (IOException e) {
                if (!servidor.isClosed()) {
                    System.err.println("Error aceptando en " + servicio + ": " + describir(e));
                }
            }
        }
    }

    private static void atender(Socket conexion, ProveedorServicio proveedor,
                                String servicio, Object accesoBaseDatos) {
        try (Socket socket = conexion;
             BufferedReader entrada = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter salida = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            socket.setSoTimeout(TIEMPO_LECTURA_MS);
            MarshallerJson marshaller = new MarshallerJson();
            Respuesta respuesta = responder(marshaller, entrada.readLine(), proveedor, accesoBaseDatos);
            salida.write(marshaller.aJson(respuesta));
            salida.newLine();
            salida.flush();
        } catch (IOException e) {
            System.err.println("Error atendiendo " + servicio + ": " + describir(e));
        }
    }

    private static Respuesta responder(MarshallerJson marshaller, String linea,
                                       ProveedorServicio proveedor, Object accesoBaseDatos) {
        Solicitud solicitud;
        try {
            solicitud = marshaller.solicitudDesde(linea);
        } catch (IllegalArgumentException e) {
            return Respuesta.error(UUID.randomUUID(), EstadoRespuesta.PARAMETRO_INVALIDO,
                    e.getMessage() == null ? "Solicitud inválida" : e.getMessage());
        }
        synchronized (accesoBaseDatos) {
            return proveedor.ejecutar(solicitud);
        }
    }

    private static void registrar(String hostBroker, String servicio, int puerto) throws IOException {
        try (Socket socket = new Socket(hostBroker, PUERTO_BROKER);
             BufferedReader entrada = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter salida = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            MensajeRegistro registro = new MensajeRegistro(
                    MensajeRegistro.TIPO_REGISTRAR, servicio, HOST_POR_DEFECTO, puerto);
            MarshallerJson marshaller = new MarshallerJson();
            salida.write(marshaller.aJson(registro));
            salida.newLine();
            salida.flush();
            Respuesta respuesta = marshaller.respuestaDesde(entrada.readLine());
            if (respuesta.getEstado() != EstadoRespuesta.EXITO) {
                throw new IOException("No se pudo registrar " + servicio + ": "
                        + respuesta.getMensajeError());
            }
        }
    }

    private static String describir(Throwable error) {
        return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
    }
}
