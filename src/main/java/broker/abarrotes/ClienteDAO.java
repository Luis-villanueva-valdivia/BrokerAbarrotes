package broker.abarrotes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO implements IClienteDAO {

    private final Connection conn;

    public ClienteDAO() {
        conn = ConexionDB.getInstancia().getConexion();
    }

    @Override
    public void registrar(ClienteModel c) {
        if (conn == null) {
            System.out.println("No hay conexión a la base de datos. No se puede registrar cliente.");
            return;
        }
        String sql = "INSERT INTO cliente (nombre, direccion, telefono, email) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDireccion());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getEmail());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) c.setId(keys.getInt(1));
            }
        } catch (SQLException e) {
            System.out.println("Error al registrar cliente: " + e.getMessage());
        }
    }

    @Override
    public ClienteModel buscar(int id) {
        if (conn == null) {
            System.out.println("No hay conexión a la base de datos. Búsqueda de cliente vacía.");
            return null;
        }
        String sql = "SELECT id, nombre, direccion, telefono, email FROM cliente WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ClienteModel c = new ClienteModel();
                    c.setId(rs.getInt("id"));
                    c.setNombre(rs.getString("nombre"));
                    c.setDireccion(rs.getString("direccion"));
                    c.setTelefono(rs.getString("telefono"));
                    c.setEmail(rs.getString("email"));
                    return c;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar cliente: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void actualizar(ClienteModel c) {
        if (conn == null) {
            System.out.println("No hay conexión a la base de datos. No se puede actualizar cliente.");
            return;
        }
        String sql = "UPDATE cliente SET nombre = ?, direccion = ?, telefono = ?, email = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getDireccion());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getEmail());
            ps.setInt(5, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al actualizar cliente: " + e.getMessage());
        }
    }

    @Override
    public void eliminar(int id) {
        if (conn == null) {
            System.out.println("No hay conexión a la base de datos. No se puede eliminar cliente.");
            return;
        }
        String sql = "DELETE FROM cliente WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error al eliminar cliente: " + e.getMessage());
        }
    }

    @Override
    public List<ClienteModel> listarFrecuentes() {
        // Clientes frecuentes = los que más compras tienen
        String sql = "SELECT c.id, c.nombre, c.direccion, c.telefono, c.email " +
                "FROM cliente c " +
                "JOIN venta v ON c.id = v.cliente_id " +
                "GROUP BY c.id " +
                "ORDER BY COUNT(v.id) DESC " +
                "LIMIT 10";
        List<ClienteModel> lista = new ArrayList<>();
        if (conn == null) {
            System.out.println("No hay conexión a la base de datos. Lista de clientes vacía.");
            return lista;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ClienteModel c = new ClienteModel();
                c.setId(rs.getInt("id"));
                c.setNombre(rs.getString("nombre"));
                c.setDireccion(rs.getString("direccion"));
                c.setTelefono(rs.getString("telefono"));
                c.setEmail(rs.getString("email"));
                lista.add(c);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar clientes frecuentes: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public List<ClienteModel> listarTodos() {
        List<ClienteModel> lista = new ArrayList<>();
        if (conn == null) {
            System.out.println("No hay conexión a la base de datos. Lista de clientes vacía.");
            return lista;
        }
        String sql = "SELECT id, nombre, direccion, telefono, email FROM cliente";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ClienteModel c = new ClienteModel();
                c.setId(rs.getInt("id"));
                c.setNombre(rs.getString("nombre"));
                c.setDireccion(rs.getString("direccion"));
                c.setTelefono(rs.getString("telefono"));
                c.setEmail(rs.getString("email"));
                lista.add(c);
            }
        } catch (SQLException e) {
            System.out.println("Error al listar clientes: " + e.getMessage());
        }
        return lista;
    }

    @Override
    public ClienteModel buscarPorTelefono(String telefono) {
        if (conn == null) {
            System.out.println("No hay conexión a la base de datos. Búsqueda de cliente vacía.");
            return null;
        }
        String sql = "SELECT id, nombre, direccion, telefono, email FROM cliente WHERE telefono = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, telefono);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ClienteModel c = new ClienteModel();
                    c.setId(rs.getInt("id"));
                    c.setNombre(rs.getString("nombre"));
                    c.setDireccion(rs.getString("direccion"));
                    c.setTelefono(rs.getString("telefono"));
                    c.setEmail(rs.getString("email"));
                    return c;
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al buscar cliente por teléfono: " + e.getMessage());
        }
        return null;
    }

    @Override
    public int contarVentasMes(int clienteId) {
        if (conn == null) {
            return 0;
        }
        String sql = "SELECT COUNT(*) FROM venta WHERE cliente_id = ? AND strftime('%Y-%m', fecha) = strftime('%Y-%m', 'now')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al contar ventas del mes: " + e.getMessage());
        }
        return 0;
    }
}