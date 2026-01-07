package modelo;

import java.sql.*;

public class ConfiguracionDAO {

    public Configuracion obtenerConfiguracion() {
        String sql = "SELECT * FROM configuracion WHERE id = 1";
        try (Connection conn = ConexionDB.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return new Configuracion(
                    rs.getString("nombre_negocio"),
                    rs.getString("mensaje_ticket"),
                    rs.getString("impresora")
                );
            }
        } catch (Exception e) { e.printStackTrace(); }
        return new Configuracion("Kiosco", "Gracias", "Predeterminada");
    }

    public boolean guardarConfiguracion(Configuracion config) {
        String sql = "UPDATE configuracion SET nombre_negocio = ?, mensaje_ticket = ?, impresora = ? WHERE id = 1";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, config.getNombreNegocio());
            pstmt.setString(2, config.getMensajeTicket());
            pstmt.setString(3, config.getImpresora());
            
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}