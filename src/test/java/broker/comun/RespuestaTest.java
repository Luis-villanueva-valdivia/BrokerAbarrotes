package broker.comun;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class RespuestaTest {
    
    @Test
    void errorSiempreLlevaMensaje() {
        Respuesta respuesta = Respuesta.error(UUID.randomUUID(), "No existe el servicio");

        assertEquals("No existe el servicio", respuesta.getMensajeError());
    }

    @Test
    void errorSinMensajeLanzaExcepcion() {
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> Respuesta.error(UUID.randomUUID(), null));

        assertEquals("Una respuesta de error debe tener mensaje", excepcion.getMessage());
    }
}
