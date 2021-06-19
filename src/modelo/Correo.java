package modelo;

import java.util.LinkedList;

public class Correo {

    public int numfilas = 0;
    public String nomInstalacion;
    String subject = "Notificación de problemas de comunicación en remarcadores"; //Título del mensaje
    String tituloContenido;
    //Título que va sobre la tabla html.
    //String mensajeintro = "<html><body><p>Estimados, a continuación se entrega un detalle de los remarcadores que han presentado problemas de comunicación en el último sondeo (últimas " + HORAS_LEIDAS + " horas para cada remarcador).</p>" + "<br />";
    String mensaje; //El html. Todo el contenido con la tabla y el cierre del html.
    public String destinatarios; //des1@mail.com,des2@mail.com,des3@mail.com........
    LinkedList<String> filas = new LinkedList();
    public static int HORAS_LEIDAS;
    public static String TABLA_PRE;
    public static String TABLA_POST;

    public Correo(int HORAS_LEIDAS, String nomInstalacion, String destinatarios) {

        this.HORAS_LEIDAS = HORAS_LEIDAS;
        this.nomInstalacion = nomInstalacion;
        this.destinatarios = destinatarios;
        this.tituloContenido = "<html><body><p>Estimados, a continuación se entrega "
            + "un detalle de los remarcadores que han presentado problemas de "
            + "comunicación en el último sondeo " 
            + (nomInstalacion.equals("Todas") ? "de todas las instalaciones " : "de la instalación " + nomInstalacion) 
            + "(últimas " + HORAS_LEIDAS + " horas para cada remarcador).</p>" + "<br />";
        
        TABLA_PRE = "<table style='border: 1px solid black; border-collapse: collapse;'>"
                + "<thead>"
                + "<tr>"
                + "<th style='border: 1px solid black; font-weight: bold; padding: 1px 2px 1px 2px;'>ID Remarcador</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding: 1px 2px 1px 2px;'>Lectura actual</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding: 1px 2px 1px 2px;'>Fecha lectura<br />actual</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding: 1px 2px 1px 2px;'>Hora lectura<br />actual</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding: 1px 2px 1px 2px;'>Lectura anterior</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding: 1px 2px 1px 2px;'>Fecha lectura<br />anterior</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding: 1px 2px 1px 2px;'>Hora lectura<br />anterior</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding: 1px 2px 1px 2px;'>Instalación</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding: 1px 2px 1px 2px;'>Empalme</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding: 1px 2px 1px 2px;'>Bodega</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding: 1px 2px 1px 2px;'>Cliente</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding: 1px 2px 1px 2px;'>Lecturas<br />procesadas</th>"
                + "</tr>"
                + "</thead>"
                + "<tbody>";
        TABLA_POST = "</tbody></table>" + "<br />" + "<p>Este es un mensaje generado automáticamente. No responda este mensaje.</p>" + "<br />" + "<p>Bodenor Flexcenter<br />Software Gestor Lectura Remota de Remarcadores Eléctricos</p>.</body></html>";
    }

    public void insertarFila(String fila) {
        this.filas.add(fila);
        this.numfilas++;
    }

    public String getContenido() {
        StringBuilder builder = new StringBuilder();
        builder.append(tituloContenido);
        builder.append(TABLA_PRE);
        for (String fila : filas) {
            builder.append(fila);
        }
        builder.append(TABLA_POST);
        return builder.toString();
    }
}
