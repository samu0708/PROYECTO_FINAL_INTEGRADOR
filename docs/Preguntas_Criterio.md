# Respuestas a las preguntas de criterio de decisión

Proyecto integrador: Gestor de red de sensores IoT (`GestorDeRed`).

---

## 1. ArrayList vs. LinkedList: eliminar eventos antiguos con frecuencia

**Costo de las operaciones**

| Operación | `ArrayList` | `LinkedList` |
|---|---|---|
| Eliminar el primer elemento | O(n): hay que correr todos los demás una posición a la izquierda | O(1): solo se mueve la referencia `primero` |
| Agregar al final | O(1) amortizado | O(1) |
| Leer la posición `i` | O(1) | O(n): hay que recorrer nodo por nodo |

**Argumento.** Si la única carga fuera "eliminar los eventos más antiguos con mucha frecuencia", la estructura más eficiente es la basada en nodos (`LinkedList`, o mejor aún `ArrayDeque` si solo se necesitan los extremos), porque quitar el primero no obliga a desplazar nada. En un `ArrayList`, cada `remove(0)` mueve los `n - 1` elementos restantes. Si se hace en un `while` para recortar el historial, el costo total es O(n²).

**Decisión tomada en el proyecto.** En `GestorDeRed` el historial es un `ArrayList` y `limpiarHistorialAntiguo` hace:

```java
historialEventos.subList(0, exceso).clear();
```

Esto desplaza los elementos **una sola vez** (O(n) por limpieza, no por cada elemento eliminado). Elegí `ArrayList` porque el historial también se consulta por posición: `obtenerEventosRecientes` toma los últimos `n` con `subList`, y eso en una `LinkedList` implicaría recorrer la cadena.

**Costo oculto de `LinkedList`.** Cada elemento ocupa un nodo extra con dos referencias, y los nodos quedan dispersos en memoria, lo que empeora el uso de la caché del procesador. Por eso no es siempre mejor aunque sus eliminaciones al inicio sean O(1).

**Conclusión.** Si la limpieza es frecuente y por lotes, `ArrayList` con `subList().clear()` es suficiente. Si se eliminara un evento a la vez y miles de veces por segundo, cambiaría a `LinkedList` o `ArrayDeque`, y renunciaría al acceso por posición.

---

## 2. Deque vs. Queue y su relación con `NodoDoble`

**Diferencia.**

- `Queue` es una cola FIFO: se inserta por un extremo (`add`/`offer`, al final) y se saca por el otro (`poll`, del frente). Solo se opera en dos puntos fijos y en una sola dirección.
- `Deque` (*double-ended queue*) permite insertar, consultar y sacar por **ambos extremos**: `addFirst`, `addLast`, `pollFirst`, `pollLast`. Por eso puede comportarse como cola, como pila o como ambas cosas a la vez.

**Aplicación en el navegador de alertas.** Un botón "Atrás/Adelante" necesita mover alertas de un lado al otro del cursor. Con una `Queue` simple no se puede devolver un elemento al frente ni sacar del final. Usé dos `Deque`:

- `navegadorAlertas`: alertas que están **adelante** del cursor.
- `alertasAnteriores`: alertas que están **atrás**; su tope es la más cercana.

```java
// siguiente
alertasAnteriores.addFirst(alertaActual);
alertaActual = navegadorAlertas.pollFirst();

// anterior
navegadorAlertas.addFirst(alertaActual);
alertaActual = alertasAnteriores.pollFirst();
```

Cada movimiento es O(1), y cuando no hay más alertas en una dirección se devuelve `null`.

**Relación con `NodoDoble`.** Un `NodoDoble` (como en `HistorialNavegable`) guarda el dato, `siguiente` y `anterior`. Esos dos enlaces son lo que permite recorrer la lista en ambos sentidos y operar en ambos extremos en O(1). `Deque` es la interfaz que expone esa capacidad. `LinkedList` la implementa literalmente con nodos dobles. `ArrayDeque` ofrece el mismo contrato con un arreglo circular, sin nodos, y suele ser más rápido en la práctica. Por eso conviene programar contra la interfaz `Deque`.

---

## 3. Colas y equidad frente a una pila

**Por qué una cola.** El ciclo de monitoreo debe consultar a todos los sensores por turnos, sin que ninguno se quede esperando. Una `Queue` es FIFO: el sensor que lleva más tiempo esperando es el siguiente. Con la rotación de `obtenerSiguienteSensor` (sacar del frente con `poll` y volver a insertar al final) se obtiene un *round-robin*:

