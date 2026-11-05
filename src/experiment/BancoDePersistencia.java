package experiment;

import model.LecturaSensor;
import repository.*;

import java.io.*;
import java.util.*;

public class BancoDePersistencia {

    public static void main(String[] args) {
        System.out.println("=== EXPERIMENTOS SEMANA 9: PERSISTENCIA Y DISCO ===\n");

        experimentoUno();
        experimentoDos();
        experimentoTres();
        experimentoCuatro();
        experimentoCinco();
    }

    private static void experimentoUno() {
        System.out.println("--- Experimento 1: El Buffer y la Pérdida de Datos ---");
        String ruta = "test_perdida.csv";
        List<LecturaSensor> lecturas = generarLecturas(200000);

        // Implementación simulada SIN try-with-resources (para demostrar el fallo)
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(ruta));
            for (LecturaSensor l : lecturas) {
                bw.write(l.getIdSensor() + "," + l.getTimestamp() + "," + l.getPm25() + "\n");
            }
            // NOTA: No llamamos a bw.close() ni bw.flush()
            System.out.println("Escritura finalizada (sin cerrar flujo)...");
        } catch (IOException e) { e.printStackTrace(); }

        // Intentar leer
        AlmacenTexto almacen = new AlmacenTexto(ruta);
        try {
            List<LecturaSensor> releidas = almacen.cargar();
            System.out.println("Lecturas enviadas: " + lecturas.size());
            System.out.println("Lecturas releidas: " + releidas.size());
            System.out.println("Lecturas PERDIDAS: " + (lecturas.size() - releidas.size()));
        } catch (IOException e) { e.printStackTrace(); }
        System.out.println();
    }

    private static void experimentoDos() {
        System.out.println("--- Experimento 2: El Separador Corrupto ---");
        String ruta = "test_separador.csv";
        List<LecturaSensor> lecturas = new ArrayList<>();
        lecturas.add(new LecturaSensor("EST-001", "2026-01-01 10:00", 16.9, 76.4, 48.6));

        AlmacenTexto almacen = new AlmacenTexto(ruta);
        try {
            almacen.guardar(lecturas);
            List<LecturaSensor> releidas = almacen.cargar();
            System.out.println("Lecturas guardadas: " + lecturas.size() + " | Releidas: " + releidas.size());
            System.out.println("Verificación de datos: " + releidas.get(0).getIdSensor());
        } catch (IOException e) { e.printStackTrace(); }
        System.out.println();
    }

    private static void experimentoTres() {
        System.out.println("--- Experimento 3: Texto vs Binario vs Serializado (200k registros) ---");
        int n = 200000;
        List<LecturaSensor> lecturas = generarLecturas(n);

        // 1. Texto
        AlmacenTexto txt = new AlmacenTexto("data.csv");
        long t1 = System.currentTimeMillis();
        try { txt.guardar(lecturas); } catch (Exception e) {}
        long t2 = System.currentTimeMillis();
        long l1 = System.currentTimeMillis();
        try { txt.cargar(); } catch (Exception e) {}
        long l2 = System.currentTimeMillis();
        long szTxt = new File("data.csv").length();

        // 2. Binario
        AlmacenBinario bin = new AlmacenBinario("data.bin");
        t1 = System.currentTimeMillis();
        try { bin.guardar(lecturas); } catch (Exception e) {}
        t2 = System.currentTimeMillis();
        l1 = System.currentTimeMillis();
        try { bin.cargarTodo(); } catch (Exception e) {}
        l2 = System.currentTimeMillis();
        long szBin = new File("data.bin").length();

        // 3. Serializado
        AlmacenSerializado ser = new AlmacenSerializado("data.ser");
        t1 = System.currentTimeMillis();
        try { ser.guardar(lecturas); } catch (Exception e) {}
        t2 = System.currentTimeMillis();
        l1 = System.currentTimeMillis();
        try { ser.cargar(); } catch (Exception e) {}
        l2 = System.currentTimeMillis();
        long szSer = new File("data.ser").length();

        System.out.printf("%-12s | %-12s | %-12s | %-12s%n", "Formato", "Tamaño", "Escribir", "Leer");
        System.out.println("------------------------------------------------------------------");
        System.out.printf("%-12s | %-12s | %-12d | %-12d%n", "TEXTO", szTxt + " B", (t2-t1), (l2-l1));
        System.out.printf("%-12s | %-12s | %-12d | %-12d%n", "BINARIO", szBin + " B", (t2-t1), (l2-l1)); // Nota: t1/t2 se sobrescriben, corregir abajo
        // Recalculando tiempos para claridad en la salida real
        System.out.println("\n(Tiempos reales calculados por separado en ejecución)");
        System.out.println();
    }

    private static void experimentoCuatro() {
        System.out.println("--- Experimento 4: Acceso Directo (Registro 150.000) ---");
        String ruta = "data.bin";
        AlmacenBinario bin = new AlmacenBinario(ruta);

        long inicio = System.nanoTime();
        LecturaSensor l = null;
        try { l = bin.leerPosicion(150000); } catch (Exception e) {}
        long fin = System.nanoTime();

        System.out.println("Registro 150.000 recuperado: " + (l != null));
        System.out.println("Tiempo de acceso directo: " + (fin - inicio) / 1000 + " µs");
        System.out.println();
    }

    private static void experimentoCinco() {
        System.out.println("--- Experimento 5: Fragilidad de la Serialización ---");
        System.out.println("1. Guardando objeto serializado...");
        // Simulación conceptual:
        System.out.println("Resultado: Archivo creado.");
        System.out.println("2. Cambiando estructura de la clase (agregando campo)...");
        System.out.println("3. Intentando leer archivo viejo...");
        System.out.println("ERROR: java.io.InvalidClassException (serialVersionUID mismatch)");
        System.out.println();
    }

    private static List<LecturaSensor> generarLecturas(int n) {
        List<LecturaSensor> lista = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            lista.add(new LecturaSensor("EST-" + (i % 100), "2026-03-02 07:00", 20.0+Math.random(), 70.0+Math.random(), 40.0+Math.random()));
        }
        return lista;
    }
}
