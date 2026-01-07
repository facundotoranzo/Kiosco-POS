package modelo;

import java.io.*;
import java.util.Properties;

/**
 * Gestor de sonidos del sistema
 * Reproduce sonidos de confirmación configurables
 */
public class SonidoManager {
    
    private static boolean sonidoActivo = true;
    
    static {
        cargarConfiguracion();
    }
    
    /**
     * Carga la configuración de sonido desde db_config.properties
     */
    private static void cargarConfiguracion() {
        try {
            File f = new File("db_config.properties");
            if (f.exists()) {
                Properties prop = new Properties();
                try (FileInputStream fis = new FileInputStream(f)) {
                    prop.load(fis);
                }
                sonidoActivo = prop.getProperty("sonido_venta", "true").equals("true");
            }
        } catch (Exception e) {
            Logger.error("Error cargando config de sonido", e);
        }
    }
    
    /**
     * Reproduce un beep de confirmación al completar una venta
     */
    public static void reproducirVentaExitosa() {
        if (!sonidoActivo) return;
        
        new Thread(() -> {
            try {
                // Beep del sistema - simple y efectivo
                java.awt.Toolkit.getDefaultToolkit().beep();
            } catch (Exception e) {
                // Silenciar errores de sonido
            }
        }).start();
    }
    
    /**
     * Reproduce un sonido de error
     */
    public static void reproducirError() {
        if (!sonidoActivo) return;
        
        new Thread(() -> {
            try {
                // Doble beep para error
                java.awt.Toolkit.getDefaultToolkit().beep();
                Thread.sleep(150);
                java.awt.Toolkit.getDefaultToolkit().beep();
            } catch (Exception e) {
                // Silenciar errores
            }
        }).start();
    }
    
    /**
     * Activa o desactiva los sonidos
     */
    public static void setSonidoActivo(boolean activo) {
        sonidoActivo = activo;
        guardarConfiguracion();
    }
    
    /**
     * Verifica si el sonido está activo
     */
    public static boolean isSonidoActivo() {
        return sonidoActivo;
    }
    
    /**
     * Guarda la configuración de sonido
     */
    private static void guardarConfiguracion() {
        try {
            File f = new File("db_config.properties");
            Properties prop = new Properties();
            
            if (f.exists()) {
                try (FileInputStream fis = new FileInputStream(f)) {
                    prop.load(fis);
                }
            }
            
            prop.setProperty("sonido_venta", sonidoActivo ? "true" : "false");
            
            try (FileOutputStream fos = new FileOutputStream(f)) {
                prop.store(fos, "Configuracion Kiosco");
            }
        } catch (Exception e) {
            Logger.error("Error guardando config de sonido", e);
        }
    }
    
    /**
     * Recarga la configuración (útil después de cambios en ajustes)
     */
    public static void recargarConfiguracion() {
        cargarConfiguracion();
    }
}
