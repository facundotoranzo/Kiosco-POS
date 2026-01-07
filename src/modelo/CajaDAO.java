package modelo;

import java.io.File;
import java.io.FileInputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class CajaDAO {

    // 1. OBTENER ID CAJA ABIERTA (O CREAR UNA NUEVA)
    public int obtenerOIniciarCaja() {
        String sqlBuscar = "SELECT id FROM cajas WHERE estado = 'ABIERTA' ORDER BY id DESC LIMIT 1";
        try (Connection conn = ConexionDB.conectar(); 
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery(sqlBuscar)) {
            if (rs.next()) return rs.getInt("id");
        } catch (Exception e) { Logger.error("Error buscando caja abierta", e); }
        return abrirNuevaCaja();
    }

    private int abrirNuevaCaja() {
        String sql = "INSERT INTO cajas(fecha_apertura, estado, total_cierre, total_cigarrillos_efectivo, total_cigarrillos_transferencia) VALUES(?, 'ABIERTA', 0, 0, 0)";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { Logger.error("Error abriendo nueva caja", e); }
        return -1;
    }

    // 2. CÁLCULO DESGLOSADO INTELIGENTE (CON OPCIÓN DE CIGARRILLOS)
    public double[] calcularDesgloseCaja(int idCaja) {
        double brutoEfectivo = 0, brutoDigital = 0;
        double cigaEfectivo = 0, cigaDigital = 0;

        Connection conn = null;
        try {
            conn = ConexionDB.conectar();
            
            // A. Totales Brutos
            String sqlBruto = "SELECT medio_pago, SUM(total) FROM ventas WHERE id_caja = ? GROUP BY medio_pago";
            PreparedStatement pstBruto = conn.prepareStatement(sqlBruto);
            pstBruto.setInt(1, idCaja);
            ResultSet rsBruto = pstBruto.executeQuery();
            while(rsBruto.next()) {
                String medio = rsBruto.getString(1);
                double monto = rsBruto.getDouble(2);
                if ("Efectivo".equalsIgnoreCase(medio)) brutoEfectivo += monto;
                else brutoDigital += monto;
            }

            // B. Solo Cigarrillos
            String sqlCiga = """
                SELECT v.medio_pago, SUM(d.subtotal) 
                FROM detalle_ventas d 
                JOIN ventas v ON d.id_venta = v.id 
                JOIN productos p ON d.nombre_producto = p.nombre 
                WHERE v.id_caja = ? AND p.es_cigarrillo = 1 
                GROUP BY v.medio_pago
                """;
            PreparedStatement pstCiga = conn.prepareStatement(sqlCiga);
            pstCiga.setInt(1, idCaja);
            ResultSet rsCiga = pstCiga.executeQuery();
            while(rsCiga.next()) {
                String medio = rsCiga.getString(1);
                double monto = rsCiga.getDouble(2);
                if ("Efectivo".equalsIgnoreCase(medio)) cigaEfectivo += monto;
                else cigaDigital += monto;
            }

        } catch (Exception e) { Logger.error("Error calculando desglose de caja: " + idCaja, e); }
        finally { try { if(conn!=null) conn.close(); } catch(Exception e){} }

        // C. Leer Preferencia del Usuario
        boolean separarCigarros = true; 
        try {
            File f = new File("db_config.properties");
            if (f.exists()) {
                Properties prop = new Properties();
                prop.load(new FileInputStream(f));
                separarCigarros = prop.getProperty("separar_cigarros", "true").equals("true");
            }
        } catch (Exception e) {}

        double netoEfectivo, netoDigital;

        if (separarCigarros) {
            netoEfectivo = brutoEfectivo - cigaEfectivo;
            netoDigital = brutoDigital - cigaDigital;
        } else {
            netoEfectivo = brutoEfectivo;
            netoDigital = brutoDigital;
            cigaEfectivo = 0;
            cigaDigital = 0;
        }

        double totalCaja = netoEfectivo + netoDigital + cigaEfectivo + cigaDigital;

        // Retorna: [0]Total, [1]EfecNeto, [2]DigiNeto, [3]CigaEfectivo, [4]CigaTransf
        return new double[] { totalCaja, netoEfectivo, netoDigital, cigaEfectivo, cigaDigital };
    }

    // 3. CERRAR CAJA (CON USUARIO)
    public boolean cerrarCaja(int idCaja) {
        double[] totales = calcularDesgloseCaja(idCaja);
        
        try (Connection conn = ConexionDB.conectar()) {
            String sql = "UPDATE cajas SET estado='CERRADA', fecha_cierre=?, total_cierre=?, total_efectivo=?, total_transferencia=?, total_cigarrillos_efectivo=?, total_cigarrillos_transferencia=?, usuario_cierre=? WHERE id=?";
            
            PreparedStatement pst = conn.prepareStatement(sql);
            
            pst.setString(1, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            pst.setDouble(2, totales[0]);
            pst.setDouble(3, totales[1]);
            pst.setDouble(4, totales[2]);
            pst.setDouble(5, totales[3]); 
            pst.setDouble(6, totales[4]); 
            
            // Usuario que cierra
            String quienCierra = modelo.Sesion.getUsuario();
            if (quienCierra == null || quienCierra.equals("Desconocido")) {
                quienCierra = "Sistema";
            }
            pst.setString(7, quienCierra); 
            
            pst.setInt(8, idCaja);
            
            return pst.executeUpdate() > 0;
            
        } catch (Exception e) { 
            Logger.error("Error cerrando caja: " + idCaja, e);
            return false; 
        }
    }

    // 4. LISTAR CAJAS (PARA EL PANEL DE HISTORIAL)
    public List<Object[]> listarCajasCerradas() {
        List<Object[]> lista = new ArrayList<>();
        String sql = "SELECT id, fecha_apertura, fecha_cierre, estado, usuario_cierre FROM cajas ORDER BY id DESC";
        
        try (Connection conn = ConexionDB.conectar();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while(rs.next()) {
                int id = rs.getInt("id");
                double[] desglose = calcularDesgloseCaja(id);
                double totalCigarros = desglose[3] + desglose[4];
                
                String usuario = rs.getString("usuario_cierre");
                if (usuario == null) usuario = "-";

                // Debe coincidir con las columnas de PanelCajas
                lista.add(new Object[]{
                    id,                             // 0
                    rs.getString("fecha_apertura"), // 1
                    rs.getString("fecha_cierre"),   // 2
                    desglose[0],                    // 3 (Total)
                    desglose[1],                    // 4 (Efectivo)
                    desglose[2],                    // 5 (Transf)
                    totalCigarros,                  // 6 (Cigarros)
                    rs.getString("estado"),         // 7
                    usuario,                        // 8 (Responsable)
                    ""                              // 9 (Botón)
                });
            }
        } catch (Exception e) { Logger.error("Error listando cajas cerradas", e); }
        return lista;
    }

    // 5. OBTENER DETALLE (PARA VER QUÉ SE VENDIÓ)
    public List<Object[]> obtenerDetalleVentas(int idCaja) {
        List<Object[]> detalles = new ArrayList<>();
        String sql = """
            SELECT v.hora, d.nombre_producto, d.precio_unitario, d.cantidad, d.subtotal, v.medio_pago 
            FROM detalle_ventas d 
            JOIN ventas v ON d.id_venta = v.id 
            WHERE v.id_caja = ? ORDER BY v.id DESC
            """;
        try (Connection conn = ConexionDB.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCaja);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                detalles.add(new Object[]{
                    rs.getString("hora"), 
                    rs.getString("nombre_producto"),
                    Formato.moneda(rs.getDouble("precio_unitario")), 
                    rs.getInt("cantidad"),
                    Formato.moneda(rs.getDouble("subtotal")),
                    rs.getString("medio_pago")
                });
            }
        } catch (Exception e) { Logger.error("Error obteniendo detalle de ventas: " + idCaja, e); }
        return detalles;
    }

    // 6. OBTENER DETALLE EDITABLE (PARA ELIMINAR ÍTEMS)
    // Devuelve el ID del detalle en la col 0 oculta
    public List<Object[]> obtenerDetalleVentasEditable(int idCaja) {
        List<Object[]> detalles = new ArrayList<>();
        String sql = """
            SELECT d.id, v.hora, d.nombre_producto, d.precio_unitario, d.cantidad, d.subtotal, v.medio_pago 
            FROM detalle_ventas d 
            JOIN ventas v ON d.id_venta = v.id 
            WHERE v.id_caja = ? ORDER BY v.id DESC
            """;
        try (Connection conn = ConexionDB.conectar(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCaja);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                detalles.add(new Object[]{
                    rs.getInt("id"), // ID Oculto para borrar
                    rs.getString("hora"), 
                    rs.getString("nombre_producto"),
                    Formato.moneda(rs.getDouble("precio_unitario")), 
                    rs.getInt("cantidad"),
                    Formato.moneda(rs.getDouble("subtotal")),
                    rs.getString("medio_pago")
                });
            }
        } catch (Exception e) { Logger.error("Error obteniendo detalle editable: " + idCaja, e); }
        return detalles;
    }

    // 7. ELIMINAR UN ITEM DE UNA VENTA (DEVOLUCIÓN)
    public boolean eliminarItemVenta(int idDetalle) {
        Connection conn = null;
        try {
            conn = ConexionDB.conectar();
            conn.setAutoCommit(false); // Transacción segura

            // A. Recuperar datos para devolver stock
            String sqlGet = "SELECT nombre_producto, cantidad, id_venta, subtotal FROM detalle_ventas WHERE id = ?";
            PreparedStatement psGet = conn.prepareStatement(sqlGet);
            psGet.setInt(1, idDetalle);
            ResultSet rs = psGet.executeQuery();
            
            if (rs.next()) {
                String prod = rs.getString("nombre_producto");
                int cant = rs.getInt("cantidad");
                int idVenta = rs.getInt("id_venta");
                double subtotal = rs.getDouble("subtotal");

                // B. Devolver Stock
                String sqlStock = "UPDATE productos SET stock = stock + ? WHERE nombre = ?";
                PreparedStatement psStock = conn.prepareStatement(sqlStock);
                psStock.setInt(1, cant);
                psStock.setString(2, prod);
                psStock.executeUpdate();

                // C. Borrar el detalle
                String sqlDel = "DELETE FROM detalle_ventas WHERE id = ?";
                PreparedStatement psDel = conn.prepareStatement(sqlDel);
                psDel.setInt(1, idDetalle);
                psDel.executeUpdate();

                // D. Restar plata a la venta original
                String sqlUpdVenta = "UPDATE ventas SET total = total - ? WHERE id = ?";
                PreparedStatement psUpd = conn.prepareStatement(sqlUpdVenta);
                psUpd.setDouble(1, subtotal);
                psUpd.setInt(2, idVenta);
                psUpd.executeUpdate();
            }
            
            conn.commit();
            return true;

        } catch (Exception e) {
            try { if(conn!=null) conn.rollback(); } catch(Exception ex){}
            return false;
        } finally {
            try { if(conn!=null) conn.close(); } catch(Exception e){}
        }
    }

    // 8. ELIMINAR CAJA COMPLETA (ADMIN)
    public boolean eliminarCaja(int idCaja) {
        Connection conn = null;
        try {
            conn = ConexionDB.conectar();
            conn.setAutoCommit(false);

            // Borrar detalles de ventas de esa caja
            String sql1 = "DELETE FROM detalle_ventas WHERE id_venta IN (SELECT id FROM ventas WHERE id_caja = ?)";
            PreparedStatement ps1 = conn.prepareStatement(sql1);
            ps1.setInt(1, idCaja);
            ps1.executeUpdate();

            // Borrar ventas de esa caja
            String sql2 = "DELETE FROM ventas WHERE id_caja = ?";
            PreparedStatement ps2 = conn.prepareStatement(sql2);
            ps2.setInt(1, idCaja);
            ps2.executeUpdate();

            // Borrar la caja
            String sql3 = "DELETE FROM cajas WHERE id = ?";
            PreparedStatement ps3 = conn.prepareStatement(sql3);
            ps3.setInt(1, idCaja);
            ps3.executeUpdate();

            conn.commit();
            return true;
        } catch (Exception e) {
            try { if(conn!=null) conn.rollback(); } catch(Exception ex){}
            return false;
        } finally {
            try { if(conn!=null) conn.close(); } catch(Exception e){}
        }
    }
    
    // Auxiliar para totales parciales
    public double obtenerVentasActuales(int idCaja) { 
        double[] desglose = calcularDesgloseCaja(idCaja);
        return desglose[0]; // Total bruto
    }

    public boolean restarCantidadOEliminar(int idDetalle, int idCaja) {
        // Redirige la llamada al método nuevo
        return eliminarItemVenta(idDetalle);
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Obtiene el estado de la caja actual
     * @return Estado de la caja (ABIERTA/CERRADA)
     */
    public String obtenerEstadoCajaActual() {
        String sql = "SELECT estado FROM cajas WHERE estado = 'ABIERTA' ORDER BY id DESC LIMIT 1";
        
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return "Abierta #" + obtenerOIniciarCaja();
            }
            
        } catch (SQLException e) {
            Logger.error("Error obteniendo estado de caja", e);
        }
        
        return "Cerrada";
    }
}