package notificaciones;

public class Destinatario {
    public int idinstalacion;
    public String nominstalacion;
    public String correos;
    public String contenido;

    public Destinatario() {
    }

    public Destinatario(int idinstalacion, String nominstalacion, String correos) {
        this.idinstalacion = idinstalacion;
        this.nominstalacion = nominstalacion;
        this.correos = correos;
    }
}
