package modelo;

import java.io.*;
import java.util.Properties;
import java.awt.event.KeyEvent;

/**
 * Gestor de atajos de teclado configurables
 * Permite al usuario personalizar las teclas de acceso rápido
 */
public class AtajosManager {
    
    private static final String ARCHIVO_ATAJOS = "atajos.properties";
    private static Properties atajos = new Properties();
    
    // Valores por defecto
    private static final int DEFAULT_COBRAR = KeyEvent.VK_F1;
    private static final int DEFAULT_MANUAL = KeyEvent.VK_F2;
    private static final int DEFAULT_BUSCAR = KeyEvent.VK_F3;
    private static final int DEFAULT_NUEVO_PRODUCTO = KeyEvent.VK_F4;
    private static final int DEFAULT_VER_CAJA = KeyEvent.VK_F5;
    
    static {
        cargarAtajos();
    }
    
    /**
     * Carga los atajos desde el archivo de configuración
     */
    public static void cargarAtajos() {
        File f = new File(ARCHIVO_ATAJOS);
        if (f.exists()) {
            try (FileInputStream fis = new FileInputStream(f)) {
                atajos.load(fis);
                Logger.debug("Atajos de teclado cargados");
            } catch (Exception e) {
                Logger.error("Error cargando atajos", e);
                cargarDefaults();
            }
        } else {
            cargarDefaults();
            guardarAtajos();
        }
    }
    
    /**
     * Carga los valores por defecto
     */
    private static void cargarDefaults() {
        atajos.setProperty("cobrar", String.valueOf(DEFAULT_COBRAR));
        atajos.setProperty("manual", String.valueOf(DEFAULT_MANUAL));
        atajos.setProperty("buscar", String.valueOf(DEFAULT_BUSCAR));
        atajos.setProperty("nuevo_producto", String.valueOf(DEFAULT_NUEVO_PRODUCTO));
        atajos.setProperty("ver_caja", String.valueOf(DEFAULT_VER_CAJA));
    }
    
    /**
     * Guarda los atajos en el archivo de configuración
     */
    public static void guardarAtajos() {
        try (FileOutputStream fos = new FileOutputStream(ARCHIVO_ATAJOS)) {
            atajos.store(fos, "Atajos de Teclado - Kiosco Manager");
            Logger.info("Atajos de teclado guardados");
        } catch (Exception e) {
            Logger.error("Error guardando atajos", e);
        }
    }
    
    // Getters para cada atajo
    public static int getAtajoCobrar() {
        return Integer.parseInt(atajos.getProperty("cobrar", String.valueOf(DEFAULT_COBRAR)));
    }
    
    public static int getAtajoManual() {
        return Integer.parseInt(atajos.getProperty("manual", String.valueOf(DEFAULT_MANUAL)));
    }
    
    public static int getAtajoBuscar() {
        return Integer.parseInt(atajos.getProperty("buscar", String.valueOf(DEFAULT_BUSCAR)));
    }
    
    public static int getAtajoNuevoProducto() {
        return Integer.parseInt(atajos.getProperty("nuevo_producto", String.valueOf(DEFAULT_NUEVO_PRODUCTO)));
    }
    
    public static int getAtajoVerCaja() {
        return Integer.parseInt(atajos.getProperty("ver_caja", String.valueOf(DEFAULT_VER_CAJA)));
    }
    
    // Setters para cada atajo
    public static void setAtajoCobrar(int keyCode) {
        atajos.setProperty("cobrar", String.valueOf(keyCode));
    }
    
    public static void setAtajoManual(int keyCode) {
        atajos.setProperty("manual", String.valueOf(keyCode));
    }
    
    public static void setAtajoBuscar(int keyCode) {
        atajos.setProperty("buscar", String.valueOf(keyCode));
    }
    
    public static void setAtajoNuevoProducto(int keyCode) {
        atajos.setProperty("nuevo_producto", String.valueOf(keyCode));
    }
    
    public static void setAtajoVerCaja(int keyCode) {
        atajos.setProperty("ver_caja", String.valueOf(keyCode));
    }
    
    /**
     * Restaura todos los atajos a sus valores por defecto
     */
    public static void restaurarDefaults() {
        cargarDefaults();
        guardarAtajos();
        Logger.info("Atajos restaurados a valores por defecto");
    }
    
    /**
     * Obtiene el nombre legible de una tecla
     */
    public static String getNombreTecla(int keyCode) {
        return KeyEvent.getKeyText(keyCode);
    }
    
    /**
     * Verifica si un keyCode ya está en uso por otro atajo
     */
    public static String atajoEnUso(int keyCode, String excluir) {
        if (!excluir.equals("cobrar") && getAtajoCobrar() == keyCode) return "Cobrar";
        if (!excluir.equals("manual") && getAtajoManual() == keyCode) return "Manual";
        if (!excluir.equals("buscar") && getAtajoBuscar() == keyCode) return "Buscar";
        if (!excluir.equals("nuevo_producto") && getAtajoNuevoProducto() == keyCode) return "Nuevo Producto";
        if (!excluir.equals("ver_caja") && getAtajoVerCaja() == keyCode) return "Ver Caja";
        return null;
    }
}
