package vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PanelEstadisticas extends JPanel {

    // Componentes principales
    public JLabel lblVentasHoy, lblVentasAyer, lblTotalProds, lblProductoMes, lblPromedioVenta, lblStockBajo;
    public JTable tablaTop, tablaSemana, tablaPeriodos;
    public DefaultTableModel modeloTop, modeloSemana, modeloPeriodos;
    public JButton btnActualizar, btnExportar, btnReporte;
    
    // Colores del tema
    private final Color bgPrincipal = new Color(33, 37, 43);
    private final Color bgTarjetas = new Color(40, 44, 52);
    private final Color colorExito = new Color(46, 204, 113);
    private final Color colorInfo = new Color(52, 152, 219);
    private final Color colorAdvertencia = new Color(241, 196, 15);
    private final Color colorPeligro = new Color(231, 76, 60);
    private final Color colorSecundario = new Color(155, 89, 182);
    private final Color colorNeutral = new Color(149, 165, 166);

    public PanelEstadisticas() {
        setLayout(new BorderLayout(15, 15));
        setBackground(bgPrincipal);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Crear componentes
        crearEncabezado();
        crearPanelKPIs();
        crearPanelTablas();
        crearPanelInferior();
    }
    
    /**
     * Crea el encabezado con título y botones de acción
     */
    private void crearEncabezado() {
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);
        
        // Título con fecha actual
        JPanel pnlTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlTitulo.setOpaque(false);
        
        JLabel lblTitulo = new JLabel("Estadísticas del Negocio");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        
        JLabel lblFecha = new JLabel("  •  " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM", java.util.Locale.forLanguageTag("es-ES"))));
        lblFecha.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        lblFecha.setForeground(new Color(149, 165, 166));
        
        pnlTitulo.add(lblTitulo);
        pnlTitulo.add(lblFecha);
        
        // Panel de botones
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBotones.setOpaque(false);
        
        btnExportar = crearBotonAccion("Exportar", colorInfo);
        btnReporte = crearBotonAccion("Reporte", colorSecundario);
        btnActualizar = crearBotonAccion("Actualizar", colorExito);
        
        pnlBotones.add(btnExportar);
        pnlBotones.add(btnReporte);
        pnlBotones.add(btnActualizar);
        
        pnlTop.add(pnlTitulo, BorderLayout.WEST);
        pnlTop.add(pnlBotones, BorderLayout.EAST);
        
        add(pnlTop, BorderLayout.NORTH);
    }
    
    /**
     * Crea el panel de KPIs con 6 métricas principales
     */
    private void crearPanelKPIs() {
        JPanel pnlKPIs = new JPanel(new GridLayout(2, 3, 15, 15));
        pnlKPIs.setOpaque(false);
        pnlKPIs.setPreferredSize(new Dimension(0, 200));

        // Primera fila de KPIs
        lblVentasHoy = crearTarjetaKPI(pnlKPIs, "Ventas de Hoy", "$ 0.00", colorExito, "Total facturado en el día actual");
        lblVentasAyer = crearTarjetaKPI(pnlKPIs, "Ventas de Ayer", "$ 0.00", colorInfo, "Comparativo del día anterior");
        lblTotalProds = crearTarjetaKPI(pnlKPIs, "Productos Vendidos", "0", colorSecundario, "Unidades vendidas hoy");
        
        // Segunda fila de KPIs
        lblProductoMes = crearTarjetaKPI(pnlKPIs, "Producto del Mes", "Sin datos", colorAdvertencia, "Producto más vendido este mes");
        lblPromedioVenta = crearTarjetaKPI(pnlKPIs, "Promedio por Venta", "$ 0.00", colorNeutral, "Ticket promedio de la semana");
        lblStockBajo = crearTarjetaKPI(pnlKPIs, "Stock Bajo", "0", colorPeligro, "Productos que necesitan reposición");
        
        add(pnlKPIs, BorderLayout.CENTER);
    }
    
    /**
     * Crea el panel con las tres tablas de datos
     */
    private void crearPanelTablas() {
        JPanel pnlTablas = new JPanel(new GridLayout(1, 3, 15, 0));
        pnlTablas.setOpaque(false);
        pnlTablas.setPreferredSize(new Dimension(0, 300));

        // Tabla Top Productos
        modeloTop = new DefaultTableModel(new Object[]{"Producto", "Vendidos"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaTop = crearTablaEstilo(modeloTop);
        JPanel pnlTopProd = crearPanelTabla("Top Productos", tablaTop, bgTarjetas);

        // Tabla Semana
        modeloSemana = new DefaultTableModel(new Object[]{"Día", "Total"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaSemana = crearTablaEstilo(modeloSemana);
        JPanel pnlSemana = crearPanelTabla("Últimos 7 Días", tablaSemana, bgTarjetas);
        
        // Tabla Períodos del Día
        modeloPeriodos = new DefaultTableModel(new Object[]{"Período", "Ventas", "Total"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaPeriodos = crearTablaEstilo(modeloPeriodos);
        JPanel pnlPeriodos = crearPanelTabla("Ventas por Período", tablaPeriodos, bgTarjetas);

        pnlTablas.add(pnlTopProd);
        pnlTablas.add(pnlSemana);
        pnlTablas.add(pnlPeriodos);
        
        add(pnlTablas, BorderLayout.SOUTH);
    }
    
    /**
     * Crea un panel inferior con información adicional (placeholder para futuras mejoras)
     */
    private void crearPanelInferior() {
        // Reservado para futuras mejoras como gráficos o métricas adicionales
    }

    
    /**
     * Crea un botón de acción con estilo consistente
     */
    private JButton crearBotonAccion(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(120, 35));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        
        // Efecto hover
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
            }
        });
        
        return btn;
    }

    /**
     * Crea una tarjeta KPI con diseño mejorado y tooltip
     */
    private JLabel crearTarjetaKPI(JPanel padre, String titulo, String valorInicial, Color colorBorde, String tooltip) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgTarjetas);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, colorBorde),
            BorderFactory.createEmptyBorder(15, 15, 15, 10)
        ));
        card.setToolTipText(tooltip);
        
        // Título de la métrica
        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12));
        lblTit.setForeground(new Color(189, 195, 199));
        
        // Valor de la métrica
        JLabel lblVal = new JLabel(valorInicial);
        lblVal.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));
        lblVal.setForeground(Color.WHITE);

        // Panel interno con layout mejorado
        JPanel pInterno = new JPanel(new BorderLayout());
        pInterno.setOpaque(false);
        pInterno.add(lblTit, BorderLayout.NORTH);
        pInterno.add(lblVal, BorderLayout.CENTER);
        
        // Indicador visual adicional
        JPanel indicador = new JPanel();
        indicador.setBackground(colorBorde);
        indicador.setPreferredSize(new Dimension(3, 0));
        
        card.add(indicador, BorderLayout.WEST);
        card.add(pInterno, BorderLayout.CENTER);
        
        // Efecto hover sutil
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(bgTarjetas.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(bgTarjetas);
            }
        });
        
        padre.add(card);
        return lblVal;
    }

    /**
     * Crea un panel contenedor para tablas con título mejorado
     */
    private JPanel crearPanelTabla(String titulo, JTable tabla, Color bg) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bg);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 63, 65), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Título con línea decorativa
        JPanel pnlTitulo = new JPanel(new BorderLayout());
        pnlTitulo.setOpaque(false);
        
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        lblTitulo.setForeground(Color.WHITE);
        
        JSeparator separador = new JSeparator();
        separador.setForeground(new Color(60, 63, 65));
        separador.setBackground(new Color(60, 63, 65));
        
        pnlTitulo.add(lblTitulo, BorderLayout.NORTH);
        pnlTitulo.add(Box.createVerticalStrut(8), BorderLayout.CENTER);
        pnlTitulo.add(separador, BorderLayout.SOUTH);
        
        // Scroll pane con estilo
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBackground(bg);
        scroll.getViewport().setBackground(bg);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        panel.add(pnlTitulo, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        
        return panel;
    }

    /**
     * Crea una tabla con estilo mejorado y renderizado personalizado
     */
    private JTable crearTablaEstilo(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(35);
        tabla.setEnabled(false);
        tabla.setBackground(new Color(45, 45, 45));
        tabla.setForeground(new Color(220, 220, 220));
        tabla.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        tabla.setShowVerticalLines(false);
        tabla.setShowHorizontalLines(true);
        tabla.setGridColor(new Color(60, 63, 65));
        tabla.setIntercellSpacing(new Dimension(0, 1));
        
        // Header personalizado
        tabla.getTableHeader().setBackground(new Color(30, 30, 30));
        tabla.getTableHeader().setForeground(new Color(189, 195, 199));
        tabla.getTableHeader().setFont(new Font("Segoe UI Emoji", Font.BOLD, 11));
        tabla.getTableHeader().setBorder(BorderFactory.createEmptyBorder());
        tabla.getTableHeader().setReorderingAllowed(false);
        
        // Renderizado personalizado para celdas
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Alternar colores de filas
                if (row % 2 == 0) {
                    c.setBackground(new Color(45, 45, 45));
                } else {
                    c.setBackground(new Color(50, 50, 50));
                }
                
                c.setForeground(new Color(220, 220, 220));
                setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                
                // Alineación especial para columnas numéricas
                if (column > 0 && value instanceof String) {
                    String texto = (String) value;
                    if (texto.startsWith("$") || texto.matches("\\d+")) {
                        setHorizontalAlignment(SwingConstants.RIGHT);
                    } else {
                        setHorizontalAlignment(SwingConstants.LEFT);
                    }
                }
                
                return c;
            }
        };
        
        // Aplicar renderer a todas las columnas
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        
        return tabla;
    }
    
    /**
     * Actualiza el valor de un KPI sin animación
     */
    public void actualizarKPI(JLabel label, String nuevoValor) {
        if (!label.getText().equals(nuevoValor)) {
            label.setText(nuevoValor);
        }
    }
    
    /**
     * Limpia todas las tablas
     */
    public void limpiarTablas() {
        modeloTop.setRowCount(0);
        modeloSemana.setRowCount(0);
        modeloPeriodos.setRowCount(0);
    }
}