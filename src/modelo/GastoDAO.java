package modelo;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GastoDAO {

    // 1. REGISTRAR (Guardar en la BD)
    public boolean registrarGasto(int idCaja, String proveedor, String descripcion, double monto, double saldoRemanente) {
        // Aseguramos que la columna exista antes de insertar (Parche de seguridad)
        verificarColumnaSaldo();

        String sql = "INSERT INTO gastos(id_caja, nombre_proveedor, descripcion, monto, saldo_remanente, fecha) VALUES(?,?,?,?,?,?)";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCaja);
            pstmt.setString(2, proveedor);
            pstmt.setString(3, descripcion);
            pstmt.setDouble(4, monto);
            pstmt.setDouble(5, saldoRemanente); // Aquí guardamos el valor
            pstmt.setString(6, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM HH:mm"))); 
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error al registrar gasto: " + e.getMessage());
            return false;
        }
    }

    // 2. LISTAR POR CAJA
    public List<Gasto> listarGastosDeCaja(int idCaja) {
        return ejecutarConsulta("SELECT * FROM gastos WHERE id_caja = " + idCaja + " ORDER BY id DESC");
    }

    // 3. LISTAR TODOS
    public List<Gasto> listarTodos() {
        return ejecutarConsulta("SELECT * FROM gastos ORDER BY id DESC");
    }

    // MÉTODO PRIVADO PARA NO REPETIR CÓDIGO DE LECTURA
    private List<Gasto> ejecutarConsulta(String sql) {
        List<Gasto> lista = new ArrayList<>();
        try (Connection conn = ConexionDB.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                // AQUÍ ESTABA EL POSIBLE ERROR: 
                // A veces si la columna es nueva, getDouble puede fallar si no manejamos el error,
                // o si usamos un constructor viejo.
                double saldo = 0.0;
                try {
                    saldo = rs.getDouble("saldo_remanente");
                } catch (Exception ex) {
                    // Si la columna no existe en registros viejos, queda en 0.
                }

                lista.add(new Gasto(
                    rs.getInt("id"), 
                    rs.getString("nombre_proveedor"),
                    rs.getString("descripcion"), 
                    rs.getDouble("monto"), 
                    saldo, // <--- Pasamos el saldo recuperado
                    rs.getString("fecha")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean eliminar(int id) {
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM gastos WHERE id = ?")) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public double obtenerTotalGastosCaja(int idCaja) {
        String sql = "SELECT SUM(monto) FROM gastos WHERE id_caja = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idCaja);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) {}
        return 0.0;
    }
    
    // --- MÉTODO DE AYUDA: Crea la columna si no existe ---
    // Esto soluciona si tu base de datos vieja no tenía la columna
    private void verificarColumnaSaldo() {
        try (Connection conn = ConexionDB.conectar();
             Statement stmt = conn.createStatement()) {
            try {
                stmt.executeQuery("SELECT saldo_remanente FROM gastos LIMIT 1");
            } catch (Exception e) {
                // Si falla el select, es que no existe. La creamos.
                stmt.executeUpdate("ALTER TABLE gastos ADD COLUMN saldo_remanente REAL DEFAULT 0");
                System.out.println("Base de datos parcheada: Columna saldo_remanente creada.");
            }
        } catch (Exception e) {}
    }
}