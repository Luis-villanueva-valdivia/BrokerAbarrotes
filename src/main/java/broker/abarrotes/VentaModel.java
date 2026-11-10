package broker.abarrotes;

import java.util.Date;

public class VentaModel {
    private int id;
    private Date fecha;
    private double total;
    private ProductoModel[] productos;
    private String cliente; // nombre del cliente
    private Integer clienteId; // id del cliente
    private double descuento;

    private String metodoPago;
    private String numeroTarjeta;
    private double montoPagado;
    private double cambio;

    // Getters
    public int getId() { return id; }
    public Date getFecha() { return fecha; }
    public double getTotal() { return total; }
    public ProductoModel[] getProductos() { return productos != null ? productos : new ProductoModel[0]; }
    public String getCliente() { return cliente; }
    public Integer getClienteId() { return clienteId; }
    public double getDescuento() { return descuento; }
    public String getMetodoPago() { return metodoPago; }
    public String getNumeroTarjeta() { return numeroTarjeta; }
    public double getMontoPagado() { return montoPagado; }
    public double getCambio() { return cambio; }

    // Setters
    public void setId(int id) { this.id = id; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    public void setTotal(double total) { this.total = total; }
    public void setProductos(ProductoModel[] productos) { this.productos = productos; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public void setClienteId(Integer clienteId) { this.clienteId = clienteId; }
    public void setDescuento(double descuento) { this.descuento = descuento; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public void setNumeroTarjeta(String numeroTarjeta) { this.numeroTarjeta = numeroTarjeta; }
    public void setMontoPagado(double montoPagado) { this.montoPagado = montoPagado; }
    public void setCambio(double cambio) { this.cambio = cambio; }
}