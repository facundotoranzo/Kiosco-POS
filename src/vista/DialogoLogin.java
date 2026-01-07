package vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DialogoLogin extends JDialog {

    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnIngresar, btnSalir;
    
    // Esta variable guardará el rol si el login es exitoso (null si falla)
    private String rolDetectado = null; 

    public DialogoLogin() {
        setTitle("Acceso al Sistema");
        setModal(true);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setUndecorated(true); // Sin bordes de ventana (Estilo moderno)
        setLayout(new BorderLayout());
        
        // --- COLORES ---
        Color fondo = new Color(33, 37, 43);
        Color azul = new Color(52, 152, 219);
        Color texto = new Color(230, 230, 230);

        // --- PANEL PRINCIPAL CON BORDE AZUL ---
        JPanel pnlMain = new JPanel(new BorderLayout(20, 20));
        pnlMain.setBackground(fondo);
        pnlMain.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(azul, 2),
            new EmptyBorder(30, 40, 30, 40)
        ));

        // 1. TÍTULO
        JLabel lblTitulo = new JLabel("INICIAR SESIÓN");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(texto);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        pnlMain.add(lblTitulo, BorderLayout.NORTH);

        // 2. FORMULARIO
        JPanel pnlForm = new JPanel(new GridLayout(4, 1, 10, 10));
        pnlForm.setOpaque(false);

        JLabel lblU = new JLabel("Usuario:");
        lblU.setForeground(Color.GRAY);
        lblU.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        txtUsuario = new JTextField();
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtUsuario.putClientProperty("Component.arc", 10);
        
        JLabel lblP = new JLabel("Contraseña:");
        lblP.setForeground(Color.GRAY);
        lblP.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        txtPassword.putClientProperty("Component.arc", 10);

        pnlForm.add(lblU); pnlForm.add(txtUsuario);
        pnlForm.add(lblP); pnlForm.add(txtPassword);
        pnlMain.add(pnlForm, BorderLayout.CENTER);

        // 3. BOTONES
        JPanel pnlBtn = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlBtn.setOpaque(false);
        
        btnSalir = new JButton("Salir");
        btnSalir.setBackground(new Color(200, 60, 60));
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSalir.addActionListener(e -> System.exit(0));

        btnIngresar = new JButton("INGRESAR");
        btnIngresar.setBackground(azul);
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // ACCIÓN DE LOGIN
        btnIngresar.addActionListener(e -> validarLogin());

        pnlBtn.add(btnSalir);
        pnlBtn.add(btnIngresar);
        pnlMain.add(pnlBtn, BorderLayout.SOUTH);

        add(pnlMain);

        modelo.GestorFuentes.forzarFuente(this);
        
        // Enter en el password activa el botón
        getRootPane().setDefaultButton(btnIngresar);
    }

    private void validarLogin() {
        String u = txtUsuario.getText().trim();
        String p = new String(txtPassword.getPassword()).trim();

        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete todos los campos.");
            return;
        }

        try {
            modelo.UsuarioDAO usuarioDao = new modelo.UsuarioDAO();
            String rol = usuarioDao.validarLogin(u, p);
            
            if (rol != null) {
                rolDetectado = rol;
                modelo.Sesion.guardarSesion(u, rolDetectado);
                dispose(); 
            } else {
                JOptionPane.showMessageDialog(this, "Usuario o Clave incorrectos", "Acceso Denegado", JOptionPane.ERROR_MESSAGE);
                txtPassword.setText("");
                txtPassword.requestFocus();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error de BD: " + e.getMessage());
        }
    }

    public String getRolDetectado() {
        return rolDetectado;
    }

    public String getUsuarioIngresado() {
        return txtUsuario.getText().trim();
    }

}