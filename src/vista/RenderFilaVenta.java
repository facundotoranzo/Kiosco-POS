package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class RenderFilaVenta extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // Obtenemos el medio de pago de la columna oculta (la columna 5 en nuestra lógica)
        // La tabla tiene: Hora, Prod, Precio, Cant, Sub, MEDIO_PAGO
        String medioPago = "";
        try {
            // El modelo tiene el dato en la columna 5
            medioPago = (String) table.getModel().getValueAt(table.convertRowIndexToModel(row), 5);
        } catch (Exception e) {}

        // LÓGICA DE COLOR
        if ("Transferencia".equalsIgnoreCase(medioPago) || "Tarjeta".equalsIgnoreCase(medioPago)) {
            // Si es transferencia: TEXTO AZUL y negrita
            c.setForeground(new Color(30, 144, 255)); // Azul Dodson
            setFont(getFont().deriveFont(Font.BOLD));
        } else {
            // Si es efectivo: Color normal (blanco/gris claro en modo oscuro)
            c.setForeground(new Color(220, 220, 220)); 
            setFont(getFont().deriveFont(Font.PLAIN));
        }
        
        // Mantener color de selección
        if (isSelected) {
            c.setBackground(table.getSelectionBackground());
            c.setForeground(table.getSelectionForeground());
        } else {
            c.setBackground(new Color(45, 45, 45)); // Fondo oscuro por defecto
        }

        return c;
    }
}