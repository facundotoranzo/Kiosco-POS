package modelo;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class GestorFuentes {

    // Define aquí la fuente que SÍ funciona con Emojis en Windows
    private static final String FUENTE_EMOJIS = "Segoe UI Emoji";

    /**
     * Este método recorre todos los componentes dentro de un panel/ventana
     * y les fuerza la fuente de emojis, manteniendo su tamaño y estilo original.
     */
    public static void forzarFuente(Container contenedor) {
        // 1. Cambiar la fuente del propio contenedor si es necesario
        aplicarAlComponente(contenedor);

        // 2. Recorrer todos los hijos (botones, labels, tablas, paneles internos)
        for (Component c : contenedor.getComponents()) {
            
            aplicarAlComponente(c);

            // Si el componente tiene hijos (es un panel, scroll, etc), entramos recursivamente
            if (c instanceof Container) {
                forzarFuente((Container) c);
            }
            
            // Caso especial: Menús
            if (c instanceof JMenuBar) {
                JMenuBar bar = (JMenuBar) c;
                for (int i = 0; i < bar.getMenuCount(); i++) {
                    aplicarAlMenu(bar.getMenu(i));
                }
            }
        }
    }

    private static void aplicarAlMenu(JMenuItem item) {
        aplicarAlComponente(item);
        if (item instanceof JMenu) {
            JMenu menu = (JMenu) item;
            for (int i = 0; i < menu.getItemCount(); i++) {
                if (menu.getItem(i) != null) {
                    aplicarAlMenu(menu.getItem(i));
                }
            }
        }
    }

    private static void aplicarAlComponente(Component c) {
        Font actual = c.getFont();
        if (actual != null) {
            // Creamos una nueva fuente con el Nombre "Emoji", pero con el mismo Estilo y Tamaño que ya tenía
            Font nueva = new Font(FUENTE_EMOJIS, actual.getStyle(), actual.getSize());
            c.setFont(nueva);
        }
    }
}