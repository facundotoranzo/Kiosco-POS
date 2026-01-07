package vista;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class RenderBoton extends JButton implements TableCellRenderer {

    private final Color colorOriginal;
    private final Color colorHover;
    private final Color colorPressed;
    
    private int rolloverRow = -1;
    private int rolloverCol = -1;
    private boolean isPressed = false;

    public RenderBoton(String texto, Color colorFondo) {
        this.colorOriginal = colorFondo;
        this.colorHover = colorFondo.brighter(); 
        this.colorPressed = colorFondo.darker();

        setText(texto);
        setOpaque(true);
        setForeground(Color.WHITE);
        
        // ⚠️ CLAVE: Usar "Segoe UI Emoji" para que Windows muestre los íconos
        setFont(new Font("Segoe UI Emoji", Font.BOLD, 18)); 
        
        setFocusPainted(false);
        setBorderPainted(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        putClientProperty("JButton.buttonType", "roundRect"); 
        setMargin(new Insets(5, 5, 5, 5)); 
    }

    public void setMouseState(int row, int col, boolean pressed) {
        this.rolloverRow = row;
        this.rolloverCol = col;
        this.isPressed = pressed;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        
        if (row == rolloverRow && column == rolloverCol) {
            if (isPressed) setBackground(colorPressed);
            else setBackground(colorHover);
        } else {
            setBackground(colorOriginal);
        }
        return this;
    }
}