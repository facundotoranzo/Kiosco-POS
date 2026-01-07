package vista;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class RenderStock extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        // 1. Obtenemos el stock de la fila actual (Columna 3 es Stock)
        int stock = 0;
        try {
            // ConvertRowIndexToModel es vital si algún día ordenas la tabla
            int modeloRow = table.convertRowIndexToModel(row);
            stock = (int) table.getModel().getValueAt(modeloRow, 3); 
        } catch (Exception e) {
            stock = 100; // Por seguridad si falla
        }

        // 2. Lógica del Semáforo (Colores para Tema Oscuro)
        if (stock <= 0) {
            // AGOTADO: Gris oscuro y tachado (o simplemente gris)
            c.setForeground(Color.GRAY);
            setFont(getFont().deriveFont(Font.ITALIC)); // Cursiva
        } 
        else if (stock <= 5) {
            // CRÍTICO: Rojo Intenso
            c.setForeground(new Color(255, 80, 80)); 
            setFont(getFont().deriveFont(Font.BOLD)); // Negrita para alertar
        } 
        else if (stock <= 15) {
            // ADVERTENCIA: Naranja / Amarillo
            c.setForeground(new Color(255, 180, 0)); 
            setFont(getFont().deriveFont(Font.BOLD));
        } 
        else {
            // NORMAL: Blanco (o el color por defecto del tema)
            c.setForeground(new Color(220, 220, 220));
            setFont(getFont().deriveFont(Font.PLAIN));
        }

        // 3. Mantener el color de selección (Azul) si la fila está seleccionada
        if (isSelected) {
            c.setBackground(table.getSelectionBackground());
            c.setForeground(table.getSelectionForeground());
        } else {
            c.setBackground(new Color(45, 45, 45)); // Fondo oscuro normal
        }

        // Centrar el texto para que quede prolijo
        setHorizontalAlignment(SwingConstants.CENTER);

        return c;
    }
}