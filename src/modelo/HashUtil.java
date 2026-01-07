package modelo;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilidad para hash seguro de contraseñas usando SHA-256 con salt
 */
public class HashUtil {
    
    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    
    /**
     * Genera un hash seguro de la contraseña con salt aleatorio
     * @param password Contraseña en texto plano
     * @return Hash en formato "salt:hash" codificado en Base64
     */
    public static String hashPassword(String password) {
        try {
            // Generar salt aleatorio
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Crear hash con salt
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes("UTF-8"));
            
            // Codificar salt y hash en Base64
            String saltBase64 = Base64.getEncoder().encodeToString(salt);
            String hashBase64 = Base64.getEncoder().encodeToString(hashedPassword);
            
            return saltBase64 + ":" + hashBase64;
            
        } catch (Exception e) {
            throw new RuntimeException("Error al generar hash de contraseña", e);
        }
    }
    
    /**
     * Verifica si una contraseña coincide con el hash almacenado
     * @param password Contraseña en texto plano
     * @param storedHash Hash almacenado en formato "salt:hash"
     * @return true si la contraseña es correcta
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Separar salt y hash
            String[] parts = storedHash.split(":");
            if (parts.length != 2) {
                return false;
            }
            
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] storedHashBytes = Base64.getDecoder().decode(parts[1]);
            
            // Generar hash con el mismo salt
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes("UTF-8"));
            
            // Comparar hashes
            return MessageDigest.isEqual(storedHashBytes, hashedPassword);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verifica si un hash está en formato antiguo (texto plano)
     * @param hash Hash a verificar
     * @return true si es texto plano (no contiene ":")
     */
    public static boolean isPlainText(String hash) {
        return hash != null && !hash.contains(":");
    }
}