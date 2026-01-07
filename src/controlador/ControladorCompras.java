package controlador;

import modelo.*;
import vista.PanelCompras;
import javax.swing.*;
import java.util.List;

public class ControladorCompras {

    private PanelCompras vista;
    private ProveedorDAO provDao;
    private GastoDAO gastoDao;
    private CajaDAO cajaDao;
    
    private boolean viendoHistorialCompleto = false; 

    public ControladorCompras(PanelCompras vista) {
        this.vista = vista;
        this.provDao = new ProveedorDAO();
        this.gastoDao = new GastoDAO();
        this.cajaDao = new CajaDAO();

        cargarProveedores();
        actualizarPantalla();
        initListeners();
    }

    private void initListeners() {
        vista.btnNuevoProv.addActionListener(e -> nuevoProveedor());
        vista.btnEliminarProv.addActionListener(e -> eliminarProveedor());
        vista.btnRegistrarGasto.addActionListener(e -> registrarGasto());
        vista.btnEliminarGasto.addActionListener(e -> eliminarGasto());
        vista.btnVerHistorial.addActionListener(e -> alternarHistorial());
    }

    public void actualizarPantalla() {
        int idCaja = cajaDao.obtenerOIniciarCaja();
        List<Gasto> lista;
        
        if (viendoHistorialCompleto) {
            lista = gastoDao.listarTodos();
            vista.btnVerHistorial.setText("🔙 Ver solo Caja Actual");
        } else {
            lista = gastoDao.listarGastosDeCaja(idCaja);
            vista.btnVerHistorial.setText("📜 Ver Historial Completo");
        }

        vista.modeloGastos.setRowCount(0);
        for (Gasto g : lista) {
            String[] fechaHora = g.getFecha().split(" ");
            String fecha = (fechaHora.length > 0) ? fechaHora[0] : "";
            String hora = (fechaHora.length > 1) ? fechaHora[1] : g.getFecha();

            vista.modeloGastos.addRow(new Object[]{
                g.getId(),
                fecha,
                hora,
                g.getProveedor(),
                g.getDescripcion(),
                // ⚠️ CAMBIO: Usamos Formato.moneda
                Formato.moneda(g.getMonto()),
                // ⚠️ CAMBIO: Usamos Formato.moneda
                "<html><font color='#f39c12'>" + Formato.moneda(g.getSaldoRemanente()) + "</font></html>"
            });
        }
    }

    private void registrarGasto() {
        try {
            String montoStr = vista.txtMontoGasto.getText().replace(",", ".");
            if (montoStr.isEmpty()) {
                JOptionPane.showMessageDialog(vista, "Ingresa un monto.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            double montoGasto = Double.parseDouble(montoStr);

            String desc = vista.txtDetalleGasto.getText();
            if (desc.isEmpty()) desc = "Varios";
            
            Proveedor prov = (Proveedor) vista.cmbProveedores.getSelectedItem();
            String nombreProv = (prov != null) ? prov.getNombre() : "General";

            int idCaja = cajaDao.obtenerOIniciarCaja();
            double ventasTotales = cajaDao.obtenerVentasActuales(idCaja);
            double gastosTotalesPrevios = gastoDao.obtenerTotalGastosCaja(idCaja);
            
            double saldoAntesDelGasto = ventasTotales - gastosTotalesPrevios;
            double saldoDespuesDelGasto = saldoAntesDelGasto - montoGasto;

            if (gastoDao.registrarGasto(idCaja, nombreProv, desc, montoGasto, saldoDespuesDelGasto)) {
                // ⚠️ CAMBIO: Formato en el mensaje
                JOptionPane.showMessageDialog(vista, "Gasto registrado.\nQuedan en caja: " + Formato.moneda(saldoDespuesDelGasto));
                
                vista.txtMontoGasto.setText("");
                vista.txtDetalleGasto.setText("");
                actualizarPantalla();
            } else {
                JOptionPane.showMessageDialog(vista, "Error al guardar en BD.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(vista, "El monto debe ser un número válido.", "Error", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void eliminarGasto() {
        int row = vista.tablaGastos.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(vista, "Selecciona un gasto de la lista.");
            return;
        }
        
        int idGasto = (int) vista.modeloGastos.getValueAt(row, 0);
        String monto = (String) vista.modeloGastos.getValueAt(row, 5);
        
        int confirm = JOptionPane.showConfirmDialog(vista, "¿Anular gasto de " + monto + "?\nEl dinero 'volverá' a la caja.", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (gastoDao.eliminar(idGasto)) {
                actualizarPantalla();
            }
        }
    }

    private void alternarHistorial() {
        viendoHistorialCompleto = !viendoHistorialCompleto;
        actualizarPantalla();
    }
    
    private void cargarProveedores() {
        List<Proveedor> lista = provDao.listar();
        vista.modeloProveedores.setRowCount(0);
        vista.cmbProveedores.removeAllItems();
        vista.cmbProveedores.addItem(new Proveedor(0, "Gasto General", "", "", ""));
        for (Proveedor p : lista) {
            vista.modeloProveedores.addRow(new Object[]{ p.getId(), p.getNombre(), p.getTelefono(), p.getContacto() });
            vista.cmbProveedores.addItem(p);
        }
    }
    private void nuevoProveedor() {
        String nombre = JOptionPane.showInputDialog(vista, "Nombre Empresa:");
        if (nombre != null && !nombre.isEmpty()) {
            String tel = JOptionPane.showInputDialog(vista, "Teléfono:");
            provDao.insertar(new Proveedor(0, nombre, tel, "", ""));
            cargarProveedores();
        }
    }
    private void eliminarProveedor() {
        int row = vista.tablaProveedores.getSelectedRow();
        if (row != -1) {
            int id = (int) vista.modeloProveedores.getValueAt(row, 0);
            if (JOptionPane.showConfirmDialog(vista, "¿Borrar?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                provDao.eliminar(id);
                cargarProveedores();
            }
        }
    }
}