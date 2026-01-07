package vista;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Sistema de notificaciones Toast modernas y elegantes
 */
public class ToastNotification {
    
    public enum TipoToast {
        EXITO(new Color(40, 167, 69), "✓"),
        ERROR(new Color(220, 53, 69), "✗"),
        ADVERTENCIA(new Color(255, 193, 7), "⚠"),
        INFO(new Color(0, 123, 255), "ℹ");
        
        public final Color color;
        public final String icono;
        
        TipoToast(Color color, String icono) {
            this.color = color;
            this.icono = icono;
        }
    }
    
    /**
     * Muestra una notificación toast elegante
     */
    public static void mostrar(Component parent, String mensaje, TipoToast tipo) {
        mostrar(parent, mensaje, tipo, 3000); // 3 segundos por defecto
    }
    
    /**
     * Muestra una notificación toast con duración personalizada
     */
    public static void mostrar(Component parent, String mensaje, TipoToast tipo, int duracionMs) {
        SwingUtilities.invokeLater(() -> {
            // Obtener ventana padre
            Window ventanaPadre = SwingUtilities.getWindowAncestor(parent);
            if (ventanaPadre == null) return;
            
            // Crear ventana toast
            JWindow toast = new JWindow(ventanaPadre);
            toast.setAlwaysOnTop(true);
            
            // Panel principal con diseño moderno
            JPanel panel = new JPanel(new BorderLayout(10, 0));
            panel.setBackground(tipo.color);
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tipo.color.darker(), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
            ));
            
            // Icono
            JLabel lblIcono = new JLabel(tipo.icono);
            lblIcono.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblIcono.setForeground(Color.WHITE);
            
            // Mensaje
            JLabel lblMensaje = new JLabel(mensaje);
            lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblMensaje.setForeground(Color.WHITE);
            
            panel.add(lblIcono, BorderLayout.WEST);
            panel.add(lblMensaje, BorderLayout.CENTER);
            
            toast.add(panel);
            toast.pack();
            
            // Posicionar en esquina superior derecha
            Rectangle bounds = ventanaPadre.getBounds();
            int x = bounds.x + bounds.width - toast.getWidth() - 20;
            int y = bounds.y + 60;
            toast.setLocation(x, y);
            
            // Animación de entrada (fade in)
            toast.setOpacity(0.0f);
            toast.setVisible(true);
            
            Timer fadeIn = new Timer(20, null);
            fadeIn.addActionListener(new ActionListener() {
                float opacity = 0.0f;
                public void actionPerformed(ActionEvent e) {
                    opacity += 0.05f;
                    if (opacity >= 0.95f) {
                        opacity = 0.95f;
                        fadeIn.stop();
                        
                        // Timer para mantener visible
                        Timer mantener = new Timer(duracionMs, ev -> {
                            // Animación de salida (fade out)
                            Timer fadeOut = new Timer(20, null);
                            fadeOut.addActionListener(new ActionListener() {
                                float opacidadSalida = 0.95f;
                                public void actionPerformed(ActionEvent e) {
                                    opacidadSalida -= 0.05f;
                                    if (opacidadSalida <= 0.0f) {
                                        fadeOut.stop();
                                        toast.dispose();
                                    } else {
                                        toast.setOpacity(opacidadSalida);
                                    }
                                }
                            });
                            fadeOut.start();
                        });
                        mantener.setRepeats(false);
                        mantener.start();
                    }
                    toast.setOpacity(opacity);
                }
            });
            fadeIn.start();
        });
    }
    
    // Métodos de conveniencia
    public static void exito(Component parent, String mensaje) {
        mostrar(parent, mensaje, TipoToast.EXITO);
    }
    
    public static void error(Component parent, String mensaje) {
        mostrar(parent, mensaje, TipoToast.ERROR);
    }
    
    public static void advertencia(Component parent, String mensaje) {
        mostrar(parent, mensaje, TipoToast.ADVERTENCIA);
    }
    
    public static void warning(Component parent, String mensaje) {
        mostrar(parent, mensaje, TipoToast.ADVERTENCIA);
    }
    
    public static void info(Component parent, String mensaje) {
        mostrar(parent, mensaje, TipoToast.INFO);
    }
    
    /**
     * NUEVA FUNCIONALIDAD: Toast con acción personalizada
     */
    public static void mostrarConAccion(Component parent, String mensaje, String textoBoton, Runnable accion) {
        SwingUtilities.invokeLater(() -> {
            Window ventanaPadre = SwingUtilities.getWindowAncestor(parent);
            if (ventanaPadre == null) return;
            
            JWindow toast = new JWindow(ventanaPadre);
            toast.setAlwaysOnTop(true);
            
            JPanel panel = new JPanel(new BorderLayout(10, 0));
            panel.setBackground(TipoToast.INFO.color);
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TipoToast.INFO.color.darker(), 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
            ));
            
            JLabel lblMensaje = new JLabel(mensaje);
            lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblMensaje.setForeground(Color.WHITE);
            
            JButton btnAccion = new JButton(textoBoton);
            btnAccion.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnAccion.setBackground(Color.WHITE);
            btnAccion.setForeground(TipoToast.INFO.color);
            btnAccion.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            btnAccion.setFocusPainted(false);
            btnAccion.addActionListener(e -> {
                toast.dispose();
                if (accion != null) accion.run();
            });
            
            panel.add(lblMensaje, BorderLayout.CENTER);
            panel.add(btnAccion, BorderLayout.EAST);
            
            toast.add(panel);
            toast.pack();
            
            Rectangle bounds = ventanaPadre.getBounds();
            int x = bounds.x + bounds.width - toast.getWidth() - 20;
            int y = bounds.y + 60;
            toast.setLocation(x, y);
            
            toast.setOpacity(0.95f);
            toast.setVisible(true);
            
            // Auto-cerrar después de 10 segundos
            Timer autoClose = new Timer(10000, e -> toast.dispose());
            autoClose.setRepeats(false);
            autoClose.start();
        });
    }
}