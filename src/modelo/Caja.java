package modelo;

public class Caja {
    private int id;
    private String fechaApertura;
    private String fechaCierre;
    private double totalCierre;      // Solo mercadería
    private double totalEfectivo;    // Solo mercadería efectivo
    private double totalTransferencia; // Solo mercadería transf
    
    // NUEVOS CAMPOS DIVIDIDOS
    private double cigarrillosEfectivo; 
    private double cigarrillosTransferencia;
    
    private String estado;

    public Caja(int id, String fAp, String fCi, double tot, double eff, double transf, double cigEfec, double cigTransf, String est) {
        this.id = id;
        this.fechaApertura = fAp;
        this.fechaCierre = fCi;
        this.totalCierre = tot;
        this.totalEfectivo = eff;
        this.totalTransferencia = transf;
        this.cigarrillosEfectivo = cigEfec;
        this.cigarrillosTransferencia = cigTransf;
        this.estado = est;
    }

    public int getId() { return id; }
    public String getFechaApertura() { return fechaApertura; }
    public String getFechaCierre() { return fechaCierre; }
    public double getTotal() { return totalCierre; }
    public double getTotalEfectivo() { return totalEfectivo; }
    public double getTotalTransferencia() { return totalTransferencia; }
    
    // Getters nuevos
    public double getCigarrillosEfectivo() { return cigarrillosEfectivo; }
    public double getCigarrillosTransferencia() { return cigarrillosTransferencia; }
    public double getTotalCigarrillosGlobal() { return cigarrillosEfectivo + cigarrillosTransferencia; }
    
    public String getEstado() { return estado; }
}