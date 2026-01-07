package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Efectos visuales y animaciones para mejorar la UX
 */
public class EfectosVisuales {
    
    /**
     * Agrega efecto hover elegante a un botón
     */
    public static void agregarHoverEffect(JButton boton) {
        Color colorOriginal = boton.getBackground();
        Color colorHover = colorOriginal.brighter();
        
        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Animación suave de entrada
                animarColorBoton(boton, colorOriginal, colorHover, 150);
                boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                // Animación suave de salida
                animarColorBoton(boton, boton.getBackground(), colorOriginal, 150);
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                // Efecto de "click" - color más oscuro momentáneamente
                Color colorClick = colorOriginal.darker();
                boton.setBackground(colorClick);
                
                // Volver al color hover después de 100ms
                Timer timer = new Timer(100, ev -> {
                    boton.setBackground(colorHover);
                });
                timer.setRepeats(false);
                timer.start();
            }
        });
    }
    
    /**
     * Anima suavemente el cambio de color de un botón
     */
    private static void animarColorBoton(JButton boton, Color desde, Color hacia, int duracionMs) {
        Timer timer = new Timer(20, null);
        final int pasos = duracionMs / 20;
        final int[] paso = {0};
        
        timer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                paso[0]++;
                float progreso = (float) paso[0] / pasos;
                
                if (progreso >= 1.0f) {
                    boton.setBackground(hacia);
                    timer.stop();
                } else {
                    // Interpolación de color
                    int r = (int) (desde.getRed() + (hacia.getRed() - desde.getRed()) * progreso);
                    int g = (int) (desde.getGreen() + (hacia.getGreen() - desde.getGreen()) * progreso);
                    int b = (int) (desde.getBlue() + (hacia.getBlue() - desde.getBlue()) * progreso);
                    
                    boton.setBackground(new Color(r, g, b));
                }
            }
        });
        timer.start();
    }
    
    /**
     * Agrega efecto hover a filas de tabla
     */
    public static void agregarHoverTabla(JTable tabla) {
        Color colorOriginal = tabla.getBackground();
        Color colorHover = new Color(colorOriginal.getRed() + 10, 
                                   colorOriginal.getGreen() + 10, 
                                   colorOriginal.getBlue() + 10);
        
        tabla.addMouseMotionListener(new MouseAdapter() {
            private int filaAnterior = -1;
            
            @Override
            public void mouseMoved(MouseEvent e) {
                int fila = tabla.rowAtPoint(e.getPoint());
                if (fila != filaAnterior) {
                    tabla.repaint();
                    filaAnterior = fila;
                }
            }
        });
    }
    
    /**
     * Animación de feedback al agregar producto al carrito
     */
    public static void mostrarFeedbackAgregar(JTable tablaCarrito, String nombreProducto) {
        // Crear label temporal de feedback
        JLabel feedback = new JLabel("✓ " + nombreProducto + " agregado");
        feedback.setFont(new Font("Segoe UI", Font.BOLD, 12));
        feedback.setForeground(new Color(40, 167, 69));
        feedback.setOpaque(true);
        feedback.setBackground(new Color(40, 167, 69, 30));
        feedback.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        // Posicionar sobre la tabla del carrito
        JScrollPane scrollPane = (JScrollPane) tablaCarrito.getParent().getParent();
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(scrollPane.getSize());
        
        // Agregar temporalmente
        Container parent = scrollPane.getParent();
        Component[] components = parent.getComponents();
        parent.removeAll();
        
        layeredPane.add(scrollPane, JLayeredPane.DEFAULT_LAYER);
        
        // Posicionar feedback
        feedback.setBounds(10, 10, 200, 30);
        layeredPane.add(feedback, JLayeredPane.POPUP_LAYER);
        
        parent.add(layeredPane);
        parent.revalidate();
        parent.repaint();
        
        // Animación de desvanecimiento
        Timer timer = new Timer(2000, e -> {
            // Restaurar estado original
            parent.removeAll();
            for (Component comp : components) {
                parent.add(comp);
            }
            parent.revalidate();
            parent.repaint();
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    /**
     * Animación del total cuando cambia
     */
    public static void animarCambioTotal(JLabel lblTotal, String nuevoTotal) {
        // Efecto de "pulso" en el total
        Font fuenteOriginal = lblTotal.getFont();
        Font fuenteGrande = new Font(fuenteOriginal.getName(), fuenteOriginal.getStyle(), fuenteOriginal.getSize() + 4);
        
        // Animación de crecimiento y vuelta
        Timer timer = new Timer(50, null);
        final boolean[] creciendo = {true};
        final int[] paso = {0};
        
        timer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                paso[0]++;
                
                if (creciendo[0]) {
                    if (paso[0] <= 3) {
                        int nuevoTamaño = fuenteOriginal.getSize() + paso[0];
                        lblTotal.setFont(new Font(fuenteOriginal.getName(), fuenteOriginal.getStyle(), nuevoTamaño));
                    } else {
                        creciendo[0] = false;
                        lblTotal.setText(nuevoTotal); // Cambiar el texto en el punto máximo
                    }
                } else {
                    if (paso[0] <= 6) {
                        int nuevoTamaño = fuenteOriginal.getSize() + (6 - paso[0]);
                        lblTotal.setFont(new Font(fuenteOriginal.getName(), fuenteOriginal.getStyle(), nuevoTamaño));
                    } else {
                        lblTotal.setFont(fuenteOriginal);
                        timer.stop();
                    }
                }
            }
        });
        timer.start();
    }
    
    /**
     * Efecto de carga elegante
     */
    public static JDialog mostrarCargando(Component parent, String mensaje) {
        Window window = SwingUtilities.getWindowAncestor(parent);
        JDialog dialogo;
        
        if (window instanceof Frame) {
            dialogo = new JDialog((Frame) window, "Procesando...", true);
        } else if (window instanceof Dialog) {
            dialogo = new JDialog((Dialog) window, "Procesando...", true);
        } else {
            dialogo = new JDialog((Frame) null, "Procesando...", true);
        }
        
        dialogo.setUndecorated(true);
        
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(33, 37, 43));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 123, 255), 2),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        
        // Spinner animado
        JLabel spinner = new JLabel("⟳");
        spinner.setFont(new Font("Segoe UI", Font.BOLD, 24));
        spinner.setForeground(new Color(0, 123, 255));
        spinner.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Animación del spinner
        Timer spinTimer = new Timer(100, e -> {
            String texto = spinner.getText();
            switch (texto) {
                case "⟳": spinner.setText("⟲"); break;
                case "⟲": spinner.setText("⟳"); break;
            }
        });
        spinTimer.start();
        
        // Mensaje
        JLabel lblMensaje = new JLabel(mensaje);
        lblMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMensaje.setForeground(Color.WHITE);
        lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
        
        panel.add(spinner, BorderLayout.CENTER);
        panel.add(lblMensaje, BorderLayout.SOUTH);
        
        dialogo.add(panel);
        dialogo.pack();
        dialogo.setLocationRelativeTo(parent);
        
        // Guardar referencia al timer en el panel (JDialog no tiene putClientProperty)
        panel.putClientProperty("spinTimer", spinTimer);
        panel.putClientProperty("dialog", dialogo);
        
        return dialogo;
    }
    
    /**
     * Cierra el diálogo de carga
     */
    public static void cerrarCargando(JDialog dialogo) {
        if (dialogo != null) {
            // Buscar el timer en el panel contenido
            Component[] components = dialogo.getContentPane().getComponents();
            for (Component comp : components) {
                if (comp instanceof JPanel) {
                    JPanel panel = (JPanel) comp;
                    Timer timer = (Timer) panel.getClientProperty("spinTimer");
                    if (timer != null) {
                        timer.stop();
                    }
                    break;
                }
            }
            dialogo.dispose();
        }
    }
}