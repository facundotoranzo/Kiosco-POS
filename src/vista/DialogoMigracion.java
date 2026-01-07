package vista;

import modelo.MigradorDatos;
import modelo.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Diálogo para migración de datos de SQLite a MySQL
 */
public class DialogoMigracion extends JDialog {
    
    private boolean migracionRealizada = false;
    private JProgressBar progressBar;
    private JLabel lblEstado;
    
    public DialogoMigracion(JFrame parent) {
        super(parent, "Migración de Datos", true);
        
        initComponents();
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // Forzar decisión del usuario
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(33, 37, 43));
        
        // Panel superior con título e icono
        JPanel pnlTitulo = new JPanel(new BorderLayout());
        pnlTitulo.setOpaque(false);
        pnlTitulo.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel lblTitulo = new JLabel("🔄 Migración de Datos Detectada");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        
        pnlTitulo.add(lblTitulo, BorderLayout.CENTER);
        add(pnlTitulo, BorderLayout.NORTH);
        
        // Panel central con información
        JPanel pnlCentro = new JPanel();
        pnlCentro.setLayout(new BoxLayout(pnlCentro, BoxLayout.Y_AXIS));
        pnlCentro.setOpaque(false);
        pnlCentro.setBorder(new EmptyBorder(0, 30, 20, 30));
        
