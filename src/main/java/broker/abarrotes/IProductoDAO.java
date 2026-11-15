package broker.abarrotes;

import java.util.List;

public interface IProductoDAO {
    void agregar(ProductoModel p);
    void actualizar(ProductoModel p);
    void eliminar(int id);
    List<ProductoModel> listarTodos();
    BuscarProductoService getBuscador();
}
