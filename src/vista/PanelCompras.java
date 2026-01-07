package vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelCompras extends JPanel {

    public JTable tablaProveedores, tablaGastos;
    public DefaultTableModel modeloProveedores, modeloGastos;
    
    public JButton btnNuevoProv, btnEliminarProv, btnRegistrarGasto;
    public JButton btnVerHistorial, btnEliminarGasto;

    public JTextField txtMontoGasto;   
    public JTextField txtDetalleGasto; 
    public JComboBox<modelo.Proveedor> cmbProveedores;

    public PanelCompras() {
        Color bgPrincipal = new Color(33, 37, 43);
        Color bgPaneles = new Color(40, 44, 52);
        Color colorBorde = new Color(60, 63, 65);
        Color colorRojo = new Color(231, 76, 60);
        Color colorAzul = new Color(52, 152, 219);
        Color colorGris = new Color(100, 100, 100);

        setLayout(new GridLayout(1, 2, 20, 0)); 
        setBackground(bgPrincipal);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- PANEL IZQUIERDO: PROVEEDORES ---
        JPanel pnlProv = new JPanel(new BorderLayout(0, 10));
        pnlProv.setOpaque(false);
        JLabel lblTitProv = new JLabel("📂 Agenda de Proveedores");
        lblTitProv.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18)); // CORREGIDO
        lblTitProv.setForeground(Color.WHITE);

        String[] colsProv = {"ID", "Empresa", "Teléfono", "Contacto"};
        modeloProveedores = new DefaultTableModel(colsProv, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        tablaProveedores = estilizarTabla(modeloProveedores);

        JPanel pnlBotonesProv = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlBotonesProv.setOpaque(false);
        btnNuevoProv = crearBoton("➕ Nuevo", colorAzul);
        btnEliminarProv = crearBoton("🗑️ Eliminar", colorRojo);
        pnlBotonesProv.add(btnNuevoProv);
        pnlBotonesProv.add(btnEliminarProv);

        JScrollPane scrollProv = new JScrollPane(tablaProveedores);
        scrollProv.getViewport().setBackground(bgPaneles);
        scrollProv.setBorder(BorderFactory.createLineBorder(colorBorde));

        pnlProv.add(lblTitProv, BorderLayout.NORTH);
        pnlProv.add(scrollProv, BorderLayout.CENTER);
        pnlProv.add(pnlBotonesProv, BorderLayout.SOUTH);

        // --- PANEL DERECHO: GASTOS ---
        JPanel pnlGastos = new JPanel(new BorderLayout(0, 15));
        pnlGastos.setBackground(bgPaneles);
        pnlGastos.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(colorBorde),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Formulario
        JPanel pnlForm = new JPanel(new GridLayout(4, 2, 10, 10));
        pnlForm.setOpaque(false);
        // Título del borde con fuente Emoji
        pnlForm.setBorder(BorderFactory.createTitledBorder(null, "Registrar Nuevo Gasto", 0, 0, new Font("Segoe UI Emoji", Font.BOLD, 14), Color.LIGHT_GRAY));

        txtMontoGasto = crearInputGrande();
        txtDetalleGasto = crearInput();
        cmbProveedores = new JComboBox<>();
        cmbProveedores.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14)); // CORREGIDO

        pnlForm.add(crearLabel("Monto a Retirar ($):"));
        pnlForm.add(txtMontoGasto);
        pnlForm.add(crearLabel("Proveedor / Destino:"));
        pnlForm.add(cmbProveedores);
        pnlForm.add(crearLabel("Descripción:"));
        pnlForm.add(txtDetalleGasto);
        
        btnRegistrarGasto = crearBoton("💸 RETIRAR DINERO", colorRojo);
        pnlForm.add(new JLabel("")); 
        pnlForm.add(btnRegistrarGasto);

        // Tabla Gastos
        String[] colsGas = {"ID", "Fecha", "Hora", "Proveedor", "Detalle", "Monto", "Quedó en Caja"};
        modeloGastos = new DefaultTableModel(colsGas, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        tablaGastos = estilizarTabla(modeloGastos);
        
        tablaGastos.getColumnModel().getColumn(0).setMinWidth(0);
        tablaGastos.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaGastos.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scrollGas = new JScrollPane(tablaGastos);
        scrollGas.setBorder(null);

        // Botonera Inferior
        JPanel pnlBotonesGastos = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBotonesGastos.setOpaque(false);
        
        btnVerHistorial = crearBoton("📜 Ver Historial Completo", colorGris);
        btnEliminarGasto = crearBoton("🗑️ Anular Gasto", colorRojo);
        
        pnlBotonesGastos.add(btnVerHistorial);
        pnlBotonesGastos.add(btnEliminarGasto);

        pnlGastos.add(pnlForm, BorderLayout.NORTH);
        pnlGastos.add(scrollGas, BorderLayout.CENTER);
        pnlGastos.add(pnlBotonesGastos, BorderLayout.SOUTH);

        add(pnlProv);
        add(pnlGastos);
    }

    // --- MÉTODOS AUXILIARES ---
    private JLabel crearLabel(String t) {
        JLabel l = new JLabel(t); 
        l.setForeground(Color.LIGHT_GRAY); 
        l.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12)); // CORREGIDO
        return l;
    }
    
    private JButton crearBoton(String t, Color bg) {
        JButton b = new JButton(t); 
        b.setBackground(bg); 
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12)); // CORREGIDO
        b.setFocusPainted(false); 
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "roundRect"); 
        return b;
    }
    
    private JTextField crearInput() {
        JTextField t = new JTextField(); 
        t.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14)); // CORREGIDO
        t.putClientProperty("Component.arc", 10); 
        return t;
    }
    
    private JTextField crearInputGrande() {
        JTextField t = new JTextField(); 
        t.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18)); // CORREGIDO
        t.setHorizontalAlignment(SwingConstants.CENTER); 
        t.putClientProperty("Component.arc", 10); 
        return t;
    }
    
    private JTable estilizarTabla(DefaultTableModel m) {
        JTable t = new JTable(m); 
        t.setRowHeight(30); 
        t.setShowVerticalLines(false);
        t.setBackground(new Color(45, 45, 45)); 
        t.setForeground(Color.WHITE);
        t.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14)); // CORREGIDO (Tabla)
        t.setGridColor(new Color(80, 80, 80));
        t.getTableHeader().setBackground(new Color(30, 30, 30));
        t.getTableHeader().setForeground(Color.LIGHT_GRAY); 
        t.getTableHeader().setFont(new Font("Segoe UI Emoji", Font.BOLD, 12)); // CORREGIDO (Header)
        return t;
    }
}