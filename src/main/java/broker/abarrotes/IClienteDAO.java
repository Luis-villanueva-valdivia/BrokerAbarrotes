package broker.abarrotes;

import java.util.List;

public interface IClienteDAO {
    void registrar(ClienteModel c);
    ClienteModel buscar(int id);
    ClienteModel buscarPorTelefono(String telefono);
    void actualizar(ClienteModel c);
    List<ClienteModel> listarFrecuentes();
    List<ClienteModel> listarTodos();
    int contarVentasMes(int clienteId);
    void eliminar(int id);
}