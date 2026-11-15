package broker.proveedores;

import broker.abarrotes.VentaDAO;
import broker.abarrotes.VentaModel;
import broker.comun.EstadoRespuesta;
import broker.comun.ProveedorServicio;
import broker.comun.Respuesta;
import broker.comun.Solicitud;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Proveedor del servicio de ventas conectado al DAO de ventas.
 *
 * <p>Permite consultar una venta por su identificador y calcular el total
 * vendido en una fecha determinada.</p>
 */
public class ServicioVentas implements ProveedorServicio {

    private static final String OPERACION_CONSULTAR = "consultar";
    private static final String OPERACION_TOTAL_DEL_DIA = "totalDelDia";
    private static final String PARAMETRO_ID_VENTA = "idVenta";
    private static final String PARAMETRO_FECHA = "fecha";
    private static final String MENSAJE_VENTA_NO_ENCONTRADA = "venta no encontrada";
    private static final String MENSAJE_ID_INVALIDO = "El parámetro idVenta debe ser un entero positivo";
    private static final String MENSAJE_FECHA_INVALIDA = "El parámetro fecha debe tener formato yyyy-MM-dd";
    private static final String MENSAJE_OPERACION_INVALIDA = "La operación es obligatoria";

    private final VentaDAO ventaDAO;

    /**
     * Crea el proveedor que utiliza el DAO de ventas recibido.
     *
     * @param ventaDAO DAO utilizado para recuperar las ventas
     */
    public ServicioVentas(VentaDAO ventaDAO) {
        this.ventaDAO = Objects.requireNonNull(ventaDAO, "El VentaDAO es obligatorio");
    }

    /**
     * Ejecuta una operación del servicio de ventas.
     *
     * @param solicitud solicitud con la operación y sus parámetros
     * @return respuesta con el resultado o el error correspondiente
     */
    @Override
    public Respuesta ejecutar(Solicitud solicitud) {
        if (solicitud == null) {
            return Respuesta.error(UUID.randomUUID(), EstadoRespuesta.PARAMETRO_INVALIDO,
                    "La solicitud es obligatoria");
        }

        try {
            String operacion = solicitud.getOperacion();
            if (operacion == null || operacion.isBlank()) {
                return Respuesta.error(solicitud.getId(), EstadoRespuesta.PARAMETRO_INVALIDO,
                        MENSAJE_OPERACION_INVALIDA);
            }

            return switch (operacion) {
                case OPERACION_CONSULTAR -> consultar(solicitud);
                case OPERACION_TOTAL_DEL_DIA -> totalDelDia(solicitud);
                default -> Respuesta.error(solicitud.getId(), EstadoRespuesta.ERROR_PROVEEDOR,
                        "Operación desconocida: " + operacion);
            };
        } catch (IllegalArgumentException e) {
            return Respuesta.error(solicitud.getId(), EstadoRespuesta.PARAMETRO_INVALIDO, e.getMessage());
        } catch (RuntimeException e) {
            String mensaje = e.getMessage() == null || e.getMessage().isBlank()
                    ? e.getClass().getSimpleName()
                    : e.getMessage();
            return Respuesta.error(solicitud.getId(), EstadoRespuesta.ERROR_PROVEEDOR, mensaje);
        }
    }

    private Respuesta consultar(Solicitud solicitud) {
        int idVenta = obtenerIdVenta(solicitud);
        List<VentaModel> ventas = ventaDAO.listarTodos();

        return ventas.stream()
                .filter(venta -> venta.getId() == idVenta)
                .findFirst()
                .<Respuesta>map(venta -> Respuesta.exito(solicitud, venta))
                .orElseGet(() -> Respuesta.error(solicitud.getId(), EstadoRespuesta.ERROR_PROVEEDOR,
                        MENSAJE_VENTA_NO_ENCONTRADA));
    }

    private Respuesta totalDelDia(Solicitud solicitud) {
        LocalDate fechaSolicitada = obtenerFecha(solicitud);
        double total = ventaDAO.listarTodos().stream()
                .filter(venta -> venta.getFecha() != null)
                .filter(venta -> convertirFecha(venta.getFecha()).equals(fechaSolicitada))
                .mapToDouble(VentaModel::getTotal)
                .sum();

        return Respuesta.exito(solicitud, total);
    }

    private static int obtenerIdVenta(Solicitud solicitud) {
        String valor = solicitud.getParametros().get(PARAMETRO_ID_VENTA);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta el parámetro obligatorio: " + PARAMETRO_ID_VENTA);
        }

        try {
            int idVenta = Integer.parseInt(valor.trim());
            if (idVenta <= 0) {
                throw new IllegalArgumentException(MENSAJE_ID_INVALIDO);
            }
            return idVenta;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(MENSAJE_ID_INVALIDO, e);
        }
    }

    private static LocalDate obtenerFecha(Solicitud solicitud) {
        String valor = solicitud.getParametros().get(PARAMETRO_FECHA);
        if (valor == null || valor.isBlank()) {
            return LocalDate.now();
        }

        try {
            return LocalDate.parse(valor.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(MENSAJE_FECHA_INVALIDA, e);
        }
    }

    private static LocalDate convertirFecha(Date fecha) {
        return fecha.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