        // Mensaje principal
        JLabel lblMensaje = new JLabel("<html><center>" +
            "<p style='color: #E0E0E0; font-size: 14px;'>Se detectó que cambió de <b>LITE/PRO</b> a <b>RED</b>.</p>" +
            "<p style='color: #E0E0E0; font-size: 14px;'>¿Desea migrar sus datos existentes al nuevo servidor MySQL?</p>" +
            "</center></html>");
        lblMensaje.setHorizontalAlignment(SwingConstants.CENTER);
        lblMensaje.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Estadísticas de datos
        String estadisticas = MigradorDatos.obtenerEstadisticasSQLite();
        JLabel lblEstadisticas = new JLabel("<html><center>" +
            "<p style='color: #52A3FF; font-size: 13px;'><b>Datos encontrados:</b></p>" +
            "<p style='color: #A0A0A0; font-size: 12px;'>" + estadisticas + "</p>" +
            "</center></html>");
        lblEstadisticas.setHorizontalAlignment(SwingConstants.CENTER);
        lblEstadisticas.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Lista de lo que se migrará
        JLabel lblQueSeM = new JLabel("<html><center>" +
            "<p style='color: #40A967; font-size: 13px;'><b>Se migrarán:</b></p>" +
            "<p style='color: #E0E0E0; font-size: 12px;'>✅ Todos los productos y precios</p>" +
            "<p style='color: #E0E0E0; font-size: 12px;'>✅ Historial completo de ventas</p>" +
            "<p style='color: #E0E0E0; font-size: 12px;'>✅ Usuarios y configuraciones</p>" +
            "<p style='color: #E0E0E0; font-size: 12px;'>✅ Historial de cajas</p>" +
            "<br>" +
            "<p style='color: #FFA500; font-size: 11px;'>📋 Se creará un backup automático de seguridad</p>" +
            "</center></html>");
        lblQueSeM.setHorizontalAlignment(SwingConstants.CENTER);
        lblQueSeM.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Barra de progreso (inicialmente oculta)
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setString("Preparando migración...");
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(0, 25));
        
        // Label de estado (inicialmente oculto)
        lblEstado = new JLabel("Iniciando migración...");
        lblEstado.setForeground(Color.WHITE);
        lblEstado.setHorizontalAlignment(SwingConstants.CENTER);
        lblEstado.setVisible(false);
        lblEstado.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        pnlCentro.add(lblMensaje);
        pnlCentro.add(lblEstadisticas);
        pnlCentro.add(lblQueSeM);
        pnlCentro.add(progressBar);
        pnlCentro.add(lblEstado);
        
        add(pnlCentro, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        pnlBotones.setOpaque(false);
        pnlBotones.setBorder(new EmptyBorder(0, 0, 25, 0));
        
        JButton btnMigrar = crearBoton("🚀 Migrar Datos", new Color(40, 167, 69));
        JButton btnEmpezarLimpio = crearBoton("🆕 Empezar Limpio", new Color(108, 117, 125));
        JButton btnCancelar = crearBoton("❌ Cancelar", new Color(220, 53, 69));
        
        // Listeners de botones
        btnMigrar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                iniciarMigracion(btnMigrar, btnEmpezarLimpio, btnCancelar);
            }
        });
        
        btnEmpezarLimpio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                    DialogoMigracion.this,
                    "⚠️ ¿Está seguro?\n\nSe perderán todos los datos actuales:\n" +
                    "• Productos cargados\n• Historial de ventas\n• Usuarios creados\n\n" +
                    "Esta acción NO se puede deshacer.",
                    "Confirmar Empezar Limpio",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    Logger.info("Usuario eligió empezar limpio - no migrar datos");
                    dispose();
                }
            }
        });
        
        btnCancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int confirm = JOptionPane.showConfirmDialog(
                    DialogoMigracion.this,
                    "Si cancela, el sistema seguirá usando SQLite.\n¿Está seguro?",
                    "Cancelar Migración",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (confirm == JOptionPane.YES_OPTION) {
                    System.exit(0); // Salir del programa
                }
            }
        });
        
        pnlBotones.add(btnMigrar);
        pnlBotones.add(btnEmpezarLimpio);
        pnlBotones.add(btnCancelar);
        
        add(pnlBotones, BorderLayout.SOUTH);
    }
    
    private void iniciarMigracion(JButton btnMigrar, JButton btnEmpezarLimpio, JButton btnCancelar) {
        // Deshabilitar botones
        btnMigrar.setEnabled(false);
        btnEmpezarLimpio.setEnabled(false);
        btnCancelar.setEnabled(false);
        
        // Mostrar barra de progreso
        progressBar.setVisible(true);
        lblEstado.setVisible(true);
        
        // Ejecutar migración en hilo separado
        SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                publish("Conectando a base de datos...");
                progressBar.setValue(10);
                Thread.sleep(500);
                
                publish("Migrando productos...");
                progressBar.setValue(30);
                Thread.sleep(500);
                
                publish("Migrando ventas...");
                progressBar.setValue(50);
                Thread.sleep(500);
                
                publish("Migrando usuarios...");
                progressBar.setValue(70);
                Thread.sleep(500);
                
                publish("Creando backup...");
                progressBar.setValue(90);
                
                // Ejecutar migración real
                boolean resultado = MigradorDatos.migrarSQLiteAMySQL();
                
                progressBar.setValue(100);
                return resultado;
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                if (!chunks.isEmpty()) {
                    lblEstado.setText(chunks.get(chunks.size() - 1));
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean exito = get();
                    
                    if (exito) {
                        progressBar.setString("✅ Migración Completada");
                        lblEstado.setText("¡Todos los datos fueron migrados exitosamente!");
                        lblEstado.setForeground(new Color(40, 167, 69));
                        
                        JOptionPane.showMessageDialog(
                            DialogoMigracion.this,
                            "✅ ¡Migración Completada Exitosamente!\n\n" +
                            "Todos sus datos han sido transferidos al servidor MySQL.\n" +
                            "Se creó un backup de seguridad automáticamente.",
                            "Migración Exitosa",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        
                        migracionRealizada = true;
                        dispose();
                        
                    } else {
                        progressBar.setString("❌ Error en Migración");
                        lblEstado.setText("Error durante la migración. Revise los logs.");
                        lblEstado.setForeground(new Color(220, 53, 69));
                        
                        JOptionPane.showMessageDialog(
                            DialogoMigracion.this,
                            "❌ Error durante la migración.\n\n" +
                            "Sus datos originales están seguros.\n" +
                            "Revise los logs para más detalles.",
                            "Error en Migración",
                            JOptionPane.ERROR_MESSAGE
                        );
                        
                        // Rehabilitar botones para reintentar
                        btnMigrar.setEnabled(true);
                        btnEmpezarLimpio.setEnabled(true);
                        btnCancelar.setEnabled(true);
                    }
                    
                } catch (Exception e) {
                    Logger.error("Error en worker de migración", e);
                    lblEstado.setText("Error inesperado durante la migración");
                    lblEstado.setForeground(new Color(220, 53, 69));
                }
            }
        };
        
        worker.execute();
    }
    
    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(150, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efecto hover
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(color);
            }
        });
        
        return btn;
    }
    
    public boolean isMigracionRealizada() {
        return migracionRealizada;
    }
}