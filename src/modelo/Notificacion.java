package modelo;

/**
 * @author Jorgeg Silva Borda
 */
public class Notificacion {
    public int numremarcador;
    public String codestado;
    public String fechaactual;
    public String horaactual;
    public String fechaanterior;
    public String horaanterior;
    public double lecturaactual;
    public double lecturaanterior;
    public int cantregistros;
    
    public String numempalme;
    public String nomparque;
    public String nominstalacion;
    public String rutfullcliente;
    public String nomcliente;

    public Notificacion(int numremarcador, String codestado, String fechaactual, String horaactual, String fechaanterior, String horaanterior, double lecturaactual, double lecturaanterior, int cantregistros, String numempalme, String nomparque, String nominstalacion, String rutfullcliente, String nomcliente) {
        this.numremarcador = numremarcador;
        this.codestado = codestado;
        this.fechaactual = fechaactual;
        this.horaactual = horaactual;
        this.fechaanterior = fechaanterior;
        this.horaanterior = horaanterior;
        this.lecturaactual = lecturaactual;
        this.lecturaanterior = lecturaanterior;
        this.cantregistros = cantregistros;
        this.numempalme = numempalme;
        this.nomparque = nomparque;
        this.nominstalacion = nominstalacion;
        this.rutfullcliente = rutfullcliente;
        this.nomcliente = nomcliente;
    }
    
    
    
}
