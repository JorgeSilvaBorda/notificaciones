/*
 * Programa creado para leer cierta cantidad de lecturas de los remarcadores
 * y generar notificaciones en caso de que exista un cantidad de lecturas nulas
 * en las tablas de los remarcadores.
 */
package notificaciones;

import etl.FilaNormal;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Clase principal que se ejecuta del programa
 *
 * @author jorge
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    private static String USERNAME, PASSWORD;

    public static void main(String[] args) {
        String fechaactual = args[0];
        String fechaant = args[1];
        int cantlecturas = Integer.parseInt(args[2]);

        Conexion c = new Conexion();
        String query = "CALL SP_GET_ORIGEN_REMARCADORES_FECHAS("
                + "'" + fechaant + "',"
                + "'" + fechaactual + "'"
                + ")";
        c.abrir();
        ResultSet rs = c.ejecutarQuery(query);
        LinkedList<Integer> ids = new LinkedList();
        try {
            while (rs.next()) {
                ids.add(rs.getInt("EQUIPO_ID"));
            }
        } catch (SQLException ex) {
            System.out.println("No se puede obtener el listado de remarcadores para consultar.");
            System.out.println(ex);
            ex.printStackTrace();
        }
        c.cerrar();
        int[] ides = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++) {
            ides[i] = ids.get(i);
        }

        //int[] ides = new int[1];
        //ides[0] = 21;
        procesarRemarcadores(ides, fechaant, fechaactual, cantlecturas);
    }

    private static void procesarRemarcadores(int[] ides, String desde, String hasta, int cantregistros) {
        LinkedList<FilaNormal[]> remarcadores = new LinkedList();
        //Traerse todos los remarcadores
        for (int idremarcador : ides) {
            System.out.println("Procesando ID: " + idremarcador);
            FilaNormal[] remarcador = etl.ETL.getDatasetRemarcador(idremarcador, desde, hasta);
            remarcadores.add(remarcador);
        }
        //Dejar solo las últimas x lecturas para cada uno
        LinkedList<FilaNormal[]> ultimaslecturas = new LinkedList();
        int cont = 1;
        for (FilaNormal[] remarcador : remarcadores) {
            FilaNormal[] actual = new FilaNormal[cantregistros];
            for (int i = (remarcador.length - 1); i >= 0 && cont <= cantregistros; i--) {
                actual[cont - 1] = remarcador[i];
                cont++;
            }
            cont = 1;
            ultimaslecturas.add(actual);
        }

        LinkedList<Notificacion> notificaciones = new LinkedList();
        //Por cada listado de lecturas de remarcador, operar lecturaactual - primera lectura
        for (FilaNormal[] remarcador : ultimaslecturas) {
            int numremarcador = remarcador[0].idremarcador;
            double ultimalectura = remarcador[0].lecturareal;
            double hacehoras = remarcador[remarcador.length - 1].lecturareal;
            String fechahoraanterior = remarcador[remarcador.length - 1].fechahora;
            String fechahoraactual = remarcador[0].fechahora;
            //System.out.println("ID: " + remarcador[0].idremarcador + " Last: " + ultimalectura + " - Ant: " + hacehoras + " = " + (ultimalectura - hacehoras) );
            if (ultimalectura - hacehoras == 0.000000d) {
                System.out.println("Se escribe notificacion:");
                System.out.println("IDREMARCADOR: " + numremarcador);
                System.out.println("Fecha actual: " + fechahoraactual);
                System.out.println("Fecha anterior: " + fechahoraanterior);
                System.out.println("Lectura actual: " + ultimalectura);
                System.out.println("Lectura anterior: " + hacehoras);
                System.out.println("Lecturas de diferencia: " + cantregistros);

                Conexion con = new Conexion();
                String query = "CALL SP_INS_NOTIFICACION("
                        + numremarcador + ", "
                        + "'" + fechahoraactual + "',"
                        + "'" + fechahoraanterior + "',"
                        + ultimalectura + ","
                        + hacehoras + ", "
                        + cantregistros + ""
                        + ")";
                con.abrir();
                System.out.println("Query: " + query);
                ResultSet rs = con.ejecutarQuery(query);
                try {
                    while (rs.next()) {
                        notificaciones.add(new Notificacion(
                                rs.getInt("NUMREMARCADOR"),
                                rs.getString("CODESTADO"),
                                rs.getString("FECHAACTUALFORMAT"),
                                rs.getString("HORAACTUAL"),
                                rs.getString("FECHAANTERIORFORMAT"),
                                rs.getString("HORAANTERIOR"),
                                rs.getDouble("LECTURAACTUAL"),
                                rs.getDouble("LECTURAANTERIOR"),
                                rs.getInt("LECTURASDIFERENCIA"),
                                rs.getString("NUMEMPALME"),
                                rs.getString("NOMPARQUE"),
                                rs.getString("NOMINSTALACION"),
                                rs.getString("RUTFULLCLIENTE"),
                                rs.getString("NOMCLIENTE")
                        ));
                    }
                } catch (SQLException ex) {
                    System.out.println("No se pudo insertar la notificación ni obtener el código de estado del remarcador.");
                    System.out.println(ex);
                    ex.printStackTrace();
                }
                con.cerrar();
            }

        }
        procesarEmail(notificaciones);
    }

    public static void procesarEmail(LinkedList<Notificacion> notificaciones) {
        //Construir tabla de remarcadores a notificar
        String mensajeintro = "<html><body><p>Estimados, a continuación se entrega un detalle de los remarcadores que han presentado problemas de comunicación en el último sondeo.</p>" + "<br />";
        String tabla = "<table style='border: 1px solid black; border-collapse: collapse;'>"
                + "<thead>"
                + "<tr>"
                + "<th style='border: 1px solid black; font-weight: bold; padding-left: 2px; padding-right: 2px;'>ID Remarcador</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding-left: 2px; padding-right: 2px;'>Lectura actual</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding-left: 2px; padding-right: 2px;'>Fecha lectura<br />actual</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding-left: 2px; padding-right: 2px;'>Hora lectura<br />actual</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding-left: 2px; padding-right: 2px;'>Lectura anterior</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding-left: 2px; padding-right: 2px;'>Fecha lectura<br />anterior</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding-left: 2px; padding-right: 2px;'>Hora lectura<br />anterior</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding-left: 2px; padding-right: 2px;'>Instalación</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding-left: 2px; padding-right: 2px;'>Empalme</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding-left: 2px; padding-right: 2px;'>Bodega</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding-left: 2px; padding-right: 2px;'>Cliente</th>"
                + "<th style='border: 1px solid black; font-weight: bold; padding-left: 2px; padding-right: 2px;'>Lecturas<br />procesadas</th>"
                + "</tr>"
                + "</thead>"
                + "<tbody>";

        for (Notificacion notificacion : notificaciones) {
            if (notificacion.codestado.equals("NUEVO")) {
                tabla += "<tr>";
                tabla += "<td style='border: 1px solid black'>" + notificacion.numremarcador + "</td>";
                tabla += "<td style='border: 1px solid black'>" + notificacion.lecturaactual + "</td>";
                tabla += "<td style='border: 1px solid black'>" + notificacion.fechaactual + "</td>";
                tabla += "<td style='border: 1px solid black'>" + notificacion.horaactual + "</td>";
                tabla += "<td style='border: 1px solid black'>" + notificacion.lecturaanterior + "</td>";
                tabla += "<td style='border: 1px solid black'>" + notificacion.fechaanterior + "</td>";
                tabla += "<td style='border: 1px solid black'>" + notificacion.horaanterior + "</td>";
                tabla += "<td style='border: 1px solid black'>" + notificacion.nominstalacion + "</td>";
                tabla += "<td style='border: 1px solid black'>" + notificacion.numempalme + "</td>";
                tabla += "<td style='border: 1px solid black'>" + notificacion.nomparque + "</td>";
                tabla += "<td style='border: 1px solid black'>" + notificacion.nomcliente + "</td>";
                tabla += "<td style='border: 1px solid black'>" + notificacion.cantregistros + "</td>";
                tabla += "</tr>";
            }
        }
        tabla += "</tbody></table>" + "<br />" + "<p>Este es un mensaje generado automáticamente. No responda este mensaje.</p>" + "<br />" + "<p>Gestión Bodenor</p>.</body></html>";

        //Buscar destinatarios
        String query = "CALL SP_GET_DESTINATARIOS_NOTIFICACION()";
        Conexion c = new Conexion();
        c.abrir();
        ResultSet rs = c.ejecutarQuery(query);
        String destinatarios = "";
        int cantdestinatarios = 0;
        try {
            while (rs.next()) {
                destinatarios += rs.getString("EMAILDESTINATARIO") + ",";
                cantdestinatarios++;
            }

        } catch (SQLException ex) {
            System.out.println("No se pudo obtener el listado de destinatarios para enviar notificaciones.");
            System.out.println(ex);
            ex.printStackTrace();
            c.cerrar();
        }
        c.cerrar();

        //Si hay destinatarios se envía mail. Si no, no.
        if (cantdestinatarios > 0) {
            try {
                String rutaProperties = System.getenv("RUTA_PROPERTIES");
                InputStream entrada = new FileInputStream(rutaProperties);
                Properties propUser = new Properties();
                propUser.load(entrada);
                USERNAME = propUser.getProperty("mail.username");
                PASSWORD = propUser.getProperty("mail.password");
            } catch (IOException ex) {
                System.out.println("No se puede obtener la información de credenciales para envio de correos.");
                System.out.println(ex);
                ex.printStackTrace();
            }

            final String username = USERNAME;
            final String password = PASSWORD;

            //java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            Properties prop = new Properties();

            prop.put("mail.smtp.host", "smtp.gmail.com");
            prop.put("mail.smtp.port", "587");
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.starttls.enable", "true"); //TLS
            Session session = Session.getInstance(prop,
                    new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(USERNAME));
                message.setRecipients(
                        Message.RecipientType.TO,
                        InternetAddress.parse(destinatarios)
                );
                message.setSubject("Notificación de problemas de comunicación en remarcadores");
                message.setContent(mensajeintro + tabla, "text/html");

                Transport.send(message);
                System.out.println("Email enviado");
            } catch (MessagingException ex) {
                System.out.println("No se pudo enviar el mensaje");
                System.out.println(ex);
                ex.printStackTrace();
            }
        }

    }

}
