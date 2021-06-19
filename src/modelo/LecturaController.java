package modelo;

import java.sql.ResultSet;
import java.util.LinkedList;
import modelo.lectura.Registro;

public class LecturaController {
    
    public static LinkedList<Registro> getRegistrosDesdeHastaRemarcadorNumremarcador(int numremarcador, String fechadesde, String fechahasta) {
        LinkedList<Registro> registros = new LinkedList<>();
        System.out.println("Buscando dataset remarcador num: " + numremarcador + " Desde: " + fechadesde + " hasta: " + fechahasta);

        Conexion c;

        //Obtener orígen del remarcador que viene con su número
        String queryOrigen = "SELECT FN_GET_ORIGEN_NUMREMARCADOR(" + numremarcador + ") ORIGEN";
        System.out.println(queryOrigen);
        c = new Conexion();
        c.setReplica();
        c.abrir();
        ResultSet rs = c.ejecutarQuery(queryOrigen);
        String origen = "";
        try {
            while (rs.next()) {
                origen = rs.getString("ORIGEN");
            }
        } catch (Exception ex) {
            System.out.println("No se pudo obtener el origen del remarcador NUM: " + numremarcador);
            System.out.println(ex);
            c.cerrar();
        }
        c.cerrar();
        String query = "";

        //Ir a traer todas las lecturas del mes
        if (origen.equals("circutorcvmC10")) {
            query = "CALL SP_GET_LECTURAS_DESDE_HASTA_CIRCUTOR(" + numremarcador + ", '" + fechadesde + "', '" + fechahasta + "')";
        } else if (origen.equals("schneiderPM710")) {
            query = "CALL SP_GET_LECTURAS_DESDE_HASTA_PM710(" + numremarcador + ", '" + fechadesde + "', '" + fechahasta + "')";
        } else if (origen.equals("schneiderPM5300")) {
            query = "CALL SP_GET_LECTURAS_DESDE_HASTA_PM5300(" + numremarcador + ", '" + fechadesde + "', '" + fechahasta + "')";
        }
        c = new Conexion();
        c.setReplica();
        c.abrir();
        System.out.println(query);
        rs = c.ejecutarQuery(query);
        Registro anterior = new Registro();
        Registro r = new Registro();
        try {
            while (rs.next()) {

                r = new Registro();

                float lecactual = rs.getFloat("LECTURA");
                if (rs.wasNull()) {//Si viene una lectura nula-------------------------------------------------------------------------------------------
                    float lecactualmanual = rs.getFloat("LECTURAMANUAL");
                    if (rs.wasNull()) { //Si la lectura manual también es nula --------------------------------------------------------------------------
                        if (anterior.dia != null) { //Cuando existe un registro anterior
                            r = new Registro(rs.getString("DIA"), rs.getInt("NUMREMARCADOR"), rs.getString("TIMESTAMP"), anterior.lectura);
                            //r.esmanual = true;
                            r.delta = 0f;
                        } else { //Cuando no existe un registro anterior, no se puede obtener lectura hacia atrás.
                            //Se deja la lectura y delta en cero.
                            r = new Registro(rs.getString("DIA"), rs.getInt("NUMREMARCADOR"), rs.getString("TIMESTAMP"), 0f);
                            r.delta = 0f;
                        }
                        r.delta = 0f; //Se deja el delta en cero.
                    } else { //Si la lectura manual no es nula.
                        r = new Registro(rs.getString("DIA"), rs.getInt("NUMREMARCADOR"), rs.getString("TIMESTAMP"), rs.getFloat("LECTURAMANUAL"));
                        r.existe = true;
                        r.esmanual = true;
                        if (!anterior.existe) { //Cuando existe un registro anterior, es decir, es el primero
                            r.delta = anterior.lectura - r.lectura;
                        } else { //Cuando no existe un registro anterior, no se puede obtener lectura hacia atrás.
                            //Se deja el delta en cero
                            r.delta = 0f;
                        }
                    }
                } else { //La lectura que viene no es nula----------------------------------------------------------------------------------------------
                    r = new Registro(rs.getString("DIA"), rs.getInt("NUMREMARCADOR"), rs.getString("TIMESTAMP"), rs.getFloat("LECTURA"));
                    r.existe = true;
                    float lecactualmanual = rs.getFloat("LECTURAMANUAL");
                    if (!rs.wasNull()) {//Si la lectura es manual, manda ésta. Se cambia.
                        r.esmanual = true;
                        r.lectura = rs.getFloat("LECTURAMANUAL");
                    }
                    if (!anterior.existe) { //Si no existe anterior. es decir, ésta es la primera
                        r.delta = 0;
                    } else {//No es la primera
                        if(r.lectura >= anterior.lectura){
                            r.delta = r.lectura - anterior.lectura;
                        }else{
                            //r.lectura = anterior.lectura;
                            r.delta = 0f;
                        }
                        
                    }
                }

                anterior = r;
                registros.add(r);
            }
        } catch (Exception ex) {
            System.out.println("No se pudo obtener los registros del remarcador.");
            System.out.println(ex);
            c.cerrar();
        }
        return registros;
    }
    
