# Protocolo de mensajes JSON

Este documento describe los mensajes que viajan por la red entre cliente, broker y proveedores.
Todos los mensajes se codifican con `MarshallerJson` (paquete `broker.red`) usando Gson, y cada
mensaje ocupa una sola línea, para leerlo con `readLine()` desde un socket.

## Reglas generales

- Un mensaje por línea, terminado por salto de línea.
- Las fechas van en formato ISO local, por ejemplo `2026-05-12T10:30:00`.
- Los identificadores (`id`, `idSolicitud`) van como UUID en texto.
- Un JSON vacío, mal formado, con campos faltantes o con valores fuera de rango no produce una excepción
  sin control: `MarshallerJson` lanza `IllegalArgumentException`, y el broker lo responde con
  `PARAMETRO_INVALIDO`.

## Solicitud

Lo envía el cliente al broker para pedir una operación a un servicio.

```json
{"id":"e0599048-e7f7-4951-a5b3-d107969a1744","servicio":"productos","operacion":"consultar","parametros":{"codigo":"CC001"},"fechaHora":"2026-11-14T01:12:42.354478497"}
```

| Campo        | Tipo                 | Obligatorio | Descripción                          |
|--------------|----------------------|-------------|--------------------------------------|
| `id`         | UUID                 | Sí          | Identificador de la solicitud        |
| `servicio`   | texto                | Sí          | Nombre del servicio, por ejemplo `productos` |
| `operacion`  | texto                | Sí          | Operación solicitada                 |
| `parametros` | objeto (texto a texto) | Sí        | Parámetros de la operación           |
| `fechaHora`  | fecha ISO            | Sí          | Momento en que se creó la solicitud  |

## Respuesta

Lo envía el broker o el proveedor como resultado de una solicitud.

Respuesta exitosa:

```json
{"idSolicitud":"e0599048-e7f7-4951-a5b3-d107969a1744","resultado":"Refresco cola","estado":"EXITO","mensajeError":null,"fechaHora":"2026-11-14T01:12:42.400000"}
```

Respuesta de error:

```json
{"idSolicitud":"e0599048-e7f7-4951-a5b3-d107969a1744","resultado":null,"estado":"PARAMETRO_INVALIDO","mensajeError":"umbral inválido","fechaHora":"2026-11-14T01:12:42.400000"}
```

| Campo          | Tipo         | Descripción                                                              |
|----------------|--------------|--------------------------------------------------------------------------|
| `idSolicitud`  | UUID         | Id de la solicitud que se atiende                                        |
| `resultado`    | cualquiera   | Resultado de la operación; `null` si hubo error                          |
| `estado`       | texto        | `EXITO`, `SERVICIO_DESCONOCIDO`, `PARAMETRO_INVALIDO`, `ERROR_PROVEEDOR` o `TIMEOUT` |
| `mensajeError` | texto o null | Descripción del error; `null` si fue exitosa                             |
| `fechaHora`    | fecha ISO    | Momento en que se creó la respuesta                                      |

Nota: al reconstruir una respuesta, `resultado` llega como texto, número, lista o mapa genéricos,
porque el JSON no conserva la clase original. La fecha de la respuesta reconstruida es el momento
de la reconstrucción.

## Registro dinámico

Lo envía un proveedor al broker para registrar un servicio mientras el broker está en ejecución.

```json
{"tipo":"REGISTRAR","servicio":"productos","host":"localhost","puerto":6001}
```

| Campo      | Tipo  | Obligatorio | Descripción                                   |
|------------|-------|-------------|-----------------------------------------------|
| `tipo`     | texto | Sí          | Debe ser `REGISTRAR`                          |
| `servicio` | texto | Sí          | Nombre del servicio que se registra           |
| `host`     | texto | Sí          | Dirección donde escucha el proveedor          |
| `puerto`   | entero| Sí          | Puerto entre 1 y 65535                        |

Un mensaje con otro `tipo`, con `servicio` o `host` vacíos, o con un puerto fuera de rango se rechaza
con `IllegalArgumentException`.

## Errores de formato

| Entrada                              | Resultado                                  |
|--------------------------------------|--------------------------------------------|
| Línea vacía o `null`                 | `IllegalArgumentException`                 |
| JSON mal formado, por ejemplo `{servicio:` | `IllegalArgumentException`           |
| Solicitud sin `servicio` u `operacion` | `IllegalArgumentException`               |
| Fecha con formato inválido, por ejemplo `ayer` | `IllegalArgumentException`      |
| Estado de respuesta desconocido      | `IllegalArgumentException`                 |
| Registro con tipo distinto de `REGISTRAR` | `IllegalArgumentException`            |
| Registro con puerto fuera de rango   | `IllegalArgumentException`                 |
