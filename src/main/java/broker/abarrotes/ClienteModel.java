package broker.abarrotes;

public class ClienteModel {
	private int id;
	private String nombre;
	private String email;
	private String telefono;
	private String direccion;

	// Getters
	public int getId() { return id; }
	public String getNombre() { return nombre; }
	public String getEmail() { return email; }
	public String getTelefono() { return telefono; }
	public String getDireccion() { return direccion; }

	// Setters
	public void setId(int id) { this.id = id; }
	public void setNombre(String nombre) { this.nombre = nombre; }
	public void setEmail(String email) { this.email = email; }
	public void setTelefono(String telefono) { this.telefono = telefono; }
	public void setDireccion(String direccion) { this.direccion = direccion; }
}