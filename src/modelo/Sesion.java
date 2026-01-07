package modelo;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Sesion {
    // Variables en memoria con sincronización
    private static volatile String usuario = "Desconocido";
    private static volatile String rol = "EMPLEADO";
    
    // Lock para acceso thread-safe
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static final String ARCHIVO_SESION = "session.properties";

    // Getters thread-safe
    public static String getUsuario() {
        lock.readLock().lock();
        try {
            return usuario;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public static String getRol() {
        lock.readLock().lock();
        try {
            return rol;
        } finally {
            lock.readLock().unlock();
        }
    }

    // 1. GUARDAR SESIÓN (Crear el archivo)
    public static void guardarSesion(String user, String role) {
        if (!Validador.esTextoValido(user) || !Validador.esRolValido(role)) {
            Logger.warn("Intento de guardar sesión con datos inválidos");
            return;
        }
        
        lock.writeLock().lock();
        try {
            usuario = user;
            rol = role;
            
            try (FileOutputStream fos = new FileOutputStream(ARCHIVO_SESION)) {
                Properties prop = new Properties();
                prop.setProperty("usuario", user);
                prop.setProperty("rol", role);
                prop.setProperty("timestamp", String.valueOf(System.currentTimeMillis()));
                prop.store(fos, "Sesion Activa Kiosco");
                Logger.info("Sesión guardada para usuario: " + user);
            } catch (IOException e) {
                Logger.error("Error al guardar sesión", e);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    // 2. CARGAR SESIÓN (Leer el archivo al inicio)
    public static boolean recuperarSesion() {
        File f = new File(ARCHIVO_SESION);
        if (!f.exists()) return false; // No hay sesión guardada

        lock.writeLock().lock();
        try (FileInputStream fis = new FileInputStream(f)) {
            Properties prop = new Properties();
            prop.load(fis);
            
            String sessionUser = prop.getProperty("usuario");
            String sessionRole = prop.getProperty("rol");
            String timestamp = prop.getProperty("timestamp");
            
            // Validar que los datos sean válidos
            if (!Validador.esTextoValido(sessionUser) || !Validador.esRolValido(sessionRole)) {
                Logger.warn("Sesión guardada contiene datos inválidos");
                return false;
            }
            
            // Opcional: Validar que la sesión no sea muy antigua (24 horas)
            if (timestamp != null) {
                try {
                    long sessionTime = Long.parseLong(timestamp);
                    long currentTime = System.currentTimeMillis();
                    long hoursElapsed = (currentTime - sessionTime) / (1000 * 60 * 60);
                    
                    if (hoursElapsed > 24) {
                        Logger.info("Sesión expirada (más de 24 horas)");
                        cerrarSesion();
                        return false;
                    }
                } catch (NumberFormatException e) {
                    Logger.warn("Timestamp de sesión inválido");
                }
            }
            
            usuario = sessionUser;
            rol = sessionRole;
            
            Logger.info("Sesión recuperada para usuario: " + sessionUser);
            return true;
            
        } catch (Exception e) {
            Logger.error("Error al recuperar sesión", e);
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // 3. CERRAR SESIÓN (Borrar el archivo)
    public static void cerrarSesion() {
        lock.writeLock().lock();
        try {
            String oldUser = usuario;
            usuario = "Desconocido";
            rol = "EMPLEADO";
            
            try {
                File sessionFile = new File(ARCHIVO_SESION);
                if (sessionFile.exists()) {
                    sessionFile.delete();
                }
                Logger.info("Sesión cerrada para usuario: " + oldUser);
            } catch (Exception e) {
                Logger.error("Error al cerrar sesión", e);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}