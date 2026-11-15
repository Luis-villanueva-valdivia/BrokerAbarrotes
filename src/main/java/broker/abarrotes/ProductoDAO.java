package broker.abarrotes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO implements IProductoDAO {

    private final Connection conn;

    public ProductoDAO() {
        conn = ConexionDB.getInstancia().getConexion();
    }

    @Override
    public void agregar(ProductoModel p) {
        if (conn == null) {
            System.out.println("No hay conexión a la base de datos. No se puede agregar producto.");
            return;
        }
        String sql = "INSERT INTO producto (nombre, precio, cantidad, vencimiento, descripcion, categoria, proveedor, codigo) VALUES (?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setInt(3, p.getCantidad());
            ps.setString(4, p.getVencimiento());
            ps.setString(5, p.getDescripcion());
            ps.setString(6, p.getCategoria());
            ps.setString(7, p.getProveedor());
            ps.setString(8, p.getCodigo());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al agregar producto: " + e.getMessage());
        }
    }

    @Override
    public void actualizar(ProductoModel p) {
        if (conn == null) {
            System.out.println("No hay conexión a la base de datos. No se puede actualizar producto.");
            return;
        }
        String sql = "UPDATE producto SET nombre=?, precio=?, cantidad=?, vencimiento=?, descripcion=?, categoria=?, proveedor=?, codigo=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getPrecio());
            ps.setInt(3, p.getCantidad());
            ps.setString(4, p.getVencimiento());
            ps.setString(5, p.getDescripcion());
            ps.setString(6, p.getCategoria());
            ps.setString(7, p.getProveedor());
            ps.setString(8, p.getCodigo());
            ps.setInt(9, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al actualizar producto: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(int id) {
        if (conn == null) {
            System.out.println("No hay conexión a la base de datos. No se puede eliminar producto.");
            return;
        }
        String sql = "DELETE FROM producto WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al eliminar producto: " + e.getMessage());
        }
    }

    @Override
    public List<ProductoModel> listarTodos() {
        List<ProductoModel> lista = new ArrayList<>();
        if (conn == null) {
            System.out.println("No hay conexión a la base de datos. Lista de productos vacía.");
            return lista;
        }
        String sql = "SELECT id, nombre, precio, cantidad, vencimiento, descripcion, categoria, proveedor, codigo FROM producto";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error al listar productos: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public BuscarProductoService getBuscador() {
        return new BuscarProductoService(conn);
    }

    private ProductoModel mapRow(ResultSet rs) throws SQLException {
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
        return p;
    }
}