package controlador;

import modelo.*;
import vista.PanelVentas;
import vista.ToastNotification;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.awt.*;

/**
 * Controlador principal del módulo de ventas
 * Maneja la lógica de productos, carrito, cobros y sincronización entre terminales
 */
public class ControladorVentas {

    private PanelVentas vista;
    private ProductoDAO productoDao;
    private VentaDAO ventaDao;
    private CajaDAO cajaDao;
    private ControladorCajas controladorCajas; 
    
    private double totalVenta = 0.0;
    private double recargoPct = 0.0;
    private int idCajaActual;
    
    private List<Producto> listaProductosCache; 
    private Timer timerBusqueda;
    private String modoCarrito = "LOCAL"; 
    private Timer timerReceptor;
    private boolean imprimirTicketActivo = true;
    private Timer timerCarritoCompartido;
    private boolean usarCarritoCompartido = false;


    /**
     * Constructor principal del controlador de ventas
     * Inicializa DAOs, configuración y listeners
     */
    public ControladorVentas(PanelVentas vista) {
        this.vista = vista;
        this.productoDao = new ProductoDAO();
        this.ventaDao = new VentaDAO();
        this.cajaDao = new CajaDAO();

        this.idCajaActual = cajaDao.obtenerOIniciarCaja();
        
        cargarConfiguracion();
        cargarDatosPrueba(); 
        actualizarTablaProductos("");
        initListeners();
        configurarModoCarrito();
    }
    
    /**
     * Carga la configuración desde el archivo properties
     */
    private void cargarConfiguracion() {
        try {
            File f = new File("db_config.properties");
            if (f.exists()) {
                Properties prop = new Properties();
                prop.load(new FileInputStream(f));
                
                // Leer todas las configuraciones
                modoCarrito = prop.getProperty("modo_carrito", "LOCAL").toUpperCase();
                String ticketConfig = prop.getProperty("imprimir_ticket", "true").trim();
                imprimirTicketActivo = Boolean.parseBoolean(ticketConfig);
                String compartido = prop.getProperty("carrito_compartido", "false").trim();
                usarCarritoCompartido = Boolean.parseBoolean(compartido);
                
                Logger.info("Configuración cargada: carrito=" + modoCarrito + 
                           ", ticket=" + imprimirTicketActivo + 
                           ", compartido=" + usarCarritoCompartido);
            }
        } catch (Exception e) {
            Logger.error("Error al cargar configuración", e);
        }
    }
    
    /**
     * Configura el modo de carrito según la configuración cargada
     */
    private void configurarModoCarrito() {
        if (modoCarrito.equals("EMISOR")) {
            vista.btnCobrar.setText("📤 ENVIAR A CAJA");
            vista.btnCobrar.setBackground(new Color(255, 140, 0));
            // Limpiar listeners existentes
            for (java.awt.event.ActionListener al : vista.btnCobrar.getActionListeners()) {
                vista.btnCobrar.removeActionListener(al);
            }
            vista.btnCobrar.addActionListener(e -> enviarPedidoACaja());
            
        } else if (modoCarrito.equals("RECEPTOR")) {
            timerReceptor = new Timer(2000, e -> revisarBuzonCompartido());
            timerReceptor.start();
            
        } else if (usarCarritoCompartido) {
            // Modo carrito compartido
            vista.btnCobrar.setText("☁️ COBRAR (RED)");
            vista.btnCobrar.setBackground(new Color(100, 149, 237)); // Azul Cornflower
            
            timerCarritoCompartido = new Timer(2000, e -> sincronizarCarritoDesdeBD());
            timerCarritoCompartido.start();
        }
    }
    
    /**
     * Método para limpiar recursos al cerrar
     */
    public void limpiarRecursos() {
        if (timerBusqueda != null && timerBusqueda.isRunning()) {
            timerBusqueda.stop();
        }
        if (timerReceptor != null && timerReceptor.isRunning()) {
            timerReceptor.stop();
        }
        if (timerCarritoCompartido != null && timerCarritoCompartido.isRunning()) {
            timerCarritoCompartido.stop();
        }
        Logger.info("Recursos de ControladorVentas limpiados");
    }
    /**
     * Sincroniza el carrito local con la base de datos compartida
     * Evita parpadeos innecesarios comparando totales antes de actualizar
     */
    private void sincronizarCarritoDesdeBD() {
        if (vista.txtPagaCon.hasFocus()) return;

        try (Connection c = ConexionDB.conectar(); Statement s = c.createStatement()) {
            ResultSet rs = s.executeQuery("SELECT * FROM carrito_compartido");
            
            List<Object[]> datosNuevos = new ArrayList<>();
            double totalNuevo = 0;
            
            while(rs.next()) {
                String nombre = rs.getString("nombre_producto");
                double precio = rs.getDouble("precio");
                datosNuevos.add(new Object[]{1, nombre, precio, "➖"});
                totalNuevo += precio;
            }

            // Validación anti-parpadeo: solo actualizar si hay cambios reales
            double totalActual = 0;
            try {
                 String textoTotal = vista.lblTotal.getText().replace("$", "").replace(".", "").replace(",", ".").trim();
                 totalActual = Double.parseDouble(textoTotal);
            } catch(Exception e) { totalActual = -1; }

            if (Math.abs(totalNuevo - totalActual) > 0.01 || datosNuevos.size() != vista.modeloCarrito.getRowCount()) {
                vista.modeloCarrito.setRowCount(0);
                for (Object[] fila : datosNuevos) {
                    vista.modeloCarrito.addRow(fila);
                }
                calcularTotal();
            }
            
        } catch(Exception e) {
            System.out.println("Error sync carrito: " + e.getMessage());
        }
    }
    
