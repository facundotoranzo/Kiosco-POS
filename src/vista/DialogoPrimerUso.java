package vista;

import modelo.UsuarioDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DialogoPrimerUso extends JDialog {

    private JTextField txtUser;
    private JPasswordField txtPass, txtConfirm;
    private UsuarioDAO dao = new UsuarioDAO();
    
    // Variable para saber si terminó bien
    private boolean registroExitoso = false;

    public DialogoPrimerUso() {
        setTitle("Configuración Inicial");
        setModal(true);
        setSize(400, 450);
        setLocationRelativeTo(null);
        setUndecorated(true);
        setLayout(new BorderLayout());

        // --- FONDO --+
        JPanel pnlMain = new JPanel(new BorderLayout(20, 20));
        pnlMain.setBackground(new Color(33, 37, 43));
        pnlMain.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(46, 204, 113), 2), // Borde Verde
            new EmptyBorder(30, 30, 30, 30)
        ));

        // 1. TÍTULO
        JLabel lblTitulo = new JLabel("Registrarse");
        lblTitulo.setFont(new Font("Segoe UI Emoji", Font.BOLD, 28));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel lblSub = new JLabel("Crea el primer usuario ADMIN");
        lblSub.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        lblSub.setForeground(Color.LIGHT_GRAY);
        lblSub.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel pnlTop = new JPanel(new GridLayout(2, 1));
        pnlTop.setOpaque(false);
        pnlTop.add(lblTitulo);
        pnlTop.add(lblSub);

        // 2. FORMULARIO
        JPanel pnlForm = new JPanel(new GridLayout(6, 1, 5, 5));
        pnlForm.setOpaque(false);
        
        txtUser = crearInput("Usuario");
        txtPass = crearPass("Contraseña");
        txtConfirm = crearPass("Repetir Contraseña");

        pnlForm.add(crearLabel("Usuario:"));
        pnlForm.add(txtUser);
        pnlForm.add(crearLabel("Contraseña:"));
        pnlForm.add(txtPass);
        pnlForm.add(crearLabel("Confirmar:"));
        pnlForm.add(txtConfirm);

        // 3. BOTONES
        JPanel pnlBtn = new JPanel(new GridLayout(1, 2, 10, 0));
        pnlBtn.setOpaque(false);

        JButton btnSalir = new JButton("Salir");
        btnSalir.setBackground(new Color(231, 76, 60));
        btnSalir.setForeground(Color.WHITE);
        btnSalir.addActionListener(e -> System.exit(0));

        JButton btnCrear = new JButton("CREAR CUENTA");
        btnCrear.setBackground(new Color(46, 204, 113));
        btnCrear.setForeground(Color.WHITE);
        btnCrear.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        btnCrear.addActionListener(e -> registrarAdmin());

        pnlBtn.add(btnSalir);
        pnlBtn.add(btnCrear);

        pnlMain.add(pnlTop, BorderLayout.NORTH);
        pnlMain.add(pnlForm, BorderLayout.CENTER);
        pnlMain.add(pnlBtn, BorderLayout.SOUTH);
        
        add(pnlMain);
        
        try { modelo.GestorFuentes.forzarFuente(this); } catch(Exception e){}
    }
    
    private JLabel crearLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        return l;
    }

    private JTextField crearInput(String placeholder) {
        JTextField t = new JTextField();
        t.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        return t;
    }

    private JPasswordField crearPass(String placeholder) {
        JPasswordField t = new JPasswordField();
        t.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        return t;
    }

    private void registrarAdmin() {
        String u = txtUser.getText().trim();
        String p1 = new String(txtPass.getPassword());
        String p2 = new String(txtConfirm.getPassword());

        if (u.isEmpty() || p1.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Faltan datos.");
            return;
        }
        if (!p1.equals(p2)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.");
            return;
        }

        if (dao.crear(u, p1, "ADMIN")) {
            JOptionPane.showMessageDialog(this, "✅ Cuenta Creada");
            registroExitoso = true; // Marcamos éxito
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Error al crear usuario.");
        }
    }

    // --- ESTE ES EL MÉTODO QUE FALTABA Y CAUSABA EL ERROR ---
    public boolean isRegistroExitoso() {
        return registroExitoso;
    }
}