```
S1, S2, S3 → S1 → S2 → S3 → S1 → ...
```

Cada sensor recibe exactamente un turno por vuelta.

**Efecto de usar una pila (`Stack`).** Una pila es LIFO: el último en entrar es el primero en salir. Si se sacara un sensor y se volviera a apilar, quedaría otra vez en el tope y sería el siguiente en salir. El sistema consultaría siempre el mismo sensor, y los que están debajo **nunca** serían atendidos (inanición, *starvation*). Además, si se programan sensores nuevos mientras otros esperan, los nuevos pasarían delante de los antiguos.

**Conclusión.** La cola garantiza equidad y orden predecible. La pila es la estructura adecuada para "deshacer" (como `PilaDeAcciones`), donde sí se quiere atender primero lo último ocurrido.

---

## 4. Lista de adyacencia (`Map<String, List<String>>`) vs. matriz de adyacencia

| Aspecto | Lista de adyacencia (Map) | Matriz de adyacencia |
|---|---|---|
| Memoria | O(V + E): solo guarda las conexiones que existen | O(V²): guarda todas las parejas posibles |
| Listar vecinos de un sensor | O(grado): recorre solo sus vecinos | O(V): recorre toda una fila |
| ¿Existe la conexión A–B? | O(grado) con `contains` sobre la lista | O(1) |
| Agregar un sensor nuevo | `computeIfAbsent`, inmediato | Hay que redimensionar la matriz |
| Identificadores | Usa directamente `String` como clave | Requiere traducir cada `String` a un índice |

**Ventajas del `Map`.** En una red de sensores cada estación se conecta apenas con 2 o 3 vecinas, así que el grafo es **disperso**. Una matriz de N × N tendría casi todas las celdas vacías. El `Map` ahorra memoria, permite agregar sensores sin reorganizar nada y trabaja con los ID como `"EST-001"` sin transformarlos.

**Desventajas.** Comprobar si dos sensores están conectados cuesta O(grado), no O(1). Si el grafo fuera denso, o esa consulta muy frecuente, la matriz sería mejor.

**Relación con el concepto.** Cada clave del mapa es un nodo del grafo, y su valor es su **lista de adyacencia**: la lista de nodos a los que se conecta. Es la idea de un `NodoGrafo` con una lista de vecinos, solo que representada con una tabla hash en lugar de objetos enlazados. En el proyecto, `registrarConexion` agrega la arista en ambas listas (grafo no dirigido), y `detectarEstacionesAisladas` recorre el mapa buscando listas vacías, en O(V).

---

## 5. ArrayDeque vs. ConcurrentLinkedQueue en un escenario multihilo

**Seguridad para concurrencia (*thread-safety*).** Una estructura es segura para hilos cuando se comporta correctamente aunque varios hilos la usen al mismo tiempo, sin que el programador agregue sincronización externa.

**`ArrayDeque` no es segura para hilos.** Sus operaciones modifican varios campos internos (índices de cabeza y cola, el arreglo). Si dos hilos hacen `add` o `poll` simultáneamente, pueden ocurrir condiciones de carrera: lecturas perdidas, el mismo elemento entregado a dos consumidores, tamaño inconsistente o excepciones. Dentro de un solo hilo es muy rápida, y por eso es la elección correcta en el `GestorDeRed` actual.

**`ConcurrentLinkedQueue` sí lo es.** Es una cola FIFO basada en nodos que usa operaciones atómicas (*compare-and-swap*) en lugar de bloqueos. Varios productores pueden encolar y varios consumidores desencolar a la vez, y cada elemento lo recibe un solo consumidor. `poll()` devuelve `null` si está vacía, igual que en el reto 8.

**Impacto en la elección.** Si la ingesta pasa a procesarse con varios hilos, el buffer debe cambiar a `ConcurrentLinkedQueue`. Como el campo está declarado como `Queue<LecturaSensor>`, el cambio se limita a una línea del constructor, sin tocar el resto del código. Esa es la ventaja de programar contra la interfaz.

**Matices que conviene conocer.**

- Cada operación individual es segura, pero las **compuestas** no son atómicas (por ejemplo, `contains` seguido de `add` en `programarConsulta`).
- `size()` recorre la cola (O(n)) y su valor es aproximado bajo concurrencia.
- Si se necesita que los consumidores esperen cuando no hay datos, o limitar el tamaño del buffer, existen colas bloqueantes como `LinkedBlockingQueue`.
- Con un solo hilo, `ArrayDeque` sigue siendo más eficiente, porque no paga el costo de la sincronización.