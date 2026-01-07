package vista;

import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import modelo.ConexionDB;
import modelo.Sesion;
import modelo.CajaDAO;

/**
 * Barra de estado inferior que muestra información útil del sistema
 * Usuario logueado, caja actual, hora, licencia
 */
public class BarraEstado extends JPanel {
    
    private JLabel lblUsuario;
    private JLabel lblCaja;
    private JLabel lblHora;
    private JLabel lblLicencia;
    private JLabel lblEstado;
    
    private Timer timerReloj;
    private CajaDAO cajaDao;
    
    // Colores
    private final Color bgBarra = new Color(25, 28, 32);
    private final Color colorTexto = new Color(150, 155, 165);
    private final Color colorDestacado = new Color(100, 180, 255);
    private final Color colorExito = new Color(80, 200, 120);
    private final Color colorAdvertencia = new Color(255, 180, 50);
    
    public BarraEstado() {
        this.cajaDao = new CajaDAO();
        
        setLayout(new BorderLayout());
        setBackground(bgBarra);
        setPreferredSize(new Dimension(0, 28));
        setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(50, 55, 60)));
        
        crearComponentes();
        iniciarReloj();
        actualizarInfo();
    }
    
    private void crearComponentes() {
        // Panel izquierdo - Usuario y Caja
        JPanel pnlIzquierdo = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 4));
        pnlIzquierdo.setOpaque(false);
        
        lblUsuario = crearLabel("Usuario: ---");
        lblCaja = crearLabel("Caja: ---");
        lblEstado = crearLabel("");
        
        pnlIzquierdo.add(lblUsuario);
        pnlIzquierdo.add(crearSeparador());
        pnlIzquierdo.add(lblCaja);
        pnlIzquierdo.add(lblEstado);
        
        // Panel derecho - Hora y Licencia
        JPanel pnlDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 4));
        pnlDerecho.setOpaque(false);
        
        lblHora = crearLabel("00:00:00");
        lblHora.setForeground(colorDestacado);
        
        lblLicencia = crearLabel("LITE");
        lblLicencia.setFont(new Font("Segoe UI", Font.BOLD, 11));
        
        pnlDerecho.add(lblLicencia);
        pnlDerecho.add(crearSeparador());
        pnlDerecho.add(lblHora);
        
        add(pnlIzquierdo, BorderLayout.WEST);
        add(pnlDerecho, BorderLayout.EAST);
    }
    
    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(colorTexto);
        return lbl;
    }
    
    private JSeparator crearSeparador() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 16));
        sep.setForeground(new Color(60, 65, 70));
        return sep;
    }
    
    private void iniciarReloj() {
        timerReloj = new Timer(1000, e -> {
            lblHora.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        });
        timerReloj.start();
    }
    
    /**
     * Actualiza toda la información de la barra
     */
    public void actualizarInfo() {
        // Usuario
        String usuario = Sesion.getUsuario();
        String rol = Sesion.getRol();
        lblUsuario.setText("Usuario: " + usuario + " (" + rol + ")");
        
        // Caja
        int idCaja = cajaDao.obtenerOIniciarCaja();
        lblCaja.setText("Caja #" + idCaja);
        
        // Licencia con color según tipo
        String licencia = ConexionDB.licencia;
        lblLicencia.setText(licencia);
        
        switch (licencia) {
            case "LITE":
                lblLicencia.setForeground(colorTexto);
                break;
            case "PRO":
                lblLicencia.setForeground(colorDestacado);
                break;
            case "RED":
                lblLicencia.setForeground(colorExito);
                break;
            default:
                lblLicencia.setForeground(colorTexto);
        }
    }
    
    /**
     * Muestra un mensaje temporal en la barra de estado
     */
    public void mostrarMensaje(String mensaje, TipoMensaje tipo) {
        Color color;
        switch (tipo) {
            case EXITO:
                color = colorExito;
                break;
            case ADVERTENCIA:
                color = colorAdvertencia;
                break;
            case ERROR:
                color = new Color(255, 100, 100);
                break;
            default:
                color = colorTexto;
        }
        
        lblEstado.setText("  |  " + mensaje);
        lblEstado.setForeground(color);
        
        // Limpiar mensaje después de 5 segundos
        Timer timerLimpiar = new Timer(5000, e -> {
            lblEstado.setText("");
        });
        timerLimpiar.setRepeats(false);
        timerLimpiar.start();
    }
    
    /**
     * Actualiza el número de caja mostrado
     */
    public void actualizarCaja(int idCaja) {
        lblCaja.setText("Caja #" + idCaja);
    }
    
    /**
     * Detiene el timer del reloj (llamar al cerrar)
     */
    public void detener() {
        if (timerReloj != null && timerReloj.isRunning()) {
            timerReloj.stop();
        }
    }
    
    public enum TipoMensaje {
        INFO, EXITO, ADVERTENCIA, ERROR
    }
}
