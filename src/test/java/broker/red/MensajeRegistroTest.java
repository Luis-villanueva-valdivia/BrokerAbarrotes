package broker.red;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pruebas de construcción directa de MensajeRegistro con datos inválidos.
 */
class MensajeRegistroTest {

    @Test
    void mensajeValidoSeCreaSinError() {
        assertDoesNotThrow(() -> new MensajeRegistro("REGISTRAR", "productos", "localhost", 6001));
    }

    @Test
    void tipoDistintoDeRegistrarDaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new MensajeRegistro("BORRAR", "productos", "localhost", 6001));
    }

    @Test
    void tipoNuloDaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new MensajeRegistro(null, "productos", "localhost", 6001));
    }

    @Test
    void servicioNuloDaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new MensajeRegistro("REGISTRAR", null, "localhost", 6001));
    }

    @Test
    void servicioVacioDaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new MensajeRegistro("REGISTRAR", "   ", "localhost", 6001));
    }

    @Test
    void hostNuloDaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new MensajeRegistro("REGISTRAR", "productos", null, 6001));
    }

    @Test
    void hostVacioDaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new MensajeRegistro("REGISTRAR", "productos", "", 6001));
    }

    @Test
    void puertoCeroDaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new MensajeRegistro("REGISTRAR", "productos", "localhost", 0));
    }

    @Test
    void puertoNegativoDaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new MensajeRegistro("REGISTRAR", "productos", "localhost", -1));
    }

    @Test
    void puertoMayorQueMaximoDaIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> new MensajeRegistro("REGISTRAR", "productos", "localhost", 65536));
    }
}
