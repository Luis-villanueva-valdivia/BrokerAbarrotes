package broker.abarrotes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BuscarProductoService {

    private final Connection conn;

    public BuscarProductoService(Connection conn) {
        this.conn = conn;
    }

    public List<ProductoModel> buscarPorCodigo(String codigo) {
        String sql = "SELECT id, nombre, precio, cantidad, vencimiento, descripcion, categoria, proveedor, codigo FROM producto WHERE codigo = ?";
        return buscar(sql, codigo);
    }

    public List<ProductoModel> buscarPorNombre(String nombre) {
        String sql = "SELECT id, nombre, precio, cantidad, vencimiento, descripcion, categoria, proveedor, codigo FROM producto WHERE nombre LIKE ?";
        return buscar(sql, "%" + nombre + "%");
    }

    public List<ProductoModel> buscarPorCategoria(String categoria) {
        String sql = "SELECT id, nombre, precio, cantidad, vencimiento, descripcion, categoria, proveedor, codigo FROM producto WHERE categoria LIKE ?";
        return buscar(sql, "%" + categoria + "%");
    }

    public List<ProductoModel> buscarPorId(int id) {
        String sql = "SELECT id, nombre, precio, cantidad, vencimiento, descripcion, categoria, proveedor, codigo FROM producto WHERE id = ?";
        List<ProductoModel> lista = new ArrayList<>();
        if (conn == null) {
            System.out.println("No hay conexión a la base de datos. Búsqueda de productos vacía.");
            return lista;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductoModel p = new ProductoModel();
                    p.setId(rs.getInt("id"));
                    p.setNombre(rs.getString("nombre"));
                    p.setPrecio(rs.getDouble("precio"));
                    p.setCantidad(rs.getInt("cantidad"));
                    p.setVencimiento(rs.getString("vencimiento"));
                    p.setDescripcion(rs.getString("descripcion"));
                    p.setCategoria(rs.getString("categoria"));
                    p.setProveedor(rs.getString("proveedor"));
                    p.setCodigo(rs.getString("codigo"));
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error en búsqueda de producto por id: " + e.getMessage());
        }
        return lista;
    }

    private List<ProductoModel> buscar(String sql, String param) {
        List<ProductoModel> lista = new ArrayList<>();
        if (conn == null) {
            System.out.println("No hay conexión a la base de datos. Búsqueda de productos vacía.");
            return lista;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductoModel p = new ProductoModel();
                    p.setId(rs.getInt("id"));
                    p.setNombre(rs.getString("nombre"));
                    p.setPrecio(rs.getDouble("precio"));
                    p.setCantidad(rs.getInt("cantidad"));
                    p.setVencimiento(rs.getString("vencimiento"));
                    p.setDescripcion(rs.getString("descripcion"));
                    p.setCategoria(rs.getString("categoria"));
                    p.setProveedor(rs.getString("proveedor"));
                    p.setCodigo(rs.getString("codigo"));
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error en búsqueda de productos: " + e.getMessage());
        }
        return lista;
    }
}