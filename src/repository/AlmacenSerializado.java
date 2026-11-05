package repository;

import model.LecturaSensor;
import java.io.*;
import java.util.*;

/**
 * Implementación de persistencia mediante Serialización de Objetos de Java.
 * Demuestra la comodidad pero la rigidez de este formato.
 */
public class AlmacenSerializado {
    private final String ruta;

    public AlmacenSerializado(String ruta) {
        this.ruta = ruta;
    }

    public void guardar(List<LecturaSensor> lecturas) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ruta))) {
            oos.writeObject(lecturas);
        }
    }

    @SuppressWarnings("unchecked")
    public List<LecturaSensor> cargar() throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ruta))) {
            return (List<LecturaSensor>) ois.readObject();
        }
    }
}
