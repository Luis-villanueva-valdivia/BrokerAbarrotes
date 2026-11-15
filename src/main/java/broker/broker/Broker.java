package broker.broker;

import broker.comun.EstadoRespuesta;
import broker.comun.ProveedorServicio;
import broker.comun.Respuesta;
import broker.comun.Solicitud;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Intermediario entre el cliente y los proveedores.
 *
 * <p>Recibe una Solicitud, busca en el RegistroServicios al proveedor
 * del servicio pedido, le reenvía la solicitud y devuelve su Respuesta.
 * Nunca deja escapar una excepción hacia quien lo llama: cualquier falla se
 * convierte en una respuesta de error.</p>
 */
public class Broker {

    private static final String PREFIJO_BITACORA = "[BROKER]";
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int LARGO_ID_CORTO = 8;
    private static final String MENSAJE_SOLICITUD_NULA = "La solicitud es obligatoria";
    private static final String MENSAJE_RESPUESTA_NULA = "El proveedor no devolvió respuesta";

    private final RegistroServicios registro;
    private final Consumer<String> bitacora;

    /**
     * Crea un broker que escribe su bitácora en la consola.
     *
     * @param registro registro de servicios que consultará el broker
     */
    public Broker(RegistroServicios registro) {
        this(registro, System.out::println);
    }

    /**
     * Crea un broker con un destino de bitácora propio, útil en pruebas
     * o para mandar la bitácora a un archivo en el nivel 2.
     *
     * @param registro registro de servicios que consultará el broker
     * @param bitacora destino de cada línea de bitácora
     */
    public Broker(RegistroServicios registro, Consumer<String> bitacora) {
        this.registro = Objects.requireNonNull(registro, "El registro es obligatorio");
        this.bitacora = Objects.requireNonNull(bitacora, "La bitácora es obligatoria");
    }

    /**
     * Envía una solicitud al proveedor que atiende su servicio.
     *
     * @param solicitud solicitud del cliente
     * @return respuesta del proveedor, o una respuesta de error con estado
     *         PARAMETRO_INVALIDO, SERVICIO_DESCONOCIDO o ERROR_PROVEEDOR;
     *         nunca null y nunca lanza excepción
     */
    public Respuesta enviar(Solicitud solicitud) {
        long inicio = System.nanoTime();
        Respuesta respuesta = atender(solicitud);
        registrarEnBitacora(solicitud, respuesta, inicio);
        return respuesta;
    }

    /** @return registro de servicios que usa este broker */
    public RegistroServicios getRegistro() {
        return registro;
    }

    private Respuesta atender(Solicitud solicitud) {
        if (solicitud == null) {
            return Respuesta.error(UUID.randomUUID(), EstadoRespuesta.PARAMETRO_INVALIDO,
                    MENSAJE_SOLICITUD_NULA);
        }
        UUID id = solicitud.getId();
        try {
            ProveedorServicio proveedor = registro.buscar(solicitud.getServicio());
            Respuesta respuesta = proveedor.ejecutar(solicitud);
            if (respuesta == null) {
                return Respuesta.error(id, EstadoRespuesta.ERROR_PROVEEDOR, MENSAJE_RESPUESTA_NULA);
            }
            return respuesta;
        } catch (ServicioDesconocidoException e) {
            return Respuesta.error(id, EstadoRespuesta.SERVICIO_DESCONOCIDO, e.getMessage());
        } catch (RuntimeException e) {
            // Cualquier falla del proveedor se reporta al cliente como respuesta, no como excepción.
            return Respuesta.error(id, EstadoRespuesta.ERROR_PROVEEDOR, describir(e));
        }
    }

    private void registrarEnBitacora(Solicitud solicitud, Respuesta respuesta, long inicio) {
        long milisegundos = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - inicio);
        String linea = String.format("%s %s id=%s %s → %s (%d ms)",
                PREFIJO_BITACORA,
                LocalTime.now().format(FORMATO_HORA),
                idCorto(respuesta.getIdSolicitud()),
                describirServicio(solicitud),
                respuesta.getEstado(),
                milisegundos);
        try {
            bitacora.accept(linea);
        } catch (RuntimeException e) {
            // Una falla al escribir la bitácora no debe romper la respuesta al cliente.
            System.err.println(PREFIJO_BITACORA + " no se pudo escribir la bitácora: " + describir(e));
        }
    }

    private static String describirServicio(Solicitud solicitud) {
        if (solicitud == null) {
            return "(sin solicitud)";
        }
        return solicitud.getServicio() + "." + solicitud.getOperacion();
    }

    private static String idCorto(UUID id) {
        return id.toString().substring(0, LARGO_ID_CORTO);
    }

    private static String describir(Throwable error) {
        String mensaje = error.getMessage();
        if (mensaje == null || mensaje.isBlank()) {
            return error.getClass().getSimpleName();
        }
        return error.getClass().getSimpleName() + ": " + mensaje;
    }
}