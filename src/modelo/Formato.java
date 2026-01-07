package modelo;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class Formato {
    
    // Método estático para usar en todo el programa
    public static String moneda(double valor) {
        // Configuramos los símbolos: Punto para miles, Coma para decimales
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols();
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');

        // Patrón: Signo pesos, espacio, separador de miles y siempre 2 decimales
        DecimalFormat df = new DecimalFormat("$ #,##0.00", simbolos);
        
        return df.format(valor);
    }
}