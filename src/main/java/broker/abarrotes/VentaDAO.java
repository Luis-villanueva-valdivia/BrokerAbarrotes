package broker.abarrotes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VentaDAO implements IVentaDAO {

    private final Connection conn;
    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public VentaDAO() {
        conn = ConexionDB.getInstancia().getConexion();
    }

    @Override
    public void registrar(VentaModel v) {
        registrarVenta(v);
    }

    @Override
    public void historial() {
        List<VentaModel> lista = listarTodos();
        if (lista.isEmpty()) {
            System.out.println("No hay ventas registradas.");
            return;
        }
        for (VentaModel v : lista) {
            System.out.printf("Venta ID:%d Fecha:%s Total:%.2f Cliente:%s%n", v.getId(), v.getFecha(), v.getTotal(), v.getCliente());
        }
    }

    @Override
    public ReporteModel generarReporte(String periodo) {
        return null; // delegar al ReporteDAO si se requiere
    }

    @Override
    public void historialPorCliente(int clienteId) {
        String sql = "SELECT v.id, v.fecha, v.total, c.nombre FROM venta v LEFT JOIN cliente c ON v.cliente_id = c.id WHERE v.cliente_id = ? ORDER BY v.fecha DESC";
        if (conn == null) {
            System.out.println("No hay conexión a la base de datos.");
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("ID:%d Fecha:%s Total:%.2f Cliente:%s%n",
                            rs.getInt(1), rs.getString(2), rs.getDouble(3), rs.getString(4));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al listar historial por cliente: " + e.getMessage());
        }
    }

    @Override
    public void registrarVenta(VentaModel v) {
        if (conn == null) {
            System.out.println("No hay conexión a la base de datos. Venta no registrada.");
            return;
        }

        // Recalcular total desde los items para evitar inconsistencias
        double totalCalc = 0.0;
        for (ProductoModel p : v.getProductos()) {
            totalCalc += p.getPrecio() * p.getCantidad();
        }
        v.setTotal(totalCalc);

        String insVenta = "INSERT INTO venta (fecha, total, cliente_id, metodo_pago, numero_tarjeta) VALUES (?,?,?,?,?)";
        String insItem = "INSERT INTO venta_item (venta_id, producto_id, cantidad, precio_unitario, descuento) VALUES (?,?,?,?,?)";

        try {
            conn.setAutoCommit(false);
            // Fecha: si no viene, usar ahora
            String fechaStr = null;
            if (v.getFecha() != null) {
                fechaStr = fmt.format(v.getFecha());
            } else {
                fechaStr = fmt.format(new Date());
            }

            try (PreparedStatement ps = conn.prepareStatement(insVenta, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, fechaStr);
                ps.setDouble(2, v.getTotal());
                ps.setObject(3, v.getClienteId());
                ps.setString(4, v.getMetodoPago());
                ps.setString(5, v.getNumeroTarjeta());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int ventaId = keys.getInt(1);
                        v.setId(ventaId);
                        // insertar items
                        for (ProductoModel p : v.getProductos()) {
                            try (PreparedStatement psi = conn.prepareStatement(insItem)) {
                                psi.setInt(1, ventaId);
                                psi.setInt(2, p.getId());
                                psi.setInt(3, p.getCantidad());
                                psi.setDouble(4, p.getPrecio());
                                psi.setDouble(5, 0.0);
                                psi.executeUpdate();
                            }
                        }
                    }
                }
            }
            conn.commit();
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            System.out.println("Error al registrar venta en BD: " + e.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException ex) {}
        }
    }

    // Método de apoyo para la vista que lista las ventas desde la BD
    public List<VentaModel> listarTodos() {
        List<VentaModel> lista = new ArrayList<>();
        if (conn == null) return lista;
        String sql = "SELECT v.id, v.fecha, v.total, c.nombre FROM venta v LEFT JOIN cliente c ON v.cliente_id = c.id ORDER BY v.id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VentaModel v = new VentaModel();
                    v.setId(rs.getInt(1));
                    String f = rs.getString(2);
                    try {
                        Date d = fmt.parse(f);
                        v.setFecha(d);
                    } catch (ParseException | NullPointerException e) {
                        v.setFecha(new Date());
                    }
                    v.setTotal(rs.getDouble(3));
                    String nombre = rs.getString(4);
                    v.setCliente(nombre != null ? nombre : "");
                    // No cargamos detalle de productos aquí
                    lista.add(v);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error al listar ventas: " + e.getMessage());
        }
        return lista;
    }
}