package vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class PanelConfiguracion extends JPanel {

    public JComboBox<String> cmbModoManual;
    public JCheckBox chkSepararCigarros, chkImprimirTicket, chkSonidoVenta;
    public JTextField txtNombreLocal;
    public JButton btnGuardar, btnGestionarUsuarios, btnCerrarSesion, btnVisualizarTicket, btnConfigAtajos;
    
    // Contenedores para poder ocultarlos si es empleado
    public JPanel pnlContainerAdmin, pnlContainerGeneral, pnlContainerTickets;

    public PanelConfiguracion() {
        setLayout(new BorderLayout());
        setBackground(new Color(33, 37, 43));

        // Título
        JLabel lblTitulo = new JLabel("Configuracion del Sistema");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setBorder(new EmptyBorder(30, 0, 30, 0));
        add(lblTitulo, BorderLayout.NORTH);

        // Panel Central Vertical
        JPanel pnlCentro = new JPanel();
        pnlCentro.setLayout(new BoxLayout(pnlCentro, BoxLayout.Y_AXIS));
        pnlCentro.setOpaque(false);
        pnlCentro.setBorder(new EmptyBorder(0, 100, 0, 100)); // Márgenes laterales

        // --- SECCIÓN GENERAL (Visible para todos) ---
        pnlContainerGeneral = new JPanel(new GridLayout(0, 1, 10, 10));
        pnlContainerGeneral.setOpaque(false);
        pnlContainerGeneral.setBorder(crearBordeTitulo("Interfaz de Venta"));

        JLabel lblInfo = new JLabel("Estilo de ventana Manual:");
        lblInfo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblInfo.setForeground(Color.LIGHT_GRAY);
        
        cmbModoManual = new JComboBox<>(new String[]{"Modo Escritorio", "Modo Tactil / Carniceria"});
        estilizarCombo(cmbModoManual);
        
        // Checkbox para sonido al vender
        chkSonidoVenta = new JCheckBox("Sonido de confirmacion al vender");
        chkSonidoVenta.setOpaque(false);
        chkSonidoVenta.setForeground(Color.WHITE);
        chkSonidoVenta.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        chkSonidoVenta.setSelected(modelo.SonidoManager.isSonidoActivo());
        
        // Botón para configurar atajos
        btnConfigAtajos = crearBoton("Configurar Atajos de Teclado", new Color(108, 117, 125));

        pnlContainerGeneral.add(lblInfo);
        pnlContainerGeneral.add(cmbModoManual);
        pnlContainerGeneral.add(chkSonidoVenta);
        pnlContainerGeneral.add(btnConfigAtajos);

        // --- SECCIÓN TICKETS (Visible según licencia) ---
        pnlContainerTickets = new JPanel(new GridLayout(0, 1, 10, 10));
        pnlContainerTickets.setOpaque(false);
        pnlContainerTickets.setBorder(crearBordeTitulo("Configuracion de Tickets"));

        // Checkbox para activar/desactivar impresión
        chkImprimirTicket = new JCheckBox("Activar impresion de tickets");
        chkImprimirTicket.setOpaque(false);
        chkImprimirTicket.setForeground(Color.WHITE);
        chkImprimirTicket.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        pnlContainerTickets.add(chkImprimirTicket);

        // Campo para nombre del local (solo PRO/RED)
        JLabel lblNombreLocal = new JLabel("Nombre del local en ticket:");
        lblNombreLocal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblNombreLocal.setForeground(Color.LIGHT_GRAY);
        
        txtNombreLocal = new JTextField();
        estilizarTextField(txtNombreLocal);
        txtNombreLocal.setText("Mi Negocio"); // Valor por defecto

        // Botón para visualizar ticket
        btnVisualizarTicket = crearBoton("Visualizar Ticket", new Color(108, 117, 125));

        // --- SECCIÓN ADMIN (Solo Admin) ---
        pnlContainerAdmin = new JPanel(new GridLayout(0, 1, 10, 10));
        pnlContainerAdmin.setOpaque(false);
        pnlContainerAdmin.setBorder(crearBordeTitulo("Administracion"));

        chkSepararCigarros = new JCheckBox("Separar Recaudacion de Cigarrillos");
        chkSepararCigarros.setOpaque(false);
        chkSepararCigarros.setForeground(Color.WHITE);
        chkSepararCigarros.setFont(new Font("Segoe UI", Font.PLAIN, 15));

        btnGestionarUsuarios = crearBoton("Gestionar Usuarios y Claves", new Color(52, 152, 219));

        pnlContainerAdmin.add(chkSepararCigarros);
        pnlContainerAdmin.add(btnGestionarUsuarios);

        // --- BOTONES FINALES ---
        JPanel pnlSur = new JPanel(new GridLayout(2, 1, 10, 15));
        pnlSur.setOpaque(false);
        pnlSur.setBorder(new EmptyBorder(30, 0, 30, 0));

        btnGuardar = crearBoton("Guardar Cambios", new Color(40, 167, 69));
        btnCerrarSesion = crearBoton("Cerrar Sesion", new Color(220, 53, 69));

        pnlSur.add(btnGuardar);
        pnlSur.add(btnCerrarSesion);

        // Armar todo
        pnlCentro.add(pnlContainerGeneral);
        pnlCentro.add(Box.createVerticalStrut(20)); // Espacio
        pnlCentro.add(pnlContainerTickets);
        pnlCentro.add(Box.createVerticalStrut(20)); // Espacio
        pnlCentro.add(pnlContainerAdmin);
        pnlCentro.add(pnlSur);

        // Scroll por si la pantalla es chica
        JScrollPane scroll = new JScrollPane(pnlCentro);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        
        add(scroll, BorderLayout.CENTER);
        
        // ===== APLICAR EFECTOS VISUALES PREMIUM =====
        aplicarEfectosVisuales();
    }
    
    /**
     * Configura la visibilidad de secciones según la licencia
     */
    public void configurarSegunLicencia(String licencia) {
        if ("LITE".equals(licencia)) {
            // En LITE: Solo checkbox de activar/desactivar, nombre fijo "Kiosco"
            txtNombreLocal.setVisible(false);
            btnVisualizarTicket.setVisible(false);
            
            // Remover componentes de PRO del panel de tickets
            pnlContainerTickets.removeAll();
            pnlContainerTickets.add(chkImprimirTicket);
            
        } else {
            // En PRO/RED: Todas las opciones disponibles
            pnlContainerTickets.removeAll();
            pnlContainerTickets.add(chkImprimirTicket);
            
            JLabel lblNombreLocal = new JLabel("Nombre del local en ticket:");
            lblNombreLocal.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            lblNombreLocal.setForeground(Color.LIGHT_GRAY);
            
            pnlContainerTickets.add(lblNombreLocal);
            pnlContainerTickets.add(txtNombreLocal);
            pnlContainerTickets.add(btnVisualizarTicket);
        }
        
        pnlContainerTickets.revalidate();
        pnlContainerTickets.repaint();
    }
    
    /**
     * Aplica efectos visuales a los botones de configuración
     */
    private void aplicarEfectosVisuales() {
        // Efectos hover en botones
        EfectosVisuales.agregarHoverEffect(btnGuardar);
        EfectosVisuales.agregarHoverEffect(btnCerrarSesion);
        EfectosVisuales.agregarHoverEffect(btnConfigAtajos);
        if (btnGestionarUsuarios != null) {
            EfectosVisuales.agregarHoverEffect(btnGestionarUsuarios);
        }
        if (btnVisualizarTicket != null) {
            EfectosVisuales.agregarHoverEffect(btnVisualizarTicket);
        }
    }

    private JButton crearBoton(String t, Color bg) {
        JButton b = new JButton(t);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(0, 45));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void estilizarCombo(JComboBox<String> b) {
        b.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        b.setBackground(new Color(40, 44, 52)); // Fondo oscuro
        b.setForeground(Color.WHITE); // Texto blanco
        b.setBorder(BorderFactory.createLineBorder(new Color(60, 63, 65))); // Borde sutil
        b.setPreferredSize(new Dimension(0, 40));
        
        // Personalizar el renderer para que se vea oscuro
        b.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                
                if (isSelected) {
                    setBackground(new Color(52, 152, 219)); // Azul cuando está seleccionado
                    setForeground(Color.WHITE);
                } else {
                    setBackground(new Color(40, 44, 52)); // Fondo oscuro
                    setForeground(Color.WHITE);
                }
                
                return this;
            }
        });
    }
    
    private void estilizarTextField(JTextField txt) {
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBackground(new Color(40, 44, 52)); // Fondo oscuro
        txt.setForeground(Color.WHITE); // Texto blanco
        txt.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 63, 65)), // Borde sutil
            BorderFactory.createEmptyBorder(8, 12, 8, 12) // Padding interno
        ));
        txt.setCaretColor(Color.WHITE); // Cursor blanco
        txt.setPreferredSize(new Dimension(0, 40));
    }

    private javax.swing.border.Border crearBordeTitulo(String titulo) {
        return BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(60, 60, 60)),
            titulo,
            0, 0,
            new Font("Segoe UI", Font.BOLD, 12),
            new Color(52, 152, 219)
        );
    }
}