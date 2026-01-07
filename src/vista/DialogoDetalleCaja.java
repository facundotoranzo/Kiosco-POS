package vista;

import modelo.CajaDAO;
import modelo.Formato; 
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DialogoDetalleCaja extends JDialog {

    public JTable tablaDetalle;
    public DefaultTableModel modeloDetalle;
    public JButton btnCerrar;
    
    // Etiquetas de totales
    public JLabel lblTotalCaja; 
    public JLabel lblEfectivo, lblTransferencia;
    public JLabel lblCigarrillosEfec, lblCigarrillosTransf;

    // Variables lógicas
    private CajaDAO dao;
    private int idCajaActual;

    public DialogoDetalleCaja(JFrame parent, int idCaja, CajaDAO dao) {
        super(parent, "Detalle de Caja N° " + idCaja, true);
        this.dao = dao;
        this.idCajaActual = idCaja;

        setSize(950, 650); // Un poco más ancho para que entren los números
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        // Fondo Oscuro
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setBackground(new Color(33, 37, 43));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(content);

        // Título
        JLabel lblTitulo = new JLabel("📝 Movimientos de Caja");
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 22));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);

        // Tabla Detalle
        // Col 0: ID (Oculto), Col 6: MedioPago (Oculto pero usado para pintar)
        String[] cols = {"ID", "Hora", "Producto", "Precio Unit.", "Cant.", "Subtotal", "MedioPago"};
        modeloDetalle = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        
        tablaDetalle = new JTable(modeloDetalle);
        estilizarTabla(tablaDetalle);
        
        // Ocultar columnas ID (0) y MedioPago (6)
        ocultarColumna(0);
        ocultarColumna(6); 

        // --- CORRECCIÓN 1: APLICAR EL RENDER A TODAS LAS COLUMNAS VISIBLES ---
        RenderTabla renderPersonalizado = new RenderTabla();
        for (int i = 0; i < tablaDetalle.getColumnCount(); i++) {
            tablaDetalle.getColumnModel().getColumn(i).setCellRenderer(renderPersonalizado);
        }

        JScrollPane scroll = new JScrollPane(tablaDetalle);
        scroll.getViewport().setBackground(new Color(45, 45, 45));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));

        // --- PANEL SUR (Totales Desglosados) ---
        JPanel pnlSur = new JPanel(new BorderLayout(10, 10));
        pnlSur.setOpaque(false);
        pnlSur.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(80, 80, 80)));
        
        JPanel pnlTotales = new JPanel(new GridLayout(3, 2, 20, 10));
        pnlTotales.setOpaque(false);
        pnlTotales.setBorder(new EmptyBorder(15, 10, 5, 10));
        
        // Fila 1: Mercadería Común
        lblEfectivo = crearLabelTotal("💵 Efectivo: $ ...", new Color(52, 152, 219)); 
        lblTransferencia = crearLabelTotal("📲 Transf.: $ ...", new Color(155, 89, 182)); 
        
        // Fila 2: Cigarrillos
        lblCigarrillosEfec = crearLabelTotal("🚬 Cig. Efec.: $ ...", new Color(230, 126, 34)); 
        lblCigarrillosTransf = crearLabelTotal("🚬 Cig. Transf.: $ ...", new Color(211, 84, 0));
        
        // Fila 3: Total General
        JLabel lblVacio = new JLabel(""); 
        lblTotalCaja = crearLabelTotal("TOTAL CAJA: $ ...", new Color(46, 204, 113)); 
        lblTotalCaja.setFont(new Font("Segoe UI Emoji", Font.BOLD, 26)); 
        lblTotalCaja.setHorizontalAlignment(SwingConstants.RIGHT);

        pnlTotales.add(lblEfectivo);            pnlTotales.add(lblTransferencia);
        pnlTotales.add(lblCigarrillosEfec);     pnlTotales.add(lblCigarrillosTransf);
        pnlTotales.add(lblVacio);               pnlTotales.add(lblTotalCaja);

        // Botonera de Acciones
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBtn.setOpaque(false);
        
        JButton btnEliminar = new JButton("🗑️ Eliminar Ítem (Devolución)");
        btnEliminar.setBackground(new Color(231, 76, 60));
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        btnEliminar.setFocusPainted(false);
        btnEliminar.addActionListener(e -> eliminarItem());

        btnCerrar = new JButton("Cerrar");
        btnCerrar.setBackground(new Color(100, 100, 100));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        btnCerrar.addActionListener(e -> dispose());
        
        pnlBtn.add(btnEliminar);
        pnlBtn.add(btnCerrar);

        pnlSur.add(pnlTotales, BorderLayout.CENTER);
        pnlSur.add(pnlBtn, BorderLayout.SOUTH);

        add(lblTitulo, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(pnlSur, BorderLayout.SOUTH);

        cargarDatos();
        
        try { modelo.GestorFuentes.forzarFuente(this); } catch (Exception e) {}
    }

    private void cargarDatos() {
        modeloDetalle.setRowCount(0);
        List<Object[]> lista = dao.obtenerDetalleVentasEditable(idCajaActual);
        
        for (Object[] fila : lista) {
            modeloDetalle.addRow(fila);
        }
        
        double[] totales = dao.calcularDesgloseCaja(idCajaActual);
        // [0]Total, [1]EfecNeto, [2]DigiNeto, [3]CigaEfectivo, [4]CigaTransf

        lblTotalCaja.setText("TOTAL CAJA: " + Formato.moneda(totales[0]));
        lblEfectivo.setText("💵 Efectivo: " + Formato.moneda(totales[1]));
        lblTransferencia.setText("📲 Digi.: " + Formato.moneda(totales[2]));
        lblCigarrillosEfec.setText("🚬 Cig. Efec.: " + Formato.moneda(totales[3]));
        lblCigarrillosTransf.setText("🚬 Cig. Transf.: " + Formato.moneda(totales[4]));
    }

    private void eliminarItem() {
        int row = tablaDetalle.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto.");
            return;
        }

        int idDetalle = (int) modeloDetalle.getValueAt(row, 0); 
        String producto = (String) modeloDetalle.getValueAt(row, 2);
        String cantidad = modeloDetalle.getValueAt(row, 4).toString();

        if (JOptionPane.showConfirmDialog(this, "¿Devolver " + cantidad + " x " + producto + "?\nSe ajustará el stock y la caja.", "Confirmar Devolución", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (dao.eliminarItemVenta(idDetalle)) {
                JOptionPane.showMessageDialog(this, "✅ Devolución realizada.");
                cargarDatos(); 
            } else {
                JOptionPane.showMessageDialog(this, "❌ Error al eliminar.");
            }
        }
    }

    private JLabel crearLabelTotal(String texto, Color color) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16)); 
        l.setForeground(color);
        return l;
    }

    private void estilizarTabla(JTable t) {
        t.setRowHeight(30);
        t.setShowVerticalLines(false);
        t.setBackground(new Color(45, 45, 45));
        t.setForeground(new Color(220, 220, 220));
        t.setGridColor(new Color(80, 80, 80));
        t.getTableHeader().setBackground(new Color(35, 35, 35));
        t.getTableHeader().setForeground(Color.LIGHT_GRAY);
        t.getTableHeader().setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
    }
    
    private void ocultarColumna(int index) {
        tablaDetalle.getColumnModel().getColumn(index).setMinWidth(0);
        tablaDetalle.getColumnModel().getColumn(index).setMaxWidth(0);
        tablaDetalle.getColumnModel().getColumn(index).setWidth(0);
    }

    // --- CORRECCIÓN 2: RENDERIZADOR MEJORADO PARA MODO OSCURO ---
    class RenderTabla extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            // Llamamos al padre para que configure lo básico
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // 1. Buscamos el Medio de Pago (Está oculto en la Columna 6)
            Object medioPagoObj = table.getModel().getValueAt(row, 6);
            String medioPago = (medioPagoObj != null) ? medioPagoObj.toString() : "";
            
            // 2. Lógica de Colores
            if (isSelected) {
                // Si está seleccionado, respetamos el azul de selección de Swing
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            } else {
                setBackground(new Color(45, 45, 45)); // Tu fondo oscuro
                
                if (medioPago.contains("Transferencia")) {
                    // CIAN BRILLANTE para que resalte en oscuro (el azul normal no se ve)
                    setForeground(new Color(0, 255, 255)); 
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    // BLANCO/GRIS para efectivo
                    setForeground(new Color(220, 220, 220));
                }
            }

            // 3. Formato de Moneda (Para Precio Unitario (3) y Subtotal (5))
            if (value instanceof Number) {
                if (column == 3 || column == 5) {
                    setText(Formato.moneda(((Number) value).doubleValue()));
                    setHorizontalAlignment(SwingConstants.RIGHT); // Alinear números a la derecha
                }
            }
            
            return this;
        }
    }
}