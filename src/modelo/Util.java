package modelo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Properties;
import static javax.management.Query.value;

public class Util {

    public static int generaRandom(int ini, int fin) {
        return (int) Math.floor(Math.random() * (fin - ini + 1) + ini);
    }

    public static String hashMD5(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashInBytes = md.digest(texto.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : hashInBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("Problemas al cifrar el mensaje");
            System.out.println(ex);
            return "";
        }

    }

    public static String armarSelect(ResultSet rs, String primerItemValue, String primerItemText, String colValues, String colTextos) {
        String salida = "<option value='" + primerItemValue + "'>" + primerItemText + "</option>";
        try {
            while (rs.next()) {
                salida += "<option value='" + rs.getString(colValues) + "'>" + rs.getString(colTextos) + "</option>";
            }
        } catch (SQLException ex) {
            System.out.println("No se puede armar el select.");
            System.out.println(ex);
        }
        return salida;
    }

    public static float redondearDecimales(float numero, int cantDecimales) {
        BigDecimal bd = new BigDecimal(Float.toString(numero));
        bd = bd.setScale(cantDecimales, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public static BigDecimal redondear(float numero, int cantDecimales) {
        BigDecimal bd = new BigDecimal(Float.toString(numero));
        bd = bd.setScale(cantDecimales, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

    public static String formatRut(String rut) {

        int cont = 0;
        String format;
        rut = rut.replace(".", "");
        rut = rut.replace("-", "");
        format = "-" + rut.substring(rut.length() - 1);
        for (int i = rut.length() - 2; i >= 0; i--) {
            format = rut.substring(i, i + 1) + format;
            cont++;
            if (cont == 3 && i != 0) {
                format = "." + format;
                cont = 0;
            }
        }
        return format;
    }

    public static boolean validarRut(String rut) {

        boolean validacion = false;
        try {
            rut = rut.toUpperCase();
            rut = rut.replace(".", "");
            rut = rut.replace("-", "");
            int rutAux = Integer.parseInt(rut.substring(0, rut.length() - 1));

            char dv = rut.charAt(rut.length() - 1);

            int m = 0, s = 1;
            for (; rutAux != 0; rutAux /= 10) {
                s = (s + rutAux % 10 * (9 - m++ % 6)) % 11;
            }
            if (dv == (char) (s != 0 ? s + 47 : 75)) {
                validacion = true;
            }

        } catch (java.lang.NumberFormatException e) {
        } catch (Exception e) {
        }
        return validacion;
    }

    public static String formatMiles(int numero) {
        String pattern = "###,###,###.###";
        //Si no le paso ningun Locale, toma el del sistema, que en mi caso es Locale("es","MX");
        DecimalFormat myFormatter = new DecimalFormat(pattern);
        String output = myFormatter.format(numero);
        return output;
    }

    public static String formatMiles(String numero) {
        BigDecimal num = BigDecimal.valueOf(Double.parseDouble(numero));
        String pattern = "###,###,###.###";
        //Si no le paso ningun Locale, toma el del sistema, que en mi caso es Locale("es","MX");
        DecimalFormat myFormatter = new DecimalFormat(pattern);
        String output = myFormatter.format(num);
        return output;
    }

    public static String formatMiles(BigDecimal numero) {
        String pattern = "###,###,###.###";
        //Si no le paso ningun Locale, toma el del sistema, que en mi caso es Locale("es","MX");
        DecimalFormat myFormatter = new DecimalFormat(pattern);
        String output = myFormatter.format(numero);
        return output;
    }
    
    public static String formatMiles(Double numero) {
        String pattern = "###,###,###.###";
        //Si no le paso ningun Locale, toma el del sistema, que en mi caso es Locale("es","MX");
        DecimalFormat myFormatter = new DecimalFormat(pattern);
        String output = myFormatter.format(numero);
        return output;
    }

    public static String armarBody(ResultSet rs, String[] arrCampos) throws SQLException {
        String salida = "<thead><tr>";
        for (int i = 0; i < arrCampos.length; i++) {
            salida += "<th>" + arrCampos[i] + "</th>";
        }
        salida += "</tr></thead>";
        salida += "<tbody>";
        while (rs.next()) {
            salida += "<tr>" + rs.getRef(salida);
            for (int x = 0; x < arrCampos.length; x++) {
                salida += "<td>" + rs.getRef(arrCampos[x]) + "</td>";
            }
            salida += "</tr>";
        }
        salida += "</tbody>";
        return salida;
    }

    public static String getProperty(String attr) {
        String rutaProperties = System.getenv("RUTA_PROPERTIES"); //Habilitar para lectura desde variable de entorno
        if (rutaProperties == null) {
            return "";
        }
        try {
            InputStream entrada = new FileInputStream(rutaProperties);
            Properties prop = new Properties();
            prop.load(entrada);
            return prop.getProperty(attr);
        } catch (IOException | NumberFormatException ex) {
            System.out.println("Ocurrió un problema al obtener un valor del archivo properties.");
            System.out.println(ex);
            return "";
        }
    }

    public static String capitalizarString(String frase) {
        frase = frase.toLowerCase();
        String[] palabras = frase.split("\\s");
        String capitalizada = "";
        for (String palabra : palabras) {
            String first = palabra.substring(0, 1);
            String afterfirst = palabra.substring(1);
            capitalizada += first.toUpperCase() + afterfirst + " ";
        }
        return capitalizada.trim();
    }

    public static String invertirFecha(String fecha) {
        String[] campos = fecha.split("-");
        return campos[2] + "-" + campos[1] + "-" + campos[0];
    }

    public static String invertirDateTimeGetFecha(String datetime) {
        String fecha = datetime.substring(0, 10);
        String[] campos = fecha.split("-");
        return campos[2] + "-" + campos[1] + "-" + campos[0];
    }

    public static String capitalizar(String frase) {
        System.out.println(frase);
        if (frase.contains(" ")) {
            String[] palabras = frase.split("\\s");
            StringBuilder b = new StringBuilder();
            for (String palabra : palabras) {
                String p = palabra.substring(0, 1).toUpperCase() + palabra.substring(1).toLowerCase();
                b.append(p);
            }
            return b.toString().trim();
        } else if (frase.equals("")) {
            return frase;
        }

        return frase.substring(0, 1).toUpperCase() + frase.substring(1).toLowerCase().trim();
    }

    public static String mesAPalabraCorto(int mes) {
        switch (mes) {
            case 1:
                return "Ene";
            case 2:
                return "Feb";
            case 3:
                return "Mar";
            case 4:
                return "Abr";
            case 5:
                return "May";
            case 6:
                return "Jun";
            case 7:
                return "Jul";
            case 8:
                return "Ago";
            case 9:
                return "Sep";
            case 10:
                return "Oct";
            case 11:
                return "Nov";
            case 12:
                return "Dic";
            default:
                return "";
        }
    }

    public static String mesAPalabraCorto(String mes) {
        try {
            int mesint = Integer.parseInt(mes);
            switch (mesint) {
                case 1:
                    return "Ene";
                case 2:
                    return "Feb";
                case 3:
                    return "Mar";
                case 4:
                    return "Abr";
                case 5:
                    return "May";
                case 6:
                    return "Jun";
                case 7:
                    return "Jul";
                case 8:
                    return "Ago";
                case 9:
                    return "Sep";
                case 10:
                    return "Oct";
                case 11:
                    return "Nov";
                case 12:
                    return "Dic";
                default:
                    return "";
            }
        } catch (Exception ex) {
            System.out.println("Problemas al castear desde String a int (Util.mesAPalabraCorto(String mes))");
            return "";
        }

    }

    public static String getApiHttpURL() {
        String rutaProperties = System.getenv("RUTA_PROPERTIES"); //Habilitar para lectura desde variable de entorno
        if (rutaProperties == null) {
            System.out.println("Error: No se puede leer desde la variable de entorno RUTA_PROPERTIES.");
            System.out.println("    Asegúrese de que se encuentre correctamente ajustada en el sistema");
            System.out.println("    como variable global y que la ruta que contenga sea la que corresponde");
            System.out.println("    a la ubicación del archivo application.properties que contiene la ");
            System.out.println("    configuración de la aplicación WebPanel");
            return "";
        } else {
            try {
                InputStream entrada = new FileInputStream(rutaProperties);
                Properties prop = new Properties();
                prop.load(entrada);

                String apiHost = prop.getProperty("api.host");
                String apiPort = prop.getProperty("api.port");
                String apiUrlBase = prop.getProperty("api.url.base");

                return "http://" + apiHost + ((apiPort.equals("") || apiPort == null) ? "" : ":" + apiPort) + apiUrlBase;
            } catch (IOException | NumberFormatException ex) {
                System.out.println("Ocurrió un problema al ajustar los parámetros del API.");
                System.out.println(ex);
                return "";
            }
        }
    }
    
    public static String getApiHttpsURL() {
        String rutaProperties = System.getenv("RUTA_PROPERTIES"); //Habilitar para lectura desde variable de entorno
        if (rutaProperties == null) {
            System.out.println("Error: No se puede leer desde la variable de entorno RUTA_PROPERTIES.");
            System.out.println("    Asegúrese de que se encuentre correctamente ajustada en el sistema");
            System.out.println("    como variable global y que la ruta que contenga sea la que corresponde");
            System.out.println("    a la ubicación del archivo application.properties que contiene la ");
            System.out.println("    configuración de la aplicación WebPanel");
            return "";
        } else {
            try {
                InputStream entrada = new FileInputStream(rutaProperties);
                Properties prop = new Properties();
                prop.load(entrada);

                String apiHost = prop.getProperty("api.host");
                String apiPort = prop.getProperty("api.port");
                String apiUrlBase = prop.getProperty("api.url.base");

                return "https://" + apiHost + ((apiPort.equals("") || apiPort == null) ? "" : ":" + apiPort) + apiUrlBase;
            } catch (IOException | NumberFormatException ex) {
                System.out.println("Ocurrió un problema al ajustar los parámetros del API.");
                System.out.println(ex);
                return "";
            }
        }
    }
}
