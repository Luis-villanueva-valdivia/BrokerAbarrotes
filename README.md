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
├── convenciones.md
├── diagramas/
└── evidencias/
```

---

## Compilar y ejecutar

> Se completa en [#10](../../issues/10) (nivel 2).

El proyecto se ejecuta desde la raíz(donde está `pom.xml`) ya que el archivo `ConexionDB` usa la ruta relativa `data/tienda.db`

```bash
mvn clean compile
mvn test
```

---

## Origen del código

Los modelos y DAO de `broker/abarrotes` vienen del [Sistema de Gestión de Abarrotes](https://github.com/Diego-Ceron/SistemaGestionAbarrotes) de Diego Cerón y su equipo, en la versión [`v1.0-original`](https://github.com/Luis-villanueva-valdivia/SistemaGestionAbarrotes/releases/tag/v1.0-original), antes de la refactorización que se hace en Construcción de Software.
