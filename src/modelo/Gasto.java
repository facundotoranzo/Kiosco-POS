package modelo;

public class Gasto {
    private int id;
    private String proveedor;
    private String descripcion;
    private double monto;
    private double saldoRemanente; // <--- ESTO ES IMPORTANTE
    private String fecha;

    // Constructor completo
    public Gasto(int id, String proveedor, String descripcion, double monto, double saldoRemanente, String fecha) {
        this.id = id;
        this.proveedor = proveedor;
        this.descripcion = descripcion;
        this.monto = monto;
        this.saldoRemanente = saldoRemanente;
        this.fecha = fecha;
    }

    // Getters
    public int getId() { return id; }
    public String getProveedor() { return proveedor; }
    public String getDescripcion() { return descripcion; }
    public double getMonto() { return monto; }
    public double getSaldoRemanente() { return saldoRemanente; } // <--- ESTE GETTER ES VITAL
    public String getFecha() { return fecha; }
}