    public void setControladorCajas(ControladorCajas controladorCajas) {
        this.controladorCajas = controladorCajas;
    }

    /**
     * Inicializa todos los listeners de eventos de la interfaz
     * Configura búsqueda, clics en tablas, botones y timers de actualización
     */
    private void initListeners() {
        timerBusqueda = new Timer(300, e -> actualizarTablaProductos(vista.txtBuscar.getText()));
        timerBusqueda.setRepeats(false);

        vista.txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    timerBusqueda.stop();
                    String codigoEscaneado = vista.txtBuscar.getText().trim();
                    boolean productoEncontrado = false;

                    for (int i = 0; i < listaProductosCache.size(); i++) {
                        Producto p = listaProductosCache.get(i);
                        if (String.valueOf(p.getCodigo()).equals(codigoEscaneado)) {
                            validarYAgregarProducto(p.getCodigo(), p.getNombre(), p.getPrecio(), p.getStock(), i);
                            vista.txtBuscar.setText(""); 
                            productoEncontrado = true;
                            break;
                        }
                    }
                    if (!productoEncontrado) {
                        actualizarTablaProductos(codigoEscaneado);
                    }
                } else {
                    timerBusqueda.restart(); 
                }
            }
        });

        vista.tablaProductos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    int row = vista.tablaProductos.rowAtPoint(e.getPoint());
                    int col = vista.tablaProductos.columnAtPoint(e.getPoint());
                    if (row >= 0 && (col == 4 || e.getClickCount() == 2)) {
                        agregarDesdeTabla(row);
                    }
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) {
                    int row = vista.tablaProductos.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        vista.tablaProductos.setRowSelectionInterval(row, row);
                        vista.menuContextual.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });

        vista.tablaCarrito.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = vista.tablaCarrito.rowAtPoint(e.getPoint());
                int col = vista.tablaCarrito.columnAtPoint(e.getPoint());
                if (row >= 0 && col == 3) {
                    restarOQuitarDelCarrito(row);
                }
            }
        });

        vista.txtPagaCon.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) { calcularVuelto(); }
        });

        vista.btnNuevoProducto.addActionListener(e -> abrirVentanaNuevoProducto());
        
        vista.btnManual.addActionListener(e -> {
            String modoActual = "NORMAL"; 
            try {
                File f = new File("db_config.properties");
                if (f.exists()) {
                    Properties prop = new Properties();
                    prop.load(new FileInputStream(f));
                    modoActual = prop.getProperty("modo_manual", "NORMAL").trim().toUpperCase();
                }
            } catch (Exception ex) {}

            if (modoActual.equals("GRANDE")) {
                mostrarSelectorManual(); 
            } else {
                mostrarManualNormal();   
            }
        });

        vista.btnCerrarCaja.addActionListener(e -> intentarCerrarCaja()); 
        vista.btnVerCaja.addActionListener(e -> verCajaActual());
        vista.btnCobrar.addActionListener(e -> cobrarVenta());
        vista.btnTarjeta.addActionListener(e -> aplicarRecargo());
        vista.itemEliminar.addActionListener(e -> eliminarProducto());
        vista.itemEditar.addActionListener(e -> abrirVentanaEditar());

        // Timer de refresco automático con preservación de scroll
        Timer timerRefresco = new Timer(5000, e -> {
            if (vista.txtBuscar.getText().trim().isEmpty()) {
                JScrollPane scroll = (JScrollPane) vista.tablaProductos.getParent().getParent();
                Point posicionGuardada = scroll.getViewport().getViewPosition();
                
                actualizarTablaProductos("");
                
                SwingUtilities.invokeLater(() -> {
                    try {
                        scroll.getViewport().setViewPosition(posicionGuardada);
                    } catch(Exception ex) {}
                });
            }
        });
        timerRefresco.start();
    }

    private void verCajaActual() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(vista);
        new vista.DialogoDetalleCaja(frame, idCajaActual, cajaDao).setVisible(true);
    }

    /**
     * Procesa la venta completa de forma directa y simple
     */
    private void cobrarVenta() {
        DefaultTableModel carrito = vista.modeloCarrito;
        if (carrito.getRowCount() == 0) {
            JOptionPane.showMessageDialog(vista, "El carrito está vacío.", "Carrito Vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Definir medio de pago
        String medioPago = determinarMedioPago();
        if (medioPago == null) return; // Usuario canceló

        // Preparar objetos para la Base de Datos
        List<Venta.DetalleVenta> items = prepararItemsVenta(carrito);
        Venta nuevaVenta = new Venta(totalVenta, medioPago, items);
        
        // Intentar guardar con mejor manejo de errores
        procesarVentaFinal(nuevaVenta, medioPago, carrito);
    }

    /**
     * Determina el medio de pago simplificado
     * @return Medio de pago seleccionado o null si canceló
     */
    private String determinarMedioPago() {
        String[] opciones = {"💵 Efectivo", "📲 Transferencia"};
        int seleccion = JOptionPane.showOptionDialog(vista, 
            "💰 Total a Pagar: " + vista.lblTotal.getText() + "\n\n🔄 Seleccione medio de pago:", 
            "Medio de Pago", 
            JOptionPane.DEFAULT_OPTION, 
            JOptionPane.QUESTION_MESSAGE, 
            null, 
            opciones, 
            opciones[0]);

        switch (seleccion) {
            case 0: return "Efectivo";
            case 1: return "Transferencia";
            default: return null; // Canceló
        }
    }

    /**
     * Prepara los items de venta desde el carrito
     */
    private List<Venta.DetalleVenta> prepararItemsVenta(DefaultTableModel carrito) {
        List<Venta.DetalleVenta> items = new ArrayList<>();
        for (int i = 0; i < carrito.getRowCount(); i++) {
            int cant = (int) carrito.getValueAt(i, 0);
            String nom = (String) carrito.getValueAt(i, 1);
            double tot = Double.parseDouble(carrito.getValueAt(i, 2).toString());
            items.add(new Venta.DetalleVenta(nom, tot / cant, cant));
        }
        return items;
    }

    /**
     * Procesa la venta final con mejor manejo de errores y feedback
     */
    private void procesarVentaFinal(Venta nuevaVenta, String medioPago, DefaultTableModel carrito) {
        try {
            if (ventaDao.registrarVenta(idCajaActual, nuevaVenta)) {
                
                // Éxito: Mostrar confirmación y manejar ticket
                manejarVentaExitosa(medioPago, carrito);
                
                // Limpiar carrito compartido si está activo
                if (usarCarritoCompartido) {
                    limpiarCarritoCompartido();
                }

                limpiarInterfazVenta();
                
            } else {
                // Error en la venta
                ToastNotification.error(vista, "❌ Error al procesar la venta - Verifique la conexión");
                Logger.error("Error al registrar venta en BD");
            }
        } catch (Exception e) {
            // Error crítico
            ToastNotification.error(vista, "❌ Error crítico - Contacte al administrador");
            Logger.error("Error crítico procesando venta", e);
            
            JOptionPane.showMessageDialog(vista, 
                "Error crítico al procesar la venta.\n" +
                "Los datos no se perdieron.\n" +
                "Contacte al administrador del sistema.", 
                "Error Crítico", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Maneja una venta exitosa con feedback mejorado y auditoría completa
     */
    private void manejarVentaExitosa(String medioPago, DefaultTableModel carrito) {
        // Feedback visual mejorado
        String iconoMedio = medioPago.equals("Efectivo") ? "💵" : 
                           medioPago.equals("Transferencia") ? "📲" : "💳";
        
        ToastNotification.exito(vista, 
            String.format("✅ Venta procesada %s %s - %s", 
                iconoMedio, medioPago, vista.lblTotal.getText()));

        // Sonido de confirmación
        modelo.SonidoManager.reproducirVentaExitosa();

        if (imprimirTicketActivo) {
            manejarImpresionTicket(medioPago, carrito);
        }
        
        // MEJORADO: Auditoría completa de la venta
        Logger.logVenta(totalVenta, medioPago, carrito.getRowCount(), idCajaActual);
        
        // Log detallado de productos vendidos
        StringBuilder productosVendidos = new StringBuilder();
        for (int i = 0; i < carrito.getRowCount(); i++) {
            int cant = (int) carrito.getValueAt(i, 0);
            String nom = (String) carrito.getValueAt(i, 1);
            double precio = Double.parseDouble(carrito.getValueAt(i, 2).toString());
            
            if (i > 0) productosVendidos.append(", ");
            productosVendidos.append(String.format("%dx %s ($%.2f)", cant, nom, precio));
        }
        
        Logger.auditoria("DETALLE_VENTA", 
            String.format("Productos: [%s] | Total: $%.2f | Medio: %s", 
                productosVendidos.toString(), totalVenta, medioPago));
    }

    /**
     * Maneja la impresión de tickets con mejor UX
     */
    private void manejarImpresionTicket(String medioPago, DefaultTableModel carrito) {
        String[] opcionesTicket = {"🖨️ Imprimir Ticket", "📱 Solo Digital", "❌ Sin Ticket"};
        int respuestaTicket = JOptionPane.showOptionDialog(vista, 
            "¿Cómo desea el comprobante?", 
            "Comprobante de Venta", 
            JOptionPane.DEFAULT_OPTION, 
            JOptionPane.QUESTION_MESSAGE, 
            null, 
            opcionesTicket, 
            opcionesTicket[0]);

        if (respuestaTicket == 0) { // Imprimir
            generarEImprimirTicket(medioPago, carrito);
        } else if (respuestaTicket == 1) { // Solo digital
            ToastNotification.info(vista, "📱 Comprobante digital registrado");
        }
    }

    /**
     * Genera e imprime el ticket con formato mejorado
     */
    private void generarEImprimirTicket(String medioPago, DefaultTableModel carrito) {
        try {
            StringBuilder ticket = new StringBuilder();
            
            // Encabezado mejorado
            ticket.append("================================\n");
            ticket.append("         🏪 KIOSCO SYSTEM       \n");
            ticket.append("================================\n");
            ticket.append("Fecha: ").append(java.time.LocalDate.now()).append("\n");
            ticket.append("Hora:  ").append(java.time.LocalTime.now().toString().substring(0,5)).append("\n");
            ticket.append("Caja:  #").append(idCajaActual).append("\n");
            ticket.append("Cajero: ").append(modelo.Sesion.getUsuario()).append("\n");
            ticket.append("--------------------------------\n");
            ticket.append("CANT  PRODUCTO           TOTAL\n");
            ticket.append("--------------------------------\n");
            
            // Productos con formato mejorado
            for (int i = 0; i < carrito.getRowCount(); i++) {
                int c = (int) carrito.getValueAt(i, 0);
                String n = carrito.getValueAt(i, 1).toString();
                if(n.length() > 15) n = n.substring(0, 15);
                double t = Double.parseDouble(carrito.getValueAt(i, 2).toString());
                
                ticket.append(String.format("%2d x %-15s $%,.0f\n", c, n, t));
            }
            
            // Totales mejorados
            ticket.append("--------------------------------\n");
            if (recargoPct > 0) {
                double subtotal = totalVenta / (1 + recargoPct);
                double recargo = totalVenta - subtotal;
                ticket.append(String.format("Subtotal:            $%,.0f\n", subtotal));
                ticket.append(String.format("Recargo (%.0f%%):        $%,.0f\n", recargoPct * 100, recargo));
            }
            ticket.append(String.format("TOTAL:               $%,.0f\n", totalVenta));
            ticket.append("Medio: ").append(medioPago).append("\n");
            ticket.append("================================\n");
            ticket.append("    ¡Gracias por su compra!    \n");
            ticket.append("================================\n\n\n");
            
            modelo.TicketImpresora.imprimir(ticket.toString());
            ToastNotification.info(vista, "🖨️ Ticket enviado a impresora");
            
        } catch (Exception e) {
            Logger.error("Error generando ticket", e);
            ToastNotification.error(vista, "❌ Error al generar ticket");
        }
    }

    /**
     * Limpia el carrito compartido de forma segura
     */
    private void limpiarCarritoCompartido() {
        try (Connection c = ConexionDB.conectar(); Statement s = c.createStatement()) {
            s.executeUpdate("DELETE FROM carrito_compartido");
        } catch (Exception e) {
            Logger.error("Error limpiando carrito compartido", e);
        }
    }

    private void limpiarInterfazVenta() {
        vista.modeloCarrito.setRowCount(0);
        recargoPct = 0.0;
        totalVenta = 0.0;
        vista.txtPagaCon.setText("");
        vista.lblVuelto.setText(Formato.moneda(0));
        vista.lblTotal.setText(Formato.moneda(0));
        calcularTotal();
    }

    // ... EL RESTO DE TUS MÉTODOS DE AGREGAR PRODUCTO, EDITAR, ETC, SIGUEN IGUAL ...
    // ... CÓPIALOS TAL CUAL LOS TENÍAS O ÚSALOS DE TU BACKUP SI LOS BORRASTE ...
    
    // Para no hacer el mensaje eterno, asumo que tienes los metodos:
    // agregarDesdeTabla, agregarAlCarrito, restarOQuitarDelCarrito, actualizarStockVisual,
    // devolverTodoElStockDelCarrito, intentarCerrarCaja, cerrarCajaDefinitivo, actualizarTablaProductos,
    // agregarProductoManual, agregarAlCarritoVisual, buscarCodigoPorNombre, eliminarProducto,
    // abrirVentanaEditar, abrirVentanaNuevoProducto, cargarDatosPrueba, calcularTotal, calcularVuelto,
    // aplicarRecargo, mostrarSelectorManual, mostrarFormularioManual, enviarPedidoACaja, revisarBuzonCompartido, mostrarManualNormal
    
    // (Asegúrate de no borrar esos métodos privados que están más abajo en tu archivo original)
    
    // Solo te pego estos que son criticos para que compile:
    private void agregarDesdeTabla(int row) {
        long codigo = (long) vista.modeloProductos.getValueAt(row, 0);
        String nombre = (String) vista.modeloProductos.getValueAt(row, 1);
        double precio = (double) vista.modeloProductos.getValueAt(row, 2);
        int stockDisponible = (int) vista.modeloProductos.getValueAt(row, 3); 

        validarYAgregarProducto(codigo, nombre, precio, stockDisponible, row);
    }

    /**
     * Valida stock disponible antes de agregar producto al carrito
     * En LITE: stock ilimitado. En PRO/RED: validación con opciones de exceder stock
     */
    private void validarYAgregarProducto(long codigo, String nombre, double precio, int stockDisponible, int rowTablaProductos) {
        if (ConexionDB.licencia.equals("LITE")) {
            agregarAlCarrito(codigo, nombre, precio, stockDisponible, rowTablaProductos);
            return;
        }

        int cantidadEnCarrito = obtenerCantidadEnCarrito(nombre);
        int stockRestante = stockDisponible - cantidadEnCarrito;

        if (stockRestante > 0) {
            agregarAlCarrito(codigo, nombre, precio, stockDisponible, rowTablaProductos);
        } else if (stockRestante == 0) {
            mostrarDialogoStockExcedido(codigo, nombre, precio, stockDisponible, cantidadEnCarrito, rowTablaProductos);
        } else {
            mostrarDialogoStockExcedido(codigo, nombre, precio, stockDisponible, cantidadEnCarrito, rowTablaProductos);
        }
    }

    private int obtenerCantidadEnCarrito(String nombreProducto) {
        DefaultTableModel carrito = vista.modeloCarrito;
        for (int i = 0; i < carrito.getRowCount(); i++) {
            String nombreEnCarrito = (String) carrito.getValueAt(i, 1);
            if (nombreEnCarrito.equals(nombreProducto)) {
                return (int) carrito.getValueAt(i, 0);
            }
        }
        return 0;
    }

    private void mostrarDialogoStockExcedido(long codigo, String nombre, double precio, int stockDisponible, int cantidadEnCarrito, int row) {
        String mensaje = String.format(
            "⚠️ ATENCIÓN: Stock Insuficiente\n\n" +
            "Producto: %s\n" +
            "Stock disponible: %d\n" +
            "Ya en carrito: %d\n" +
            "Stock restante: %d\n\n" +
            "¿Desea agregar de todas formas?\n" +
            "(Esto dejará el stock en negativo)",
            nombre, stockDisponible, cantidadEnCarrito, stockDisponible - cantidadEnCarrito
        );

        String[] opciones = {
            "✅ Sí, Agregar (Stock Negativo)", 
            "📦 Actualizar Stock Primero", 
            "❌ Cancelar"
        };

        int respuesta = JOptionPane.showOptionDialog(
            vista, 
            mensaje, 
            "Confirmación de Stock", 
            JOptionPane.DEFAULT_OPTION, 
            JOptionPane.WARNING_MESSAGE, 
            null, 
            opciones, 
            opciones[1] // Por defecto "Actualizar Stock"
        );

        switch (respuesta) {
            case 0: // Agregar con stock negativo
                Logger.warn(String.format("Stock negativo autorizado para %s. Stock: %d, En carrito: %d", 
                    nombre, stockDisponible, cantidadEnCarrito + 1));
                agregarAlCarrito(codigo, nombre, precio, stockDisponible, row);
                break;
                
            case 1: // Actualizar stock primero
                abrirDialogoActualizarStock(codigo, nombre, precio, stockDisponible, row);
                break;
                
            case 2: // Cancelar
            default:
                // No hacer nada
                break;
        }
    }

    private void abrirDialogoActualizarStock(long codigo, String nombre, double precio, int stockActual, int row) {
        String input = JOptionPane.showInputDialog(
            vista,
            String.format("Actualizar Stock de: %s\n\nStock actual: %d\nIngrese el nuevo stock:", nombre, stockActual),
            "Actualizar Stock",
            JOptionPane.QUESTION_MESSAGE
        );

        if (input != null && !input.trim().isEmpty()) {
            try {
                int nuevoStock = Integer.parseInt(input.trim());
                if (nuevoStock >= 0) {
                    // Actualizar en la base de datos
                    Producto productoActualizado = new Producto(codigo, nombre, precio, nuevoStock, false);
                    if (productoDao.actualizar(productoActualizado)) {
                        // Actualizar visualmente
                        vista.modeloProductos.setValueAt(nuevoStock, row, 3);
                        Logger.info(String.format("Stock actualizado para %s: %d -> %d", nombre, stockActual, nuevoStock));
                        
                        // Ahora intentar agregar al carrito de nuevo
                        agregarDesdeTabla(row);
                    } else {
                        JOptionPane.showMessageDialog(vista, "Error al actualizar el stock en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(vista, "El stock no puede ser negativo.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(vista, "Ingrese un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Agrega producto al carrito visual y sincroniza con BD compartida si está activa
     * El stock se valida en la transacción de venta, no al agregar al carrito
     */
    private void agregarAlCarrito(long codigo, String nombre, double precio, int stockMaximo, int rowTablaProductos) {
        DefaultTableModel carrito = vista.modeloCarrito;
        
        if (!ConexionDB.licencia.equals("LITE") && stockMaximo <= 0) {
            JOptionPane.showMessageDialog(vista, "¡No hay stock disponible!");
            return;
        }

        boolean existeEnCarrito = false;
        for (int i = 0; i < carrito.getRowCount(); i++) {
            if (carrito.getValueAt(i, 1).toString().equals(nombre)) {
                int cantActual = (int) carrito.getValueAt(i, 0);
                carrito.setValueAt(cantActual + 1, i, 0);
                carrito.setValueAt((cantActual + 1) * precio, i, 2);
                existeEnCarrito = true;
                break;
            }
        }
        if (!existeEnCarrito) {
            carrito.addRow(new Object[]{ 1, nombre, precio, "➖" });
        }
        calcularTotal();

        // Sincronizar con carrito compartido si está activo
        if (usarCarritoCompartido) {
            try (Connection c = ConexionDB.conectar(); 
                 PreparedStatement ps = c.prepareStatement("INSERT INTO carrito_compartido(nombre_producto, precio) VALUES(?,?)")) {
                ps.setString(1, nombre);
                ps.setDouble(2, precio);
                ps.executeUpdate();
            } catch (Exception e) {}
        }
    }
   
   
    /**
     * Remueve o reduce cantidad de producto del carrito
     * Sincroniza cambios con carrito compartido si está activo
     */
    private void restarOQuitarDelCarrito(int row) {
        DefaultTableModel carrito = vista.modeloCarrito;
        int cant = (int) carrito.getValueAt(row, 0);
        String nombre = (String) carrito.getValueAt(row, 1);
        double totalFila = (double) carrito.getValueAt(row, 2);
        double unitario = totalFila / cant;

        if (cant > 1) {
            carrito.setValueAt(cant - 1, row, 0);
            carrito.setValueAt((cant - 1) * unitario, row, 2);
        } else {
            carrito.removeRow(row);
        }
        calcularTotal();

        // Sincronizar con carrito compartido
        if (usarCarritoCompartido) {
            try (Connection c = ConexionDB.conectar(); 
                 PreparedStatement ps = c.prepareStatement("DELETE FROM carrito_compartido WHERE nombre_producto = ? LIMIT 1")) {
                ps.setString(1, nombre);
                ps.executeUpdate();
            } catch (Exception e) {
                System.out.println("Error al borrar del carrito compartido: " + e.getMessage());
            }
        }
    }
   
   private void devolverTodoElStockDelCarrito() {
       // CAMBIO: Como ya no descontamos stock al agregar al carrito,
       // este método solo necesita limpiar la interfaz
       // El stock se maneja completamente en la transacción de venta
   }
   
   private void intentarCerrarCaja() {
       if (vista.modeloCarrito.getRowCount() > 0) {
           String[] opciones = {"Cobrar y Cerrar", "Devolver Stock y Cerrar", "Cancelar"};
           int respuesta = JOptionPane.showOptionDialog(vista, "Hay productos pendientes.\n¿Qué desea hacer?", "Caja Pendiente", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, opciones, opciones[0]);
           if (respuesta == 0) { cobrarVenta(); if (vista.modeloCarrito.getRowCount() == 0) cerrarCajaDefinitivo(); }
           else if (respuesta == 1) { devolverTodoElStockDelCarrito(); limpiarInterfazVenta(); cerrarCajaDefinitivo(); }
       } else {
           if (JOptionPane.showConfirmDialog(vista, "¿Cerrar la caja actual?", "Cerrar Caja", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) cerrarCajaDefinitivo();
       }
   }

   private void cerrarCajaDefinitivo() {
       cajaDao.cerrarCaja(idCajaActual);
       JOptionPane.showMessageDialog(vista, "Caja cerrada correctamente.\nSe ha abierto una nueva sesión.");
       if (controladorCajas != null) controladorCajas.cargarCajas();
       idCajaActual = cajaDao.obtenerOIniciarCaja();
   }

    /**
     * MEJORADO: Actualiza la tabla de productos con búsqueda inteligente
     * Soporta búsqueda por código, nombre parcial y filtros avanzados
     */
    private void actualizarTablaProductos(String filtro) {
        try {
            listaProductosCache = productoDao.listar(filtro);
            DefaultTableModel modelo = vista.modeloProductos;
            modelo.setRowCount(0);
            
            // Contador para mostrar resultados
            int totalProductos = 0;
            int productosStockBajo = 0;
            
            for (Producto p : listaProductosCache) {
                // Agregar indicador visual de stock
                String indicadorStock = obtenerIndicadorStock(p);
                
                modelo.addRow(new Object[]{ 
                    p.getCodigo(), 
                    p.getNombre() + indicadorStock, 
                    p.getPrecio(), 
                    p.getStock(), 
                    "➕" 
                });
                
                totalProductos++;
                if (p.getStock() <= 5 && !ConexionDB.licencia.equals("LITE")) {
                    productosStockBajo++;
                }
            }
            
            // Mostrar estadísticas en la interfaz (si hay componente para ello)
            actualizarEstadisticasBusqueda(filtro, totalProductos, productosStockBajo);
            
        } catch (Exception e) {
            Logger.error("Error actualizando tabla productos", e);
            ToastNotification.error(vista, "Error cargando productos");
        }
    }

    /**
     * NUEVA FUNCIONALIDAD: Obtiene indicador visual de stock
     */
    private String obtenerIndicadorStock(Producto p) {
        // Sin indicadores de texto, solo colores en los números
        return "";
    }

    /**
     * NUEVA FUNCIONALIDAD: Actualiza estadísticas de búsqueda
     */
    private void actualizarEstadisticasBusqueda(String filtro, int total, int stockBajo) {
        // Si hay un label de estadísticas en la vista, actualizarlo
        try {
            if (filtro.trim().isEmpty()) {
                // Búsqueda general - Solo mostrar alerta de stock bajo si es crítico (más de 10 productos)
                if (stockBajo > 10) {
                    ToastNotification.warning(vista, 
                        String.format("ALERTA: %d productos con stock bajo (<=5 unidades)", stockBajo));
                }
            } else {
                // Búsqueda específica
                if (total == 0) {
                    ToastNotification.info(vista, "No se encontraron productos con: " + filtro);
                } else if (total == 1) {
                    ToastNotification.info(vista, "Producto encontrado");
                } else {
                    ToastNotification.info(vista, String.format("%d productos encontrados", total));
                }
            }
        } catch (Exception e) {
            // Ignorar errores de UI
        }
    }

   private void agregarAlCarritoVisual(String nombre, double precio) {
       DefaultTableModel carrito = vista.modeloCarrito;
       boolean existe = false;
       for (int i = 0; i < carrito.getRowCount(); i++) {
           if (carrito.getValueAt(i, 1).toString().equals(nombre)) {
               int cantActual = (int) carrito.getValueAt(i, 0);
               carrito.setValueAt(cantActual + 1, i, 0);
               carrito.setValueAt((cantActual + 1) * precio, i, 2);
               existe = true;
               break;
           }
       }
       if (!existe) carrito.addRow(new Object[]{ 1, nombre, precio, "➖" });
       calcularTotal();
   }

   private void eliminarProducto() {
       int row = vista.tablaProductos.getSelectedRow();
       if (row == -1) return;
       long codigo = (long) vista.modeloProductos.getValueAt(row, 0);
       if (productoDao.eliminar(codigo)) {
           actualizarTablaProductos("");
           JOptionPane.showMessageDialog(vista, "Producto eliminado.");
       }
   }

   private void abrirVentanaEditar() {
       int row = vista.tablaProductos.getSelectedRow();
       if (row == -1) {
           JOptionPane.showMessageDialog(vista, "Selecciona un producto de la tabla para editar.");
           return;
       }
       long codigo = (long) vista.modeloProductos.getValueAt(row, 0);
       boolean esCigarrillo = false;
       for (Producto p : listaProductosCache) {
           if (p.getCodigo() == codigo) { esCigarrillo = p.isEsCigarrillo(); break; }
       }
       String nombre = (String) vista.modeloProductos.getValueAt(row, 1);
       double precio = (double) vista.modeloProductos.getValueAt(row, 2);
       int stock = (int) vista.modeloProductos.getValueAt(row, 3);
       
       JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(vista);
       vista.DialogoEditarProducto d = new vista.DialogoEditarProducto(frame, codigo, nombre, precio, stock, esCigarrillo);
       
       d.btnGuardar.addActionListener(ev -> {
           try {
               int stockSumar = 0;
               try { stockSumar = Integer.parseInt(d.txtStockSumar.getText()); } catch (Exception e) { stockSumar = 0; }
               double nuevoPrecio = Double.parseDouble(d.txtPrecio.getText().replace(",", "."));
               boolean nuevoEsCigarrillo = d.chkCigarrillo.isSelected(); 
               Producto prodActualizado = new Producto(codigo, d.txtNombre.getText(), nuevoPrecio, stock + stockSumar, nuevoEsCigarrillo);

               if(productoDao.actualizar(prodActualizado)) {
                   actualizarTablaProductos(vista.txtBuscar.getText());
                   d.dispose();
                   JOptionPane.showMessageDialog(vista, "Producto actualizado correctamente.");
               }
           } catch (Exception ex) { JOptionPane.showMessageDialog(d, "Error en los datos: " + ex.getMessage()); }
       });
       d.btnCancelar.addActionListener(ev -> d.dispose());
       d.setVisible(true);
   }
   
    /**
     * Crea un nuevo producto con stock automático según licencia
     * LITE: stock fijo de 9999, PRO/RED: stock configurable
     */
    private void abrirVentanaNuevoProducto() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(vista);
        vista.DialogoProducto d = new vista.DialogoProducto(frame);
        d.btnGuardar.addActionListener(ev -> {
            try {
                long cod = Long.parseLong(d.txtCodigo.getText());
                String nom = d.txtNombre.getText();
                double pre = Double.parseDouble(d.txtPrecio.getText().replace(",", "."));
                boolean esCig = d.chkCigarrillo.isSelected();
                
                int stock = ConexionDB.licencia.equals("LITE") ? 9999 : Integer.parseInt(d.txtStock.getText());
                
                productoDao.insertar(new Producto(cod, nom, pre, stock, esCig));
                actualizarTablaProductos("");
                d.dispose();
            } catch (Exception ex) { 
                JOptionPane.showMessageDialog(d, "Error: Verifique los datos."); 
            }
        });
        d.setVisible(true);
    }

   private void cargarDatosPrueba() {
       if (productoDao.listar("").isEmpty()) productoDao.insertar(new Producto(101, "Coca Cola", 1500, 50));
   }

   private void calcularTotal() {
       double sub = 0.0;
       for (int i=0; i<vista.modeloCarrito.getRowCount(); i++) {
           sub += Double.parseDouble(vista.modeloCarrito.getValueAt(i, 2).toString());
       }
       totalVenta = sub * (1 + recargoPct);
       vista.lblTotal.setText(Formato.moneda(totalVenta));
       calcularVuelto();
   }

   private void calcularVuelto() {
       try {
           double pago = Double.parseDouble(vista.txtPagaCon.getText().replace(",", "."));
           double vuelto = pago - totalVenta;
           vista.lblVuelto.setForeground(vuelto < 0 ? new java.awt.Color(231, 76, 60) : new java.awt.Color(46, 204, 113));
           vista.lblVuelto.setText(Formato.moneda(vuelto));
       } catch (Exception e) { 
           vista.lblVuelto.setText(Formato.moneda(0)); 
       }
   }

   private void mostrarSelectorManual() {
       JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(vista), "Seleccione Tipo", true);
       dialog.setSize(500, 250);
       dialog.setLocationRelativeTo(vista);
       dialog.setLayout(new GridLayout(1, 2, 20, 20));
       JButton btnCarne = new JButton("🥩 CARNE");
       btnCarne.setFont(new Font("Segoe UI", Font.BOLD, 24));
       btnCarne.setBackground(new Color(200, 100, 100)); 
       btnCarne.setForeground(Color.WHITE);
       JButton btnOtros = new JButton("📝 OTROS");
       btnOtros.setFont(new Font("Segoe UI", Font.BOLD, 24));
       btnOtros.setBackground(new Color(100, 150, 200)); 
       btnOtros.setForeground(Color.WHITE);
       btnCarne.addActionListener(e -> { dialog.dispose(); mostrarFormularioManual("Carne"); });
       btnOtros.addActionListener(e -> { dialog.dispose(); mostrarFormularioManual(""); });
       dialog.add(btnCarne); dialog.add(btnOtros);
       modelo.GestorFuentes.forzarFuente(dialog);
       dialog.setVisible(true);
   }

   private void mostrarFormularioManual(String nombreFijo) {
        JDialog d = new JDialog((JFrame) SwingUtilities.getWindowAncestor(vista), "Agregar Manual", true);
        d.setSize(600, 450); 
        d.setLocationRelativeTo(vista); 
        d.setLayout(new BorderLayout());
        
        JPanel p = new JPanel(new GridLayout(2, 2, 20, 30));
        p.setBorder(BorderFactory.createEmptyBorder(40,40,40,40));
        
        Font fInput = new Font("Segoe UI", Font.PLAIN, 32);
        JTextField txtN = new JTextField(); txtN.setFont(fInput);
        JTextField txtP = new JTextField(); txtP.setFont(fInput);
        
        if(!nombreFijo.isEmpty()) { 
            txtN.setText(nombreFijo); 
            SwingUtilities.invokeLater(txtP::requestFocus); 
        } else { 
            SwingUtilities.invokeLater(txtN::requestFocus); 
        }
        
        p.add(new JLabel("Producto:")); p.add(txtN);
        p.add(new JLabel("Precio:")); p.add(txtP);
        
        JButton btn = new JButton("AGREGAR");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 22));
        btn.setBackground(new Color(46, 204, 113));
        btn.setForeground(Color.WHITE);
        btn.setPreferredSize(new Dimension(0, 70));
        
        btn.addActionListener(e -> {
            try {
                String n = txtN.getText().isEmpty() ? "Varios" : txtN.getText();
                String nombreFinal = n + " (Manual)"; 
                double val = Double.parseDouble(txtP.getText().replace(",", "."));
                
                // 1. Agregar visualmente
                agregarAlCarritoVisual(nombreFinal, val);
                
                // 2. GUARDAR EN BD
                try (Connection c = ConexionDB.conectar(); 
                     PreparedStatement ps = c.prepareStatement("INSERT INTO carrito_compartido(nombre_producto, precio) VALUES(?,?)")) {
                    
                    ps.setString(1, nombreFinal);
                    ps.setDouble(2, val);
                    ps.executeUpdate();
                    
                } catch (Exception ex) {
                    // --- AQUI ESTA EL CAMBIO: TE MOSTRARÁ EL ERROR EN PANTALLA ---
                    JOptionPane.showMessageDialog(d, "Error al guardar en RED:\n" + ex.getMessage(), "Error Sincronización", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
                
                d.dispose();
            } catch(Exception ex) { 
                JOptionPane.showMessageDialog(d, "Precio inválido"); 
            }
        });

        d.add(p, BorderLayout.CENTER); d.add(btn, BorderLayout.SOUTH);
        modelo.GestorFuentes.forzarFuente(d);
        d.setVisible(true);
    }

   private void enviarPedidoACaja() {
       if(vista.modeloCarrito.getRowCount()==0) return;
       try(Connection c = ConexionDB.conectar(); PreparedStatement ps = c.prepareStatement("INSERT INTO carrito_compartido(nombre_producto, precio) VALUES(?,?)")) {
           for(int i=0; i<vista.modeloCarrito.getRowCount(); i++) {
               ps.setString(1, vista.modeloCarrito.getValueAt(i, 1).toString());
               ps.setDouble(2, Double.parseDouble(vista.modeloCarrito.getValueAt(i, 2).toString()));
               ps.executeUpdate();
           }
           vista.modeloCarrito.setRowCount(0); calcularTotal();
           JOptionPane.showMessageDialog(vista, "✅ Enviado a Caja");
       } catch(Exception e) { JOptionPane.showMessageDialog(vista, "Error Red: " + e.getMessage()); }
   }

   private void revisarBuzonCompartido() {
       if(vista.txtPagaCon.hasFocus()) return;
       try(Connection c = ConexionDB.conectar(); Statement s = c.createStatement()) {
           ResultSet rs = s.executeQuery("SELECT * FROM carrito_compartido");
           boolean hay = false;
           while(rs.next()) {
               vista.modeloCarrito.addRow(new Object[]{1, "📨 " + rs.getString("nombre_producto"), rs.getDouble("precio"), "➖"});
               hay = true;
           }
           if(hay) { s.executeUpdate("DELETE FROM carrito_compartido"); calcularTotal(); Toolkit.getDefaultToolkit().beep(); }
       } catch(Exception e) {}
   }

   

   private void mostrarManualNormal() {
        JTextField n = new JTextField(); 
        JTextField p = new JTextField();
        Object[] msg = { "Nombre:", n, "Precio:", p };
        
        int op = JOptionPane.showConfirmDialog(vista, msg, "Agregar Manual", JOptionPane.OK_CANCEL_OPTION);
        
        if (op == JOptionPane.OK_OPTION) {
            try {
                if (!n.getText().isEmpty()) {
                    String nombre = n.getText() + " (Manual)";
                    // Reemplazamos coma por punto por si el usuario usa coma decimal
                    double precio = Double.parseDouble(p.getText().replace(",", "."));
                    
                    // 1. AGREGAR VISUALMENTE (Para que se vea ya)
                    agregarAlCarritoVisual(nombre, precio);
                    
                    // 2. ¡IMPORTANTE! GUARDAR EN BASE DE DATOS PARA QUE NO SE BORRE
                    if (usarCarritoCompartido) {
                        try (Connection c = ConexionDB.conectar(); 
                             PreparedStatement ps = c.prepareStatement("INSERT INTO carrito_compartido(nombre_producto, precio) VALUES(?,?)")) {
                            ps.setString(1, nombre);
                            ps.setDouble(2, precio);
                            ps.executeUpdate();
                        } catch (Exception e) {
                            System.out.println("Error al subir manual a la nube: " + e.getMessage());
                        }
                    }
                }
            } catch (Exception ex) { 
                JOptionPane.showMessageDialog(vista, "Precio inválido (Use solo números)", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void aplicarRecargo() {
        String[] op = {"0%", "10%", "15%", "20%"};
        int el = JOptionPane.showOptionDialog(vista, "Recargo", "Tarjeta", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, op, op[0]);
        if (el >= 0) {
            if(el==0) recargoPct=0; else if(el==1) recargoPct=0.10; else if(el==2) recargoPct=0.15; else recargoPct=0.20;
            calcularTotal();
        }
    }

   
}