package vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PanelVentas extends JPanel {

    // --- COMPONENTES PÚBLICOS ---
    public JTextField txtBuscar;
    public JTable tablaProductos, tablaCarrito;
    public DefaultTableModel modeloProductos, modeloCarrito;
    public JLabel lblTotal;
    public JTextField txtPagaCon;
    public JLabel lblVuelto;
    
    // BOTONES
    public JButton btnCobrar, btnTarjeta, btnManual, btnCerrarCaja, btnNuevoProducto, btnEliminarItem, btnVerCaja;
    
    // MENÚ CONTEXTUAL
    public JPopupMenu menuContextual;
    public JMenuItem itemEditar, itemEliminar;

    // Renderers
    private RenderBoton renderBotonAgregar;
    private RenderBoton renderBotonQuitar;

    public PanelVentas() {
        // --- CONFIGURACIÓN DE COLORES (TEMA OSCURO) ---
        Color bgPrincipal = new Color(33, 37, 43);
        Color bgPaneles = new Color(40, 44, 52);
        Color colorBorde = new Color(60, 63, 65);
        Color colorVerde = new Color(40, 167, 69);
        Color colorAzul = new Color(0, 123, 255);
        Color colorRojo = new Color(220, 53, 69);
        Color colorTexto = new Color(220, 223, 228);
        boolean esOscuro = true;

        setLayout(new BorderLayout(15, 15));
        setBackground(bgPrincipal);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- INICIALIZAR BOTONES Y MENÚS ---
        inicializarComponentesLogicos();

        // =============================================================
        // 1. BARRA SUPERIOR (HERRAMIENTAS DE GESTIÓN)
        // =============================================================
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlToolbar.setOpaque(false);
        pnlToolbar.setBorder(new EmptyBorder(0, 5, 5, 5));

        // Botones de gestión con estilo más sobrio y profesional
        btnNuevoProducto = crearBoton("+ Nuevo Producto", new Color(60, 60, 60), Color.WHITE);
        btnVerCaja = crearBoton("■ Ver Caja", new Color(60, 60, 60), Color.WHITE);
        btnCerrarCaja = crearBoton("▣ Cerrar Caja", new Color(200, 50, 50), Color.WHITE);

        pnlToolbar.add(btnNuevoProducto);
        pnlToolbar.add(btnVerCaja);
        pnlToolbar.add(Box.createHorizontalGlue()); // Espacio flexible (opcional)
        pnlToolbar.add(btnCerrarCaja);

        add(pnlToolbar, BorderLayout.NORTH);

        // =============================================================
        // 2. ZONA CENTRAL (DIVIDIDA)
        // =============================================================
        JPanel pnlCentral = new JPanel(new GridLayout(1, 2, 15, 0)); // 1 fila, 2 columnas
        pnlCentral.setOpaque(false);

        // --- IZQUIERDA: CATÁLOGO ---
        JPanel pnlIzquierdo = new JPanel(new BorderLayout(0, 10));
        pnlIzquierdo.setOpaque(false);

        // Buscador y Botón Manual
        JPanel pnlBuscador = new JPanel(new BorderLayout(10, 0));
        pnlBuscador.setOpaque(false);
        
        txtBuscar = new JTextField();
        txtBuscar.putClientProperty("JTextField.placeholderText", "● Buscar producto por nombre o código...");
        txtBuscar.putClientProperty("Component.arc", 4);  // Bordes menos redondeados
        txtBuscar.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtBuscar.setPreferredSize(new Dimension(0, 40));
        
        btnManual = crearBoton("▼ Manual", colorAzul, Color.WHITE);
        btnManual.setPreferredSize(new Dimension(100, 40));

        pnlBuscador.add(txtBuscar, BorderLayout.CENTER);
        pnlBuscador.add(btnManual, BorderLayout.EAST);

        // Tabla Productos
        String[] colsProd = {"Código", "Producto", "Precio", "Stock", ""};
        modeloProductos = new DefaultTableModel(colsProd, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        tablaProductos = estilizarTabla(modeloProductos, esOscuro);
        
        RenderStock renderSemaforo = new RenderStock();
        // Aplicamos el semáforo a la columna 3 (Stock)
        tablaProductos.getColumnModel().getColumn(3).setCellRenderer(renderSemaforo);
        
        // Opcional: Si quieres que el Precio también se vea centrado, puedes reusar el render o usar uno por defecto
        DefaultTableCellRenderer renderCentro = new DefaultTableCellRenderer();
        renderCentro.setHorizontalAlignment(SwingConstants.CENTER);
        tablaProductos.getColumnModel().getColumn(0).setCellRenderer(renderCentro); // Código centrado
        tablaProductos.getColumnModel().getColumn(2).setCellRenderer(renderCentro); // Precio centrado

        renderBotonAgregar = new RenderBoton("➕", colorAzul);
        configurarColumnaBoton(tablaProductos, 4, renderBotonAgregar);
        agregarEfectoHover(tablaProductos, renderBotonAgregar, 4);

        // Contenedor tabla con borde
        JScrollPane scrollProd = new JScrollPane(tablaProductos);
        scrollProd.setBorder(BorderFactory.createLineBorder(colorBorde));
        scrollProd.getViewport().setBackground(bgPaneles);

        pnlIzquierdo.add(pnlBuscador, BorderLayout.NORTH);
        pnlIzquierdo.add(scrollProd, BorderLayout.CENTER);


        // --- DERECHA: CARRITO Y PAGO ---
        JPanel pnlDerecho = new JPanel(new BorderLayout(0, 0));
        pnlDerecho.setBackground(bgPaneles);
        pnlDerecho.setBorder(BorderFactory.createLineBorder(colorBorde));

        // Título Carrito
        JLabel lblTituloCarrito = new JLabel("CARRITO DE VENTA", SwingConstants.CENTER);
        lblTituloCarrito.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTituloCarrito.setForeground(Color.GRAY);
        lblTituloCarrito.setBorder(new EmptyBorder(10, 0, 10, 0));

        // Tabla Carrito
        String[] colsCarr = {"Cant", "Producto", "Total", ""};
        modeloCarrito = new DefaultTableModel(colsCarr, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        tablaCarrito = estilizarTabla(modeloCarrito, esOscuro);
        
        renderBotonQuitar = new RenderBoton("➖", colorRojo);
        configurarColumnaBoton(tablaCarrito, 3, renderBotonQuitar);
        agregarEfectoHover(tablaCarrito, renderBotonQuitar, 3);

        JScrollPane scrollCarr = new JScrollPane(tablaCarrito);
        scrollCarr.setBorder(null); // Sin borde interno
        scrollCarr.getViewport().setBackground(bgPaneles);

        // PANEL DE PAGO (ABAJO DERECHA)
        JPanel pnlPago = new JPanel(new BorderLayout(0, 15));
        pnlPago.setBackground(bgPaneles); // Mismo fondo que el panel derecho
        pnlPago.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Sub-panel: Controles de pago
        JPanel pnlControlesPago = new JPanel(new GridLayout(2, 2, 10, 10));
        pnlControlesPago.setOpaque(false);

        // Fila 1: Botón Tarjeta y Campo "Paga con"
        btnTarjeta = crearBoton("◊ Tarjeta", new Color(60, 60, 60), Color.WHITE);
        
        JPanel pnlPagaCon = new JPanel(new BorderLayout(5, 0));
        pnlPagaCon.setOpaque(false);
        JLabel lblPaga = new JLabel("Abona con: ");
        lblPaga.setForeground(Color.LIGHT_GRAY);
        txtPagaCon = new JTextField();
        txtPagaCon.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtPagaCon.setHorizontalAlignment(SwingConstants.RIGHT);
        txtPagaCon.putClientProperty("Component.arc", 4);  // Bordes menos redondeados
        pnlPagaCon.add(lblPaga, BorderLayout.WEST);
        pnlPagaCon.add(txtPagaCon, BorderLayout.CENTER);

        // Fila 2: Etiquetas de Total y Vuelto
        lblTotal = new JLabel("$ 0.00", SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTotal.setForeground(colorTexto);
        
        lblVuelto = new JLabel("Vuelto: $ 0.00", SwingConstants.RIGHT);
        lblVuelto.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblVuelto.setForeground(colorVerde);

        pnlControlesPago.add(btnTarjeta);
        pnlControlesPago.add(lblTotal); // Total grande a la derecha
        pnlControlesPago.add(pnlPagaCon);
        pnlControlesPago.add(lblVuelto); // Vuelto debajo del total

        // Botón Cobrar Gigante
        btnCobrar = new JButton("COBRAR VENTA");
        btnCobrar.setFont(new Font("Segoe UI", Font.BOLD, 20));
        btnCobrar.setBackground(colorVerde);
        btnCobrar.setForeground(new Color(30, 40, 30));
        btnCobrar.setPreferredSize(new Dimension(0, 60));
        btnCobrar.setFocusPainted(false);
        btnCobrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Removido roundRect para look más empresarial

        pnlPago.add(pnlControlesPago, BorderLayout.CENTER);
        pnlPago.add(btnCobrar, BorderLayout.SOUTH);

        // Armar derecha
        pnlDerecho.add(lblTituloCarrito, BorderLayout.NORTH);
        pnlDerecho.add(scrollCarr, BorderLayout.CENTER);
        pnlDerecho.add(pnlPago, BorderLayout.SOUTH);

        // Agregar todo al panel principal
        pnlCentral.add(pnlIzquierdo);
        pnlCentral.add(pnlDerecho);
        add(pnlCentral, BorderLayout.CENTER);
        
        // ===== APLICAR EFECTOS VISUALES PREMIUM =====
        aplicarEfectosVisuales();
    }
    
    /**
     * Aplica todos los efectos visuales y animaciones premium
     */
    private void aplicarEfectosVisuales() {
        // Efectos hover en botones principales
        EfectosVisuales.agregarHoverEffect(btnNuevoProducto);
        EfectosVisuales.agregarHoverEffect(btnVerCaja);
        EfectosVisuales.agregarHoverEffect(btnCerrarCaja);
        EfectosVisuales.agregarHoverEffect(btnManual);
        EfectosVisuales.agregarHoverEffect(btnTarjeta);
        EfectosVisuales.agregarHoverEffect(btnCobrar);
        
        // Efectos hover en tablas
        EfectosVisuales.agregarHoverTabla(tablaProductos);
        EfectosVisuales.agregarHoverTabla(tablaCarrito);
    }

    private void inicializarComponentesLogicos() {
        menuContextual = new JPopupMenu();
        itemEditar = new JMenuItem("\u270F\uFE0F Editar Stock / Precio");
        itemEliminar = new JMenuItem("\uD83D\uDDD1 Eliminar Producto");
        Font fontMenu = new Font("Segoe UI Emoji", Font.PLAIN, 14);
        itemEditar.setFont(fontMenu);
        itemEliminar.setFont(fontMenu);
        itemEliminar.setForeground(new Color(220, 50, 50));
        menuContextual.add(itemEditar);
        menuContextual.add(itemEliminar);
        
        // Inicializamos el dummy para que no de error en el controlador
        btnEliminarItem = new JButton();
    }

    // --- MÉTODOS DE DISEÑO ---
    private void configurarColumnaBoton(JTable tabla, int colIndex, RenderBoton renderer) {
        TableColumn col = tabla.getColumnModel().getColumn(colIndex);
        col.setCellRenderer(renderer);
        col.setMaxWidth(60); // Botón compacto
        col.setMinWidth(60);
    }

    private void agregarEfectoHover(JTable tabla, RenderBoton renderer, int columnaBoton) {
        tabla.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = tabla.rowAtPoint(e.getPoint());
                int col = tabla.columnAtPoint(e.getPoint());
                if (col == columnaBoton && row >= 0) {
                    renderer.setMouseState(row, col, false);
                    tabla.setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    renderer.setMouseState(-1, -1, false);
                    tabla.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                tabla.repaint();
            }
        });
        tabla.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                renderer.setMouseState(-1, -1, false);
                tabla.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                tabla.repaint();
            }
            @Override
            public void mousePressed(MouseEvent e) {
                int row = tabla.rowAtPoint(e.getPoint());
                int col = tabla.columnAtPoint(e.getPoint());
                if (col == columnaBoton && row >= 0) {
                    renderer.setMouseState(row, col, true);
                    tabla.repaint();
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                int row = tabla.rowAtPoint(e.getPoint());
                int col = tabla.columnAtPoint(e.getPoint());
                if (col == columnaBoton && row >= 0) renderer.setMouseState(row, col, false);
                else renderer.setMouseState(-1, -1, false);
                tabla.repaint();
            }
        });
    }

    private JButton crearBoton(String texto, Color bg, Color fg) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));  // Sin "Emoji" para más profesional
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Removido roundRect para look más empresarial y cuadrado
        return btn;
    }

    private JTable estilizarTabla(DefaultTableModel modelo, boolean esOscuro) {
        JTable t = new JTable(modelo);
        t.setRowHeight(40);
        t.setShowVerticalLines(false);
        t.setShowHorizontalLines(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFocusable(false); 
        t.setRowSelectionAllowed(false);
        t.setCellSelectionEnabled(false);
        if (esOscuro) {
            t.setBackground(new Color(45, 45, 45));
            t.setForeground(new Color(220, 220, 220));
            t.setGridColor(new Color(60, 60, 60));
            t.getTableHeader().setBackground(new Color(35, 35, 35));
            t.getTableHeader().setForeground(Color.LIGHT_GRAY);
            t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        }
        return t;
    }

    // EN: PanelVentas.java

public void configurarModoLite() {
    // Ocultar la columna "Stock" (Índice 3)
    TableColumn columnaStock = tablaProductos.getColumnModel().getColumn(3);
    columnaStock.setMinWidth(0);
    columnaStock.setMaxWidth(0);
    columnaStock.setWidth(0);
}
}