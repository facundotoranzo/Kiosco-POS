package modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para obtener estadísticas y métricas del negocio
 * Proporciona datos para el dashboard y reportes
 */
public class EstadisticasDAO {
    
    /**
     * NUEVA FUNCIONALIDAD: Cuenta las ventas realizadas en una fecha específica
     */
    public int contarVentasDelDia(String fecha) {
        String sql = "SELECT COUNT(*) FROM ventas WHERE fecha = ?";
        
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, fecha);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            Logger.error("Error contando ventas del día: " + fecha, e);
        }
        
        return 0;
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Obtiene el total de ventas de una fecha específica
     */
    public double totalVentasDelDia(String fecha) {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE fecha = ?";
        
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, fecha);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
            
        } catch (SQLException e) {
            Logger.error("Error obteniendo total de ventas del día: " + fecha, e);
        }
        
        return 0.0;
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Cuenta productos vendidos en una fecha específica
     */
    public int productosVendidosDelDia(String fecha) {
        String sql = "SELECT COALESCE(SUM(dv.cantidad), 0) " +
                    "FROM detalle_ventas dv " +
                    "INNER JOIN ventas v ON dv.id_venta = v.id " +
                    "WHERE v.fecha = ?";
        
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, fecha);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            Logger.error("Error contando productos vendidos del día: " + fecha, e);
        }
        
        return 0;
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Obtiene los productos más vendidos del día
     */
    public List<ProductoVendido> obtenerTopProductosDelDia(String fecha, int limite) {
        List<ProductoVendido> topProductos = new ArrayList<>();
        
        String sql = "SELECT dv.nombre_producto, SUM(dv.cantidad) as total_vendido " +
                    "FROM detalle_ventas dv " +
                    "INNER JOIN ventas v ON dv.id_venta = v.id " +
                    "WHERE v.fecha = ? " +
                    "GROUP BY dv.nombre_producto " +
                    "ORDER BY total_vendido DESC " +
                    "LIMIT ?";
        
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, fecha);
            ps.setInt(2, limite);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                String nombre = rs.getString("nombre_producto");
                int cantidadVendida = rs.getInt("total_vendido");
                topProductos.add(new ProductoVendido(nombre, cantidadVendida));
            }
            
        } catch (SQLException e) {
            Logger.error("Error obteniendo top productos del día: " + fecha, e);
        }
        
        return topProductos;
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Obtiene estadísticas de ventas por hora del día actual
     */
    public List<VentaPorHora> obtenerVentasPorHora(String fecha) {
        List<VentaPorHora> ventasPorHora = new ArrayList<>();
        
        String sql = "SELECT " +
                    "CASE " +
                    "  WHEN CAST(SUBSTR(hora, 1, 2) AS INTEGER) BETWEEN 6 AND 11 THEN 'Mañana' " +
                    "  WHEN CAST(SUBSTR(hora, 1, 2) AS INTEGER) BETWEEN 12 AND 17 THEN 'Tarde' " +
                    "  WHEN CAST(SUBSTR(hora, 1, 2) AS INTEGER) BETWEEN 18 AND 23 THEN 'Noche' " +
                    "  ELSE 'Madrugada' " +
                    "END as periodo, " +
                    "COUNT(*) as cantidad_ventas, " +
                    "SUM(total) as total_periodo " +
                    "FROM ventas " +
                    "WHERE fecha = ? " +
                    "GROUP BY periodo " +
                    "ORDER BY " +
                    "CASE periodo " +
                    "  WHEN 'Mañana' THEN 1 " +
                    "  WHEN 'Tarde' THEN 2 " +
                    "  WHEN 'Noche' THEN 3 " +
                    "  ELSE 4 " +
                    "END";
        
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, fecha);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                String periodo = rs.getString("periodo");
                int cantidadVentas = rs.getInt("cantidad_ventas");
                double totalPeriodo = rs.getDouble("total_periodo");
                ventasPorHora.add(new VentaPorHora(periodo, cantidadVentas, totalPeriodo));
            }
            
        } catch (SQLException e) {
            Logger.error("Error obteniendo ventas por hora: " + fecha, e);
        }
        
        return ventasPorHora;
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Obtiene estadísticas de la semana actual
     */
    public EstadisticasSemana obtenerEstadisticasSemana() {
        EstadisticasSemana stats = new EstadisticasSemana();
        
        // Obtener fechas de la semana (últimos 7 días)
        String sql;
        if (ConexionDB.tipoBase.equals("sqlite")) {
            // SQLite
            sql = "SELECT " +
                  "COUNT(*) as total_ventas, " +
                  "COALESCE(SUM(total), 0) as total_monto, " +
                  "COALESCE(AVG(total), 0) as promedio_venta " +
                  "FROM ventas " +
                  "WHERE fecha >= date('now', '-7 days')";
        } else {
            // MySQL
            sql = "SELECT " +
                  "COUNT(*) as total_ventas, " +
                  "COALESCE(SUM(total), 0) as total_monto, " +
                  "COALESCE(AVG(total), 0) as promedio_venta " +
                  "FROM ventas " +
                  "WHERE fecha >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
        }
        
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                stats.totalVentas = rs.getInt("total_ventas");
                stats.totalMonto = rs.getDouble("total_monto");
                stats.promedioVenta = rs.getDouble("promedio_venta");
            }
            
        } catch (SQLException e) {
            Logger.error("Error obteniendo estadísticas de la semana", e);
        }
        
        return stats;
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Obtiene el producto más vendido del mes
     */
    public ProductoVendido obtenerProductoMasVendidoDelMes() {
        String sql;
        if (ConexionDB.tipoBase.equals("sqlite")) {
            // SQLite
            sql = "SELECT dv.nombre_producto, SUM(dv.cantidad) as total_vendido " +
                  "FROM detalle_ventas dv " +
                  "INNER JOIN ventas v ON dv.id_venta = v.id " +
                  "WHERE v.fecha >= date('now', 'start of month') " +
                  "GROUP BY dv.nombre_producto " +
                  "ORDER BY total_vendido DESC " +
                  "LIMIT 1";
        } else {
            // MySQL
            sql = "SELECT dv.nombre_producto, SUM(dv.cantidad) as total_vendido " +
                  "FROM detalle_ventas dv " +
                  "INNER JOIN ventas v ON dv.id_venta = v.id " +
                  "WHERE v.fecha >= DATE_FORMAT(CURDATE(), '%Y-%m-01') " +
                  "GROUP BY dv.nombre_producto " +
                  "ORDER BY total_vendido DESC " +
                  "LIMIT 1";
        }
        
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String nombre = rs.getString("nombre_producto");
                int cantidadVendida = rs.getInt("total_vendido");
                return new ProductoVendido(nombre, cantidadVendida);
            }
            
        } catch (SQLException e) {
            Logger.error("Error obteniendo producto más vendido del mes", e);
        }
        
        return new ProductoVendido("Sin datos", 0);
    }
    
    /**
     * COMPATIBILIDAD: Método para ControladorEstadisticas existente
     */
    public double obtenerTotalVentasDia(java.time.LocalDate fecha) {
        return totalVentasDelDia(fecha.toString());
    }
    
    /**
     * COMPATIBILIDAD: Método para ControladorEstadisticas existente
     */
    public int obtenerTotalProductosVendidos() {
        String fechaHoy = java.time.LocalDate.now().toString();
        return productosVendidosDelDia(fechaHoy);
    }
    
    /**
     * COMPATIBILIDAD: Método para ControladorEstadisticas existente
     */
    public List<String[]> obtenerTopProductos() {
        String fechaHoy = java.time.LocalDate.now().toString();
        List<ProductoVendido> top = obtenerTopProductosDelDia(fechaHoy, 10);
        
        List<String[]> resultado = new ArrayList<>();
        for (ProductoVendido pv : top) {
            resultado.add(new String[]{pv.getNombre(), String.valueOf(pv.getCantidadVendida())});
        }
        
        return resultado;
    }
    
    /**
     * COMPATIBILIDAD: Método para ControladorEstadisticas existente
     */
    public List<String[]> obtenerVentasSemana() {
        List<String[]> resultado = new ArrayList<>();
        
        // Obtener ventas de los últimos 7 días
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate fecha = java.time.LocalDate.now().minusDays(i);
            String fechaStr = fecha.toString();
            double total = totalVentasDelDia(fechaStr);
            
            resultado.add(new String[]{
                fecha.getDayOfWeek().toString().substring(0, 3), // Día de la semana
                String.format("%.2f", total)
            });
        }
        
        return resultado;
    }
    
    // Clases auxiliares para encapsular datos
    
    /**
     * Representa un producto vendido con su cantidad
     */
    public static class ProductoVendido {
        private String nombre;
        private int cantidadVendida;
        
        public ProductoVendido(String nombre, int cantidadVendida) {
            this.nombre = nombre;
            this.cantidadVendida = cantidadVendida;
        }
        
        public String getNombre() { return nombre; }
        public int getCantidadVendida() { return cantidadVendida; }
    }
    
    /**
     * Representa ventas agrupadas por período del día
     */
    public static class VentaPorHora {
        private String periodo;
        private int cantidadVentas;
        private double totalPeriodo;
        
        public VentaPorHora(String periodo, int cantidadVentas, double totalPeriodo) {
            this.periodo = periodo;
            this.cantidadVentas = cantidadVentas;
            this.totalPeriodo = totalPeriodo;
        }
        
        public String getPeriodo() { return periodo; }
        public int getCantidadVentas() { return cantidadVentas; }
        public double getTotalPeriodo() { return totalPeriodo; }
    }
    
    /**
     * Representa estadísticas de una semana
     */
    public static class EstadisticasSemana {
        public int totalVentas;
        public double totalMonto;
        public double promedioVenta;
    }
}