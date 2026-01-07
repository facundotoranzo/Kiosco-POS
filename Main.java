import com.formdev.flatlaf.FlatDarkLaf; 
import vista.*;
import controlador.*;
import modelo.ConexionDB;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class Main {
    
    public static void main(String[] args) {
        
        ConexionDB.inicializarBD();

        configurarEstiloVisual();

        String licenciaActual = ConexionDB.licencia;

        if (licenciaActual.equals("LITE")) {
            // LITE: Acceso directo sin autenticación
            modelo.Sesion.guardarSesion("Administrador Lite", "ADMIN");
            iniciarSistema("ADMIN");
        } else {
            // PRO/RED: Sistema completo con autenticación
            modelo.UsuarioDAO usuarioDao = new modelo.UsuarioDAO();
            
            if (!usuarioDao.hayUsuariosRegistrados()) {
                DialogoPrimerUso primerUso = new DialogoPrimerUso();
                primerUso.setVisible(true);
                if (!primerUso.isRegistroExitoso()) System.exit(0);
            }

            if (modelo.Sesion.recuperarSesion()) {
                iniciarSistema(modelo.Sesion.getRol());
            } else {
                DialogoLogin login = new DialogoLogin();
                login.setVisible(true); 
                
                String rol = login.getRolDetectado();
                if (rol != null) {
                    modelo.Sesion.guardarSesion(login.getUsuarioIngresado(), rol);
                    iniciarSistema(rol);
                } else {
                    System.exit(0);
                }
            }
        }
    }

    /**
     * Configura el tema visual oscuro y fuentes del sistema
     */
    private static void configurarEstiloVisual() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
            Font fuenteEmojis = new Font("Segoe UI Emoji", Font.PLAIN, 14);
            Font fuenteEmojisBold = new Font("Segoe UI Emoji", Font.BOLD, 14);
            UIManager.put("Button.font", fuenteEmojisBold);
            UIManager.put("Label.font", fuenteEmojis);
            UIManager.put("TextField.font", fuenteEmojis);
            UIManager.put("Table.font", fuenteEmojis);
            UIManager.put("TableHeader.font", fuenteEmojisBold);
            UIManager.put("MenuItem.font", fuenteEmojis);
            UIManager.put("Menu.font", fuenteEmojis);
            UIManager.put("TabbedPane.font", fuenteEmojis);
        } catch (Exception e) {}
    }

    /**
     * Inicializa la interfaz principal con pestañas según el rol y licencia
     * 
     * @param rol Rol del usuario (ADMIN/EMPLEADO) - determina pestañas disponibles
     */
    private static void iniciarSistema(String rol) {
        SwingUtilities.invokeLater(() -> {
            String licencia = ConexionDB.licencia;
            
            String titulo = String.format("Kiosco Manager %s | %s", licencia, modelo.Sesion.getUsuario());
            JFrame frame = new JFrame(titulo);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1280, 720);
            frame.setLocationRelativeTo(null);
            
            // Panel principal con BorderLayout
            JPanel mainPanel = new JPanel(new BorderLayout());
            
            JTabbedPane tabs = new JTabbedPane();
            
            // Pestaña Ventas: Disponible para todos los roles
            PanelVentas pVentas = new PanelVentas();
            if (licencia.equals("LITE")) pVentas.configurarModoLite();
            ControladorVentas cVentas = new ControladorVentas(pVentas);
            tabs.addTab("🛒 Punto de Venta", pVentas);

            // Pestañas administrativas: Solo ADMIN en PRO/RED
            if (rol != null && rol.trim().equalsIgnoreCase("ADMIN") && !licencia.equals("LITE")) {
                
                PanelCajas pCajas = new PanelCajas();
                ControladorCajas cCajas = new ControladorCajas(pCajas);
                tabs.addTab("📜 Historial de Cajas", pCajas);
                
                cVentas.setControladorCajas(cCajas);
                
                PanelEstadisticas pStats = new PanelEstadisticas();
                ControladorEstadisticas cStats = new ControladorEstadisticas(pStats);
                tabs.addTab("📊 Estadísticas", pStats);

                PanelCompras pCompras = new PanelCompras();
                ControladorCompras cCompras = new ControladorCompras(pCompras);
                tabs.addTab("🚚 Compras / Gastos", pCompras);
                
                // Actualización automática al cambiar pestañas
                tabs.addChangeListener(e -> {
                    Component seleccionado = tabs.getSelectedComponent();
                    if (seleccionado == pStats) cStats.cargarDatos();
                    if (seleccionado == pCompras) cCompras.actualizarPantalla();
                    if (seleccionado == pCajas) cCajas.cargarCajas();
                });

            } else {
                cVentas.setControladorCajas(null);
            }

            // Pestaña Configuración: Visible para todos con restricciones internas
            PanelConfiguracion pConfig = new PanelConfiguracion();
            new ControladorConfiguracion(pConfig, rol);
            tabs.addTab("⚙️ Ajustes", pConfig);
            
            // Barra de estado inferior
            BarraEstado barraEstado = new BarraEstado();
            
            // Ensamblar panel principal
            mainPanel.add(tabs, BorderLayout.CENTER);
            mainPanel.add(barraEstado, BorderLayout.SOUTH);
            
            frame.add(mainPanel);
            
            // Configurar atajos de teclado globales
            configurarAtajosTeclado(frame, pVentas, cVentas);
            
            try { modelo.GestorFuentes.forzarFuente(frame); } catch(Exception e){}

            frame.setVisible(true);
            
            // Limpiar recursos al cerrar
            frame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    barraEstado.detener();
                    cVentas.limpiarRecursos();
                }
            });
        });
    }
    
    /**
     * Configura los atajos de teclado globales
     * Los atajos se recargan dinámicamente desde AtajosManager
     */
    private static void configurarAtajosTeclado(JFrame frame, PanelVentas pVentas, ControladorVentas cVentas) {
        JRootPane rootPane = frame.getRootPane();
        InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = rootPane.getActionMap();
        
        // Acciones que verifican el atajo actual cada vez que se presiona una tecla
        // Esto permite que los cambios en configuración se apliquen sin reiniciar
        
        // Registrar todas las teclas F1-F12 y verificar dinámicamente
        for (int i = KeyEvent.VK_F1; i <= KeyEvent.VK_F12; i++) {
            final int keyCode = i;
            String actionName = "f" + (i - KeyEvent.VK_F1 + 1);
            
            inputMap.put(KeyStroke.getKeyStroke(keyCode, 0), actionName);
            actionMap.put(actionName, new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    ejecutarAtajo(keyCode, pVentas);
                }
            });
        }
        
        // También registrar teclas comunes que podrían usarse como atajos
        int[] teclasExtra = {
            KeyEvent.VK_ESCAPE, KeyEvent.VK_ENTER, KeyEvent.VK_SPACE,
            KeyEvent.VK_INSERT, KeyEvent.VK_DELETE, KeyEvent.VK_HOME, KeyEvent.VK_END
        };
        
        for (int keyCode : teclasExtra) {
            String actionName = "key" + keyCode;
            inputMap.put(KeyStroke.getKeyStroke(keyCode, 0), actionName);
            final int kc = keyCode;
            actionMap.put(actionName, new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    ejecutarAtajo(kc, pVentas);
                }
            });
        }
    }
    
    /**
     * Ejecuta la acción correspondiente al atajo presionado
     * Verifica dinámicamente la configuración actual
     */
    private static void ejecutarAtajo(int keyCode, PanelVentas pVentas) {
        // Verificar qué acción corresponde a esta tecla (lectura dinámica)
        if (keyCode == modelo.AtajosManager.getAtajoCobrar()) {
            pVentas.btnCobrar.doClick();
        } else if (keyCode == modelo.AtajosManager.getAtajoManual()) {
            pVentas.btnManual.doClick();
        } else if (keyCode == modelo.AtajosManager.getAtajoBuscar()) {
            pVentas.txtBuscar.requestFocus();
            pVentas.txtBuscar.selectAll();
        } else if (keyCode == modelo.AtajosManager.getAtajoNuevoProducto()) {
            pVentas.btnNuevoProducto.doClick();
        } else if (keyCode == modelo.AtajosManager.getAtajoVerCaja()) {
            pVentas.btnVerCaja.doClick();
        }
        // Si no coincide con ningún atajo configurado, no hace nada
    }
}