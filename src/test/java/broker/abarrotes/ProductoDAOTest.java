package broker.abarrotes;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProductoDAOTest {

    @Test
    void listaLosProductosDeLaBaseDeDatos() {
        ProductoDAO productoDAO = new ProductoDAO();

        List<ProductoModel> productos = productoDAO.listarTodos();

        assertNotNull(productos);
        productos.forEach(producto ->
                System.out.printf("%d - %s - %.2f - cantidad: %d%n",
                        producto.getId(),
                        producto.getNombre(),
                        producto.getPrecio(),
                        producto.getCantidad()));
    }
}
