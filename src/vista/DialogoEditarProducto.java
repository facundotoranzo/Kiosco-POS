package vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import modelo.ConexionDB;

public class DialogoEditarProducto extends JDialog {

    public JTextField txtNombre, txtPrecio, txtStockSumar;
    public JLabel lblCodigo, lblStockActual;
    public JCheckBox chkCigarrillo; // <--- NUEVO
    public JButton btnGuardar, btnCancelar;

    // Agregamos el boolean esCigarrillo al constructor
    public DialogoEditarProducto(JFrame parent, long codigo, String nombre, double precio, int stockActual, boolean esCigarrillo) {
        super(parent, "Editar Producto", true);
        setSize(400, 480); // Un poco más alto
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel pnlForm = new JPanel(new GridLayout(7, 2, 10, 15)); // Aumentamos filas
        pnlForm.setBorder(new EmptyBorder(20, 30, 20, 30));

        // 1. Código
        lblCodigo = new JLabel(String.valueOf(codigo));
        lblCodigo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCodigo.setForeground(Color.GRAY);

        // 2. Nombre
        txtNombre = crearInput(nombre);

        // 3. Precio
        txtPrecio = crearInput(String.valueOf(precio));

        // 4. Stock Actual
        lblStockActual = new JLabel(String.valueOf(stockActual));
        lblStockActual.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // 5. Stock a Sumar
        txtStockSumar = crearInput("0");
        txtStockSumar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtStockSumar.setForeground(new Color(0, 120, 215)); 
        
        // 6. Checkbox Cigarrillo (NUEVO)
        chkCigarrillo = new JCheckBox("🚬 Es Cigarrillo / Tabaco");
        chkCigarrillo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chkCigarrillo.setSelected(esCigarrillo); // Marcamos según lo que viene de la BD

        pnlForm.add(crearLabel("Código:"));
        pnlForm.add(lblCodigo);
        pnlForm.add(crearLabel("Nombre:"));
        pnlForm.add(txtNombre);
        pnlForm.add(crearLabel("Precio:"));
        pnlForm.add(txtPrecio);
        pnlForm.add(crearLabel("Stock Actual:"));
        pnlForm.add(lblStockActual);
        pnlForm.add(crearLabel("➕ Sumar Stock:"));
        pnlForm.add(txtStockSumar);
        pnlForm.add(new JLabel("Tipo:")); // Etiqueta vacía o texto
        pnlForm.add(chkCigarrillo);

        // Botones
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCancelar = new JButton("Cancelar");
        btnGuardar = new JButton("Guardar Cambios");
        btnGuardar.setBackground(new Color(0, 120, 215));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnGuardar.putClientProperty("JButton.buttonType", "roundRect");

        pnlBotones.add(btnCancelar);
        pnlBotones.add(btnGuardar);

        add(new JLabel("  Editar detalles e inventario"), BorderLayout.NORTH);
        add(pnlForm, BorderLayout.CENTER);
        add(pnlBotones, BorderLayout.SOUTH);

        if (ConexionDB.licencia.equals("LITE")) {
            txtStockSumar.setVisible(false);
            lblStockActual.setVisible(false);
            
            for (Component c : pnlForm.getComponents()) {
                if (c instanceof JLabel) {
                    JLabel lbl = (JLabel) c;
                    if (lbl.getText().contains("Stock")) {
                        lbl.setVisible(false);
                    }
                }
            }
        }
    }

    private JTextField crearInput(String texto) {
        JTextField t = new JTextField(texto);
        t.putClientProperty("Component.arc", 10);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return t;
    }

    private JLabel crearLabel(String texto) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return l;
    }
}