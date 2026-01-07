package vista;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DialogoVisualizarTicket extends JDialog {
    
    private JTextArea txtTicket;
    private String nombreLocal;
    
    public DialogoVisualizarTicket(JFrame parent, String nombreLocal) {
        super(parent, "Vista Previa del Ticket", true);
        this.nombreLocal = nombreLocal != null ? nombreLocal : "Kiosco";
        
        initComponents();
        generarTicketEjemplo();
        
        setSize(400, 600);
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(33, 37, 43));
        
        // Título
        JLabel lblTitulo = new JLabel("Vista Previa del Ticket");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitulo.setBorder(new EmptyBorder(15, 0, 15, 0));
        add(lblTitulo, BorderLayout.NORTH);
        
        // Área de texto para mostrar el ticket
        txtTicket = new JTextArea();
        txtTicket.setFont(new Font("Courier New", Font.PLAIN, 12)); // Fuente monoespaciada
        txtTicket.setBackground(Color.WHITE);
        txtTicket.setForeground(Color.BLACK);
        txtTicket.setEditable(false);
        txtTicket.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JScrollPane scroll = new JScrollPane(txtTicket);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 63, 65)));
        scroll.setPreferredSize(new Dimension(350, 450));
        
        JPanel pnlCentro = new JPanel(new BorderLayout());
        pnlCentro.setOpaque(false);
        pnlCentro.setBorder(new EmptyBorder(0, 20, 0, 20));
        pnlCentro.add(scroll, BorderLayout.CENTER);
        
        add(pnlCentro, BorderLayout.CENTER);
        
        // Botón cerrar
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCerrar.setBackground(new Color(108, 117, 125));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setPreferredSize(new Dimension(0, 40));
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> dispose());
        
        JPanel pnlSur = new JPanel(new FlowLayout());
        pnlSur.setOpaque(false);
        pnlSur.setBorder(new EmptyBorder(15, 0, 15, 0));
        pnlSur.add(btnCerrar);
        
        add(pnlSur, BorderLayout.SOUTH);
    }
    
    private void generarTicketEjemplo() {
        StringBuilder ticket = new StringBuilder();
        
        // Encabezado centrado
        String titulo = nombreLocal.toUpperCase();
        int espacios = Math.max(0, (32 - titulo.length()) / 2);
        ticket.append(" ".repeat(espacios)).append(titulo).append("\n");
        ticket.append("================================\n");
        
        // Fecha y hora
        LocalDateTime ahora = LocalDateTime.now();
        ticket.append("Fecha: ").append(ahora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        ticket.append("Hora:  ").append(ahora.format(DateTimeFormatter.ofPattern("HH:mm:ss"))).append("\n");
        ticket.append("Caja:  001\n");
        ticket.append("Ticket: #00123\n");
        ticket.append("--------------------------------\n");
        
        // Productos de ejemplo
        ticket.append("PRODUCTOS:\n");
        ticket.append("--------------------------------\n");
        ticket.append(String.format("%-20s %3s %8s\n", "Producto", "Qty", "Total"));
        ticket.append("--------------------------------\n");
        ticket.append(String.format("%-20s %3d %8s\n", "Coca Cola 500ml", 2, "$1,200"));
        ticket.append(String.format("%-20s %3d %8s\n", "Pan Lactal", 1, "$850"));
        ticket.append(String.format("%-20s %3d %8s\n", "Leche Entera 1L", 1, "$650"));
        ticket.append("--------------------------------\n");
        
        // Total
        ticket.append(String.format("%-24s %8s\n", "SUBTOTAL:", "$2,700"));
        ticket.append(String.format("%-24s %8s\n", "TOTAL:", "$2,700"));
        ticket.append("--------------------------------\n");
        
        // Pago
        ticket.append("FORMA DE PAGO:\n");
        ticket.append("Efectivo:               $3,000\n");
        ticket.append("Vuelto:                  $300\n");
        ticket.append("--------------------------------\n");
        
        // Pie de página
        ticket.append("\n");
        ticket.append("        ¡Gracias por su compra!\n");
        ticket.append("     Vuelva pronto a visitarnos\n");
        ticket.append("\n");
        ticket.append("Sistema: Kiosco Manager PRO\n");
        ticket.append("================================\n");
        
        txtTicket.setText(ticket.toString());
        txtTicket.setCaretPosition(0); // Scroll al inicio
    }
    
    /**
     * Actualiza el nombre del local en la vista previa
     */
    public void actualizarNombreLocal(String nuevoNombre) {
        this.nombreLocal = nuevoNombre != null ? nuevoNombre : "Kiosco";
        generarTicketEjemplo();
    }
}