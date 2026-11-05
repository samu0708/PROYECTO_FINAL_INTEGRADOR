package repository;

import model.LecturaSensor;
import java.io.*;
import java.util.*;

/**
 * Implementación de persistencia en formato texto (CSV).
 * Demuestra la importancia del cierre de flujos y el manejo de separadores.
 */
public class AlmacenTexto {
    private final String ruta;
    private static final String SEPARADOR = ",";

    public AlmacenTexto(String ruta) {
        this.ruta = ruta;
    }

    /**
     * Guarda una lista de lecturas en formato CSV.
     * Implementado con try-with-resources para garantizar el vaciado del buffer.
     */
    public void guardar(List<LecturaSensor> lecturas) throws IOException {
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(ruta))) {
            // Encabezado
            escritor.write("idSensor,timestamp,pm25,temp,humedad,estado,observacion");
            escritor.newLine();

            for (LecturaSensor l : lecturas) {
                StringBuilder sb = new StringBuilder();
                sb.append(l.getIdSensor()).append(SEPARADOR)
                  .append(l.getTimestamp()).append(SEPARADOR)
                  .append(l.getPm25()).append(SEPARADOR)
                  .append(l.getTemperatura()).append(SEPARADOR)
                  .append(l.getHumedad());

                escritor.write(sb.toString());
                escritor.newLine();
            }
        }
    }

    public List<LecturaSensor> cargar() throws IOException {
        List<LecturaSensor> lecturas = new ArrayList<>();
        try (BufferedReader lector = new BufferedReader(new FileReader(ruta))) {
            String linea = lector.readLine(); // Saltar encabezado
            while ((linea = lector.readLine()) != null) {
                String[] campos = linea.split(SEPARADOR);
                if (campos.length >= 5) {
                    lecturas.add(new LecturaSensor(
                        campos[0],
                        campos[1],
                        Double.parseDouble(campos[2]),
                        Double.parseDouble(campos[3]),
                        Double.parseDouble(campos[4])
                    ));
                }
            }
        }
        return lecturas;
    }

    private String sanitizar(String texto) {
        if (texto == null) return "";
        // Solución simple: reemplazar el separador por un espacio
        // En una versión profesional se usarían comillas (" ")
        return texto.replace(SEPARADOR, " ");
    }
}
