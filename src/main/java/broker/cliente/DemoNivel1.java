package broker.cliente;

import broker.abarrotes.ClienteDAO;
import broker.abarrotes.ProductoDAO;
import broker.abarrotes.VentaDAO;
import broker.broker.Broker;
import broker.broker.RegistroServicios;
import broker.comun.ProveedorServicio;
import broker.comun.Respuesta;
import broker.comun.Solicitud;
import broker.proveedores.ServicioClientes;
import broker.proveedores.ServicioProductos;
import broker.proveedores.ServicioProductosMemoria;
import broker.proveedores.ServicioVentas;

import java.util.Map;

/**
 * Demostración del nivel 1: corre los 8 casos de la actividad en orden, con
 * un título para cada uno.
 *
 * <p>Es el único lugar que conoce a los proveedores, porque aquí se registran.
 * ClienteApp solo conoce Broker, Solicitud y Respuesta.</p>
 */
public class DemoNivel1 {

    private static final String SERVICIO_PRODUCTOS = "productos";
    private static final String SERVICIO_VENTAS = "ventas";
    private static final String SERVICIO_CLIENTES = "clientes";
    private static final String SERVICIO_FACTURACION = "facturacion";
    private static final String SERVICIO_PRUEBA = "prueba";

    private static final String OPERACION_CONSULTAR = "consultar";
    private static final String OPERACION_BUSCAR = "buscar";
    private static final String OPERACION_TOTAL_DEL_DIA = "totalDelDia";
    private static final String OPERACION_STOCK_BAJO = "stockBajo";
    private static final String OPERACION_EJECUTAR = "ejecutar";

    private static final String CODIGO_COCA_COLA = "CC001";
    private static final String NOMBRE_SABRITAS = "Sabritas";
    private static final String FECHA_DEMO = "2026-05-12";
    private static final String ID_CLIENTE_DEMO = "1";
    private static final String UMBRAL_INVALIDO = "abc";

    private static final String SEPARADOR_TITULO = "=== ";
    private static final String CIERRE_TITULO = " ===";

    /**
     * Ejecuta los 8 casos del nivel 1 en orden.
     *
     * @param args no se usa
     */
    public static void main(String[] args) {
        RegistroServicios registro = new RegistroServicios();
        Broker broker = new Broker(registro);
        ClienteApp cliente = new ClienteApp(broker);

        casoRegistro(registro);
        casoLocalizacion(registro);
        casoUnaOperacionPorServicio(cliente);
        casoRespuestaExitosa(cliente);
        casoServicioDesconocido(cliente);
        casoParametroInvalido(cliente);
        casoExcepcionDelProveedor(registro, cliente);
        casoSustitucion(registro, cliente);
    }

    private static void casoRegistro(RegistroServicios registro) {
        titulo("Caso 1 · Registro");
        registro.registrar(SERVICIO_PRODUCTOS, new ServicioProductos(new ProductoDAO()));
        registro.registrar(SERVICIO_VENTAS, new ServicioVentas(new VentaDAO()));
        registro.registrar(SERVICIO_CLIENTES, new ServicioClientes(new ClienteDAO()));
        System.out.println("Servicios registrados: " + registro.listar());
    }

    private static void casoLocalizacion(RegistroServicios registro) {
        titulo("Caso 2 · Localización");
        ProveedorServicio proveedor = registro.buscar(SERVICIO_PRODUCTOS);
        System.out.println(SERVICIO_PRODUCTOS + " → " + proveedor.getClass().getSimpleName());
    }

    private static void casoUnaOperacionPorServicio(ClienteApp cliente) {
        titulo("Caso 3 · Una operación por servicio");
        mostrar(cliente, "productos.consultar codigo=" + CODIGO_COCA_COLA,
                SERVICIO_PRODUCTOS, OPERACION_CONSULTAR, Map.of("codigo", CODIGO_COCA_COLA));
        mostrar(cliente, "ventas.totalDelDia fecha=" + FECHA_DEMO,
                SERVICIO_VENTAS, OPERACION_TOTAL_DEL_DIA, Map.of("fecha", FECHA_DEMO));
        mostrar(cliente, "clientes.consultar id=" + ID_CLIENTE_DEMO,
                SERVICIO_CLIENTES, OPERACION_CONSULTAR, Map.of("id", ID_CLIENTE_DEMO));
    }

    private static void casoRespuestaExitosa(ClienteApp cliente) {
        titulo("Caso 4 · Respuesta exitosa");
        mostrar(cliente, "productos.buscar nombre=" + NOMBRE_SABRITAS,
                SERVICIO_PRODUCTOS, OPERACION_BUSCAR, Map.of("nombre", NOMBRE_SABRITAS));
    }

    private static void casoServicioDesconocido(ClienteApp cliente) {
        titulo("Caso 5 · Servicio desconocido");
        mostrar(cliente, "facturacion.consultar",
                SERVICIO_FACTURACION, OPERACION_CONSULTAR, Map.of());
    }

    private static void casoParametroInvalido(ClienteApp cliente) {
        titulo("Caso 6 · Parámetro inválido");
        mostrar(cliente, "productos.stockBajo umbral=" + UMBRAL_INVALIDO,
                SERVICIO_PRODUCTOS, OPERACION_STOCK_BAJO, Map.of("umbral", UMBRAL_INVALIDO));
    }

    private static void casoExcepcionDelProveedor(RegistroServicios registro, ClienteApp cliente) {
        titulo("Caso 7 · Excepción del proveedor");
        registro.registrar(SERVICIO_PRUEBA, new ProveedorQueFalla());
        mostrar(cliente, "prueba.ejecutar", SERVICIO_PRUEBA, OPERACION_EJECUTAR, Map.of());
        mostrar(cliente, "productos.consultar codigo=" + CODIGO_COCA_COLA + " (el broker sigue activo)",
                SERVICIO_PRODUCTOS, OPERACION_CONSULTAR, Map.of("codigo", CODIGO_COCA_COLA));
    }

    private static void casoSustitucion(RegistroServicios registro, ClienteApp cliente) {
        titulo("Caso 8 · Sustitución del proveedor");
        registro.sustituir(SERVICIO_PRODUCTOS, new ServicioProductosMemoria());
        mostrar(cliente, "productos.buscar nombre=" + NOMBRE_SABRITAS + " (con proveedor en memoria)",
                SERVICIO_PRODUCTOS, OPERACION_BUSCAR, Map.of("nombre", NOMBRE_SABRITAS));
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println(SEPARADOR_TITULO + texto + CIERRE_TITULO);
    }

    private static void mostrar(ClienteApp cliente, String etiqueta, String servicio,
                                String operacion, Map<String, String> parametros) {
        Respuesta respuesta = cliente.pedir(servicio, operacion, parametros);
        System.out.println(etiqueta + ": " + cliente.describir(respuesta));
    }

    /**
     * Proveedor de prueba que siempre lanza una excepción, para el caso 7.
     */
    private static final class ProveedorQueFalla implements ProveedorServicio {

        /**
         * Lanza una excepción en lugar de responder.
         *
         * @param solicitud solicitud recibida
         * @return nunca regresa; siempre lanza IllegalStateException
         */
        @Override
        public Respuesta ejecutar(Solicitud solicitud) {
            throw new IllegalStateException("fallo simulado del proveedor de prueba");
        }
    }
}
