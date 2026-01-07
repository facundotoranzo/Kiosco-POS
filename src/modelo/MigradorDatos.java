package modelo;

import java.sql.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Migrador de datos entre SQLite y MySQL
 * Permite migrar datos al cambiar de LITE/PRO a RED
 */
public class MigradorDatos {
    
    /**
     * Migra todos los datos de SQLite a MySQL
     * Incluye productos, usuarios, ventas, cajas y configuración
     * @return true si la migración fue exitosa
     */
    public static boolean migrarSQLiteAMySQL() {
        Connection sqliteConn = null;
        Connection mysqlConn = null;
        
        try {
            Logger.info("Iniciando migración de SQLite a MySQL...");
            
            sqliteConn = DriverManager.getConnection("jdbc:sqlite:kiosco_lite.db");
            Logger.info("Conexión SQLite establecida");
            
            mysqlConn = ConexionDB.conectarDirecto();
            Logger.info("Conexión MySQL establecida");
            
            ConexionDB.crearTablasMySQL(mysqlConn);
            
            int productosM = migrarProductos(sqliteConn, mysqlConn);
            int usuariosM = migrarUsuarios(sqliteConn, mysqlConn);
            int ventasM = migrarVentas(sqliteConn, mysqlConn);
            int detallesM = migrarDetalleVentas(sqliteConn, mysqlConn);
            int cajasM = migrarCajas(sqliteConn, mysqlConn);
            migrarConfiguracion(sqliteConn, mysqlConn);
            
            crearBackupSQLite();
            
            Logger.info(String.format("Migración completada: %d productos, %d usuarios, %d ventas, %d detalles, %d cajas", 
                       productosM, usuariosM, ventasM, detallesM, cajasM));
            
            return true;
            
        } catch (Exception e) {
            Logger.error("Error crítico en migración", e);
            return false;
        } finally {
            try {
                if (sqliteConn != null) sqliteConn.close();
                if (mysqlConn != null) mysqlConn.close();
            } catch (SQLException e) {
                Logger.error("Error cerrando conexiones", e);
            }
        }
    }
    
    /**
     * Migra la tabla productos de SQLite a MySQL
     * @return número de productos migrados exitosamente
     */
    private static int migrarProductos(Connection origen, Connection destino) throws SQLException {
        String selectSQL = "SELECT * FROM productos";
        String insertSQL = "INSERT IGNORE INTO productos (codigo, nombre, precio, stock) VALUES (?, ?, ?, ?)";
        
        int migrados = 0;
        
        try (PreparedStatement select = origen.prepareStatement(selectSQL);
             PreparedStatement insert = destino.prepareStatement(insertSQL);
             ResultSet rs = select.executeQuery()) {
            
            while (rs.next()) {
                insert.setLong(1, rs.getLong("codigo"));
                insert.setString(2, rs.getString("nombre"));
                insert.setDouble(3, rs.getDouble("precio"));
                insert.setInt(4, rs.getInt("stock"));
                
                if (insert.executeUpdate() > 0) {
                    migrados++;
                }
            }
            
            Logger.info("Productos migrados: " + migrados);
            return migrados;
            
        } catch (SQLException e) {
            Logger.error("Error migrando productos", e);
            throw e;
        }
    }
    
    /**
     * Migra la tabla usuarios de SQLite a MySQL
     * @return número de usuarios migrados exitosamente
     */
    private static int migrarUsuarios(Connection origen, Connection destino) throws SQLException {
        String selectSQL = "SELECT * FROM usuarios";
        String insertSQL = "INSERT IGNORE INTO usuarios (id, usuario, password, rol) VALUES (?, ?, ?, ?)";
        
        int migrados = 0;
        
        try (PreparedStatement select = origen.prepareStatement(selectSQL);
             PreparedStatement insert = destino.prepareStatement(insertSQL);
             ResultSet rs = select.executeQuery()) {
            
            while (rs.next()) {
                insert.setInt(1, rs.getInt("id"));
                insert.setString(2, rs.getString("usuario"));
                insert.setString(3, rs.getString("password"));
                insert.setString(4, rs.getString("rol"));
                
                if (insert.executeUpdate() > 0) {
                    migrados++;
                }
            }
            
            Logger.info("Usuarios migrados: " + migrados);
            return migrados;
            
        } catch (SQLException e) {
            Logger.error("Error migrando usuarios", e);
            throw e;
        }
    }
    
    /**
     * Migra la tabla ventas de SQLite a MySQL
     * @return número de ventas migradas exitosamente
     */
    private static int migrarVentas(Connection origen, Connection destino) throws SQLException {
        String selectSQL = "SELECT * FROM ventas";
        String insertSQL = "INSERT IGNORE INTO ventas (id, id_caja, fecha, hora, total, medio_pago) VALUES (?, ?, ?, ?, ?, ?)";
        
        int migrados = 0;
        
        try (PreparedStatement select = origen.prepareStatement(selectSQL);
             PreparedStatement insert = destino.prepareStatement(insertSQL);
             ResultSet rs = select.executeQuery()) {
            
            while (rs.next()) {
                insert.setInt(1, rs.getInt("id"));
                insert.setInt(2, rs.getInt("id_caja"));
                insert.setString(3, rs.getString("fecha"));
                insert.setString(4, rs.getString("hora"));
                insert.setDouble(5, rs.getDouble("total"));
                insert.setString(6, rs.getString("medio_pago"));
                
                if (insert.executeUpdate() > 0) {
                    migrados++;
                }
            }
            
            Logger.info("Ventas migradas: " + migrados);
            return migrados;
            
        } catch (SQLException e) {
            Logger.error("Error migrando ventas", e);
            throw e;
        }
    }
    
