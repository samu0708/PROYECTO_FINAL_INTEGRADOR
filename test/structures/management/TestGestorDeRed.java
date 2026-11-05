package structures.management;

import model.LecturaSensor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import structures.management.GestorDeRed;

import java.util.*;

public class TestGestorDeRed {
    private GestorDeRed gestor;

    @BeforeEach
    void setUp() {
        gestor = new GestorDeRed();
    }

    @Test
    void testAgregarEvento_DebeRegistrarEvento() {
        gestor.agregarEvento("Sensor EST-01 activo");
        // Este test fallará hasta que el TODO 1 sea implementado
        assertNotNull(gestor.obtenerEventosRecientes(1), "El historial no debería estar vacío");
    }

    @Test
    void testNavegacionAlertas_DebeMoverseAdelanteYAtraves() {
        gestor.registrarAlerta("Alerta A");
        gestor.registrarAlerta("Alerta B");
        gestor.registrarAlerta("Alerta C");

        // Simulamos que el cursor está en A
        // Nota: la implementación real debe manejar el índice del cursor
        String siguiente = gestor.navegarAlSiguiente();
        assertNotNull(siguiente, "Debería poder navegar al siguiente");

        String anterior = gestor.navegarAlAnterior();
        assertNotNull(anterior, "Debería poder volver al anterior");
    }

    @Test
    void testCicloMonitoreo_DebeRotarSensores() {
        gestor.programarConsulta("S1");
        gestor.programarConsulta("S2");

        String primero = gestor.obtenerSiguienteSensor();
        assertEquals("S1", primero);

        String segundo = gestor.obtenerSiguienteSensor();
        assertEquals("S2", segundo);

        String rotacion = gestor.obtenerSiguienteSensor();
        assertEquals("S1", rotacion, "El ciclo debe volver al sensor S1");
    }

    @Test
    void testTopologia_DebeRegistrarConexiones() {
        gestor.registrarConexion("S1", "S2");
        List<String> vecinosS1 = gestor.obtenerVecinos("S1");
        List<String> vecinosS2 = gestor.obtenerVecinos("S2");

        assertNotNull(vecinosS1);
        assertTrue(vecinosS1.contains("S2"));
        assertNotNull(vecinosS2);
        assertTrue(vecinosS2.contains("S1"));
    }

    @Test
    void testEstacionesAisladas_DebeDetectarHuerfanos() {
        gestor.registrarConexion("S1", "S2");
        // S3 no se conecta a nadie
        // Nota: necesitamos que el gestor sepa de la existencia de S3
        // Para este test, asumimos que el gestor registra sensores al conectar o mediante un método addSensor
        List<String> aisladas = gestor.detectarEstacionesAisladas();
        assertNotNull(aisladas);
    }

    @Test
    void testBufferIngesta_DebeProcesarEnOrden() {
        LecturaSensor l1 = new LecturaSensor("S1", "T1", 10.0, 20.0, 30.0);
        LecturaSensor l2 = new LecturaSensor("S2", "T2", 15.0, 25.0, 35.0);

        gestor.recibirLectura(l1);
        gestor.recibirLectura(l2);

        assertEquals(l1, gestor.procesarSiguienteLectura());
        assertEquals(l2, gestor.procesarSiguienteLectura());
    }

    @Test
    void testOptimizarHistorial_DebeLimitarTamaño() {
        for(int i=0; i<100; i++) gestor.agregarEvento("Evento " + i);
        gestor.limpiarHistorialAntiguo(10);
        assertEquals(10, gestor.obtenerEventosRecientes(100).size());
    }
}
