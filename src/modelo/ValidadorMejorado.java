package modelo;

import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

/**
 * Validador mejorado con funcionalidades avanzadas de seguridad y UX
 * Proporciona validaciones inteligentes y mensajes de error descriptivos
 */
public class ValidadorMejorado {
    
    // Patrones de validación
    private static final Pattern PATRON_CODIGO = Pattern.compile("^[0-9]{1,15}$");
    private static final Pattern PATRON_NOMBRE_PRODUCTO = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s\\-\\.]{2,50}$");
    private static final Pattern PATRON_USUARIO = Pattern.compile("^[a-zA-Z0-9]{3,20}$");
    
    /**
     * NUEVA FUNCIONALIDAD: Validación completa de producto
     * @param codigo Código del producto
     * @param nombre Nombre del producto
     * @param precio Precio como string
     * @param stock Stock como string
     * @return Resultado de validación con errores detallados
     */
    public static ResultadoValidacion validarProducto(String codigo, String nombre, String precio, String stock) {
        List<String> errores = new ArrayList<>();
        
        // Validar código
        if (codigo == null || codigo.trim().isEmpty()) {
            errores.add("❌ El código es obligatorio");
        } else if (!PATRON_CODIGO.matcher(codigo.trim()).matches()) {
            errores.add("❌ Código inválido (solo números, máximo 15 dígitos)");
        }
        
        // Validar nombre
        if (nombre == null || nombre.trim().isEmpty()) {
            errores.add("❌ El nombre es obligatorio");
        } else if (nombre.trim().length() < 2) {
            errores.add("❌ El nombre debe tener al menos 2 caracteres");
        } else if (nombre.trim().length() > 50) {
            errores.add("❌ El nombre no puede exceder 50 caracteres");
        } else if (!PATRON_NOMBRE_PRODUCTO.matcher(nombre.trim()).matches()) {
            errores.add("❌ Nombre contiene caracteres no permitidos");
        }
        
        // Validar precio
        if (precio == null || precio.trim().isEmpty()) {
            errores.add("❌ El precio es obligatorio");
        } else {
            try {
                double precioNum = Double.parseDouble(precio.replace(",", "."));
                if (precioNum <= 0) {
                    errores.add("❌ El precio debe ser mayor a 0");
                } else if (precioNum > 999999) {
                    errores.add("❌ El precio es demasiado alto (máximo $999,999)");
                }
            } catch (NumberFormatException e) {
                errores.add("❌ Precio inválido (use formato: 123.45)");
            }
        }
        
        // Validar stock (solo si no es LITE)
        if (!ConexionDB.licencia.equals("LITE")) {
            if (stock == null || stock.trim().isEmpty()) {
                errores.add("❌ El stock es obligatorio");
            } else {
                try {
                    int stockNum = Integer.parseInt(stock.trim());
                    if (stockNum < 0) {
                        errores.add("❌ El stock no puede ser negativo");
                    } else if (stockNum > 99999) {
                        errores.add("❌ Stock demasiado alto (máximo 99,999)");
                    }
                } catch (NumberFormatException e) {
                    errores.add("❌ Stock inválido (solo números enteros)");
                }
            }
        }
        
        return new ResultadoValidacion(errores.isEmpty(), errores);
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Validación de usuario con seguridad mejorada
     */
    public static ResultadoValidacion validarUsuario(String usuario, String password, String rol) {
        List<String> errores = new ArrayList<>();
        
        // Validar usuario
        if (usuario == null || usuario.trim().isEmpty()) {
            errores.add("❌ El nombre de usuario es obligatorio");
        } else if (usuario.trim().length() < 3) {
            errores.add("❌ El usuario debe tener al menos 3 caracteres");
        } else if (usuario.trim().length() > 20) {
            errores.add("❌ El usuario no puede exceder 20 caracteres");
        } else if (!PATRON_USUARIO.matcher(usuario.trim()).matches()) {
            errores.add("❌ Usuario solo puede contener letras y números");
        }
        
        // Validar contraseña con criterios de seguridad
        if (password == null || password.isEmpty()) {
            errores.add("❌ La contraseña es obligatoria");
        } else {
            List<String> criteriosPassword = validarSeguridadPassword(password);
            errores.addAll(criteriosPassword);
        }
        
        // Validar rol
        if (rol == null || rol.trim().isEmpty()) {
            errores.add("❌ El rol es obligatorio");
        } else if (!rol.equals("ADMIN") && !rol.equals("EMPLEADO")) {
            errores.add("❌ Rol inválido (debe ser ADMIN o EMPLEADO)");
        }
        
        return new ResultadoValidacion(errores.isEmpty(), errores);
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Validación de seguridad de contraseña
     */
    private static List<String> validarSeguridadPassword(String password) {
        List<String> errores = new ArrayList<>();
        
        if (password.length() < 4) {
            errores.add("❌ La contraseña debe tener al menos 4 caracteres");
        }
        
        if (password.length() > 50) {
            errores.add("❌ La contraseña no puede exceder 50 caracteres");
        }
        
        // Verificar contraseñas comunes inseguras
        String[] passwordsInseguros = {
            "1234", "admin", "password", "123456", "qwerty", 
            "abc123", "admin123", "root", "user", "test"
        };
        
        for (String inseguro : passwordsInseguros) {
            if (password.toLowerCase().equals(inseguro)) {
                errores.add("⚠️ Contraseña muy común, considere usar una más segura");
                break;
            }
        }
        
        return errores;
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Validación de entrada de dinero
     */
    public static ResultadoValidacion validarMonto(String monto, String concepto) {
        List<String> errores = new ArrayList<>();
        
        if (monto == null || monto.trim().isEmpty()) {
            errores.add("❌ El " + concepto + " es obligatorio");
        } else {
            try {
                double montoNum = Double.parseDouble(monto.replace(",", "."));
                if (montoNum < 0) {
                    errores.add("❌ El " + concepto + " no puede ser negativo");
                } else if (montoNum > 9999999) {
                    errores.add("❌ El " + concepto + " es demasiado alto");
                }
            } catch (NumberFormatException e) {
                errores.add("❌ " + concepto + " inválido (use formato: 123.45)");
            }
        }
        
        return new ResultadoValidacion(errores.isEmpty(), errores);
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Sanitización de entrada de texto
     */
    public static String sanitizarTexto(String texto) {
        if (texto == null) return "";
        
        return texto.trim()
                   .replaceAll("[<>\"'&]", "") // Remover caracteres peligrosos
                   .replaceAll("\\s+", " ");   // Normalizar espacios
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Validación de código de barras
     */
    public static boolean esCodigoBarrasValido(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) return false;
        
        // Códigos de barras comunes: 8, 12, 13 dígitos
        String codigoLimpio = codigo.trim();
        return codigoLimpio.matches("^[0-9]{8}$") || 
               codigoLimpio.matches("^[0-9]{12}$") || 
               codigoLimpio.matches("^[0-9]{13}$");
    }
    
    /**
     * Clase para encapsular resultados de validación
     */
    public static class ResultadoValidacion {
        private final boolean valido;
        private final List<String> errores;
        
        public ResultadoValidacion(boolean valido, List<String> errores) {
            this.valido = valido;
            this.errores = errores;
        }
        
        public boolean esValido() {
            return valido;
        }
        
        public List<String> getErrores() {
            return errores;
        }
        
        public String getMensajeErrores() {
            if (errores.isEmpty()) return "";
            
            StringBuilder sb = new StringBuilder();
            sb.append("Errores de validación:\n\n");
            for (String error : errores) {
                sb.append(error).append("\n");
            }
            return sb.toString();
        }
        
        public String getPrimerError() {
            return errores.isEmpty() ? "" : errores.get(0);
        }
    }
}