    /**
     * Migra la tabla detalle_ventas de SQLite a MySQL
     * @return número de detalles migrados exitosamente
     */
    private static int migrarDetalleVentas(Connection origen, Connection destino) throws SQLException {
        String selectSQL = "SELECT * FROM detalle_ventas";
        String insertSQL = "INSERT IGNORE INTO detalle_ventas (id, id_venta, nombre_producto, precio_unitario, cantidad, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
        
        int migrados = 0;
        
        try (PreparedStatement select = origen.prepareStatement(selectSQL);
             PreparedStatement insert = destino.prepareStatement(insertSQL);
             ResultSet rs = select.executeQuery()) {
            
            while (rs.next()) {
                insert.setInt(1, rs.getInt("id"));
                insert.setInt(2, rs.getInt("id_venta"));
                insert.setString(3, rs.getString("nombre_producto"));
                insert.setDouble(4, rs.getDouble("precio_unitario"));
                insert.setInt(5, rs.getInt("cantidad"));
                insert.setDouble(6, rs.getDouble("subtotal"));
                
                if (insert.executeUpdate() > 0) {
                    migrados++;
                }
            }
            
            Logger.info("Detalles de venta migrados: " + migrados);
            return migrados;
            
        } catch (SQLException e) {
            Logger.error("Error migrando detalles de venta", e);
            throw e;
        }
    }
    
    /**
     * Migra la tabla cajas de SQLite a MySQL
     * @return número de cajas migradas exitosamente
     */
    private static int migrarCajas(Connection origen, Connection destino) throws SQLException {
        String selectSQL = "SELECT * FROM cajas";
        String insertSQL = "INSERT IGNORE INTO cajas (id, fecha_apertura, fecha_cierre, monto_inicial, monto_final, total_ventas, estado, usuario_cierre) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        int migrados = 0;
        
        try (PreparedStatement select = origen.prepareStatement(selectSQL);
             PreparedStatement insert = destino.prepareStatement(insertSQL);
             ResultSet rs = select.executeQuery()) {
            
            while (rs.next()) {
                insert.setInt(1, rs.getInt("id"));
                insert.setString(2, rs.getString("fecha_apertura"));
                insert.setString(3, rs.getString("fecha_cierre"));
                insert.setDouble(4, rs.getDouble("monto_inicial"));
                insert.setDouble(5, rs.getDouble("monto_final"));
                insert.setDouble(6, rs.getDouble("total_ventas"));
                insert.setString(7, rs.getString("estado"));
                insert.setString(8, rs.getString("usuario_cierre"));
                
                if (insert.executeUpdate() > 0) {
                    migrados++;
                }
            }
            
            Logger.info("Cajas migradas: " + migrados);
            return migrados;
            
        } catch (SQLException e) {
            Logger.error("Error migrando cajas", e);
            throw e;
        }
    }
    
    /**
     * Migra la configuración del sistema de SQLite a MySQL
     * No es crítica, por lo que no lanza excepciones si falla
     */
    private static void migrarConfiguracion(Connection origen, Connection destino) throws SQLException {
        String selectSQL = "SELECT * FROM configuracion";
        String insertSQL = "INSERT IGNORE INTO configuracion (id, nombre_negocio, mensaje_ticket, impresora) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement select = origen.prepareStatement(selectSQL);
             PreparedStatement insert = destino.prepareStatement(insertSQL);
             ResultSet rs = select.executeQuery()) {
            
            if (rs.next()) {
                insert.setInt(1, rs.getInt("id"));
                insert.setString(2, rs.getString("nombre_negocio"));
                insert.setString(3, rs.getString("mensaje_ticket"));
                insert.setString(4, rs.getString("impresora"));
                insert.executeUpdate();
                
                Logger.info("Configuración migrada");
            }
            
        } catch (SQLException e) {
            Logger.error("Error migrando configuración", e);
        }
    }
    
    /**
     * Crea un backup del archivo SQLite original
     */
    private static void crearBackupSQLite() {
        try {
            File original = new File("kiosco_lite.db");
            if (original.exists()) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                File backup = new File("backup_kiosco_lite_" + timestamp + ".db");
                
                Files.copy(original.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Logger.info("Backup creado: " + backup.getName());
            }
        } catch (IOException e) {
            Logger.error("Error creando backup de SQLite", e);
        }
    }
    
    /**
     * Verifica si existe el archivo SQLite para migrar
     */
    public static boolean existeDatosSQLite() {
        File sqliteDB = new File("kiosco_lite.db");
        return sqliteDB.exists() && sqliteDB.length() > 0;
    }
    
    /**
     * Cuenta los registros en SQLite para mostrar al usuario
     */
    public static String obtenerEstadisticasSQLite() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:kiosco_lite.db")) {
            
            int productos = contarRegistros(conn, "productos");
            int usuarios = contarRegistros(conn, "usuarios");
            int ventas = contarRegistros(conn, "ventas");
            
            return String.format("📦 %d productos, 👥 %d usuarios, 💰 %d ventas", productos, usuarios, ventas);
            
        } catch (Exception e) {
            Logger.error("Error obteniendo estadísticas SQLite", e);
            return "Datos disponibles para migrar";
        }
    }
    
    private static int contarRegistros(Connection conn, String tabla) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
            
            return rs.next() ? rs.getInt(1) : 0;
            
        } catch (SQLException e) {
            return 0;
        }
    }
}