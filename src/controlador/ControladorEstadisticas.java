package controlador;

import modelo.EstadisticasDAO;
import modelo.Formato;
import modelo.ProductoDAO;
import vista.PanelEstadisticas;
import vista.ToastNotification;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 * Controlador mejorado para el panel de estadísticas
 * Integra funcionalidades avanzadas del EstadisticasDAO
 */
public class ControladorEstadisticas {

    private PanelEstadisticas vista;
    private EstadisticasDAO dao;
    private ProductoDAO productoDao;

    public ControladorEstadisticas(PanelEstadisticas vista) {
        this.vista = vista;
        this.dao = new EstadisticasDAO();
        this.productoDao = new ProductoDAO();
        
        configurarEventos();
        cargarDatos();
    }
    
    /**
     * Configura los eventos de los botones
     */
    private void configurarEventos() {
        vista.btnActualizar.addActionListener((ActionEvent e) -> {
            cargarDatos();
            ToastNotification.exito(vista, "Datos actualizados correctamente");
        });
        
        vista.btnExportar.addActionListener((ActionEvent e) -> {
            exportarDatos();
        });
        
        vista.btnReporte.addActionListener((ActionEvent e) -> {
            generarReporte();
        });
    }

    /**
     * Carga todos los datos del panel con las nuevas métricas
     */
    public void cargarDatos() {
        try {
            // Limpiar tablas antes de cargar
            vista.limpiarTablas();
            
            // 1. KPIs principales
            cargarKPIsPrincipales();
            
            // 2. KPIs adicionales
            cargarKPIsAdicionales();
            
            // 3. Tablas de datos
            cargarTablaTopProductos();
            cargarTablaSemana();
            cargarTablaPeriodos();
            
        } catch (Exception e) {
            ToastNotification.error(vista, "Error al cargar estadísticas: " + e.getMessage());
        }
    }
    
    /**
     * Carga los KPIs principales (ventas hoy, ayer, productos vendidos)
     */
    private void cargarKPIsPrincipales() {
        LocalDate hoy = LocalDate.now();
        LocalDate ayer = hoy.minusDays(1);
        
        double ventasHoy = dao.obtenerTotalVentasDia(hoy);
        double ventasAyer = dao.obtenerTotalVentasDia(ayer);
        int totalItems = dao.obtenerTotalProductosVendidos();

        vista.actualizarKPI(vista.lblVentasHoy, Formato.moneda(ventasHoy));
        vista.actualizarKPI(vista.lblVentasAyer, Formato.moneda(ventasAyer));
        vista.actualizarKPI(vista.lblTotalProds, String.valueOf(totalItems));
    }
    
    /**
     * Carga los KPIs adicionales (producto del mes, promedio, stock bajo)
     */
    private void cargarKPIsAdicionales() {
        // Producto más vendido del mes
        EstadisticasDAO.ProductoVendido productoMes = dao.obtenerProductoMasVendidoDelMes();
        String textoProductoMes = productoMes.getCantidadVendida() > 0 ? 
            productoMes.getNombre() + " (" + productoMes.getCantidadVendida() + ")" : 
            "Sin datos";
        vista.actualizarKPI(vista.lblProductoMes, textoProductoMes);
        
        // Promedio de venta de la semana
        EstadisticasDAO.EstadisticasSemana statsSemana = dao.obtenerEstadisticasSemana();
        double promedioVenta = statsSemana.promedioVenta;
        vista.actualizarKPI(vista.lblPromedioVenta, Formato.moneda(promedioVenta));
        
        // Productos con stock bajo
        int stockBajo = productoDao.contarProductosStockBajo(5);
        vista.actualizarKPI(vista.lblStockBajo, String.valueOf(stockBajo));
    }
    
    /**
     * Carga la tabla de top productos con formato simple
     */
    private void cargarTablaTopProductos() {
        List<String[]> top = dao.obtenerTopProductos();
        
        for (String[] fila : top) {
            vista.modeloTop.addRow(new Object[]{
                fila[0], // Solo el nombre del producto
                fila[1] + " unidades"
            });
        }
        
        // Si no hay datos, mostrar mensaje
        if (top.isEmpty()) {
            vista.modeloTop.addRow(new Object[]{"Sin ventas", "0"});
        }
    }
    
    /**
     * Carga la tabla de ventas de la semana sin emojis
     */
    private void cargarTablaSemana() {
        List<String[]> semana = dao.obtenerVentasSemana();
        
        for (String[] fila : semana) {
            // Solo mostrar el nombre del día sin emojis
            String diaCompleto = obtenerNombreDiaCompleto(fila[0]);
            
            vista.modeloSemana.addRow(new Object[]{
                diaCompleto,
                "$ " + fila[1]
            });
        }
    }
    
