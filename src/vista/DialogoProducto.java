package vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import modelo.ConexionDB;

public class DialogoProducto extends JDialog {

    public JTextField txtCodigo, txtNombre, txtPrecio, txtStock;
    public JCheckBox chkCigarrillo; // NUEVO
    public JButton btnGuardar, btnCancelar;

    public DialogoProducto(JFrame parent) {
        super(parent, "Nuevo Producto", true);
        setSize(400, 500); // Más alto
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        JPanel pnlForm = new JPanel(new GridLayout(10, 1, 5, 5));
        pnlForm.setBorder(new EmptyBorder(20, 40, 20, 40));

        txtCodigo = crearInput();
        txtNombre = crearInput();
        txtPrecio = crearInput();
        txtStock = crearInput();
        
        // Checkbox
        chkCigarrillo = new JCheckBox("🚬 Es Cigarrillo / Tabaco");
        chkCigarrillo.setFont(new Font("Segoe UI", Font.BOLD, 14));

        pnlForm.add(crearLabel("Código de Barras:"));
        pnlForm.add(txtCodigo);
        pnlForm.add(crearLabel("Nombre del Producto:"));
        pnlForm.add(txtNombre);
        pnlForm.add(crearLabel("Precio ($):"));
        pnlForm.add(txtPrecio);
        pnlForm.add(crearLabel("Stock Inicial:"));
        pnlForm.add(txtStock);
        pnlForm.add(chkCigarrillo); // Agregado

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnCancelar = new JButton("Cancelar");
        btnGuardar = new JButton("GUARDAR");
        // ... (estilos botones) ...
        pnlBotones.add(btnCancelar);
        pnlBotones.add(btnGuardar);

        add(new JLabel("  Datos del producto"), BorderLayout.NORTH);
        add(pnlForm, BorderLayout.CENTER);
        add(pnlBotones, BorderLayout.SOUTH);
    

        // Cuando el lector escriba el código y mande "Enter", pasamos al campo Nombre.
        txtCodigo.addActionListener(e -> txtNombre.requestFocus());
        
        // Opcional: Que al dar Enter en Nombre pase a Precio, etc.
        txtNombre.addActionListener(e -> txtPrecio.requestFocus());
        txtPrecio.addActionListener(e -> txtStock.requestFocus());
        
        // En el último (Stock), si dan Enter, ahí sí intentamos Guardar
        txtStock.addActionListener(e -> btnGuardar.doClick());

        if (ConexionDB.licencia.equals("LITE")) {
            txtStock.setVisible(false);
            
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
    
    private JTextField crearInput() { 
        return new JTextField(); 
    }
    
    private JLabel crearLabel(String t) { 
        return new JLabel(t); 
    }
}