package modelo;

public class Producto {
    private long codigo;
    private String nombre;
    private double precio;
    private int stock;
    private boolean esCigarrillo; // NUEVO CAMPO

    public Producto(long codigo, String nombre, double precio, int stock, boolean esCigarrillo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.esCigarrillo = esCigarrillo;
    }
    
    // Constructor viejo para compatibilidad (asume falso)
    public Producto(long codigo, String nombre, double precio, int stock) {
        this(codigo, nombre, precio, stock, false);
    }

    public long getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public int getStock() { return stock; }
    public boolean isEsCigarrillo() { return esCigarrillo; } // Getter nuevo

    @Override
    public String toString() { return nombre; }
}