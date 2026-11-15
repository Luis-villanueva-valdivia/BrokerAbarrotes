package broker.abarrotes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO {

    private final Connection conn;

    public ReporteDAO() {
        this.conn = ConexionDB.getInstancia().getConexion();
    }

    public List<ReporteModel> reporteVentas(String tipo) {
        List<ReporteModel> lista = new ArrayList<>();
        if (conn == null) return lista;

        String groupBy = "date(fecha)";
        String label = "dia";
        switch (tipo.toLowerCase()) {
            case "semanal" -> {
                groupBy = "strftime('%Y-%W', fecha)";
                label = "semana";
            }
            case "mensual" -> {
                groupBy = "strftime('%Y-%m', fecha)";
                label = "mes";
            }
            case "anual" -> {
                groupBy = "strftime('%Y', fecha)";
                label = "año";
            }
            default -> {
                groupBy = "date(fecha)";
                label = "dia";
            }
        }

        String sql = "SELECT " + groupBy + " as periodo, COUNT(*) as cantidad, SUM(total) as total " +
                "FROM venta GROUP BY " + groupBy + " ORDER BY periodo DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ReporteModel r = new ReporteModel();
                r.setPeriodo(rs.getString("periodo"));
                r.setCantidadVentas(rs.getInt("cantidad"));
                r.setTotalVentas(rs.getDouble("total"));
                r.setCategoria(label);
                lista.add(r);
            }
        } catch (SQLException e) {
            System.out.println("Error en ReporteDAO.reporteVentas: " + e.getMessage());
        }
        return lista;
    }

    public ReporteModel productoVendidoPorId(int productoId) {
        if (conn == null) return null;
        String sql = "SELECT p.id, p.nombre, p.categoria, SUM(vi.cantidad) as totalVendido, SUM(vi.cantidad * vi.precio_unitario) as totalVenta " +
                "FROM venta_item vi JOIN producto p ON p.id = vi.producto_id WHERE p.id = ? GROUP BY p.id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productoId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ReporteModel r = new ReporteModel();
                    r.setIdReporte(rs.getInt("id"));
                    r.setPeriodo(rs.getString("nombre"));
                    r.setCategoria(rs.getString("categoria"));
                    r.setCantidadVentas(rs.getInt("totalVendido"));
                    r.setTotalVentas(rs.getDouble("totalVenta"));
                    return r;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error en ReporteDAO.productoVendidoPorId: " + e.getMessage());
        }
        return null;
    }

    public List<ReporteModel> productosVendidosPorCategoria(String categoriaFilter) {
        List<ReporteModel> lista = new ArrayList<>();
        if (conn == null) return lista;
        String sql = "SELECT p.id, p.nombre, SUM(vi.cantidad) as totalVendido, SUM(vi.cantidad * vi.precio_unitario) as totalVenta, p.categoria " +
                "FROM venta_item vi JOIN producto p ON p.id = vi.producto_id " +
                "WHERE p.categoria LIKE ? GROUP BY p.id ORDER BY totalVendido DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + categoriaFilter + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReporteModel r = new ReporteModel();
                    r.setIdReporte(rs.getInt("id"));
                    r.setPeriodo(rs.getString("nombre"));
                    r.setCategoria(rs.getString("categoria"));
                    r.setCantidadVentas(rs.getInt("totalVendido"));
                    r.setTotalVentas(rs.getDouble("totalVenta"));
                    lista.add(r);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error en ReporteDAO.productosVendidosPorCategoria: " + e.getMessage());
        }
        return lista;
    }

    public List<ReporteModel> reporteInventario() {
        List<ReporteModel> lista = new ArrayList<>();
        if (conn == null) return lista;
        String sql = "SELECT id, nombre, cantidad, categoria FROM producto ORDER BY nombre";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ReporteModel r = new ReporteModel();
                r.setIdReporte(rs.getInt("id"));
                r.setPeriodo(rs.getString("nombre"));
                r.setCategoria(rs.getString("categoria"));
                r.setCantidadVentas(rs.getInt("cantidad"));
                lista.add(r);
            }
        } catch (SQLException e) {
            System.out.println("Error en ReporteDAO.reporteInventario: " + e.getMessage());
        }
        return lista;
    }
}