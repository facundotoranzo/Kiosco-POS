package modelo;

public class Configuracion {
    private String nombreNegocio;
    private String mensajeTicket;
    private String impresora;
    

    public Configuracion(String nombreNegocio, String mensajeTicket, String impresora) {
        this.nombreNegocio = nombreNegocio;
        this.mensajeTicket = mensajeTicket;
        this.impresora = impresora;
    }

    public String getNombreNegocio() { return nombreNegocio; }
    public String getMensajeTicket() { return mensajeTicket; }
    public String getImpresora() { return impresora; }
}