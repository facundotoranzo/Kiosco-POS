package controlador;

import vista.PanelConfiguracion;
import vista.DialogoVisualizarTicket;
import vista.DialogoAtajos;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import modelo.ConexionDB;

/**
 * Controlador del panel de configuración del sistema
 * Maneja preferencias, roles de usuario y configuración por licencia
 */
public class ControladorConfiguracion {

    private PanelConfiguracion vista;
    private String licenciaActual;

    /**
     * Constructor del controlador de configuración
     * @param vista Panel de configuración a controlar
     * @param rolUsuario Rol del usuario actual (ADMIN/EMPLEADO)
     */
    public ControladorConfiguracion(PanelConfiguracion vista, String rolUsuario) {
        this.vista = vista;
        this.licenciaActual = ConexionDB.licencia;
        
        vista.configurarSegunLicencia(licenciaActual);
        cargarPreferencia();
        
        // Configurar visibilidad según rol
        if (rolUsuario.equalsIgnoreCase("EMPLEADO")) {
            vista.pnlContainerAdmin.setVisible(false);
        }

        // Configurar funcionalidades según licencia
        if (licenciaActual.equals("LITE")) {
            if (vista.btnGestionarUsuarios != null) {
                vista.btnGestionarUsuarios.setVisible(false);
            }
        }
        
        initListeners();
    }
    
    /**
     * Inicializa los listeners de eventos de la interfaz
     */
    private void initListeners() {
        vista.btnGuardar.addActionListener(e -> guardarPreferencia());
        vista.btnCerrarSesion.addActionListener(e -> cerrarSesionYReiniciar());
        vista.btnGestionarUsuarios.addActionListener(e -> abrirGestionUsuarios());
        vista.btnConfigAtajos.addActionListener(e -> abrirConfigAtajos());
        
        // Listener para checkbox de sonido (aplicar inmediatamente)
        vista.chkSonidoVenta.addActionListener(e -> {
            modelo.SonidoManager.setSonidoActivo(vista.chkSonidoVenta.isSelected());
        });
        
        if (vista.btnVisualizarTicket != null) {
            vista.btnVisualizarTicket.addActionListener(e -> mostrarVistaPrevia());
        }
        
        if (vista.txtNombreLocal != null) {
            vista.txtNombreLocal.addActionListener(e -> {
                // Opcional: actualizar vista previa automáticamente
            });
        }
    }
    
    private void abrirConfigAtajos() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(vista);
        DialogoAtajos dialogo = new DialogoAtajos(frame);
        dialogo.setVisible(true);
    }
    
    private void mostrarVistaPrevia() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(vista);
        String nombreLocal = vista.txtNombreLocal != null ? vista.txtNombreLocal.getText().trim() : "Mi Negocio";
        
        if (nombreLocal.isEmpty()) {
            nombreLocal = "Mi Negocio";
        }
        
        DialogoVisualizarTicket dialogo = new DialogoVisualizarTicket(frame, nombreLocal);
        dialogo.setVisible(true);
    }
    
    private void abrirGestionUsuarios() {
        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(vista);
        vista.DialogoUsuarios dialogo = new vista.DialogoUsuarios(frame);
        dialogo.setVisible(true);
    }

    private void cerrarSesionYReiniciar() {
        int confirm = JOptionPane.showConfirmDialog(vista, 
            "¿Cerrar sesión actual?\nEl programa se reiniciará.",
            "Cerrar Sesión", 
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // 1. Borrar la sesión guardada
            modelo.Sesion.cerrarSesion();
            
            // 2. Cerrar la ventana actual (Dashboard)
            JFrame frameActual = (JFrame) SwingUtilities.getWindowAncestor(vista);
            if (frameActual != null) {
                frameActual.dispose();
            }
            
            // 3. Reiniciar el programa usando el "Truco Maestro"
            reiniciarPrograma();
        }
    }

    /**
     * Reinicia el programa usando Reflection para evitar dependencias circulares
     * Busca y ejecuta el método main de la clase Main dinámicamente
     */
    private void reiniciarPrograma() {
        try {
            Class<?> claseMain = Class.forName("Main");
            java.lang.reflect.Method metodoMain = claseMain.getMethod("main", String[].class);
            metodoMain.invoke(null, (Object) new String[]{});
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Carga las preferencias desde el archivo de configuración
     */
    private void cargarPreferencia() {
        try {
            File f = new File("db_config.properties");
            if (f.exists()) {
                Properties prop = new Properties();
                try (FileInputStream fis = new FileInputStream(f)) { 
                    prop.load(fis); 
                }
                
                String modo = prop.getProperty("modo_manual", "NORMAL");
                if (modo.equals("GRANDE")) {
                    vista.cmbModoManual.setSelectedIndex(1);
                } else {
                    vista.cmbModoManual.setSelectedIndex(0);
                }

                String separar = prop.getProperty("separar_cigarros", "true");
                vista.chkSepararCigarros.setSelected(separar.equals("true"));
                
                String imprimirTicket = prop.getProperty("imprimir_ticket", "true");
                vista.chkImprimirTicket.setSelected(imprimirTicket.equals("true"));
                
                if (vista.txtNombreLocal != null) {
                    String nombreLocal = prop.getProperty("nombre_local", "Mi Negocio");
                    vista.txtNombreLocal.setText(nombreLocal);
                }
            }
        } catch (Exception e) {
            modelo.Logger.error("Error al cargar preferencias", e);
        }
    }

    /**
     * Guarda las preferencias en el archivo de configuración
     */
    private void guardarPreferencia() {
        File f = new File("db_config.properties");
        Properties prop = new Properties();
        
        try {
            if (f.exists()) { 
                try (FileInputStream fis = new FileInputStream(f)) { 
                    prop.load(fis); 
                } 
            }
            
            int seleccion = vista.cmbModoManual.getSelectedIndex();
            prop.setProperty("modo_manual", (seleccion == 1) ? "GRANDE" : "NORMAL");
            prop.setProperty("separar_cigarros", vista.chkSepararCigarros.isSelected() ? "true" : "false");
            prop.setProperty("imprimir_ticket", vista.chkImprimirTicket.isSelected() ? "true" : "false");
            
            if (vista.txtNombreLocal != null) {
                String nombreLocal = vista.txtNombreLocal.getText().trim();
                if (nombreLocal.isEmpty()) {
                    nombreLocal = "Mi Negocio";
                }
                prop.setProperty("nombre_local", nombreLocal);
            } else if (licenciaActual.equals("LITE")) {
                prop.setProperty("nombre_local", "Kiosco");
            }
            
            try (FileOutputStream fos = new FileOutputStream(f)) { 
                prop.store(fos, "Configuracion Kiosco"); 
            }
            
            JOptionPane.showMessageDialog(vista, "✅ Configuración Guardada Correctamente");
            modelo.Logger.info("Configuración guardada por usuario: " + modelo.Sesion.getUsuario());
            
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(vista, "❌ Error al guardar: " + e.getMessage());
            modelo.Logger.error("Error al guardar configuración", e);
        }
    }
}