package broker.abarrotes;

public interface IVentaDAO {
    void registrar(VentaModel v);
    void historial();
    ReporteModel generarReporte(String periodo);
    void historialPorCliente(int clienteId);

    void registrarVenta(VentaModel v);
}