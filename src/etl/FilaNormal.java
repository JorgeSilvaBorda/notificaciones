package etl;

import java.text.DecimalFormat;

/**
 * Clase creada para ordenar los datos de cada fila de salida de los remarcadores.
 * @author Jorge Silva Borda.
 */
public class FilaNormal {

    public String fechahora;
    public String fecha;
    public String hora;
    public int idremarcador;
    public double lecturaproyectada;
    public double potencia;
    public double lecturareal;
    public double delta;
    public double ultimomax;
    public String esmanual;
    public int lecturamanual;

    /**
     * Constructor de clase que recibe los parámetros de una fila de datos de un remarcador para mostrarlos de forma ordenada.
     * @param fechahora {@code String} Con el TIMESTAMP obtenido de la tabla origen.
     * @param fecha {@code String} Con la fecha procesada extraída de {@code fechahora}.
     * @param hora {@code String} Con la hora procesada extraída de {@code fechahora}.
     * @param idremarcador {@code int} con el ID del remarcador.
     * @param lecturaproyectada {@code double} con la continuidad calculada de la lectura de energía del remarcador.
     * @param potencia {@code double} con la potencia instantánea del remarcador.
     * @param lecturareal {@code double} con la lectura de energía real del remarcador. Puede tener saltos hacia atrás. Corresponde al valor real registrado en el remarcador.
     * @param delta {@code double} con la diferencia de energía en {@code lecturaproyectada}.
     * @param ultimomax {@code double} con el último valor máximo de energía real (no proyectado) del remarcador.
     * @param esmanual {@code String}. 'SI' Cuando hay lectura manual. 'NO' Cuando no hay lectura manual.
     * @param lecturamanual {@code int}. 0 cuando no hay lectura manual. [VALOR LECTURA] cuando hay lectura manual.
     */
    public FilaNormal(String fechahora, String fecha, String hora, int idremarcador, double lecturaproyectada, double potencia, double lecturareal, double delta, double ultimomax, String esmanual, int lecturamanual) {
        this.fechahora = fechahora;
        this.fecha = fecha;
        this.hora = hora;
        this.idremarcador = idremarcador;
        this.lecturaproyectada = lecturaproyectada;
        this.potencia = potencia;
        this.lecturareal = lecturareal;
        this.delta = delta;
        this.ultimomax = ultimomax;
        this.esmanual = esmanual;
        this.lecturamanual = lecturamanual;
    }
    
    /**
     * Imprime la fila con sus campos separados por ";" y sin cabeceras.
     * @return {@code String} con el contenido de la fila separado por ";".
     */
    public String printCsv(){
        DecimalFormat df = new DecimalFormat("#.##");
        return this.fechahora + ";" + this.fecha + ";" + this.hora + ";" + this.idremarcador + ";" + df.format(this.lecturaproyectada).replace(",", ".") + ";" + df.format(this.potencia).replace(",", ".") + ";" + df.format(this.lecturareal).replace(",", ".") + ";" + df.format(this.delta).replace(",", ".") + ";" + df.format(this.ultimomax).replace(",", ".") + ";" + this.esmanual + ";" + this.lecturamanual;
    }

}
