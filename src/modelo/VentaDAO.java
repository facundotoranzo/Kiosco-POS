package modelo;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class VentaDAO {

    public boolean registrarVenta(int idCaja, Venta venta) {
        String sqlVenta = "INSERT INTO ventas(id_caja, fecha, hora, total, medio_pago) VALUES(?,?,?,?,?)";
        String sqlDetalle = "INSERT INTO detalle_ventas(id_venta, nombre_producto, precio_unitario, cantidad, subtotal) VALUES(?,?,?,?,?)";
        String sqlStock = "UPDATE productos SET stock = stock - ? WHERE nombre = ?";
        
        Connection conn = null;

        try {
            conn = ConexionDB.conectar();
            conn.setAutoCommit(false); // Inicio Transacción

            // 1. Cabecera de Venta
            PreparedStatement pstVenta = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            pstVenta.setInt(1, idCaja);
            pstVenta.setString(2, LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            pstVenta.setString(3, LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            pstVenta.setDouble(4, venta.getTotal());
            pstVenta.setString(5, venta.getMedioPago());
            pstVenta.executeUpdate();

            ResultSet rs = pstVenta.getGeneratedKeys();
            int idVenta = 0;
            if (rs.next()) idVenta = rs.getInt(1);

            // 2. Detalles de Venta
            PreparedStatement pstDetalle = conn.prepareStatement(sqlDetalle);
            PreparedStatement pstStock = conn.prepareStatement(sqlStock);

            for (Venta.DetalleVenta item : venta.getItems()) {
                // Insertar detalle
                pstDetalle.setInt(1, idVenta);
                pstDetalle.setString(2, item.nombreProducto);
                pstDetalle.setDouble(3, item.precioUnitario);
                pstDetalle.setInt(4, item.cantidad);
                pstDetalle.setDouble(5, item.subtotal);
                pstDetalle.executeUpdate();
                
                // Descontar stock DENTRO de la transacción (solo si NO es LITE)
                if (!ConexionDB.licencia.equals("LITE")) {
                    pstStock.setInt(1, item.cantidad);
                    pstStock.setString(2, item.nombreProducto);
                    int filasAfectadas = pstStock.executeUpdate();
                    
                    // Verificar que se pudo descontar stock
                    if (filasAfectadas == 0) {
                        throw new SQLException("No se pudo descontar stock para: " + item.nombreProducto);
                    }
                }
            }

            conn.commit(); 
            return true;

        } catch (SQLException e) {
            try { 
                if (conn != null) conn.rollback(); 
            } catch (SQLException ex) { 
                Logger.error("Error en rollback de transacción", ex);
            }
            Logger.error("Error SQL al registrar venta", e);
            return false;
        } catch (Exception e) {
            try { 
                if (conn != null) conn.rollback(); 
            } catch (SQLException ex) { 
                Logger.error("Error en rollback de transacción", ex);
            }
            Logger.error("Error inesperado al registrar venta", e);
            return false;
        } finally {
            try { 
                if (conn != null) { 
                    conn.setAutoCommit(true); 
                    conn.close(); 
                } 
            } catch (SQLException e) {
                Logger.error("Error al cerrar conexión", e);
            }
        }
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Obtiene la hora de la última venta registrada
     * @return Hora de la última venta en formato HH:mm o null si no hay ventas
     */
    public String obtenerHoraUltimaVenta() {
        String sql = "SELECT hora FROM ventas ORDER BY id DESC LIMIT 1";
        
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String hora = rs.getString("hora");
                // Formatear a HH:mm si es necesario
                if (hora != null && hora.length() >= 5) {
                    return hora.substring(0, 5);
                }
                return hora;
            }
            
        } catch (SQLException e) {
            Logger.error("Error obteniendo hora de última venta", e);
        }
        
        return null;
    }
}