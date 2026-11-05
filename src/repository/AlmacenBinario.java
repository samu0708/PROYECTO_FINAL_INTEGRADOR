package repository;

import model.LecturaSensor;
import java.io.*;
import java.util.*;

/**
 * Implementación de persistencia en formato binario de longitud fija.
 * Permite el acceso directo (Random Access) mediante el cálculo de posición.
 */
public class AlmacenBinario {
    private final String ruta;

    // CÁLCULO DE TAMANO_REGISTRO:
    // idSensor: 8 chars * 2 bytes (UTF-16) = 16 bytes
    // timestamp: 19 chars * 2 bytes (UTF-16) = 38 bytes
    // pm25: double = 8 bytes
    // temp: double = 8 bytes
    // humedad: double = 8 bytes
    // TOTAL = 16 + 38 + 8 + 8 + 8 = 78 bytes (Ajustado a la implementación de writeChar)
    // Nota: Para simplificar y asegurar el experimento, usamos una constante verificada.
    private static final int TAMANO_REGISTRO = 78;

    public AlmacenBinario(String ruta) {
        this.ruta = ruta;
    }

    public void guardar(List<LecturaSensor> lecturas) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(ruta, "rw")) {
            raf.setLength(0); // Limpiar archivo
            for (LecturaSensor l : lecturas) {
                escribirRegistro(raf, l);
            }
        }
    }

    private void escribirRegistro(RandomAccessFile raf, LecturaSensor l) throws IOException {
        // Escribir idSensor (fijo 8 chars)
        String id = l.getIdSensor();
        for (int i = 0; i < 8; i++) {
            raf.writeChar(i < id.length() ? id.charAt(i) : ' ');
        }
        // Escribir timestamp (fijo 19 chars)
        String ts = l.getTimestamp();
        for (int i = 0; i < 19; i++) {
            raf.writeChar(i < ts.length() ? ts.charAt(i) : ' ');
        }
        // Escribir valores
        raf.writeDouble(l.getPm25());
        raf.writeDouble(l.getTemperatura());
        raf.writeDouble(l.getHumedad());
    }

    public LecturaSensor leerPosicion(int n) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(ruta, "r")) {
            long posicion = (long) n * TAMANO_REGISTRO;
            if (posicion >= raf.length()) return null;

            raf.seek(posicion);

            // Leer idSensor (8 chars)
            StringBuilder id = new StringBuilder();
            for (int i = 0; i < 8; i++) id.append(raf.readChar());

            // Leer timestamp (19 chars)
            StringBuilder ts = new StringBuilder();
            for (int i = 0; i < 19; i++) ts.append(raf.readChar());

            double pm25 = raf.readDouble();
            double temp = raf.readDouble();
            double hum = raf.readDouble();

            return new LecturaSensor(id.toString().trim(), ts.toString().trim(), pm25, temp, hum);
        }
    }

    public List<LecturaSensor> cargarTodo() throws IOException {
        List<LecturaSensor> lecturas = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(ruta, "r")) {
            int n = 0;
            while (raf.getFilePointer() < raf.length()) {
                lecturas.add(leerPosicion(n++));
            }
        }
        return lecturas;
    }
}
