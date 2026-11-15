# Guion de la demo (10 minutos)

Demo de la Tarea 05 · Broker. Una sola laptop proyectada, con **4 terminales** abiertas en la raíz del repositorio
(donde están `pom.xml` y `data/tienda.db`) y el documento técnico abierto en otra ventana.

## Antes de presentar (5 minutos antes)

1. `git clone https://github.com/Luis-villanueva-valdivia/BrokerAbarrotes.git && cd BrokerAbarrotes && git switch main`
2. `mvn clean compile` (la primera vez Maven descarga Gson y sqlite-jdbc; que no pase en vivo).
3. Abrir las 4 terminales en la raíz y dejar escritos, **sin ejecutar**, los comandos de cada una (ver tabla).
4. Si existe, borrar `logs/broker.log` para que la bitácora empiece limpia.
5. Abrir `docs/diagramas/componentes-nivel1.svg`, `componentes-nivel2.svg` y `secuencia-timeout.svg`.

| Terminal | Comando |
|---|---|
| 1 | `mvn exec:java "-Dexec.mainClass=broker.red.ServidorBroker"` |
| 2 | `mvn exec:java "-Dexec.mainClass=broker.red.NodoProveedores"` |
| 3 | `mvn exec:java "-Dexec.mainClass=broker.red.ClienteRemoto"` (antes, el nivel 1: `mvn exec:java "-Dexec.mainClass=broker.cliente.DemoNivel1"`) |
| 4 | `mvn exec:java "-Dexec.mainClass=broker.red.ClienteRemoto"` |

## Guion

| Tiempo | Quién | Qué se muestra | Qué se dice |
|---|---|---|---|
| 0:00 – 1:00 | **Luis** | Diagrama `componentes-nivel1.svg` | Sistema elegido (abarrotes), qué es el patrón Broker, y que hay dos niveles: misma JVM y tres procesos por sockets. |
| 1:00 – 2:00 | **Diego** | `Solicitud`, `Respuesta`, `EstadoRespuesta`, `ProveedorServicio` | El contrato común: qué viaja en cada solicitud y respuesta, y los 5 estados. |
| 2:00 – 4:00 | **Paola** | Terminal 3: `DemoNivel1` | Las 8 pruebas del nivel 1: registro, localización, una operación por servicio, éxito, servicio desconocido, parámetro inválido, excepción del proveedor y sustitución (`ServicioProductosMemoria`). Señalar la línea `[BROKER] … → ESTADO (ms)` de cada caso. |
| 4:00 – 6:00 | **Adriel** | Diagrama `componentes-nivel2.svg`; terminal 1, luego 2, luego 3 | Levantar el broker (puerto 5000) y el nodo (6001–6005); mostrar en la terminal 1 los `Servicio '…' registrado` (registro dinámico). Correr el cliente: mismos casos, ahora por TCP y JSON. |
| 6:00 – 8:00 | **Oswaldo** | `secuencia-timeout.svg`; terminales 1, 2 y 3 | El caso `lento.ejecutar`: 3 intentos de 2 s y `TIMEOUT` (~6 s), con los `Reintento 1/2` y `2/2` en la terminal 1. Luego **Ctrl+C en la terminal 2** y correr el cliente: `ERROR_PROVEEDOR`, el broker sigue vivo. Volver a levantar la terminal 2: `actualizado por nuevo registro`, todo responde otra vez. |
| 8:00 – 9:00 | **Adriel** | Terminales 3 y 4 al mismo tiempo | Dos clientes a la vez: los dos terminan en ~6 s y no en 12; el broker atiende cada conexión en su propio hilo. |
| 9:00 – 10:00 | **Luis** | `logs/broker.log` y documento técnico | Monitoreo con la bitácora, protocolo JSON de una línea, y límites encontrados: el broker es punto único de falla (si cae, el cliente no llega a nadie y el registro se pierde) y los reintentos pueden duplicar solicitudes (hoy sin daño porque todo es de lectura). Cierre y preguntas. |

## Ensayo cronometrado

**Ensayo técnico** (14 nov, código de `develop` después de ISS-13, solo los comandos, sin hablar):

| Paso | Tiempo medido |
|---|---|
| `DemoNivel1` completo (8 casos) | 0.4 s |
| Broker + nodo listos (`Proveedores registrados en el broker.`) | ~3 s |
| `ClienteRemoto` completo | 6.3 s (5–6 s son el caso `lento`) |
| Dos clientes al mismo tiempo | 6.5 s cada uno |
| Nodo apagado → cliente completo | < 1 s (`Connection refused` es inmediato) |

Los comandos ocupan menos de 30 s en total, así que casi todo el tiempo es explicación. Sin contar la descarga de
Maven (que se hace antes), `mvn exec:java` agrega 2–4 s por arranque.

**Ensayo con el equipo** (llenar el día de la demo):

| Bloque | Responsable | Tiempo objetivo | Tiempo real |
|---|---|---|---|
| Introducción | Luis | 1:00 | |
| Contrato común | Diego | 1:00 | |
| Nivel 1 | Paola | 2:00 | |
| Nivel 2 y registro | Adriel | 2:00 | |
| Timeout y fallas | Oswaldo | 2:00 | |
| Dos clientes | Adriel | 1:00 | |
| Monitoreo y cierre | Luis | 1:00 | |
| **Total** | | **10:00** | |

## Si algo falla en vivo

- **`Address already in use`:** quedó un broker o nodo de una corrida anterior; cerrar esas terminales (o
  `Ctrl+C`) y volver a correr.
- **El cliente muestra `SERVICIO_DESCONOCIDO` en todo:** se reinició el broker pero no el nodo; volver a correr la
  terminal 2.
- **El cliente termina con `ConnectException`:** el broker no está corriendo; levantar la terminal 1 primero.
- **Plan B:** mostrar las capturas y la salida de `docs/evidencias/nivel2/`.
