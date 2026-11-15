# BrokerAbarrotes

Prototipo del patrón arquitectónico **Broker** (Tarea 05 · Arquitectura de Software) construido sobre el **Sistema de Gestión de Abarrotes**.

El cliente pide servicios del sistema de abarrotes (productos, ventas y clientes) sin conocer las clases que los atienden: el Broker recibe la solicitud, busca en el registro qué proveedor la resuelve, se la reenvía y devuelve la respuesta.

- **Nivel 1:** cliente, Broker y proveedores en la misma aplicación Java.
- **Nivel 2:** cliente, Broker y proveedores en procesos distintos, comunicándose con sockets TCP y JSON.

---

## Equipo y plan de trabajo

Entrega: **sábado 14 de noviembre** · objetivo: todo listo el **viernes 13**.

| Persona | Issues | Qué hace |
|---|---|---|
| **Diego** | [#1](../../issues/1), [#2](../../issues/2), [#12](../../issues/12) | Estructura del proyecto, `Solicitud`/`Respuesta`/`ProveedorServicio`, diagramas |
| **Luis** | [#3](../../issues/3), [#6](../../issues/6), [#8](../../issues/8) | `Broker` y `RegistroServicios`, proveedor de clientes, JSON |
| **Oswaldo** | [#4](../../issues/4), [#5](../../issues/5), [#11](../../issues/11) | Proveedores de productos y ventas, timeouts y reintentos |
| **Adriel** | [#9](../../issues/9), [#10](../../issues/10) | Servidor TCP con hilos, proveedores y cliente en procesos separados |
| **Paola** | [#7](../../issues/7), [#13](../../issues/13), [#14](../../issues/14) | Cliente y 8 pruebas, documento técnico, integración y demo |

### Calendario

| Día | Meta |
|---|---|
| Lun 9 | Estructura (#1) y clases comunes (#2) listas; arranca el Broker (#3) |
| Mar 10 | Broker y registro (#3); proveedores de productos (#4) y clientes (#6); servidor TCP (#9) |
| Mié 11 | Proveedor de ventas (#5), JSON (#8) y **las 8 pruebas del nivel 1 (#7)** |
| Jue 12 | Nivel 2: procesos separados (#10), timeouts (#11); diagramas (#12) y documento (#13) |
| Vie 13 | Pruebas del nivel 2, documento final, integración y ensayo de la demo (#14) |
| Sáb 14 | **Entrega** |

> El nivel 1 vale el 50 %: debe estar completo y probado antes de depender de los sockets.

Las 10 preguntas de análisis se reparten 2 por persona; la tabla está en [#13](../../issues/13).

---

## Cómo trabajamos

Antes de tocar código, lee **[docs/convenciones.md](docs/convenciones.md)**. En corto:

1. Toma tu issue y muévelo a *En progreso* en el [tablero](../../projects).
2. Crea tu rama desde `develop`: `tipo/iss-XX-descripcion`.
3. Commits con el formato `tipo: descripción (iss-XX)`.
4. PR hacia `develop` con `Closes #XX`; lo revisa quien te toca en la revisión cruzada.
5. Con la aprobación, *Squash and merge* y borras la rama.

---

## Estructura

```
src/main/java/broker/
├── comun/         Solicitud, Respuesta, EstadoRespuesta, ProveedorServicio
├── broker/        Broker, RegistroServicios
├── proveedores/   ServicioProductos, ServicioProductosMemoria, ServicioVentas, ServicioClientes
├── cliente/       ClienteApp, DemoNivel1
├── red/           MarshallerJson, ServidorBroker, NodoProveedores, ProveedorRemoto, ClienteRemoto
└── abarrotes/     modelos y DAO copiados del sistema original
docs/
├── documento-tecnico.md   documento técnico (arquitectura, protocolo, evidencias, 10 preguntas)
├── guion-demo.md          guion de la demo de 10 minutos
├── protocolo.md           mensajes JSON del nivel 2
├── convenciones.md
├── diagramas/             componentes (nivel 1 y 2) y secuencia (éxito y timeout), .svg + .drawio
└── evidencias/nivel2/     capturas, broker.log y salida de consolas del nivel 2
```

---

## Compilar y ejecutar

El proyecto se ejecuta desde la raíz (donde está `pom.xml`), ya que el archivo `ConexionDB` usa la ruta relativa `data/tienda.db`

Requisitos: JDK 17 y Maven 3.9 o superior.

```bash
git clone https://github.com/Luis-villanueva-valdivia/BrokerAbarrotes.git
cd BrokerAbarrotes
mvn clean compile
mvn test
```

### Demo del nivel 1

Cliente, broker y proveedores en la misma aplicación. Desde la raíz del repositorio:

```bash
mvn exec:java "-Dexec.mainClass=broker.cliente.DemoNivel1"
```

Ejecuta los 8 casos: registro, localización, una operación por servicio, respuesta exitosa, servicio desconocido,
parámetro inválido, excepción del proveedor y sustitución del proveedor. Cada solicitud deja una línea
`[BROKER] hh:mm:ss id=… servicio.operacion → ESTADO (ms)` en la consola.

### Demo del nivel 2

Ejecuta cada comando desde la raíz del repositorio, donde están `pom.xml` y
`data/tienda.db`. Abre tres terminales y mantenlas en ese directorio:

**Terminal 1 — broker**

```bash
mvn exec:java "-Dexec.mainClass=broker.red.ServidorBroker"
```

**Terminal 2 — proveedores**

```bash
mvn exec:java "-Dexec.mainClass=broker.red.NodoProveedores"
```

El nodo anuncia productos en el puerto `6001`, ventas en `6002`, clientes en
`6003`, el servicio `prueba` en `6004` (siempre falla; se usa en el caso 7) y el
servicio `lento` en `6005` (tarda 3 s; se usa para demostrar el `TIMEOUT`).
Debe mostrar `Proveedores registrados en el broker.` antes de continuar.

> Si reinicias `NodoProveedores`, no hace falta reiniciar el broker: al volver a
> registrarse, el broker sustituye a los proveedores anteriores.

**Terminal 3 — cliente**

```bash
mvn exec:java "-Dexec.mainClass=broker.red.ClienteRemoto"
```

El cliente ejecuta los 8 casos de `DemoNivel1` por socket: registro,
localización, una operación por servicio, respuesta exitosa, servicio
desconocido, parámetro inválido y excepción del proveedor. El caso 8
(sustitución del proveedor) se demuestra en el nivel 1; en el nivel 2 el broker
sustituye al proveedor cuando `NodoProveedores` se vuelve a registrar, y el
cliente solo lo explica. Al final agrega dos casos extra: producto no encontrado
y el proveedor lento, que responde `TIMEOUT` después de 2 reintentos (unos 6 s).

Para probar dos clientes a la vez, abre una cuarta terminal y ejecuta el mismo
comando de la terminal 3 al mismo tiempo.

### Timeouts, reintentos y desconexiones

El broker espera hasta 2 s la respuesta de cada proveedor y reintenta hasta 2
veces. Cada reintento queda en la consola del broker y en `logs/broker.log`:

```
[PROVEEDOR] Reintento 1/2 lento.ejecutar en localhost:6005 estado=TIMEOUT (Read timed out)
[PROVEEDOR] Reintento 2/2 lento.ejecutar en localhost:6005 estado=TIMEOUT (Read timed out)
```

- **Proveedor lento:** lo demuestra el último caso del cliente (`TIMEOUT`).
- **Proveedor apagado:** cierra la terminal 2 y vuelve a correr el cliente. El
  broker responde `ERROR_PROVEEDOR` después de 2 reintentos y sigue atendiendo;
  al levantar otra vez `NodoProveedores`, todo vuelve a funcionar sin reiniciar
  el broker.

---

## Origen del código

Los modelos y DAO de `broker/abarrotes` vienen del [Sistema de Gestión de Abarrotes](https://github.com/Diego-Ceron/SistemaGestionAbarrotes) de Diego Cerón y su equipo, en la versión [`v1.0-original`](https://github.com/Luis-villanueva-valdivia/SistemaGestionAbarrotes/releases/tag/v1.0-original), antes de la refactorización que se hace en Construcción de Software.

---

## Documentación

- [Documento técnico](docs/documento-tecnico.md)
- [Diagramas](docs/diagramas/README.md)
- [Evidencias del nivel 2](docs/evidencias/nivel2/README.md)
- [Protocolo JSON](docs/protocolo.md)
- [Guion de la demo](docs/guion-demo.md)