    /**
     * Carga la nueva tabla de períodos del día
     */
    private void cargarTablaPeriodos() {
        String fechaHoy = LocalDate.now().toString();
        List<EstadisticasDAO.VentaPorHora> periodos = dao.obtenerVentasPorHora(fechaHoy);
        
        for (EstadisticasDAO.VentaPorHora periodo : periodos) {
            vista.modeloPeriodos.addRow(new Object[]{
                periodo.getPeriodo(),
                String.valueOf(periodo.getCantidadVentas()),
                Formato.moneda(periodo.getTotalPeriodo())
            });
        }
        
        // Si no hay datos del día, mostrar mensaje
        if (periodos.isEmpty()) {
            vista.modeloPeriodos.addRow(new Object[]{"Sin ventas hoy", "0", "$ 0.00"});
        }
    }
    
    /**
     * Exporta los datos actuales a un archivo de texto
     */
    private void exportarDatos() {
        try {
            String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String nombreArchivo = "estadisticas_" + timestamp + ".txt";
            
            FileWriter writer = new FileWriter(nombreArchivo);
            writer.write("=== REPORTE DE ESTADÍSTICAS ===\n");
            writer.write("Fecha: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n\n");
            
            // KPIs principales
            writer.write("MÉTRICAS PRINCIPALES:\n");
            writer.write("- Ventas de hoy: " + vista.lblVentasHoy.getText() + "\n");
            writer.write("- Ventas de ayer: " + vista.lblVentasAyer.getText() + "\n");
            writer.write("- Productos vendidos: " + vista.lblTotalProds.getText() + "\n");
            writer.write("- Producto del mes: " + vista.lblProductoMes.getText() + "\n");
            writer.write("- Promedio por venta: " + vista.lblPromedioVenta.getText() + "\n");
            writer.write("- Productos con stock bajo: " + vista.lblStockBajo.getText() + "\n\n");
            
            // Top productos
            writer.write("TOP PRODUCTOS:\n");
            for (int i = 0; i < vista.modeloTop.getRowCount(); i++) {
                writer.write("- " + vista.modeloTop.getValueAt(i, 0) + ": " + vista.modeloTop.getValueAt(i, 1) + "\n");
            }
            
            writer.close();
            
            ToastNotification.exito(vista, "Datos exportados a: " + nombreArchivo);
            
        } catch (IOException e) {
            ToastNotification.error(vista, "Error al exportar datos: " + e.getMessage());
        }
    }
    
    /**
     * Genera un reporte completo en ventana emergente
     */
    private void generarReporte() {
        StringBuilder reporte = new StringBuilder();
        reporte.append("📊 REPORTE COMPLETO DE ESTADÍSTICAS\n");
        reporte.append("═══════════════════════════════════\n\n");
        
        reporte.append("📅 Fecha: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n\n");
        
        // Resumen ejecutivo
        reporte.append("💼 RESUMEN EJECUTIVO:\n");
        reporte.append("• Ventas del día: ").append(vista.lblVentasHoy.getText()).append("\n");
        reporte.append("• Comparativo ayer: ").append(vista.lblVentasAyer.getText()).append("\n");
        reporte.append("• Unidades vendidas: ").append(vista.lblTotalProds.getText()).append("\n");
        reporte.append("• Ticket promedio: ").append(vista.lblPromedioVenta.getText()).append("\n\n");
        
        // Alertas
        reporte.append("⚠️ ALERTAS:\n");
        int stockBajo = Integer.parseInt(vista.lblStockBajo.getText());
        if (stockBajo > 0) {
            reporte.append("• ").append(stockBajo).append(" productos necesitan reposición\n");
        } else {
            reporte.append("• Sin alertas de stock\n");
        }
        reporte.append("\n");
        
        // Producto destacado
        reporte.append("🏆 PRODUCTO DESTACADO DEL MES:\n");
        reporte.append("• ").append(vista.lblProductoMes.getText()).append("\n\n");
        
        // Mostrar en diálogo
        JOptionPane.showMessageDialog(vista, reporte.toString(), "Reporte de Estadísticas", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Obtiene el nombre completo del día
     */
    private String obtenerNombreDiaCompleto(String diaAbreviado) {
        return switch (diaAbreviado.toUpperCase()) {
            case "MON", "LUN" -> "Lunes";
            case "TUE", "MAR" -> "Martes";
            case "WED", "MIE" -> "Miércoles";
            case "THU", "JUE" -> "Jueves";
            case "FRI", "VIE" -> "Viernes";
            case "SAT", "SAB" -> "Sábado";
            case "SUN", "DOM" -> "Domingo";
            default -> diaAbreviado;
        };
    }
}