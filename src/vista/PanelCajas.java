package vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelCajas extends JPanel {

    public JTable tablaCajas;
    public DefaultTableModel modeloCajas;
    public JButton btnActualizar;
    
    public JPopupMenu menuCajas;
    public JMenuItem itemEditar, itemEliminar;

    public PanelCajas() {
        Color bgPrincipal = new Color(33, 37, 43);
        Color bgTarjetas = new Color(40, 44, 52);
        Color colorBorde = new Color(24, 26, 31);
        Color colorAzul = new Color(97, 175, 239);
        boolean esOscuro = true;

        setLayout(new BorderLayout(20, 20));
        setBackground(bgPrincipal);
        setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // MENÚ CONTEXTUAL
        menuCajas = new JPopupMenu();
        itemEditar = new JMenuItem("✏️ Editar Contenido");
        itemEliminar = new JMenuItem("🗑️ Eliminar Caja");
        itemEditar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        itemEliminar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        itemEliminar.setForeground(new Color(220, 50, 50));
        menuCajas.add(itemEditar);
        menuCajas.add(itemEliminar);

        // ENCABEZADO
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        
        JLabel lblTitulo = new JLabel("📜 Historial de Cajas");
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(220, 223, 228));
        
        btnActualizar = crearBoton("🔄 Actualizar Lista", new Color(60, 60, 60), Color.WHITE);
        btnActualizar.setPreferredSize(new Dimension(180, 40));
        
        pnlTop.add(lblTitulo, BorderLayout.WEST);
        pnlTop.add(btnActualizar, BorderLayout.EAST);

        // TABLA
        // Índices: 0=Nro, 1=Ap, 2=Cie, 3=Tot, 4=Efec, 5=Trans, 6=Ciga, 7=Est, 8=RESP, 9=BOTON
        String[] cols = {
            "Nro", "Apertura", "Cierre", "Total", 
            "Efectivo", "Transf.", "Cigarros", "Estado", "Responsable", "" 
        };
        
        modeloCajas = new DefaultTableModel(cols, 0) {
            // CORRECCIÓN 1: La columna editable (para el botón) es la 9
            public boolean isCellEditable(int r, int c) { return c == 9; } 
        };
        
        tablaCajas = estilizarTabla(modeloCajas, esOscuro);
        
        // CORRECCIÓN 2: Asignar botón "Ver Detalle" a la columna 9 (La última)
        tablaCajas.getColumnModel().getColumn(9).setCellRenderer(new RenderBoton("👁️ Ver", colorAzul));
        tablaCajas.getColumnModel().getColumn(9).setMaxWidth(100);
        tablaCajas.getColumnModel().getColumn(9).setMinWidth(100);

        // Opcional: Ajustar ancho de la columna Responsable (8) para que se vea bien
        tablaCajas.getColumnModel().getColumn(8).setMinWidth(100);

        JPanel tablaContainer = new JPanel(new BorderLayout());
        tablaContainer.setBackground(bgTarjetas);
        tablaContainer.setBorder(BorderFactory.createLineBorder(colorBorde));
        tablaContainer.add(new JScrollPane(tablaCajas), BorderLayout.CENTER);

        add(pnlTop, BorderLayout.NORTH);
        add(tablaContainer, BorderLayout.CENTER);
    }

    private JButton crearBoton(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }

    private JTable estilizarTabla(DefaultTableModel modelo, boolean esOscuro) {
        JTable t = new JTable(modelo);
        t.setRowHeight(45);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setFocusable(false);
        t.setRowSelectionAllowed(false);
        
        if (esOscuro) {
            t.setBackground(new Color(45, 45, 45));
            t.setForeground(new Color(220, 220, 220));
            t.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            t.setGridColor(new Color(80, 80, 80));
            t.getTableHeader().setBackground(new Color(35, 35, 35));
            t.getTableHeader().setForeground(Color.LIGHT_GRAY);
            t.getTableHeader().setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        }
        return t;
    }
}