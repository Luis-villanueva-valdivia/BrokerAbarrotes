
package broker.abarrotes;

public class ProductoModel {
    private int id;
    private String nombre;
    private double precio;
    private int cantidad;
    private String vencimiento;
    private String descripcion;
    private String categoria;
    private String proveedor;
    private String codigo;

    //Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getCantidad() { return cantidad; }
    public String getVencimiento() { return vencimiento; }
    public String getDescripcion() { return descripcion; }
    public String getCategoria() { return categoria; }
    public String getProveedor() { return proveedor; }
    public String getCodigo() { return codigo; }

    //Setters
    public void setId(int id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPrecio(double precio) { this.precio = precio; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public void setVencimiento(String vencimiento) { this.vencimiento = vencimiento; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public void actualizarStock() {
        if (this.cantidad > 0) {
            this.cantidad--;
        }
    }
}