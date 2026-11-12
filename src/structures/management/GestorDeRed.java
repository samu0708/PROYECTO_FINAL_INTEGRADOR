package structures.management;

import model.LecturaSensor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GestorDeRed - Módulo Integrador de Estructuras de Datos
 *
 * Gestión de la red de sensores con el Java Collections Framework (JCF).
 * Cada colección se eligió según la carga de trabajo de su problema.
 */
public class GestorDeRed {
    // 1. Historial de Eventos: se agrega al final y se lee por posición -> ArrayList
    private List<String> historialEventos;

    // 2. Navegador de Alertas: alertas que están DELANTE del cursor (Deque)
    private Deque<String> navegadorAlertas;
    // Alertas que están DETRÁS del cursor (el tope es la más cercana)
    private Deque<String> alertasAnteriores;
    private String alertaActual;

    // 3. Ciclo de Monitoreo: cola FIFO de turnos
    private Queue<String> colaMonitoreo;

    // 4. Topología de la Red: grafo como lista de adyacencia
    private Map<String, List<String>> topologiaRed;

    // 5. Buffer de Ingesta: cola FIFO de lecturas entrantes
    private Queue<LecturaSensor> bufferIngesta;

    public GestorDeRed() {
        this.historialEventos = new ArrayList<>();
        this.navegadorAlertas = new ArrayDeque<>();
        this.alertasAnteriores = new ArrayDeque<>();
        this.colaMonitoreo = new ArrayDeque<>();
        this.topologiaRed = new HashMap<>();
        this.bufferIngesta = new ArrayDeque<>();
    }

    // =========================================================================
    // SECCIÓN 1: HISTORIAL Y EVENTOS
    // =========================================================================

    // TODO 1
    public void agregarEvento(String evento) {
        historialEventos.add("[" + new java.util.Date() + "] " + evento);
    }

    public List<String> obtenerEventosRecientes(int limite) {
        // Se devuelve una COPIA: subList es una vista y falla si el historial cambia después.
        if (historialEventos.size() <= limite) return new ArrayList<>(historialEventos);
        return new ArrayList<>(
                historialEventos.subList(historialEventos.size() - limite, historialEventos.size()));
    }

    // TODO 10
    public void limpiarHistorialAntiguo(int mantener) {
        if (mantener < 0) mantener = 0;
        int exceso = historialEventos.size() - mantener;
        if (exceso > 0) {
            // Un solo desplazamiento O(n), en vez de n veces remove(0) (O(n^2)).
            historialEventos.subList(0, exceso).clear();
        }
    }

    // =========================================================================
    // SECCIÓN 2: NAVEGACIÓN DE ALERTAS
    // =========================================================================

    /**
     * La primera alerta registrada queda como actual; las demás quedan "adelante".
     */
    public void registrarAlerta(String alerta) {
        if (alertaActual == null) {
            alertaActual = alerta;
        } else {
            navegadorAlertas.addLast(alerta);
        }
    }

    // TODO 2
    public String navegarAlSiguiente() {
        if (navegadorAlertas.isEmpty()) return null;
        alertasAnteriores.addFirst(alertaActual);
        alertaActual = navegadorAlertas.pollFirst();
        return alertaActual;
    }

    // TODO 3
    public String navegarAlAnterior() {
        if (alertasAnteriores.isEmpty()) return null;
        navegadorAlertas.addFirst(alertaActual);
        alertaActual = alertasAnteriores.pollFirst();
        return alertaActual;
    }

    public String getAlertaActual() { return alertaActual; }

    // =========================================================================
    // SECCIÓN 3: CICLO DE MONITOREO
    // =========================================================================

    // TODO 4
    public void programarConsulta(String idSensor) {
        if (idSensor != null && !colaMonitoreo.contains(idSensor)) {
            colaMonitoreo.add(idSensor);
        }
    }

    // TODO 5
    public String obtenerSiguienteSensor() {
        String sensor = colaMonitoreo.poll();
        if (sensor == null) return null;
        colaMonitoreo.add(sensor); // rotación: vuelve al final
        return sensor;
    }

    // =========================================================================
    // SECCIÓN 4: TOPOLOGÍA DE RED
    // =========================================================================

    /**
     * Registra un sensor en la red sin conexiones (nodo aislado).
     * Necesario para que el reto 9 tenga sentido.
     */
    public void registrarSensor(String idSensor) {
        topologiaRed.computeIfAbsent(idSensor, k -> new ArrayList<>());
    }

    // TODO 6
    public void registrarConexion(String sensorA, String sensorB) {
        registrarSensor(sensorA);
        registrarSensor(sensorB);
        if (sensorA.equals(sensorB)) return; // sin auto-conexiones

        List<String> vecinosA = topologiaRed.get(sensorA);
        List<String> vecinosB = topologiaRed.get(sensorB);
        if (!vecinosA.contains(sensorB)) vecinosA.add(sensorB);
        if (!vecinosB.contains(sensorA)) vecinosB.add(sensorA);
    }

    // TODO 7
    public List<String> obtenerVecinos(String idSensor) {
        return new ArrayList<>(topologiaRed.getOrDefault(idSensor, Collections.emptyList()));
    }

    // TODO 9
    public List<String> detectarEstacionesAisladas() {
        List<String> aisladas = new ArrayList<>();
        for (Map.Entry<String, List<String>> e : topologiaRed.entrySet()) {
            if (e.getValue().isEmpty()) aisladas.add(e.getKey());
        }
        return aisladas;
    }

    // =========================================================================
    // SECCIÓN 5: BUFFER DE INGESTA
    // =========================================================================

    public void recibirLectura(LecturaSensor lectura) {
        bufferIngesta.add(lectura);
    }

    // TODO 8
    public LecturaSensor procesarSiguienteLectura() {
        return bufferIngesta.poll(); // FIFO; null si está vacío
    }

    // TODO EXTRA
    public List<LecturaSensor> filtrarAlertasCriticas(double limitePm25) {
        return bufferIngesta.stream()
                .filter(l -> l.getPm25() > limitePm25)
                .collect(Collectors.toList());
    }
}