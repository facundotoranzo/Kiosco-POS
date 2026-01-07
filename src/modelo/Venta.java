package modelo;

import java.util.List;

public class Venta {
    private int id;
    private String fecha;
    private String hora;
    private double total;
    private String medioPago; // "Efectivo", "Tarjeta"
    private List<DetalleVenta> items;

    public Venta(double total, String medioPago, List<DetalleVenta> items) {
        this.total = total;
        this.medioPago = medioPago;
        this.items = items;
    }

    public double getTotal() { return total; }
    public String getMedioPago() { return medioPago; }
    public List<DetalleVenta> getItems() { return items; }
    
    // Clase interna para el detalle
    public static class DetalleVenta {
        public String nombreProducto;
        public double precioUnitario;
        public int cantidad;
        public double subtotal;

        public DetalleVenta(String nombre, double precio, int cant) {
            this.nombreProducto = nombre;
            this.precioUnitario = precio;
            this.cantidad = cant;
            this.subtotal = precio * cant;
        }
    }
}