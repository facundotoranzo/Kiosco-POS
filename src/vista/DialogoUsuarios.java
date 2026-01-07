package vista;

import modelo.UsuarioDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class DialogoUsuarios extends JDialog {

    private UsuarioDAO dao = new UsuarioDAO();
    private DefaultTableModel modelo;
    private JTable tabla;
    
    private JTextField txtUser, txtPass;
    private JComboBox<String> cmbRol;

    public DialogoUsuarios(JFrame parent) {
        super(parent, "Gestión de Usuarios", true);
        setSize(600, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        
        JPanel pnlFondo = new JPanel(new BorderLayout(10, 10));
        pnlFondo.setBackground(new Color(33, 37, 43));
        pnlFondo.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(pnlFondo);

        // --- 1. FORMULARIO DE ALTA ---
        JPanel pnlForm = new JPanel(new GridLayout(1, 4, 10, 0));
        pnlForm.setOpaque(false);
        pnlForm.setBorder(BorderFactory.createTitledBorder(null, "Nuevo Usuario", 0, 0, null, Color.WHITE));

        txtUser = new JTextField(); 
        txtUser.putClientProperty("JTextField.placeholderText", "Usuario");
        
        txtPass = new JTextField(); 
        txtPass.putClientProperty("JTextField.placeholderText", "Contraseña");
        
        cmbRol = new JComboBox<>(new String[]{"EMPLEADO", "ADMIN"});
        
        JButton btnAgregar = new JButton("➕ Crear");
        btnAgregar.setBackground(new Color(46, 204, 113));
        btnAgregar.setForeground(Color.WHITE);

        pnlForm.add(txtUser);
        pnlForm.add(txtPass);
        pnlForm.add(cmbRol);
        pnlForm.add(btnAgregar);

        // --- 2. TABLA DE USUARIOS ---
        String[] cols = {"ID", "Usuario", "Rol"};
        modelo = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; }};
        tabla = new JTable(modelo);
        tabla.setRowHeight(30);
        
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(new Color(45, 45, 45));

        // --- 3. BOTÓN ELIMINAR ---
        JButton btnEliminar = new JButton("🗑️ Eliminar Seleccionado");
        btnEliminar.setBackground(new Color(231, 76, 60));
        btnEliminar.setForeground(Color.WHITE);

        // --- ACCIONES ---
        btnAgregar.addActionListener(e -> agregarUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario());

        add(pnlForm, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(btnEliminar, BorderLayout.SOUTH);

        cargarTabla();
    }

    private void cargarTabla() {
        modelo.setRowCount(0);
        for (Object[] fila : dao.listar()) {
            modelo.addRow(fila);
        }
    }

    private void agregarUsuario() {
        String u = txtUser.getText().trim();
        String p = txtPass.getText().trim();
        String r = (String) cmbRol.getSelectedItem();

        if (u.isEmpty() || p.isEmpty()) return;

        if (dao.crear(u, p, r)) {
            JOptionPane.showMessageDialog(this, "Usuario Creado!");
            txtUser.setText(""); txtPass.setText("");
            cargarTabla();
        } else {
            JOptionPane.showMessageDialog(this, "Error: El usuario ya existe.");
        }
    }

    private void eliminarUsuario() {
        int row = tabla.getSelectedRow();
        if (row == -1) return;

        int id = (int) modelo.getValueAt(row, 0);
        String nombre = (String) modelo.getValueAt(row, 1);

        if (JOptionPane.showConfirmDialog(this, "¿Borrar a " + nombre + "?") == JOptionPane.YES_OPTION) {
            if (dao.eliminar(id)) {
                cargarTabla();
            } else {
                JOptionPane.showMessageDialog(this, "No puedes borrar al Admin principal.");
            }
        }
    }
}