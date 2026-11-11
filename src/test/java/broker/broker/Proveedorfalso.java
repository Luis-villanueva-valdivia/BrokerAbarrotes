package broker.broker;

import broker.comun.ProveedorServicio;
import broker.comun.Respuesta;
import broker.comun.Solicitud;

/**
 * Proveedor de prueba que contesta un valor fijo y cuenta cuántas veces lo llamaron,
 * para probar el broker sin tocar la base de datos.
 */
class ProveedorFalso implements ProveedorServicio {

    private final Object resultado;
    private int llamadas;

    ProveedorFalso(Object resultado) {
        this.resultado = resultado;
    }

    @Override
    public Respuesta ejecutar(Solicitud solicitud) {
        llamadas++;
        return Respuesta.exito(solicitud, resultado);
    }

    int getLlamadas() {
        return llamadas;
    }
}