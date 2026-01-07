package modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> listar(String filtro) {
        List<Producto> lista = new ArrayList<>();
        String sql = "SELECT * FROM productos WHERE nombre LIKE ? OR CAST(codigo AS CHAR) LIKE ?";

        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + filtro + "%");
            pstmt.setString(2, "%" + filtro + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                lista.add(new Producto(
                    rs.getLong("codigo"),
                    rs.getString("nombre"),
                    rs.getDouble("precio"),
                    rs.getInt("stock"),
                    rs.getInt("es_cigarrillo") == 1 // Convertir 1 a true
                ));
            }
        } catch (Exception e) { Logger.error("Error listando productos", e); }
        return lista;
    }

    public boolean restarStock(long codigo, int cantidad) {
        String sql = "UPDATE productos SET stock = stock - ? WHERE codigo = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cantidad);
            pstmt.setLong(2, codigo);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public boolean sumarStock(long codigo, int cantidad) {
        String sql = "UPDATE productos SET stock = stock + ? WHERE codigo = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, cantidad);
            pstmt.setLong(2, codigo);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public void insertar(Producto p) {
        // Validaciones de entrada
        if (!Validador.esCodigoValido(p.getCodigo())) {
            Logger.warn("Intento de insertar producto con código inválido: " + p.getCodigo());
            return;
        }
        
        if (!Validador.esNombreValido(p.getNombre())) {
            Logger.warn("Intento de insertar producto con nombre inválido: " + p.getNombre());
            return;
        }
        
        if (!Validador.esPrecioValido(p.getPrecio())) {
            Logger.warn("Intento de insertar producto con precio inválido: " + p.getPrecio());
            return;
        }
        
        if (!Validador.esStockValido(p.getStock())) {
            Logger.warn("Intento de insertar producto con stock inválido: " + p.getStock());
            return;
        }
        
        String sql = "INSERT INTO productos(codigo, nombre, precio, stock, es_cigarrillo) VALUES(?,?,?,?,?)";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, p.getCodigo());
            pstmt.setString(2, Validador.sanitizarTexto(p.getNombre()));
            pstmt.setDouble(3, p.getPrecio());
            pstmt.setInt(4, p.getStock());
            pstmt.setInt(5, p.isEsCigarrillo() ? 1 : 0);
            pstmt.executeUpdate();
            Logger.info("Producto insertado exitosamente: " + p.getNombre() + " (Código: " + p.getCodigo() + ")");
        } catch (SQLException e) {
            Logger.error("Error SQL al insertar producto: " + p.getNombre(), e);
        } catch (Exception e) {
            Logger.error("Error inesperado al insertar producto: " + p.getNombre(), e);
        }
    }

    public boolean eliminar(long codigo) {
        String sql = "DELETE FROM productos WHERE codigo = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, codigo);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public boolean actualizar(Producto p) {
        // Validaciones de entrada
        if (!Validador.esCodigoValido(p.getCodigo())) {
            Logger.warn("Intento de actualizar producto con código inválido: " + p.getCodigo());
            return false;
        }
        
        if (!Validador.esNombreValido(p.getNombre())) {
            Logger.warn("Intento de actualizar producto con nombre inválido: " + p.getNombre());
            return false;
        }
        
        if (!Validador.esPrecioValido(p.getPrecio())) {
            Logger.warn("Intento de actualizar producto con precio inválido: " + p.getPrecio());
            return false;
        }
        
        if (!Validador.esStockValido(p.getStock())) {
            Logger.warn("Intento de actualizar producto con stock inválido: " + p.getStock());
            return false;
        }
        
        String sql = "UPDATE productos SET nombre = ?, precio = ?, stock = ?, es_cigarrillo = ? WHERE codigo = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, Validador.sanitizarTexto(p.getNombre()));
            pstmt.setDouble(2, p.getPrecio());
            pstmt.setInt(3, p.getStock());
            pstmt.setInt(4, p.isEsCigarrillo() ? 1 : 0);
            pstmt.setLong(5, p.getCodigo());
            
            boolean exito = pstmt.executeUpdate() > 0;
            if (exito) {
                Logger.info("Producto actualizado exitosamente: " + p.getNombre() + " (Código: " + p.getCodigo() + ")");
            }
            return exito;
        } catch (SQLException e) {
            Logger.error("Error SQL al actualizar producto: " + p.getNombre(), e);
            return false;
        } catch (Exception e) {
            Logger.error("Error inesperado al actualizar producto: " + p.getNombre(), e);
            return false;
        }
    }
    
    public Producto buscarPorCodigo(long codigo) {
        String sql = "SELECT * FROM productos WHERE codigo = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, codigo);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Producto(
                    rs.getLong("codigo"),
                    rs.getString("nombre"),
                    rs.getDouble("precio"),
                    rs.getInt("stock"),
                    rs.getInt("es_cigarrillo") == 1
                );
            }
        } catch (SQLException e) {
            Logger.error("Error SQL al buscar producto por código: " + codigo, e);
        } catch (Exception e) {
            Logger.error("Error inesperado al buscar producto por código: " + codigo, e);
        }
        return null;
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Cuenta productos con stock bajo
     * @param limite Límite de stock considerado como "bajo"
     * @return Cantidad de productos con stock menor o igual al límite
     */
    public int contarProductosStockBajo(int limite) {
        if (ConexionDB.tipoBase.equals("sqlite") && ConexionDB.licencia.equals("LITE")) {
            return 0; // En LITE no hay control de stock avanzado
        }
        
        String sql = "SELECT COUNT(*) FROM productos WHERE stock <= ?";
        
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, limite);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } catch (SQLException e) {
            Logger.error("Error contando productos con stock bajo", e);
        }
        
        return 0;
    }
}