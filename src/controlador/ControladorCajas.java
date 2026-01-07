package controlador;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import modelo.CajaDAO;
import vista.PanelCajas;

public class ControladorCajas {

    private PanelCajas vista;
    private CajaDAO dao;

    public ControladorCajas(PanelCajas vista) {
        this.vista = vista;
        this.dao = new CajaDAO();

        initListeners();
        cargarCajas();
    }

    private void initListeners() {
        // 1. Botón Actualizar
        vista.btnActualizar.addActionListener(e -> cargarCajas());

        // 2. Clic en la Tabla (Para el botón "Ver" o Click Derecho)
        vista.tablaCajas.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = vista.tablaCajas.getColumnModel().getColumnIndexAtX(e.getX());
                int row = e.getY() / vista.tablaCajas.getRowHeight();

                if (row < vista.tablaCajas.getRowCount() && row >= 0 && col < vista.tablaCajas.getColumnCount() && col >= 0) {
                    Object value = vista.tablaCajas.getValueAt(row, col);
                    
                    // Si hizo clic en la columna del botón (Columna 9)
                    if (value instanceof String && col == 9) {
                        int idCaja = (int) vista.tablaCajas.getValueAt(row, 0);
                        verDetalleCaja(idCaja);
                    }
                }
                
                // Click Derecho (Menú Contextual)
                if (SwingUtilities.isRightMouseButton(e)) {
                    int r = vista.tablaCajas.rowAtPoint(e.getPoint());
                    if (r >= 0 && r < vista.tablaCajas.getRowCount()) {
                        vista.tablaCajas.setRowSelectionInterval(r, r);
                        vista.menuCajas.show(vista.tablaCajas, e.getX(), e.getY());
                    }
                }
            }
        });

        // 3. Acciones del Menú Click Derecho
        vista.itemEditar.addActionListener(e -> {
            int row = vista.tablaCajas.getSelectedRow();
            if (row != -1) {
                int idCaja = (int) vista.tablaCajas.getValueAt(row, 0);
                verDetalleCaja(idCaja); // Reusamos la ventana de detalle para editar
            }
        });

        vista.itemEliminar.addActionListener(e -> eliminarCaja());
    }

    // --- CARGAR TABLA (AQUÍ ESTABA EL ERROR DE LIST<CAJA>) ---
    public void cargarCajas() {
        // Limpiamos la tabla
        vista.modeloCajas.setRowCount(0);
        
        // Obtenemos la lista de OBJETOS[] (No de Cajas)
        List<Object[]> lista = dao.listarCajasCerradas();
        
        // Los agregamos directo a la tabla porque ya vienen formateados del DAO
        for (Object[] fila : lista) {
            vista.modeloCajas.addRow(fila);
        }
    }

    private void verDetalleCaja(int idCaja) {
        // Abrimos el diálogo de detalle (pasándole el ID de la caja)
        // Nota: Asumo que tienes una clase DialogoDetalleCaja o similar.
        // Si no la tienes, avísame para pasártela.
        
        // Si usabas un método viejo en la vista, lo ideal es crear el diálogo aquí:
        javax.swing.JFrame frame = (javax.swing.JFrame) SwingUtilities.getWindowAncestor(vista);
        new vista.DialogoDetalleCaja(frame, idCaja, dao).setVisible(true);
    }

    private void eliminarCaja() {
        int row = vista.tablaCajas.getSelectedRow();
        if (row == -1) return;

        int idCaja = (int) vista.tablaCajas.getValueAt(row, 0);
        String fecha = (String) vista.tablaCajas.getValueAt(row, 1);

        // Seguridad: Preguntar contraseña de admin si quieres, o solo confirmación
        int confirm = JOptionPane.showConfirmDialog(vista, 
            "¿Estás seguro de ELIMINAR la caja Nro " + idCaja + " del " + fecha + "?\n" +
            "Se borrarán todas las ventas asociadas y se devolverá el stock.",
            "PELIGRO - Eliminar Caja",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.eliminarCaja(idCaja)) {
                JOptionPane.showMessageDialog(vista, "Caja eliminada correctamente.");
                cargarCajas();
            } else {
                JOptionPane.showMessageDialog(vista, "Error al eliminar la caja.");
            }
        }
    }
}