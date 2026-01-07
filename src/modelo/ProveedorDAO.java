package modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    public List<Proveedor> listar() {
        List<Proveedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM proveedores";
        try (Connection conn = ConexionDB.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new Proveedor(
                    rs.getInt("id"), rs.getString("nombre"), 
                    rs.getString("telefono"), rs.getString("contacto"), 
                    rs.getString("cuit")
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    public void insertar(Proveedor p) {
        String sql = "INSERT INTO proveedores(nombre, telefono, contacto, cuit) VALUES(?,?,?,?)";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getNombre());
            pstmt.setString(2, p.getTelefono());
            pstmt.setString(3, p.getContacto());
            pstmt.setString(4, p.getCuit());
            pstmt.executeUpdate();
        } catch (Exception e) {}
    }

    public void eliminar(int id) {
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM proveedores WHERE id = ?")) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (Exception e) {}
    }
}