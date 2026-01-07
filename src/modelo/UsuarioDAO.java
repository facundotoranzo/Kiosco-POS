package modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public boolean hayUsuariosRegistrados() {
        try (Connection conn = ConexionDB.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM usuarios")) {
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) { Logger.error("Error verificando usuarios registrados", e); }
        return false;
    }

    /**
     * Obtiene la lista de todos los usuarios registrados
     * @return Lista de arrays con [id, usuario, rol]
     */
    public List<Object[]> listar() {
        List<Object[]> lista = new ArrayList<>();
        try (Connection conn = ConexionDB.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, usuario, rol FROM usuarios")) {
            
            while (rs.next()) {
                lista.add(new Object[]{
                    rs.getInt("id"),
                    rs.getString("usuario"),
                    rs.getString("rol")
                });
            }
        } catch (Exception e) { Logger.error("Error listando usuarios", e); }
        return lista;
    }

    /**
     * Crea un nuevo usuario con validaciones de seguridad
     * @param usuario Nombre de usuario (3-20 caracteres alfanuméricos)
     * @param pass Contraseña (mínimo 4 caracteres)
     * @param rol Rol del usuario (ADMIN/EMPLEADO)
     * @return true si se creó exitosamente
     */
    public boolean crear(String usuario, String pass, String rol) {
        if (!Validador.esUsuarioValido(usuario)) {
            Logger.warn("Intento de crear usuario con nombre inválido: " + usuario);
            return false;
        }
        
        if (!Validador.esPasswordValida(pass)) {
            Logger.warn("Intento de crear usuario con contraseña inválida");
            return false;
        }
        
        if (!Validador.esRolValido(rol)) {
            Logger.warn("Intento de crear usuario con rol inválido: " + rol);
            return false;
        }
        
        String sql = "INSERT INTO usuarios (usuario, password, rol) VALUES (?, ?, ?)";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, Validador.sanitizarTexto(usuario));
            ps.setString(2, HashUtil.hashPassword(pass));
            ps.setString(3, rol.toUpperCase());
            
            boolean exito = ps.executeUpdate() > 0;
            if (exito) {
                Logger.info("Usuario creado exitosamente: " + usuario + " con rol " + rol);
            }
            return exito;
            
        } catch (SQLException e) {
            Logger.error("Error SQL al crear usuario: " + usuario, e);
            return false;
        } catch (Exception e) {
            Logger.error("Error inesperado al crear usuario: " + usuario, e);
            return false;
        }
    }

    /**
     * Elimina un usuario por ID (protege al admin principal)
     * @param id ID del usuario a eliminar
     * @return true si se eliminó exitosamente
     */
    public boolean eliminar(int id) {
        if (id == 1) return false; // Proteger admin principal
        
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM usuarios WHERE id=?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { 
            Logger.error("Error eliminando usuario: " + id, e);
            return false; 
        }
    }

    /**
     * Valida las credenciales de login con hash seguro
     * Incluye migración automática de contraseñas en texto plano
     * @param usuario Nombre de usuario
     * @param password Contraseña en texto plano
     * @return Rol del usuario si es válido, null si falla
     */
    public String validarLogin(String usuario, String password) {
        // Validaciones básicas
        if (!Validador.esTextoValido(usuario) || !Validador.esTextoValido(password)) {
            Logger.warn("Intento de login con datos vacíos");
            return null;
        }
        
        String sql = "SELECT password, rol FROM usuarios WHERE usuario = ?";
        try (Connection conn = ConexionDB.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, Validador.sanitizarTexto(usuario));
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                String rol = rs.getString("rol");
                
                // Compatibilidad: Si es texto plano (contraseña antigua), migrar automáticamente
                if (HashUtil.isPlainText(storedPassword)) {
                    if (password.equals(storedPassword)) {
                        // Migrar a hash seguro usando la misma conexión
                        migrarPasswordAHashConConexion(conn, usuario, password);
                        Logger.info("Login exitoso y contraseña migrada para usuario: " + usuario);
                        return rol;
                    }
                } else {
                    // Verificar hash seguro
                    if (HashUtil.verifyPassword(password, storedPassword)) {
                        Logger.info("Login exitoso para usuario: " + usuario);
                        return rol;
                    }
                }
                Logger.warn("Intento de login fallido para usuario: " + usuario);
            } else {
                Logger.warn("Intento de login con usuario inexistente: " + usuario);
            }
        } catch (SQLException e) {
            Logger.error("Error SQL en validación de login para usuario: " + usuario, e);
        } catch (Exception e) {
            Logger.error("Error inesperado en validación de login para usuario: " + usuario, e);
        }
        return null;
    }

    /**
     * Migra contraseña de texto plano a hash seguro usando una conexión existente
     * Evita problemas de bloqueo de base de datos
     */
    private void migrarPasswordAHashConConexion(Connection conn, String usuario, String password) {
        String sql = "UPDATE usuarios SET password = ? WHERE usuario = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, HashUtil.hashPassword(password));
            ps.setString(2, usuario);
            ps.executeUpdate();
        } catch (Exception e) {
            Logger.error("Error migrando contraseña para usuario: " + usuario, e);
        }
    }

}
