package broker.comun;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

public class SolicitudTest {
    
    @Test
    void creaSolicitudConIdYFechaAutomaticos() {
        Solicitud solicitud = new Solicitud(
                "productos",
                "buscar",
                Map.of("nombre", "arroz")
        );

        assertNotNull(solicitud.getId());
        assertNotNull(solicitud.getFechaHora());

        assertEquals("productos", solicitud.getServicio());
        assertEquals("buscar", solicitud.getOperacion());
        assertEquals("arroz", solicitud.getParametros().get("nombre"));
    }

    @Test 
    void creaSolicitudEnElMomentoActual() {
        LocalDateTime antes = LocalDateTime.now();

        Solicitud solicitud = new Solicitud(
                "clientes",
                "listar",
                Map.of()
        );

        LocalDateTime despues = LocalDateTime.now();

        assertNotNull(UUID.fromString(solicitud.getId().toString()));
        assertFalse(solicitud.getFechaHora().isBefore(antes));
        assertFalse(solicitud.getFechaHora().isAfter(despues));
    }
}
