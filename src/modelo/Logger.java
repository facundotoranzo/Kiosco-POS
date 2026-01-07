package modelo;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Sistema de logging mejorado con auditoría y seguridad
 * Incluye rotación de logs, niveles de seguridad y auditoría de acciones críticas
 */
public class Logger {
    
    private static final String LOG_FILE = "kiosco.log";
    private static final String AUDIT_FILE = "auditoria.log";
    private static final String SECURITY_FILE = "seguridad.log";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final long MAX_LOG_SIZE = 10 * 1024 * 1024; // 10MB
    
    public enum Level {
        INFO, WARN, ERROR, DEBUG, AUDIT, SECURITY
    }
    
    /**
     * MEJORADO: Log principal con rotación automática
     */
    public static void log(Level level, String message) {
        log(level, message, null);
    }
    
    /**
     * MEJORADO: Log con manejo de excepciones y rotación
     */
    public static void log(Level level, String message, Throwable throwable) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String usuario = obtenerUsuarioActual();
        String logEntry = String.format("[%s] [%s] %s: %s", timestamp, usuario, level, message);
        
        if (throwable != null) {
            logEntry += " - " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
        }
        
        // Escribir a consola (solo en desarrollo)
        if (level == Level.ERROR || level == Level.SECURITY) {
            System.out.println(logEntry);
        }
        
        // Determinar archivo de destino
        String archivo = determinarArchivoLog(level);
        
        // Verificar rotación de logs
        verificarRotacionLog(archivo);
        
        // Escribir a archivo
        escribirLog(archivo, logEntry, throwable);
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Auditoría de acciones críticas
     */
    public static void auditoria(String accion, String detalles) {
        String mensaje = String.format("ACCION: %s | DETALLES: %s", accion, detalles);
        log(Level.AUDIT, mensaje);
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Log de seguridad
     */
    public static void seguridad(String evento, String detalles) {
        String mensaje = String.format("EVENTO_SEGURIDAD: %s | DETALLES: %s", evento, detalles);
        log(Level.SECURITY, mensaje);
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Log de venta con detalles completos
     */
    public static void logVenta(double total, String medioPago, int cantidadItems, int idCaja) {
        String detalles = String.format("Total: $%.2f, Medio: %s, Items: %d, Caja: %d", 
                                       total, medioPago, cantidadItems, idCaja);
        auditoria("VENTA_PROCESADA", detalles);
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Log de acceso de usuario
     */
    public static void logAcceso(String usuario, String accion, boolean exitoso) {
        String resultado = exitoso ? "EXITOSO" : "FALLIDO";
        String detalles = String.format("Usuario: %s, Resultado: %s", usuario, resultado);
        
        if (exitoso) {
            auditoria("LOGIN_" + accion, detalles);
        } else {
            seguridad("INTENTO_ACCESO_FALLIDO", detalles);
        }
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Log de cambios en productos
     */
    public static void logCambioProducto(String accion, long codigo, String nombre, String cambios) {
        String detalles = String.format("Codigo: %d, Producto: %s, Cambios: %s", codigo, nombre, cambios);
        auditoria("PRODUCTO_" + accion, detalles);
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Log de operaciones de caja
     */
    public static void logOperacionCaja(String operacion, int idCaja, double monto) {
        String detalles = String.format("Caja: %d, Monto: $%.2f", idCaja, monto);
        auditoria("CAJA_" + operacion, detalles);
    }
    
    // Métodos de conveniencia existentes
    public static void info(String message) {
        log(Level.INFO, message);
    }
    
    public static void warn(String message) {
        log(Level.WARN, message);
    }
    
    public static void error(String message) {
        log(Level.ERROR, message);
    }
    
    public static void error(String message, Throwable throwable) {
        log(Level.ERROR, message, throwable);
    }
    
    public static void debug(String message) {
        log(Level.DEBUG, message);
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Obtener usuario actual de forma segura
     */
    private static String obtenerUsuarioActual() {
        try {
            String usuario = Sesion.getUsuario();
            return usuario != null ? usuario : "SISTEMA";
        } catch (Exception e) {
            return "DESCONOCIDO";
        }
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Determinar archivo según nivel de log
     */
    private static String determinarArchivoLog(Level level) {
        switch (level) {
            case AUDIT:
                return AUDIT_FILE;
            case SECURITY:
                return SECURITY_FILE;
            default:
                return LOG_FILE;
        }
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Verificar si necesita rotación de logs
     */
    private static void verificarRotacionLog(String archivo) {
        try {
            File file = new File(archivo);
            if (file.exists() && file.length() > MAX_LOG_SIZE) {
                rotarLog(archivo);
            }
        } catch (Exception e) {
            System.err.println("Error verificando rotación de log: " + e.getMessage());
        }
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Rotar logs cuando son muy grandes
     */
    private static void rotarLog(String archivo) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String archivoBackup = archivo.replace(".log", "_" + timestamp + ".log");
            
            Files.move(Paths.get(archivo), Paths.get(archivoBackup));
            
            // Crear nuevo archivo
            new File(archivo).createNewFile();
            
            info("Log rotado: " + archivo + " -> " + archivoBackup);
            
        } catch (Exception e) {
            System.err.println("Error rotando log: " + e.getMessage());
        }
    }
    
    /**
     * MEJORADO: Escribir log de forma segura
     */
    private static void escribirLog(String archivo, String logEntry, Throwable throwable) {
        try (FileWriter writer = new FileWriter(archivo, true)) {
            writer.write(logEntry + System.lineSeparator());
            if (throwable != null) {
                writer.write("Stack trace: " + getStackTrace(throwable) + System.lineSeparator());
            }
        } catch (IOException e) {
            System.err.println("Error escribiendo log en " + archivo + ": " + e.getMessage());
        }
    }
    
    /**
     * MEJORADO: Stack trace más legible
     */
    private static String getStackTrace(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        sb.append(throwable.getClass().getSimpleName()).append(": ").append(throwable.getMessage()).append(" | ");
        
        StackTraceElement[] elements = throwable.getStackTrace();
        int maxElements = Math.min(5, elements.length); // Limitar a 5 elementos más relevantes
        
        for (int i = 0; i < maxElements; i++) {
            StackTraceElement element = elements[i];
            if (element.getClassName().startsWith("modelo.") || element.getClassName().startsWith("controlador.")) {
                sb.append(element.getClassName()).append(".").append(element.getMethodName())
                  .append(":").append(element.getLineNumber()).append(" | ");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Limpiar logs antiguos (mantener últimos 30 días)
     */
    public static void limpiarLogsAntiguos() {
        try {
            File directorio = new File(".");
            File[] archivos = directorio.listFiles((dir, name) -> 
                name.matches(".*_\\d{8}_\\d{6}\\.log"));
            
            if (archivos != null) {
                long tiempoLimite = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000); // 30 días
                
                for (File archivo : archivos) {
                    if (archivo.lastModified() < tiempoLimite) {
                        if (archivo.delete()) {
                            info("Log antiguo eliminado: " + archivo.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            error("Error limpiando logs antiguos", e);
        }
    }
}