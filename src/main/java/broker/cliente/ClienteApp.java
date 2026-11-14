package broker.cliente;

import broker.abarrotes.ClienteModel;
import broker.abarrotes.ProductoModel;
import broker.broker.Broker;
import broker.comun.EstadoRespuesta;
import broker.comun.Respuesta;
import broker.comun.Solicitud;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Cliente del broker: arma solicitudes, las envía y muestra las respuestas
 * en texto legible.
 *
 * <p>Solo usa Broker, Solicitud y Respuesta, además de los modelos ProductoModel y ClienteModel para mostrar resultados. No sabe qué proveedor atiende
 * cada servicio ni cómo se consulta la base de datos.</p>
 */
public class ClienteApp {

    private static final String SEPARADOR = " · ";
    private static final String SIN_RESULTADOS = "sin resultados";
    private static final String FORMATO_MONTO = "$%.2f";
    private static final String SEPARADOR_LISTA = "; ";

    private final Broker broker;

    /**
     * Crea el cliente que envía sus solicitudes a través del broker recibido.
     *
     * @param broker broker que atenderá las solicitudes
     */
    public ClienteApp(Broker broker) {
        this.broker = Objects.requireNonNull(broker, "El broker es obligatorio");
    }

    /**
     * Arma una solicitud y la envía al broker.
     *
     * @param servicio nombre del servicio, por ejemplo "productos"
     * @param operacion operación a ejecutar, por ejemplo "consultar"
     * @param parametros parámetros de la operación
     * @return respuesta del broker; nunca null
     */
    public Respuesta pedir(String servicio, String operacion, Map<String, String> parametros) {
        return broker.enviar(new Solicitud(servicio, operacion, parametros));
    }

    /**
     * Convierte una respuesta en texto legible para mostrarla en consola.
     *
     * @param respuesta respuesta recibida del broker
     * @return texto con el resultado, o con el estado y el mensaje de error
     */
    public String describir(Respuesta respuesta) {
        if (respuesta.getEstado() != EstadoRespuesta.EXITO) {
            return respuesta.getEstado() + SEPARADOR + respuesta.getMensajeError();
        }
        return describirResultado(respuesta.getResultado());
    }

    private static String describirResultado(Object resultado) {
        if (resultado instanceof ProductoModel producto) {
            return describirProducto(producto);
        }
        if (resultado instanceof ClienteModel cliente) {
            return cliente.getNombre() + SEPARADOR + "tel " + cliente.getTelefono();
        }
        if (resultado instanceof List<?> lista) {
            return describirLista(lista);
        }
        if (resultado instanceof Double monto) {
            return String.format(FORMATO_MONTO, monto);
        }
        return String.valueOf(resultado);
    }

    private static String describirProducto(ProductoModel producto) {
        return producto.getNombre()
                + SEPARADOR + String.format(FORMATO_MONTO, producto.getPrecio())
                + SEPARADOR + "existencia " + producto.getCantidad();
    }

    private static String describirLista(List<?> lista) {
        if (lista.isEmpty()) {
            return SIN_RESULTADOS;
        }
        return lista.stream()
                .map(ClienteApp::describirResultado)
                .collect(Collectors.joining(SEPARADOR_LISTA));
    }
}
