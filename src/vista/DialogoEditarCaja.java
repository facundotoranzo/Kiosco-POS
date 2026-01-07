package vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DialogoEditarCaja extends JDialog {

    public JTable tablaDetalle;
    public DefaultTableModel modeloDetalle;
    public JButton btnCerrar;

    public DialogoEditarCaja(JFrame parent, String tituloCaja) {
        super(parent, "EDITAR: " + tituloCaja, true);
        setSize(750, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBackground(new Color(45, 20, 20)); // Fondo rojizo para indicar modo edición
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(content);

        JLabel lblTitulo = new JLabel("EDITANDO: " + tituloCaja);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        // Columna 0 oculta para ID, Columna 5 para botón borrar
        String[] cols = {"ID", "Hora", "Producto", "Precio", "Cant.", "Subtotal", "Acción"};
        modeloDetalle = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 6; }
        };
        
        tablaDetalle = new JTable(modeloDetalle);
        tablaDetalle.setRowHeight(40);
        tablaDetalle.setShowVerticalLines(false);
        tablaDetalle.setFocusable(false);
        tablaDetalle.setRowSelectionAllowed(false);
        tablaDetalle.setBackground(new Color(60, 40, 40));
        tablaDetalle.setForeground(Color.WHITE);
        tablaDetalle.setGridColor(new Color(100, 60, 60));
        
        // Ocultar ID
        tablaDetalle.getColumnModel().getColumn(0).setMinWidth(0);
        tablaDetalle.getColumnModel().getColumn(0).setMaxWidth(0);
        tablaDetalle.getColumnModel().getColumn(0).setWidth(0);

        // Botón Borrar
        tablaDetalle.getColumnModel().getColumn(6).setCellRenderer(new RenderBoton("🗑️", new Color(200, 50, 50)));
        tablaDetalle.getColumnModel().getColumn(6).setMaxWidth(60);

        JScrollPane scroll = new JScrollPane(tablaDetalle);
        scroll.getViewport().setBackground(new Color(60, 40, 40));
        scroll.setBorder(null);

        btnCerrar = new JButton("Terminar Edición");
        btnCerrar.setBackground(new Color(220, 220, 220));
        btnCerrar.setForeground(Color.BLACK);
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCerrar.addActionListener(e -> dispose());

        add(lblTitulo, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(btnCerrar, BorderLayout.SOUTH);
    }
}