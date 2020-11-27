/*
 * Programa creado para leer cierta cantidad de lecturas de los remarcadores
 * y generar notificaciones en caso de que exista un cantidad de lecturas nulas
 * en las tablas de los remarcadores.
 */
package notificaciones;

import etl.FilaNormal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

/**
 * Clase principal que se ejecuta del programa
 * @author jorge
 */
public class Main {

    /**
     * @param args the command line arguments
     */
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
        try{
            while(rs.next()){
                ids.add(rs.getInt("EQUIPO_ID"));
            }
        }catch (SQLException ex) {
            System.out.println("No se puede obtener el listado de remarcadores para consultar.");
            System.out.println(ex);
            ex.printStackTrace();
        }
        c.cerrar();
        int[] ides = new int[ids.size()];
        for(int i = 0; i < ids.size(); i++){
            ides[i] = ids.get(i);
        }
        
        //int[] ides = new int[1];
        //ides[0] = 21;
        procesarRemarcadores(ides, fechaant, fechaactual, cantlecturas);
    }
    
    private static void procesarRemarcadores(int[] ides, String desde, String hasta, int cantregistros){
        LinkedList<FilaNormal[]> remarcadores = new LinkedList();
        //Traerse todos los remarcadores
        for(int idremarcador : ides){
            System.out.println("Procesando ID: " + idremarcador);
            FilaNormal[] remarcador = etl.ETL.getDatasetRemarcador(idremarcador, desde, hasta);
            remarcadores.add(remarcador);
        }
        //Dejar solo las últimas x lecturas para cada uno
        LinkedList<FilaNormal[]> ultimaslecturas = new LinkedList();
        int cont = 1;
        for(FilaNormal[] remarcador : remarcadores){
            FilaNormal[] actual = new FilaNormal[cantregistros];
            for(int i = (remarcador.length - 1); i >= 0 && cont <= cantregistros; i--){
                actual[cont - 1] = remarcador[i];
                cont++;
            }
            cont = 1;
            ultimaslecturas.add(actual);
        }
        //Por cada listado de lecturas de remarcador, operar lecturaactual - primera lectura
        for(FilaNormal[] remarcador : ultimaslecturas){
            int numremarcador = remarcador[0].idremarcador;
            double ultimalectura = remarcador[0].lecturareal;
            double hacehoras = remarcador[remarcador.length - 1].lecturareal;
            String fechahoraanterior = remarcador[remarcador.length - 1].fechahora;
            String fechahoraactual = remarcador[0].fechahora;
            //System.out.println("ID: " + remarcador[0].idremarcador + " Last: " + ultimalectura + " - Ant: " + hacehoras + " = " + (ultimalectura - hacehoras) );
            if(ultimalectura - hacehoras == 0.000000d){
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
                con.ejecutar(query);
                con.cerrar();
            }
        }
        
    }
    
}