    public static LinkedList<Registro> getRegistrosMesRemarcadorNumremarcador(int numremarcador, int anio, int mes) {
        LinkedList<Registro> registros = new LinkedList<>();
        System.out.println("Buscando dataset remarcador num: " + numremarcador + " Año: " + anio + " mes: " + mes);

        Conexion c;

        //Obtener orígen del remarcador que viene con su número
        String queryOrigen = "SELECT FN_GET_ORIGEN_NUMREMARCADOR(" + numremarcador + ") ORIGEN";
        System.out.println(queryOrigen);
        c = new Conexion();
        c.setReplica();
        c.abrir();
        ResultSet rs = c.ejecutarQuery(queryOrigen);
        String origen = "";
        try {
            while (rs.next()) {
                origen = rs.getString("ORIGEN");
            }
        } catch (Exception ex) {
            System.out.println("No se pudo obtener el origen del remarcador NUM: " + numremarcador);
            System.out.println(ex);
            c.cerrar();
        }
        c.cerrar();
        String query = "";

        //Ir a traer todas las lecturas del mes
        if (origen.equals("circutorcvmC10")) {
            query = "CALL SP_GET_LECTURAS_MES_CIRCUTOR(" + numremarcador + ", " + anio + ", " + mes + ")";
        } else if (origen.equals("schneiderPM710")) {
            query = "CALL SP_GET_LECTURAS_MES_PM710(" + numremarcador + ", " + anio + ", " + mes + ")";
        } else if (origen.equals("schneiderPM5300")) {
            query = "CALL SP_GET_LECTURAS_MES_PM5300(" + numremarcador + ", " + anio + ", " + mes + ")";
        }
        c = new Conexion();
        c.setReplica();
        c.abrir();
        System.out.println(query);
        rs = c.ejecutarQuery(query);
        Registro anterior = new Registro();
        Registro r = new Registro();
        try {
            while (rs.next()) {

                r = new Registro();

                float lecactual = rs.getFloat("LECTURA");
                if (rs.wasNull()) {//Si viene una lectura nula-------------------------------------------------------------------------------------------
                    float lecactualmanual = rs.getFloat("LECTURAMANUAL");
                    if (rs.wasNull()) { //Si la lectura manual también es nula --------------------------------------------------------------------------
                        if (anterior.dia != null) { //Cuando existe un registro anterior
                            r = new Registro(rs.getString("DIA"), rs.getInt("NUMREMARCADOR"), rs.getString("TIMESTAMP"), anterior.lectura);
                            //r.esmanual = true;
                            r.delta = 0f;
                        } else { //Cuando no existe un registro anterior, no se puede obtener lectura hacia atrás.
                            //Se deja la lectura y delta en cero.
                            r = new Registro(rs.getString("DIA"), rs.getInt("NUMREMARCADOR"), rs.getString("TIMESTAMP"), 0f);
                            r.delta = 0f;
                        }
                        r.delta = 0f; //Se deja el delta en cero.
                    } else { //Si la lectura manual no es nula.
                        r = new Registro(rs.getString("DIA"), rs.getInt("NUMREMARCADOR"), rs.getString("TIMESTAMP"), rs.getFloat("LECTURAMANUAL"));
                        r.existe = true;
                        r.esmanual = true;
                        if (!anterior.existe) { //Cuando existe un registro anterior, es decir, es el primero
                            r.delta = anterior.lectura - r.lectura;
                        } else { //Cuando no existe un registro anterior, no se puede obtener lectura hacia atrás.
                            //Se deja el delta en cero
                            r.delta = 0f;
                        }
                    }
                } else { //La lectura que viene no es nula----------------------------------------------------------------------------------------------
                    r = new Registro(rs.getString("DIA"), rs.getInt("NUMREMARCADOR"), rs.getString("TIMESTAMP"), rs.getFloat("LECTURA"));
                    r.existe = true;
                    float lecactualmanual = rs.getFloat("LECTURAMANUAL");
                    if (!rs.wasNull()) {//Si la lectura es manual, manda ésta. Se cambia.
                        r.esmanual = true;
                        r.lectura = rs.getFloat("LECTURAMANUAL");
                    }
                    if (!anterior.existe) { //Si no existe anterior. es decir, ésta es la primera
                        r.delta = 0;
                    } else {//No es la primera
                        if(r.lectura >= anterior.lectura){
                            r.delta = r.lectura - anterior.lectura;
                        }else{
                            //r.lectura = anterior.lectura;
                            r.delta = 0f;
                        }
                        
                    }
                }

                anterior = r;
                registros.add(r);
            }
        } catch (Exception ex) {
            System.out.println("No se pudo obtener los registros del remarcador.");
            System.out.println(ex);
            c.cerrar();
        }
        return registros;
    }

}
