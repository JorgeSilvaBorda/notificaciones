package modelo;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Clase para realizar las conexiones con BD.
 * La configuración de las propiedades de la base de datos se obtiene del archivo application.properties.
 * La ubicación de dicho archivo debe encontrarse en una variable de entorno llamada PANEL_PROPERTIES.
 * Para Windows: set PANEL_PROPERTIES=c:\ruta\al\archivo\application.properties
 * Para Linux: PANEL_PROPERTIES=/home/algo/ruta/al/archivo/application.properties
 */
public class Conexion {

    private String host;
    private String user;
    private String pass;
    private String bd;
    private int port;
    private Connection con;

    public Conexion() {
	setParams();
    }

    /**
     * Método que ajusta los parámetros de conexión en base al archivo properties ubicado en la variable de 
     * entorno PANEL_PROPERTIES.
     * Éste método se llama al instanciar la clase Conexión. Es obligatorio
     */
    private void setParams() {
	
	String rutaProperties = System.getenv("RUTA_PROPERTIES"); //Habilitar para lectura desde variable de entorno
	if(rutaProperties == null){
	    System.out.println("Error: No se puede leer desde la variable de entorno RUTA_PROPERTIES.");
	    System.out.println("    Asegúrese de que se encuentre correctamente ajustada en el sistema");
	    System.out.println("    como variable global y que la ruta que contenga sea la que corresponde");
	    System.out.println("    a la ubicación del archivo application.properties que contiene la ");
	    System.out.println("    configuración de la aplicación WebPanel");
	}
	try{
	    InputStream entrada = new FileInputStream(rutaProperties);
	    Properties prop = new Properties();
	    prop.load(entrada);
	    this.host = prop.getProperty("bd.master.host").trim();
	    this.user = prop.getProperty("bd.master.user").trim();
	    this.port = Integer.parseInt(prop.getProperty("bd.master.port").trim());
	    this.bd = prop.getProperty("bd.master.bd").trim();
	    this.pass = prop.getProperty("bd.master.password").trim();
	}catch (IOException | NumberFormatException ex) {
	    System.out.println("Ocurrió un problema al ajustar los parámetros de conexión.");
	    System.out.println(ex);
	}
	
    }
    
    public void setReplica() {
	
	String rutaProperties = System.getenv("RUTA_PROPERTIES"); //Habilitar para lectura desde variable de entorno
	if(rutaProperties == null){
	    System.out.println("Error: No se puede leer desde la variable de entorno RUTA_PROPERTIES.");
	    System.out.println("    Asegúrese de que se encuentre correctamente ajustada en el sistema");
	    System.out.println("    como variable global y que la ruta que contenga sea la que corresponde");
	    System.out.println("    a la ubicación del archivo application.properties que contiene la ");
	    System.out.println("    configuración de la aplicación WebPanel");
	}
	try{
	    InputStream entrada = new FileInputStream(rutaProperties);
	    Properties prop = new Properties();
	    prop.load(entrada);
	    this.host = prop.getProperty("bd.replica.host").trim();
	    this.user = prop.getProperty("bd.replica.user").trim();
	    this.port = Integer.parseInt(prop.getProperty("bd.replica.port").trim());
	    this.bd = prop.getProperty("bd.replica.bd").trim();
	    this.pass = prop.getProperty("bd.replica.password").trim();
	}catch (IOException | NumberFormatException ex) {
	    System.out.println("Ocurrió un problema al ajustar los parámetros de conexión.");
	    System.out.println(ex);
	}
	
    }

    /**
     * Abre la conexión para ejecutar consultas.
     * La conexión debe cerrarse manualmente luego de una operación.
     */
    public void abrir() {
	Connection conn;
	try {
	    Class.forName("com.mysql.cj.jdbc.Driver");
	    conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + bd + "?useLegacyDatetimeCode=false&useUnicode=true&characterEncoding=utf8", user, pass);
	    this.con = conn;
	} catch (ClassNotFoundException | SQLException ex) {
	    System.out.println("Problemas al abrir la conexion.");
	    System.out.println(ex);
	    this.con = null;
	}
    }

    /**
     * Cierra la conexión con base de datos.
     */
    public void cerrar() {
	try {
	    if (!this.con.isClosed()) {
		this.con.close();
	    }
	} catch (SQLException ex) {
	    System.out.println("Problemas al cerrar la conexion.");
	    System.out.println(ex);
	}
    }

    /**
     * Ejecuta consultas del tipo INSERT, UPDATE, DELETE.
     * @param query {@code String} Contiene la query a ejecutar.
     */
    public void ejecutar(String query) {
	try {
	    Statement st = this.con.createStatement();
	    st.executeUpdate(query);
	} catch (SQLException ex) {
	    System.out.println("Problemas al ejecutar: " + query);
	    System.out.println(ex);
	}
    }

    /**
     * Ejecuta consultas del tipo SELECT
     * @param query {@code String} con la consulta a ejecutar.
     * @return {@code ResultSet} listo para recorrer con el puntero.
     */
    public ResultSet ejecutarQuery(String query) {
	Connection conn;
	try {
	    Statement st = con.createStatement();
	    ResultSet rs = st.executeQuery(query);
	    return rs;
	} catch (SQLException ex) {
	    System.out.println("Problemas al ejecutar query: " + query);
	    System.out.println(ex);
	    return null;
	}
    }
}
