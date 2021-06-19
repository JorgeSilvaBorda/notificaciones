package notificaciones;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import modelo.Conexion;
import modelo.Correo;
import modelo.LecturaController;
import modelo.lectura.Registro;

public class Start {

    protected static String USERNAME, PASSWORD;
    protected static int HORAS_LEIDAS;

    public static void main(String[] args) {
        String fechaactual = args[0];
        String fechaant = args[1];
        int cantlecturas = Integer.parseInt(args[2]);
        HORAS_LEIDAS = (int) (cantlecturas * 5) / 60;
        Conexion c = new Conexion();
        String query = "CALL SP_GET_ORIGEN_REMARCADORES_FECHAS("
                + "'" + fechaant + "',"
                + "'" + fechaactual + "'"
                + ")";
        c.abrir();
        System.out.println(query);
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

        //Traer los Destinatarios
        LinkedList<Correo> correos = getDestinatarios(HORAS_LEIDAS);
        correos = getNotificaciones(ides, fechaant, fechaactual, cantlecturas, correos);

        for (Correo cor : correos) {
            System.out.println("");
            System.out.println("");
            System.out.println("");
            System.out.println(cor.getContenido());
            System.out.println("");
            System.out.println("");
            System.out.println("");
            if(cor.numfilas > 0){
                enviar(cor);
            }
        }
    }

    private static LinkedList<Correo> getDestinatarios(int HORAS_LEIDAS) {
        String query = "CALL SP_GET_DESTINATARIOS_NOTIFICACION_ORDENADOS()";
        Conexion c = new Conexion();
        System.out.println(query);
        c.abrir();
        ResultSet rs = c.ejecutarQuery(query);
        LinkedList<Correo> correos = new LinkedList();
        try {
            while (rs.next()) {
                Correo correo = new Correo(HORAS_LEIDAS, rs.getString("NOMINSTALACION"), rs.getString("CORREOS"));
                correos.add(correo);
            }
            return correos;
        } catch (Exception ex) {
            System.out.println("No se puede obtener los destinatarios por instalación.");
            System.out.println(ex);
            ex.printStackTrace();
            return new LinkedList<Correo>();
        }
    }

    private static LinkedList<Correo> getNotificaciones(int[] ides, String desde, String hasta, int cantregistros, LinkedList<Correo> correos) {
        for (int id : ides) {
            LinkedList<Registro> registros = LecturaController.getRegistrosDesdeHastaRemarcadorNumremarcador(id, desde, hasta);
            int cont = 1;
            float sumaDelta = 0;
            float hacehoras = 0;
            String fechahoraanterior = "";
            for (int i = (registros.size() - 1); i >= registros.size() - cantregistros - 1; i--) {
                sumaDelta += registros.get(i).delta;
                fechahoraanterior = registros.get(i).timestamp;
                hacehoras = registros.get(i).lectura;
            }

            String fechahoraactual = registros.get(registros.size() - 1).timestamp;
            float ultimalectura = registros.get(registros.size() - 1).lectura;
            System.out.println("Numremarcador: " + id);
            System.out.println("Ultima lectura: " + fechahoraactual + ":" + ultimalectura);
            System.out.println("Anterior lectura: " + fechahoraanterior + ":" + hacehoras);
            System.out.println("La suma de los últimos " + cantregistros + " es: " + sumaDelta);
            if (sumaDelta == 0) {
                System.out.println("Se notifica");
                //Se inserta la notificación para poder obtener información de referencia del remarcador
                System.out.println("Se escribe notificacion:");
                System.out.println("IDREMARCADOR: " + id);
                System.out.println("Fecha actual: " + fechahoraactual);
                System.out.println("Fecha anterior: " + fechahoraanterior);
                System.out.println("Lectura actual: " + ultimalectura);
                System.out.println("Lectura anterior: " + hacehoras);
                System.out.println("Lecturas de diferencia: " + cantregistros);

                Conexion con = new Conexion();
                String query = "CALL SP_INS_NOTIFICACION("
                        + id + ", "
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
                        for (Correo correo : correos) {
                            System.out.println("Se compara del correo: " + correo.nomInstalacion + " con la que se manda: " + rs.getString("NOMINSTALACION"));
                            if (correo.nomInstalacion.trim().equals(rs.getString("NOMINSTALACION").trim()) || correo.nomInstalacion.equals("Todas")) {
                                System.out.println("Se agrega la fila al correo");
                                String fila = "";
                                fila += "<tr>";
                                fila += "<td style='border: 1px solid black'>" + id + "</td>";
                                fila += "<td style='border: 1px solid black'>" + rs.getDouble("LECTURAACTUAL") + "</td>";
                                fila += "<td style='border: 1px solid black'>" + rs.getString("FECHAACTUALFORMAT") + "</td>";
                                fila += "<td style='border: 1px solid black'>" + rs.getString("HORAACTUAL") + "</td>";
                                fila += "<td style='border: 1px solid black'>" + rs.getDouble("LECTURAANTERIOR") + "</td>";
                                fila += "<td style='border: 1px solid black'>" + rs.getString("FECHAANTERIORFORMAT") + "</td>";
                                fila += "<td style='border: 1px solid black'>" + rs.getString("HORAANTERIOR") + "</td>";
                                fila += "<td style='border: 1px solid black'>" + rs.getString("NOMINSTALACION") + "</td>";
                                fila += "<td style='border: 1px solid black'>" + rs.getString("NUMEMPALME") + "</td>";
                                fila += "<td style='border: 1px solid black'>" + rs.getString("NOMPARQUE") + "</td>";
                                fila += "<td style='border: 1px solid black'>" + rs.getString("NOMCLIENTE") + "</td>";
                                fila += "<td style='border: 1px solid black'>" + cantregistros + "</td>";
                                fila += "</tr>";
                                correo.insertarFila(fila);
                            }
                        }
                        /*
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
                         */
                    }
                } catch (SQLException ex) {
                    System.out.println("No se pudo insertar la notificación ni obtener el código de estado del remarcador.");
                    System.out.println(ex);
                    ex.printStackTrace();
                }
                con.cerrar();
            }
        }
        return correos;
    }

    public static void enviar(Correo correo) {
        //Si hay destinatarios se envía mail. Si no, no.

        //Para cada grupo de destinatarios, realizar el envío
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
                    InternetAddress.parse(correo.destinatarios)
            );
            message.setSubject("Notificación de problemas de comunicación en remarcadores");
            message.setContent(correo.getContenido(), "text/html; charset=UTF-8");

            Transport.send(message);
            System.out.println("Email enviado");
        } catch (MessagingException ex) {
            System.out.println("No se pudo enviar el mensaje");
            System.out.println(ex);
            ex.printStackTrace();
        }

    }
}
