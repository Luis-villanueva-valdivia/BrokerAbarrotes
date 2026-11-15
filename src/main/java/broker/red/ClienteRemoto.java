package broker.red;

import broker.comun.EstadoRespuesta;
import broker.comun.Respuesta;
import broker.comun.Solicitud;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Cliente de demostración del nivel 2.
 *
 * <p>Ejecuta por socket los 8 casos de {@code DemoNivel1}. El caso 8
 * (sustitución del proveedor) se demuestra en el nivel 1; en el nivel 2 el
 * broker sustituye al proveedor cuando {@code NodoProveedores} se vuelve a
 * registrar, y aquí solo se explica. Al final agrega dos casos extra: producto
 * no encontrado y un proveedor lento que termina en {@code TIMEOUT} después de
 * los reintentos.</p>
 */
public final class ClienteRemoto {

    private static final String HOST_POR_DEFECTO = "localhost";
    private static final int TIEMPO_LECTURA_MS = 10000;
    private static final MarshallerJson MARSHALLER = new MarshallerJson();

    private ClienteRemoto() {
    }

    /**
     * Ejecuta la demostración remota contra el broker.
     *
     * @param args opcionalmente {@code args[0]} es el host del broker
     * @throws IOException si no puede conectarse o leer una respuesta
     */
    public static void main(String[] args) throws IOException {
        String hostBroker = args.length == 0 ? HOST_POR_DEFECTO : args[0];
        ClienteRemoto cliente = new ClienteRemoto();
        cliente.ejecutarDemo(hostBroker);
    }

    private void ejecutarDemo(String hostBroker) throws IOException {
        titulo("Caso 1 · Registro");
        System.out.println("Los proveedores se registran al iniciar NodoProveedores.");

        titulo("Caso 2 · Localización");
        mostrar(hostBroker, "productos.consultar codigo=CC001",
                new Solicitud("productos", "consultar", Map.of("codigo", "CC001")));

        titulo("Caso 3 · Una operación por servicio");
        mostrar(hostBroker, "ventas.totalDelDia fecha=2026-05-12",
                new Solicitud("ventas", "totalDelDia", Map.of("fecha", "2026-05-12")));
        mostrar(hostBroker, "clientes.consultar id=1",
                new Solicitud("clientes", "consultar", Map.of("id", "1")));

        titulo("Caso 4 · Respuesta exitosa");
        mostrar(hostBroker, "productos.buscar nombre=Sabritas",
                new Solicitud("productos", "buscar", Map.of("nombre", "Sabritas")));

        titulo("Caso 5 · Servicio desconocido");
        mostrar(hostBroker, "facturacion.consultar",
                new Solicitud("facturacion", "consultar", Map.of()));

        titulo("Caso 6 · Parámetro inválido");
        mostrar(hostBroker, "productos.stockBajo umbral=abc",
                new Solicitud("productos", "stockBajo", Map.of("umbral", "abc")));

        titulo("Caso 7 · Excepción del proveedor");
        mostrar(hostBroker, "prueba.ejecutar",
                new Solicitud("prueba", "ejecutar", Map.of()));
        mostrar(hostBroker, "productos.consultar codigo=CC001 (el broker sigue activo)",
                new Solicitud("productos", "consultar", Map.of("codigo", "CC001")));

        titulo("Caso 8 · Sustitución del proveedor");
        System.out.println("Se demuestra en el nivel 1 (DemoNivel1). En el nivel 2, el broker sustituye");
        System.out.println("al proveedor cuando NodoProveedores se reinicia y se vuelve a registrar.");

        titulo("Extra · Producto no encontrado");
        mostrar(hostBroker, "productos.consultar codigo=NO-EXISTE",
                new Solicitud("productos", "consultar", Map.of("codigo", "NO-EXISTE")));

        titulo("Extra · Proveedor lento (timeout y reintentos)");
        mostrar(hostBroker, "lento.ejecutar",
                new Solicitud("lento", "ejecutar", Map.of()));
    }

    private void mostrar(String hostBroker, String etiqueta, Solicitud solicitud) throws IOException {
        Respuesta respuesta = pedir(hostBroker, solicitud);
        System.out.println(etiqueta + ": " + describir(respuesta));
    }

    private Respuesta pedir(String hostBroker, Solicitud solicitud) throws IOException {
        try (Socket socket = new Socket(hostBroker, ServidorBroker.PUERTO);
             BufferedReader entrada = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter salida = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
            socket.setSoTimeout(TIEMPO_LECTURA_MS);
            salida.write(MARSHALLER.aJson(solicitud));
            salida.newLine();
            salida.flush();
            String linea = entrada.readLine();
            if (linea == null) {
                throw new IOException("El broker cerró la conexión sin responder");
            }
            return MARSHALLER.respuestaDesde(linea);
        }
    }

    private static String describir(Respuesta respuesta) {
        if (respuesta.getEstado() != EstadoRespuesta.EXITO) {
            return respuesta.getEstado() + " · " + respuesta.getMensajeError();
        }
        return String.valueOf(respuesta.getResultado());
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println("=== " + texto + " ===");
    }
}
