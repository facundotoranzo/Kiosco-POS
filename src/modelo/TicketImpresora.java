package modelo;

import javax.print.*;
import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

public class TicketImpresora {

    public static void imprimir(String contenido) {
        new Thread(() -> {
            try {
                // 1. Busca la impresora PREDETERMINADA (La que tiene el check verde en Windows)
                PrintService service = PrintServiceLookup.lookupDefaultPrintService();
                
                if (service == null) {
                    JOptionPane.showMessageDialog(null, "No se encontró una impresora predeterminada.");
                    return;
                }

                // 2. Obtener nombre del local desde configuración
                String nombreLocal = obtenerNombreLocal();

                // 3. Construimos el Ticket con códigos ESC/POS
                // Estos códigos funcionan en casi cualquier térmica (XPrinter, Epson, Hasar, chinas, etc.)
                String ESC = "\u001B";
                String GS = "\u001D";
                
                StringBuilder ticket = new StringBuilder();

                // --- INICIO ---
                ticket.append(ESC).append("@"); // Inicializar impresora

                // --- TÍTULO GIGANTE CENTRADO ---
                ticket.append(ESC).append("a").append((char)1); // Centrar
                ticket.append(ESC).append("!").append((char)48); // Doble alto y Doble ancho
                ticket.append(nombreLocal.toUpperCase()).append("\n");
                ticket.append(ESC).append("!").append((char)0); // Volver a letra normal

                // --- SEPARADOR ---
                ticket.append("--------------------------------\n");
                ticket.append(ESC).append("a").append((char)0); // Alinear Izquierda

                // --- CONTENIDO DEL TICKET ---
                // Quitamos acentos porque en modo RAW a veces salen caracteres raros
                String textoLimpio = contenido
                        .replace("á", "a").replace("Á", "A")
                        .replace("é", "e").replace("É", "E")
                        .replace("í", "i").replace("Í", "I")
                        .replace("ó", "o").replace("Ó", "O")
                        .replace("ú", "u").replace("Ú", "U")
                        .replace("ñ", "n").replace("Ñ", "N")
                        .replace("°", " ");

                ticket.append(textoLimpio);

                // --- FINAL Y CORTE ---
                ticket.append("\n\n\n\n\n"); // Avanzar papel
                ticket.append(GS).append("V").append((char)66).append((char)0); // CORTAR PAPEL

                // 4. ENVIAR BYTES (Lo importante)
                byte[] bytes = ticket.toString().getBytes();

                DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
                Doc doc = new SimpleDoc(bytes, flavor, null);

                DocPrintJob job = service.createPrintJob();
                job.print(doc, null);

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error al Imprimir: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Obtiene el nombre del local desde la configuración
     */
    private static String obtenerNombreLocal() {
        try {
            File f = new File("db_config.properties");
            if (f.exists()) {
                Properties prop = new Properties();
                try (FileInputStream fis = new FileInputStream(f)) {
                    prop.load(fis);
                }
                
                String licencia = prop.getProperty("licencia", "PRO").trim().toUpperCase();
                
                if ("LITE".equals(licencia)) {
                    return "Kiosco"; // Nombre fijo para versión LITE
                } else {
                    return prop.getProperty("nombre_local", "Mi Negocio").trim();
                }
            }
        } catch (Exception e) {
            Logger.error("Error al obtener nombre del local", e);
        }
        
        // Valor por defecto
        return "Kiosco";
    }
}