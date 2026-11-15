package broker.broker;

import broker.comun.ProveedorServicio;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegistroServiciosTest {

    private RegistroServicios registro;

    @BeforeEach
    void setUp() {
        registro = new RegistroServicios();
    }

    @Test
    void registraYBuscaUnProveedor() {
        ProveedorServicio proveedor = new ProveedorFalso("ok");

        registro.registrar("productos", proveedor);

        assertSame(proveedor, registro.buscar("productos"));
        assertTrue(registro.contiene("productos"));
    }

    @Test
    void buscarServicioDesconocidoLanzaExcepcion() {
        ServicioDesconocidoException e = assertThrows(ServicioDesconocidoException.class,
                () -> registro.buscar("inventario"));

        assertEquals("inventario", e.getNombreServicio());
    }

    @Test
    void registrarDosVecesElMismoServicioFalla() {
        registro.registrar("productos", new ProveedorFalso("a"));

        assertThrows(IllegalStateException.class,
                () -> registro.registrar("productos", new ProveedorFalso("b")));
    }

    @Test
    void sustituirCambiaElProveedorYDevuelveElAnterior() {
        ProveedorServicio original = new ProveedorFalso("sqlite");
        ProveedorServicio sustituto = new ProveedorFalso("memoria");
        registro.registrar("productos", original);

        ProveedorServicio anterior = registro.sustituir("productos", sustituto);

        assertSame(original, anterior);
        assertSame(sustituto, registro.buscar("productos"));
    }

    @Test
    void sustituirServicioNoRegistradoLanzaExcepcion() {
        assertThrows(ServicioDesconocidoException.class,
                () -> registro.sustituir("ventas", new ProveedorFalso("x")));
    }

    @Test
    void eliminarQuitaElServicio() {
        registro.registrar("clientes", new ProveedorFalso("x"));

        registro.eliminar("clientes");

        assertFalse(registro.contiene("clientes"));
        assertThrows(ServicioDesconocidoException.class, () -> registro.buscar("clientes"));
    }

    @Test
    void eliminarServicioNoRegistradoLanzaExcepcion() {
        assertThrows(ServicioDesconocidoException.class, () -> registro.eliminar("clientes"));
    }

    @Test
    void listarDevuelveLosNombresEnOrden() {
        registro.registrar("ventas", new ProveedorFalso("v"));
        registro.registrar("clientes", new ProveedorFalso("c"));
        registro.registrar("productos", new ProveedorFalso("p"));

        assertEquals(List.of("clientes", "productos", "ventas"), List.copyOf(registro.listar()));
    }

    @Test
    void listarDevuelveUnaCopia() {
        registro.registrar("ventas", new ProveedorFalso("v"));

        Set<String> nombres = registro.listar();
        nombres.clear();

        assertTrue(registro.contiene("ventas"));
    }

    @Test
    void ignoraEspaciosAlrededorDelNombre() {
        ProveedorServicio proveedor = new ProveedorFalso("ok");
        registro.registrar("  productos ", proveedor);

        assertSame(proveedor, registro.buscar("productos"));
    }

    @Test
    void rechazaNombresYProveedoresInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> registro.registrar("  ", new ProveedorFalso("x")));
        assertThrows(NullPointerException.class, () -> registro.registrar(null, new ProveedorFalso("x")));
        assertThrows(NullPointerException.class, () -> registro.registrar("productos", null));
    }

    @Test
    void soportaRegistrosDesdeVariosHilos() throws InterruptedException {
        int hilos = 8;
        int serviciosPorHilo = 50;
        ExecutorService ejecutor = Executors.newFixedThreadPool(hilos);
        CountDownLatch salida = new CountDownLatch(1);

        for (int h = 0; h < hilos; h++) {
            int hilo = h;
            ejecutor.submit(() -> {
                salida.await();
                for (int i = 0; i < serviciosPorHilo; i++) {
                    registro.registrar("servicio-" + hilo + "-" + i, new ProveedorFalso(i));
                }
                return null;
            });
        }
        salida.countDown();
        ejecutor.shutdown();

        assertTrue(ejecutor.awaitTermination(5, TimeUnit.SECONDS));
        assertEquals(hilos * serviciosPorHilo, registro.listar().size());
    }
}