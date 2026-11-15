# Evidencias del nivel 2 (ISS-13)

Corrida del 14 de noviembre de 2026 con el código de `develop` (después de ISS-11), con los tres procesos del nivel 2
en la misma máquina: `ServidorBroker`, `NodoProveedores` y `ClienteRemoto`. Se ejecutaron las mismas clases `main`
que en el README (`mvn exec:java -Dexec.mainClass=…`), lanzadas con `java -cp` desde la raíz del repositorio.

| Archivo | Escenario |
|---|---|
| `01-timeout.svg` | `lento.ejecutar` → `TIMEOUT` a los 6017 ms (3 intentos de 2000 ms). |
| `02-reintentos.svg` | Líneas `[PROVEEDOR] Reintento i/2` con proveedor lento (`Read timed out`) y apagado (`Connection refused`). |
| `03-proveedor-apagado.svg` | `NodoProveedores` detenido → `ERROR_PROVEEDOR`; se levanta otra vez → `actualizado por nuevo registro` y `EXITO`, sin reiniciar el broker. |
| `04-dos-clientes.svg` | Dos `ClienteRemoto` al mismo tiempo: ambos terminan en 6.47 s; las solicitudes lentas se atienden en paralelo. |
| `broker.log` | `logs/broker.log` completo de la corrida. |
| `consola.md` | Salida completa de cada terminal. Las dos últimas secciones muestran qué pasa si el broker cae (pregunta 3 del documento). |

Orden de la corrida: broker → nodo → demo completa del cliente → dos clientes a la vez → se detiene el nodo → cliente
→ se levanta el nodo otra vez → cliente.
