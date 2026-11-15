package broker.abarrotes;

import java.util.ArrayList;
import java.util.List;

public class ReporteModel {

    private int idReporte;
    private String categoria;
    private String periodo;
    private double totalVentas;
    private int cantidadVentas;
    private String fechaGeneracion;

    public ReporteModel() {
    }

    public ReporteModel(int idReporte, String categoria, String periodo,
                        double totalVentas, int cantidadVentas,
                        String fechaGeneracion) {
        this.idReporte = idReporte;
        this.categoria = categoria;
        this.periodo = periodo;
        this.totalVentas = totalVentas;
        this.cantidadVentas = cantidadVentas;
        this.fechaGeneracion = fechaGeneracion;
    }

    public int getIdReporte() {
        return idReporte;
    }

    public void setIdReporte(int idReporte) {
        this.idReporte = idReporte;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public double getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(double totalVentas) {
        this.totalVentas = totalVentas;
    }

    public int getCantidadVentas() {
        return cantidadVentas;
    }

    public void setCantidadVentas(int cantidadVentas) {
        this.cantidadVentas = cantidadVentas;
    }

    public String getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(String fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    @Override
    public String toString() {
        return "ReporteModel{" +
                "idReporte=" + idReporte +
                ", categoria='" + categoria + '\'' +
                ", periodo='" + periodo + '\'' +
                ", totalVentas=" + totalVentas +
                ", cantidadVentas=" + cantidadVentas +
                ", fechaGeneracion='" + fechaGeneracion + '\'' +
                '}';
    }

    // ------ Métodos de consulta para reportes ------

    public static List<ReporteModel> generarReporteVentas(String tipo) {
        ReporteDAO dao = new ReporteDAO();
        return dao.reporteVentas(tipo);
    }

    public static List<ReporteModel> reporteInventario() {
        List<ReporteModel> lista = new ArrayList<>();
        ProductoDAO pd = new ProductoDAO();
        List<ProductoModel> productos = pd.listarTodos();
        for (ProductoModel p : productos) {
            ReporteModel r = new ReporteModel();
            r.setIdReporte(p.getId());
            r.setCategoria(p.getNombre());
            r.setCantidadVentas(p.getCantidad());
            r.setPeriodo(p.getCategoria());
            lista.add(r);
        }
        return lista;
    }

    public static ReporteModel productoVendidoPorId(int productoId) {
        ReporteDAO vdao = new ReporteDAO();
        return vdao.productoVendidoPorId(productoId);
    }

    public static List<ReporteModel> productosVendidosPorCategoria(String categoriaFilter) {
        ReporteDAO vdao = new ReporteDAO();
        return vdao.productosVendidosPorCategoria(categoriaFilter);
    }
}