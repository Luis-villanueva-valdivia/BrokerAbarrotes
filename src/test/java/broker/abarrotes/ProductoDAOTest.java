package broker.abarrotes;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductoDAOTest {

    private static final int PRODUCTOS_EN_LA_BASE = 4;

    @Test
    void listaLosProductosDeLaBaseDeDatos() {
        ProductoDAO productoDAO = new ProductoDAO();

        List<ProductoModel> productos = productoDAO.listarTodos();

        assertEquals(PRODUCTOS_EN_LA_BASE, productos.size());
    }
}
