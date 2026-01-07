package vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import modelo.AtajosManager;

/**
 * Diálogo para configurar los atajos de teclado
 */
public class DialogoAtajos extends JDialog {
    
    private JButton btnCobrar, btnManual, btnBuscar, btnNuevoProducto, btnVerCaja;
    private JButton btnRestaurar, btnGuardar, btnCancelar;
    
    private JButton botonCapturando = null;
    
    // Colores
    private final Color bgPanel = new Color(40, 44, 52);
    private final Color colorBoton = new Color(60, 65, 75);
    private final Color colorCapturando = new Color(100, 150, 255);
    
    public DialogoAtajos(JFrame parent) {
        super(parent, "Configurar Atajos de Teclado", true);
        setSize(450, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        crearInterfaz();
        cargarAtajosActuales();
    }
    
    private void crearInterfaz() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(bgPanel);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Título
        JLabel lblTitulo = new JLabel("Atajos de Teclado");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        
        JLabel lblSubtitulo = new JLabel("Haz clic en un botón y presiona la tecla deseada");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(new Color(150, 155, 165));
        
        JPanel pnlTitulo = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlTitulo.setOpaque(false);
        pnlTitulo.add(lblTitulo);
        pnlTitulo.add(lblSubtitulo);
        
        // Panel de atajos
        JPanel pnlAtajos = new JPanel(new GridLayout(5, 2, 15, 15));
        pnlAtajos.setOpaque(false);
        pnlAtajos.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        btnCobrar = crearBotonAtajo("cobrar");
        btnManual = crearBotonAtajo("manual");
        btnBuscar = crearBotonAtajo("buscar");
        btnNuevoProducto = crearBotonAtajo("nuevo_producto");
        btnVerCaja = crearBotonAtajo("ver_caja");
        
        pnlAtajos.add(crearLabel("Cobrar Venta:"));
        pnlAtajos.add(btnCobrar);
        pnlAtajos.add(crearLabel("Agregar Manual:"));
        pnlAtajos.add(btnManual);
        pnlAtajos.add(crearLabel("Enfocar Búsqueda:"));
        pnlAtajos.add(btnBuscar);
        pnlAtajos.add(crearLabel("Nuevo Producto:"));
        pnlAtajos.add(btnNuevoProducto);
        pnlAtajos.add(crearLabel("Ver Caja:"));
        pnlAtajos.add(btnVerCaja);
        
        // Panel de botones
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBotones.setOpaque(false);
        
        btnRestaurar = new JButton("Restaurar");
        btnRestaurar.setBackground(new Color(80, 80, 90));
        btnRestaurar.setForeground(Color.WHITE);
        btnRestaurar.setFocusPainted(false);
        
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(new Color(80, 80, 90));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        
        btnGuardar = new JButton("Guardar");
        btnGuardar.setBackground(new Color(46, 204, 113));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        
        pnlBotones.add(btnRestaurar);
        pnlBotones.add(btnCancelar);
        pnlBotones.add(btnGuardar);
        
        mainPanel.add(pnlTitulo, BorderLayout.NORTH);
        mainPanel.add(pnlAtajos, BorderLayout.CENTER);
        mainPanel.add(pnlBotones, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
        
        // Listeners
        btnRestaurar.addActionListener(e -> restaurarDefaults());
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> guardarYCerrar());
        
        // Captura de teclas global
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (botonCapturando != null && e.getID() == KeyEvent.KEY_PRESSED) {
                int keyCode = e.getKeyCode();
                
                // Ignorar teclas modificadoras solas
                if (keyCode == KeyEvent.VK_SHIFT || keyCode == KeyEvent.VK_CONTROL || 
                    keyCode == KeyEvent.VK_ALT || keyCode == KeyEvent.VK_META) {
                    return true;
                }
                
                // Verificar si ya está en uso
                String nombreAtajo = (String) botonCapturando.getClientProperty("atajo");
                String enUso = AtajosManager.atajoEnUso(keyCode, nombreAtajo);
                
                if (enUso != null) {
                    JOptionPane.showMessageDialog(this, 
                        "La tecla " + KeyEvent.getKeyText(keyCode) + " ya está asignada a: " + enUso,
                        "Tecla en uso", JOptionPane.WARNING_MESSAGE);
                    return true;
                }
                
                // Asignar tecla
                botonCapturando.setText(KeyEvent.getKeyText(keyCode));
                botonCapturando.putClientProperty("keyCode", keyCode);
                botonCapturando.setBackground(colorBoton);
                botonCapturando = null;
                
                return true;
            }
            return false;
        });
    }
    
    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }
    
    private JButton crearBotonAtajo(String nombreAtajo) {
        JButton btn = new JButton("---");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(colorBoton);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.putClientProperty("atajo", nombreAtajo);
        btn.setPreferredSize(new Dimension(100, 35));
        
        btn.addActionListener(e -> {
            // Resetear botón anterior si había uno
            if (botonCapturando != null) {
                botonCapturando.setBackground(colorBoton);
            }
            
            botonCapturando = btn;
            btn.setText("Presiona tecla...");
            btn.setBackground(colorCapturando);
        });
        
        return btn;
    }
    
    private void cargarAtajosActuales() {
        btnCobrar.setText(AtajosManager.getNombreTecla(AtajosManager.getAtajoCobrar()));
        btnCobrar.putClientProperty("keyCode", AtajosManager.getAtajoCobrar());
        
        btnManual.setText(AtajosManager.getNombreTecla(AtajosManager.getAtajoManual()));
        btnManual.putClientProperty("keyCode", AtajosManager.getAtajoManual());
        
        btnBuscar.setText(AtajosManager.getNombreTecla(AtajosManager.getAtajoBuscar()));
        btnBuscar.putClientProperty("keyCode", AtajosManager.getAtajoBuscar());
        
        btnNuevoProducto.setText(AtajosManager.getNombreTecla(AtajosManager.getAtajoNuevoProducto()));
        btnNuevoProducto.putClientProperty("keyCode", AtajosManager.getAtajoNuevoProducto());
        
        btnVerCaja.setText(AtajosManager.getNombreTecla(AtajosManager.getAtajoVerCaja()));
        btnVerCaja.putClientProperty("keyCode", AtajosManager.getAtajoVerCaja());
    }
    
    private void restaurarDefaults() {
        AtajosManager.restaurarDefaults();
        cargarAtajosActuales();
        JOptionPane.showMessageDialog(this, "Atajos restaurados a valores por defecto");
    }
    
    private void guardarYCerrar() {
        // Guardar cada atajo
        Integer keyCode;
        
        keyCode = (Integer) btnCobrar.getClientProperty("keyCode");
        if (keyCode != null) AtajosManager.setAtajoCobrar(keyCode);
        
        keyCode = (Integer) btnManual.getClientProperty("keyCode");
        if (keyCode != null) AtajosManager.setAtajoManual(keyCode);
        
        keyCode = (Integer) btnBuscar.getClientProperty("keyCode");
        if (keyCode != null) AtajosManager.setAtajoBuscar(keyCode);
        
        keyCode = (Integer) btnNuevoProducto.getClientProperty("keyCode");
        if (keyCode != null) AtajosManager.setAtajoNuevoProducto(keyCode);
        
        keyCode = (Integer) btnVerCaja.getClientProperty("keyCode");
        if (keyCode != null) AtajosManager.setAtajoVerCaja(keyCode);
        
        AtajosManager.guardarAtajos();
        
        JOptionPane.showMessageDialog(this, 
            "Atajos guardados correctamente.\nLos cambios se aplicarán de inmediato.",
            "Guardado", JOptionPane.INFORMATION_MESSAGE);
        
        dispose();
    }
}
