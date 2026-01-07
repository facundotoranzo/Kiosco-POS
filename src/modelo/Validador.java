package modelo;

import java.util.regex.Pattern;

/**
 * Clase utilitaria para validación de datos de entrada
 */
public class Validador {
    
    // Patrones de validación
    private static final Pattern PATTERN_NOMBRE = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s\\-\\.\\p{So}\\p{Sc}\\p{Sk}\\p{Sm}]{1,100}$");
    private static final Pattern PATTERN_USUARIO = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern PATTERN_TELEFONO = Pattern.compile("^[0-9\\-\\+\\s\\(\\)]{7,20}$");
    private static final Pattern PATTERN_CUIT = Pattern.compile("^[0-9]{11}$|^[0-9]{2}-[0-9]{8}-[0-9]{1}$");
    
    /**
     * Valida que un texto no esté vacío y no contenga solo espacios
     */
    public static boolean esTextoValido(String texto) {
        return texto != null && !texto.trim().isEmpty();
    }
    
    /**
     * Valida nombre de producto o proveedor
     */
    public static boolean esNombreValido(String nombre) {
        if (!esTextoValido(nombre)) return false;
        return PATTERN_NOMBRE.matcher(nombre.trim()).matches();
    }
    
    /**
     * Valida nombre de usuario
     */
    public static boolean esUsuarioValido(String usuario) {
        if (!esTextoValido(usuario)) return false;
        return PATTERN_USUARIO.matcher(usuario.trim()).matches();
    }
    
    /**
     * Valida que un precio sea positivo
     */
    public static boolean esPrecioValido(double precio) {
        return precio > 0 && precio <= 999999.99;
    }
    
    /**
     * Valida que un stock sea no negativo
     */
    public static boolean esStockValido(int stock) {
        return stock >= 0 && stock <= 999999;
    }
    
    /**
     * Valida que un código de producto sea válido
     */
    public static boolean esCodigoValido(long codigo) {
        return codigo > 0 && codigo <= 9999999999L; // Máximo 10 dígitos
    }
    
    /**
     * Valida contraseña (mínimo 4 caracteres)
     */
    public static boolean esPasswordValida(String password) {
        return esTextoValido(password) && password.trim().length() >= 4;
    }
    
    /**
     * Valida teléfono
     */
    public static boolean esTelefonoValido(String telefono) {
        if (!esTextoValido(telefono)) return true; // Teléfono es opcional
        return PATTERN_TELEFONO.matcher(telefono.trim()).matches();
    }
    
    /**
     * Valida CUIT
     */
    public static boolean esCuitValido(String cuit) {
        if (!esTextoValido(cuit)) return true; // CUIT es opcional
        return PATTERN_CUIT.matcher(cuit.trim()).matches();
    }
    
    /**
     * Valida rol de usuario
     */
    public static boolean esRolValido(String rol) {
        if (!esTextoValido(rol)) return false;
        String rolUpper = rol.trim().toUpperCase();
        return rolUpper.equals("ADMIN") || rolUpper.equals("EMPLEADO");
    }
    
    /**
     * Sanitiza texto para evitar problemas de seguridad
     */
    public static String sanitizarTexto(String texto) {
        if (texto == null) return "";
        return texto.trim()
                   .replace("'", "''")  // Escape de comillas simples
                   .replace("\"", "\\\""); // Escape de comillas dobles
    }
    
    /**
     * Valida y sanitiza entrada de texto
     */
    public static String validarYSanitizar(String texto, int maxLength) {
        if (!esTextoValido(texto)) return null;
        String sanitizado = sanitizarTexto(texto);
        if (sanitizado.length() > maxLength) {
            sanitizado = sanitizado.substring(0, maxLength);
        }
        return sanitizado;
    }
}