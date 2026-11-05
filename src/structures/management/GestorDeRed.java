package structures.management;

import model.LecturaSensor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GestorDeRed - Módulo Integrador de Estructuras de Datos
 * VERSION ESTUDIANTE
 *
 * Objetivo: Implementar la gestión de la red utilizando las colecciones de Java (JCF).
 * El éxito de este módulo no es solo que el código compile, sino que elijas
 * la estructura correcta para cada problema.
 */
public class GestorDeRed {
    // 1. Historial de Eventos: Registro lineal de todas las actividades
    private List<String> historialEventos;

    // 2. Navegador de Alertas: Permite moverse adelante y atrás entre alertas
    private Deque<String> navegadorAlertas;
    private String alertaActual;

    // 3. Ciclo de Monitoreo: Gestión de la rotación de sensores a consultar
    private Queue<String> colaMonitoreo;

    // 4. Topología de la Red: Grafo de conexiones entre estaciones (Lista de adyacencia)
    private Map<String, List<String>> topologiaRed;

    // 5. Buffer de Ingesta: Cola de espera para procesar lecturas entrantes
    private Queue<LecturaSensor> bufferIngesta;

    public GestorDeRed() {
        // TODO: Inicializa las colecciones usando las implementaciones más adecuadas de Java
        // (ArrayList, ArrayDeque, HashMap, etc.)
        this.historialEventos = new ArrayList<>();
        this.navegadorAlertas = new ArrayDeque<>();
        this.colaMonitoreo = new ArrayDeque<>();
        this.topologiaRed = new HashMap<>();
        this.bufferIngesta = new ArrayDeque<>();
    }

    // =========================================================================
    // SECCIÓN 1: HISTORIAL Y EVENTOS
    // =========================================================================

    public void agregarEvento(String evento) {
        // TODO 1: Implementar la adición del evento al historial.
        // Debe registrarse la fecha/hora actual junto con la descripción del evento.
        // Ejemplo: "[2026-10-24 10:00] Sensor EST-01 activado"
    }

    public List<String> obtenerEventosRecientes(int limite) {
        if (historialEventos.size() <= limite) return new ArrayList<>(historialEventos);
        return historialEventos.subList(historialEventos.size() - limite, historialEventos.size());
    }

    public void limpiarHistorialAntiguo(int mantener) {
        // TODO 10: Implementar la optimización del historial.
        // Eliminar los registros más antiguos para mantener solo los últimos 'mantener' eventos.
    }

    // =========================================================================
    // SECCIÓN 2: NAVEGACIÓN DE ALERTAS
    // =========================================================================

    public void registrarAlerta(String alerta) {
        navegadorAlertas.addLast(alerta);
        alertaActual = alerta;
    }

    public String navegarAlSiguiente() {
        // TODO 2: Implementar la lógica de desplazamiento hacia adelante.
        // Debe devolver la siguiente alerta o null si no hay más.
        // Recuerda actualizar el estado de 'alertaActual'.
        return null;
    }

    public String navegarAlAnterior() {
        // TODO 3: Implementar la lógica de desplazamiento hacia atrás.
        // Debe devolver la alerta anterior o null si es la primera.
        // Recuerda actualizar el estado de 'alertaActual'.
        return null;
    }

    public String getAlertaActual() { return alertaActual; }

    // =========================================================================
    // SECCIÓN 3: CICLO DE MONITOREO
    // =========================================================================

    public void programarConsulta(String idSensor) {
        // TODO 4: Implementar la programación de la consulta.
        // El sensor debe agregarse a la cola solo si NO está ya programado.
    }

    public String obtenerSiguienteSensor() {
        // TODO 5: Implementar la rotación del ciclo.
        // Debe extraer el sensor del frente de la cola y volverlo a insertar al final
        // para garantizar que todos los sensores sean consultados continuamente.
        return null;
    }

    // =========================================================================
    // SECCIÓN 4: TOPOLOGÍA DE RED
    // =========================================================================

    public void registrarConexion(String sensorA, String sensorB) {
        // TODO 6: Implementar la conexión bidireccional entre dos sensores.
        // Debe agregar sensorB a la lista de vecinos de sensorA, y viceversa.
        // Asegurar que no se creen conexiones duplicadas.
    }

    public List<String> obtenerVecinos(String idSensor) {
        // TODO 7: Implementar la consulta de conexiones.
        // Debe devolver la lista de sensores conectados al idSensor proporcionado.
        // Retornar una lista vacía si el sensor no tiene conexiones.
        return null;
    }

    public List<String> detectarEstacionesAisladas() {
        // TODO 9: Implementar la detección de nodos huérfanos.
        // Debe retornar una lista de todos los sensores registrados que no tienen ninguna conexión.
        return null;
    }

    // =========================================================================
    // SECCIÓN 5: BUFFER DE INGESTA
    // =========================================================================

    public void recibirLectura(LecturaSensor lectura) {
        bufferIngesta.add(lectura);
    }

    public LecturaSensor procesarSiguienteLectura() {
        // TODO 8: Implementar la extracción del buffer.
        // Debe remover y devolver la lectura más antigua del buffer (FIFO).
        return null;
    }

    public List<LecturaSensor> filtrarAlertasCriticas(double limitePm25) {
        // TODO EXTRA: Usar Streams para filtrar lecturas con PM2.5 superior al límite.
        // Debe retornar una lista con las lecturas filtradas.
        return null;
    }
}
