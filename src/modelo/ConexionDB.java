package modelo;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class ConexionDB {

    // Configuración por defecto
    public static String tipoBase = "mysql"; 
    private static String ip = "localhost";
    private static String puerto = "3306";
    private static String usuario = "root";
    private static String password = "";
    public static boolean imprimirTicket = true;
    public static boolean carritoCompartido = false; 
    public static String licencia = "FULL";

    /**
     * Carga la configuración desde db_config.properties
     * Determina el tipo de BD y licencia del sistema
     */
    private static void cargarConfiguracion() {
        File f = new File("db_config.properties");
        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                Properties prop = new Properties();
                prop.load(fis);
                
                tipoBase = prop.getProperty("tipo", "mysql").trim().toLowerCase();
                licencia = prop.getProperty("licencia", "FULL").trim().toUpperCase();
                
                ip = prop.getProperty("ip", "localhost").trim();
                puerto = prop.getProperty("puerto", "3306").trim();
                usuario = prop.getProperty("usuario", "root").trim();
                password = prop.getProperty("password", "").trim();
                
                String ticketConfig = prop.getProperty("imprimir_ticket", "true").trim();
                imprimirTicket = Boolean.parseBoolean(ticketConfig);
                
                String carroConfig = prop.getProperty("carrito_compartido", "false").trim();
                carritoCompartido = Boolean.parseBoolean(carroConfig);
                
            } catch (Exception e) {
                System.out.println("⚠️ No se pudo leer config, usando valores por defecto.");
            }
        }
    }

    public static Connection conectar() throws SQLException {
        // Para SQLite, usar conexión directa para evitar bloqueos
        if (tipoBase.equals("sqlite")) {
            return conectarDirecto();
        } else {
            // Para MySQL, usar pool de conexiones para mejor performance
            return ConnectionPool.getInstance().getConnection();
        }
    }
    
    /**
     * Conexión directa sin pool (usado internamente por el pool)
     * @return Conexión según el tipo configurado (SQLite/MySQL)
     */
    public static Connection conectarDirecto() throws SQLException {
        cargarConfiguracion(); 

        if (tipoBase.equals("sqlite")) {
            try {
                Class.forName("org.sqlite.JDBC");
                String url = "jdbc:sqlite:kiosco_lite.db?journal_mode=WAL&synchronous=NORMAL&cache_size=10000&temp_store=memory";
                Connection conn = DriverManager.getConnection(url);
                
                // Configurar SQLite para evitar bloqueos
                try (var stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA busy_timeout = 30000"); // 30 segundos de timeout
                    stmt.execute("PRAGMA journal_mode = WAL");    // Write-Ahead Logging
                    stmt.execute("PRAGMA synchronous = NORMAL");  // Balance entre seguridad y performance
                    stmt.execute("PRAGMA cache_size = 10000");    // Cache más grande
                    stmt.execute("PRAGMA temp_store = memory");   // Tablas temporales en memoria
                }
                
                Logger.debug("Conexión SQLite directa establecida con configuración optimizada");
                return conn;
            } catch (ClassNotFoundException e) {
                Logger.error("Driver de SQLite no encontrado", e);
                throw new SQLException("Driver SQLite no disponible", e);
            } catch (SQLException e) {
                Logger.error("Error al conectar con SQLite", e);
                throw e;
            }
        } else {
            String url = "jdbc:mysql://" + ip + ":" + puerto + "/tienda_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&createDatabaseIfNotExist=true";
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                Connection conn = DriverManager.getConnection(url, usuario, password);
                Logger.debug("Conexión MySQL directa establecida");
                return conn;
            } catch (ClassNotFoundException e) {
                Logger.error("Driver de MySQL no encontrado", e);
                throw new SQLException("Driver MySQL no disponible", e);
            } catch (SQLException e) {
                Logger.error("Error al conectar con MySQL: " + url, e);
                throw e;
            }
        }
    }

    /**
     * Inicializa la base de datos creando las tablas necesarias
     * Incluye detección automática de migración de datos
     */
    public static void inicializarBD() {
        cargarConfiguracion();
        
        if (necesitaMigracion()) {
            mostrarDialogoMigracion();
        }
        
        try (Connection conn = conectar()) {
            if (conn == null) return;
            
            Statement stmt = conn.createStatement();
            
            String SQL_AUTO_INC;
            String SQL_BIGINT; 
            
            if (tipoBase.equals("sqlite")) {
                SQL_AUTO_INC = "INTEGER PRIMARY KEY AUTOINCREMENT";
                SQL_BIGINT = "INTEGER PRIMARY KEY"; 
            } else {
                SQL_AUTO_INC = "INT PRIMARY KEY AUTO_INCREMENT";
                SQL_BIGINT = "BIGINT PRIMARY KEY";
            }

            // Crear todas las tablas del sistema
            crearTablaProductos(stmt, SQL_BIGINT);
            crearTablaCajas(stmt, SQL_AUTO_INC);
            crearTablaVentas(stmt, SQL_AUTO_INC);
            crearTablaDetalleVentas(stmt, SQL_AUTO_INC);
            crearTablaProveedores(stmt, SQL_AUTO_INC);
            crearTablaGastos(stmt, SQL_AUTO_INC);
            crearTablaConfiguracion(stmt);
            crearTablaCarritoCompartido(stmt, SQL_AUTO_INC);
            crearTablaUsuarios(stmt, SQL_AUTO_INC);

            System.out.println("✅ BD Inicializada (" + tipoBase + ")");
            
        } catch (SQLException e) {
            System.out.println("❌ Error SQL al iniciar tablas (" + tipoBase + "): " + e.getMessage());
        }
    }
    
    /**
     * Detecta si necesita migración de SQLite a MySQL
     */
    private static boolean necesitaMigracion() {
        // Solo migrar si:
        // 1. La configuración actual es MySQL (RED)
        // 2. Existe archivo SQLite con datos
        return tipoBase.equals("mysql") && MigradorDatos.existeDatosSQLite();
    }
    
    /**
     * Muestra el diálogo de migración
     */
    private static void mostrarDialogoMigracion() {
        try {
            javax.swing.SwingUtilities.invokeLater(() -> {
                vista.DialogoMigracion dialogo = new vista.DialogoMigracion(null);
                dialogo.setVisible(true);
                
                if (!dialogo.isMigracionRealizada()) {
                    Logger.info("Usuario decidió no migrar o canceló");
                }
            });
        } catch (Exception e) {
            Logger.error("Error mostrando diálogo de migración", e);
        }
    }
    
    /**
     * Crea las tablas MySQL (usado por el migrador)
     */
    public static void crearTablasMySQL(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        
        // Usar sintaxis MySQL
        String SQL_AUTO_INC = "INT PRIMARY KEY AUTO_INCREMENT";
        String SQL_BIGINT = "BIGINT PRIMARY KEY";
        
        // 1. PRODUCTOS
        String sqlProd = "CREATE TABLE IF NOT EXISTS productos (" +
                "codigo " + SQL_BIGINT + ", " + 
                "nombre VARCHAR(255), " +
                "precio DOUBLE, " +
                "stock INT, " +
                "es_cigarrillo INT DEFAULT 0)";
        stmt.execute(sqlProd);

        // 2. CAJAS
        String sqlCajas = "CREATE TABLE IF NOT EXISTS cajas (" +
                "id " + SQL_AUTO_INC + ", " +
                "fecha_apertura VARCHAR(50), " +
                "fecha_cierre VARCHAR(50), " +
                "monto_inicial DOUBLE DEFAULT 0, " +
                "monto_final DOUBLE DEFAULT 0, " +
                "total_ventas DOUBLE DEFAULT 0, " +
                "estado VARCHAR(20), " +
                "usuario_cierre VARCHAR(50))"; 
        stmt.execute(sqlCajas);

        // 3. VENTAS
        String sqlVentas = "CREATE TABLE IF NOT EXISTS ventas (" +
                "id " + SQL_AUTO_INC + ", " +
                "id_caja INT, " +
                "fecha VARCHAR(20), " +
                "hora VARCHAR(20), " +
                "total DOUBLE, " +
                "medio_pago VARCHAR(50))";
        stmt.execute(sqlVentas);

        // 4. DETALLE VENTAS
        String sqlDetalle = "CREATE TABLE IF NOT EXISTS detalle_ventas (" +
                "id " + SQL_AUTO_INC + ", " +
                "id_venta INT, " +
                "nombre_producto VARCHAR(255), " +
                "precio_unitario DOUBLE, " +
                "cantidad INT, " +
                "subtotal DOUBLE)";
        stmt.execute(sqlDetalle);

        // 5. CONFIGURACIÓN
        String sqlConfig = "CREATE TABLE IF NOT EXISTS configuracion (" +
                "id INT PRIMARY KEY, " +
                "nombre_negocio VARCHAR(100), " +
                "mensaje_ticket VARCHAR(255), " +
                "impresora VARCHAR(100))";
        stmt.execute(sqlConfig);

        // 6. CARRITO COMPARTIDO
        String sqlCarrito = "CREATE TABLE IF NOT EXISTS carrito_compartido (" +
                "id " + SQL_AUTO_INC + ", " +
                "nombre_producto VARCHAR(255), " +
                "precio DOUBLE, " +
                "origen VARCHAR(50) DEFAULT 'Puesto')";
        stmt.execute(sqlCarrito);

        // 7. USUARIOS
        String sqlUsers = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id " + SQL_AUTO_INC + ", " +
                "usuario VARCHAR(50) UNIQUE, " +
                "password VARCHAR(255), " +
                "rol VARCHAR(20))";
        stmt.execute(sqlUsers);
        
        Logger.info("Tablas MySQL creadas para migración");
    }
    
    // Métodos auxiliares para creación de tablas
    private static void crearTablaProductos(Statement stmt, String sqlBigint) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS productos (" +
                "codigo " + sqlBigint + ", " + 
                "nombre VARCHAR(255), " +
                "precio DOUBLE, " +
                "stock INT, " +
                "es_cigarrillo INT DEFAULT 0)";
        stmt.execute(sql);
    }
    
    private static void crearTablaCajas(Statement stmt, String sqlAutoInc) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS cajas (" +
                "id " + sqlAutoInc + ", " +
                "fecha_apertura VARCHAR(50), " +
                "fecha_cierre VARCHAR(50), " +
                "total_cierre DOUBLE, " +
                "total_efectivo DOUBLE, " +
                "total_transferencia DOUBLE, " +
                "total_cigarrillos_efectivo DOUBLE DEFAULT 0, " +
                "total_cigarrillos_transferencia DOUBLE DEFAULT 0, " +
                "estado VARCHAR(20), " +
                "usuario_cierre VARCHAR(50))"; 
        stmt.execute(sql);
    }
    
    private static void crearTablaVentas(Statement stmt, String sqlAutoInc) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS ventas (" +
                "id " + sqlAutoInc + ", " +
                "id_caja INT, " +
                "fecha VARCHAR(20), " +
                "hora VARCHAR(20), " +
                "total DOUBLE, " +
                "medio_pago VARCHAR(50))";
        stmt.execute(sql);
    }
    
    private static void crearTablaDetalleVentas(Statement stmt, String sqlAutoInc) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS detalle_ventas (" +
                "id " + sqlAutoInc + ", " +
                "id_venta INT, " +
                "nombre_producto VARCHAR(255), " +
                "precio_unitario DOUBLE, " +
                "cantidad INT, " +
                "subtotal DOUBLE)";
        stmt.execute(sql);
    }
    
    private static void crearTablaProveedores(Statement stmt, String sqlAutoInc) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS proveedores (" +
                "id " + sqlAutoInc + ", " +
                "nombre VARCHAR(100), " +
                "telefono VARCHAR(50), " +
                "contacto VARCHAR(100), " +
                "cuit VARCHAR(50))";
        stmt.execute(sql);
    }
    
    private static void crearTablaGastos(Statement stmt, String sqlAutoInc) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS gastos (" +
                "id " + sqlAutoInc + ", " +
                "id_caja INT, " +
                "nombre_proveedor VARCHAR(100), " +
                "descripcion VARCHAR(255), " +
                "monto DOUBLE, " +
                "saldo_remanente DOUBLE DEFAULT 0, " +
                "fecha VARCHAR(50))";
        stmt.execute(sql);
    }
    
    private static void crearTablaConfiguracion(Statement stmt) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS configuracion (" +
                "id INT PRIMARY KEY, " +
                "nombre_negocio VARCHAR(100), " +
                "mensaje_ticket VARCHAR(255), " +
                "impresora VARCHAR(100))";
        stmt.execute(sql);
        
        try {
            String sqlInsert = tipoBase.equals("sqlite") 
                    ? "INSERT OR IGNORE INTO configuracion(id, nombre_negocio) VALUES(1, 'Kiosco')"
                    : "INSERT IGNORE INTO configuracion(id, nombre_negocio) VALUES(1, 'Kiosco')";
            stmt.execute(sqlInsert);
        } catch(Exception e) {}
    }
    
    private static void crearTablaCarritoCompartido(Statement stmt, String sqlAutoInc) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS carrito_compartido (" +
                "id " + sqlAutoInc + ", " +
                "nombre_producto VARCHAR(255), " +
                "precio DOUBLE, " +
                "origen VARCHAR(50) DEFAULT 'Puesto')";
        stmt.execute(sql);
    }
    
    private static void crearTablaUsuarios(Statement stmt, String sqlAutoInc) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id " + sqlAutoInc + ", " +
                "usuario VARCHAR(50) UNIQUE, " +
                "password VARCHAR(50), " +
                "rol VARCHAR(20))";
        stmt.execute(sql);
    }
}