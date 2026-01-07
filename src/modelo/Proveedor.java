package modelo;

public class Proveedor {
    private int id;
    private String nombre;
    private String telefono;
    private String contacto;
    private String cuit;

    public Proveedor(int id, String nombre, String telefono, String contacto, String cuit) {
        this.id = id;
        this.nombre = nombre;
        this.telefono = telefono;
        this.contacto = contacto;
        this.cuit = cuit;
    }
    
    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public String getContacto() { return contacto; }
    public String getCuit() { return cuit; }
    
    // Para que el ComboBox muestre el nombre y no el objeto
    @Override
    public String toString() { return nombre; }
}