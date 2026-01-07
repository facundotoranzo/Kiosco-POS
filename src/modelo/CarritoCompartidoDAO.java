package modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para manejo seguro del carrito compartido con control de concurrencia
 */
public class CarritoCompartidoDAO {
    
    public static class ItemCarrito {
        public final String nombreProducto;
        public final double precio;
        public final String origen;
        
        public ItemCarrito(String nombreProducto, double precio, String origen) {
            this.nombreProducto = nombreProducto;
            this.precio = precio;
            this.origen = origen;
        }
    }
    
    /**
     * Agrega un item al carrito compartido de forma thread-safe
     */
    public boolean agregarItem(String nombreProducto, double precio, String origen) {
        String sql = "INSERT INTO carrito_compartido(nombre_producto, precio, origen) VALUES(?,?,?)";
        
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            if (!Validador.esNombreValido(nombreProducto)) {
                Logger.warn("Intento de agregar producto con nombre inválido al carrito compartido: " + nombreProducto);
                return false;
            }
            
            if (!Validador.esPrecioValido(precio)) {
                Logger.warn("Intento de agregar producto con precio inválido al carrito compartido: " + precio);
                return false;
            }
            
            ps.setString(1, Validador.sanitizarTexto(nombreProducto));
            ps.setDouble(2, precio);
            ps.setString(3, Validador.sanitizarTexto(origen));
            
            boolean exito = ps.executeUpdate() > 0;
            if (exito) {
                Logger.debug("Item agregado al carrito compartido: " + nombreProducto + " por " + origen);
            }
            return exito;
            
        } catch (SQLException e) {
            Logger.error("Error SQL al agregar item al carrito compartido", e);
            return false;
        } catch (Exception e) {
            Logger.error("Error inesperado al agregar item al carrito compartido", e);
            return false;
        }
    }
    
    /**
     * Obtiene todos los items del carrito compartido
     */
    public List<ItemCarrito> obtenerItems() {
        List<ItemCarrito> items = new ArrayList<>();
        String sql = "SELECT nombre_producto, precio, origen FROM carrito_compartido ORDER BY id";
        
        try (Connection conn = ConexionDB.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                items.add(new ItemCarrito(
                    rs.getString("nombre_producto"),
                    rs.getDouble("precio"),
                    rs.getString("origen")
                ));
            }
            
        } catch (SQLException e) {
            Logger.error("Error SQL al obtener items del carrito compartido", e);
        } catch (Exception e) {
            Logger.error("Error inesperado al obtener items del carrito compartido", e);
        }
        
        return items;
    }
    
    /**
     * Remueve un item específico del carrito compartido
     */
    public boolean removerItem(String nombreProducto) {
        String sql = "DELETE FROM carrito_compartido WHERE nombre_producto = ? LIMIT 1";
        
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, nombreProducto);
            boolean exito = ps.executeUpdate() > 0;
            
            if (exito) {
                Logger.debug("Item removido del carrito compartido: " + nombreProducto);
            }
            return exito;
            
        } catch (SQLException e) {
            Logger.error("Error SQL al remover item del carrito compartido", e);
            return false;
        } catch (Exception e) {
            Logger.error("Error inesperado al remover item del carrito compartido", e);
            return false;
        }
    }
    
    /**
     * Limpia completamente el carrito compartido
     */
    public boolean limpiarCarrito() {
        String sql = "DELETE FROM carrito_compartido";
        
        try (Connection conn = ConexionDB.conectar();
             Statement stmt = conn.createStatement()) {
            
            int filasAfectadas = stmt.executeUpdate(sql);
            Logger.info("Carrito compartido limpiado. Items removidos: " + filasAfectadas);
            return true;
            
        } catch (SQLException e) {
            Logger.error("Error SQL al limpiar carrito compartido", e);
            return false;
        } catch (Exception e) {
            Logger.error("Error inesperado al limpiar carrito compartido", e);
            return false;
        }
    }
    
    /**
     * Obtiene el hash del contenido del carrito para detectar cambios
     */
    public String obtenerHashCarrito() {
        StringBuilder contenido = new StringBuilder();
        List<ItemCarrito> items = obtenerItems();
        
        for (ItemCarrito item : items) {
            contenido.append(item.nombreProducto)
                    .append("|")
                    .append(item.precio)
                    .append("|");
        }
        
        return String.valueOf(contenido.toString().hashCode());
    }
    
    /**
     * Transfiere todos los items del carrito compartido a una venta
     */
    public boolean transferirAVenta(int idCaja, String medioPago) {
        List<ItemCarrito> items = obtenerItems();
        if (items.isEmpty()) {
            return true; // No hay nada que transferir
        }
        
        Connection conn = null;
        try {
            conn = ConexionDB.conectar();
            conn.setAutoCommit(false); // Iniciar transacción
            
            // Calcular total
            double total = items.stream().mapToDouble(item -> item.precio).sum();
            
            // Crear venta
            String sqlVenta = "INSERT INTO ventas(id_caja, fecha, hora, total, medio_pago) VALUES(?,?,?,?,?)";
            PreparedStatement psVenta = conn.prepareStatement(sqlVenta, Statement.RETURN_GENERATED_KEYS);
            psVenta.setInt(1, idCaja);
            psVenta.setString(2, java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            psVenta.setString(3, java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
            psVenta.setDouble(4, total);
            psVenta.setString(5, medioPago);
            psVenta.executeUpdate();
            
            ResultSet rs = psVenta.getGeneratedKeys();
            int idVenta = 0;
            if (rs.next()) idVenta = rs.getInt(1);
            
            // Crear detalles
            String sqlDetalle = "INSERT INTO detalle_ventas(id_venta, nombre_producto, precio_unitario, cantidad, subtotal) VALUES(?,?,?,?,?)";
            PreparedStatement psDetalle = conn.prepareStatement(sqlDetalle);
            
            for (ItemCarrito item : items) {
                psDetalle.setInt(1, idVenta);
                psDetalle.setString(2, item.nombreProducto);
                psDetalle.setDouble(3, item.precio);
                psDetalle.setInt(4, 1);
                psDetalle.setDouble(5, item.precio);
                psDetalle.executeUpdate();
            }
            
            // Limpiar carrito compartido
            Statement stmtLimpiar = conn.createStatement();
            stmtLimpiar.executeUpdate("DELETE FROM carrito_compartido");
            
            conn.commit();
            Logger.info("Carrito compartido transferido a venta exitosamente. ID Venta: " + idVenta);
            return true;
            
        } catch (Exception e) {
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                Logger.error("Error en rollback de transferencia de carrito", ex);
            }
            Logger.error("Error al transferir carrito compartido a venta", e);
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                Logger.error("Error al cerrar conexión en transferencia de carrito", e);
            }
        }
    }
}