package modelo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Pool de conexiones básico para mejorar performance
 */
public class ConnectionPool {
    
    private static ConnectionPool instance;
    private final BlockingQueue<Connection> pool;
    private final int maxConnections;
    private int activeConnections = 0;
    
    private ConnectionPool(int maxConnections) {
        this.maxConnections = maxConnections;
        this.pool = new ArrayBlockingQueue<>(maxConnections);
        initializePool();
    }
    
    public static synchronized ConnectionPool getInstance() {
        if (instance == null) {
            instance = new ConnectionPool(15); // Aumentado a 15 conexiones máximo
        }
        return instance;
    }
    
    private void initializePool() {
        try {
            for (int i = 0; i < 5; i++) { // Crear 5 conexiones iniciales (más que antes)
                Connection conn = createNewConnection();
                if (conn != null) {
                    pool.offer(conn);
                    activeConnections++;
                }
            }
            Logger.info("Pool de conexiones inicializado con " + activeConnections + " conexiones");
        } catch (Exception e) {
            Logger.error("Error al inicializar pool de conexiones", e);
        }
    }
    
    private Connection createNewConnection() {
        try {
            return ConexionDB.conectarDirecto(); // Método directo sin pool
        } catch (SQLException e) {
            Logger.error("Error al crear nueva conexión", e);
            return null;
        }
    }
    
    /**
     * Obtiene una conexión del pool
     */
    public Connection getConnection() throws SQLException {
        try {
            // Intentar obtener conexión del pool (timeout 5 segundos)
            Connection conn = pool.poll(5, TimeUnit.SECONDS);
            
            if (conn == null || conn.isClosed()) {
                // Si no hay conexiones disponibles o está cerrada, crear nueva
                if (activeConnections < maxConnections) {
                    conn = createNewConnection();
                    if (conn != null) {
                        activeConnections++;
                        Logger.debug("Nueva conexión creada. Total activas: " + activeConnections);
                    }
                } else {
                    throw new SQLException("Pool de conexiones agotado. Máximo: " + maxConnections);
                }
            }
            
            if (conn == null) {
                throw new SQLException("No se pudo obtener conexión del pool");
            }
            
            return new PooledConnection(conn, this);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrumpido mientras esperaba conexión", e);
        }
    }
    
    /**
     * Devuelve una conexión al pool
     */
    public void returnConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed() && conn.isValid(1)) {
                    // Resetear estado de la conexión
                    conn.setAutoCommit(true);
                    
                    if (!pool.offer(conn)) {
                        // Pool lleno, cerrar conexión
                        conn.close();
                        activeConnections--;
                        Logger.debug("Conexión cerrada (pool lleno). Total activas: " + activeConnections);
                    }
                } else {
                    // Conexión inválida, cerrar y decrementar contador
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        // Ignorar errores al cerrar conexión inválida
                    }
                    activeConnections--;
                    Logger.debug("Conexión inválida cerrada. Total activas: " + activeConnections);
                }
            } catch (SQLException e) {
                Logger.error("Error al devolver conexión al pool", e);
                try {
                    conn.close();
                } catch (SQLException ex) {
                    // Ignorar
                }
                activeConnections--;
            }
        }
    }
    
    /**
     * Cierra todas las conexiones del pool
     */
    public void closeAll() {
        Logger.info("Cerrando pool de conexiones...");
        
        Connection conn;
        while ((conn = pool.poll()) != null) {
            try {
                conn.close();
                activeConnections--;
            } catch (SQLException e) {
                Logger.error("Error al cerrar conexión del pool", e);
            }
        }
        
        Logger.info("Pool de conexiones cerrado. Conexiones cerradas: " + activeConnections);
        activeConnections = 0;
    }
    
    /**
     * Obtiene estadísticas del pool
     */
    public String getStats() {
        return String.format("Pool Stats - Activas: %d/%d, Disponibles: %d", 
                activeConnections, maxConnections, pool.size());
    }
}