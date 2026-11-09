# Convenciones del equipo

Reglas con las que trabajamos en este repositorio. Si algo no está aquí, se decide en el issue correspondiente y se agrega a este archivo por PR.

**Resumen rápido:**

1. Toma un issue y muévelo a *En progreso*.
2. Crea tu rama desde `develop`: `tipo/iss-XX-descripcion`.
3. Haz commits con el formato `tipo: descripción (iss-XX)`.
4. Abre un PR **hacia `develop`** con `Closes #N`.
5. Tu revisor lo prueba y aprueba. Tú haces *Squash and merge* y borras la rama.

---

## 1. Entorno

| Herramienta | Versión |
|---|---|
| Java (JDK) | 17 |
| Maven | 3.9 o superior |
| Gson | lo baja Maven |
| Base de datos | SQLite (`data/tienda.db`, copia de `v1.0-original`) |

Comandos básicos, desde la raíz del repo:

```bash
mvn clean compile   # compilar
mvn test            # correr las pruebas
```

---

## 2. Issues

### 2.1 Título

Formato: **`ISS-XX · verbo en infinitivo + objeto`**

- `XX` a dos dígitos.
- Se usa el punto medio `·` entre el número y el texto.
- El texto dice **qué se va a lograr**, no cómo.

Ejemplos:

```
ISS-03 · Crear el broker y el registro de servicios
ISS-08 · Convertir solicitudes y respuestas a JSON
ISS-09 · Levantar el broker como servidor TCP con hilos
```

Los errores que aparezcan durante el trabajo se registran como **`B-XX · descripción del problema`**, con la etiqueta `tipo:bug`.

### 2.2 Cuerpo

Todo issue tiene estas secciones, en este orden:

```markdown
**Dependencias:** ISS-03, ISS-08   (o "ninguna")

**Descripción.** Por qué existe este issue y qué problema resuelve. Una o dos frases.

**Entrega esperada.**
- Qué clases, archivos o cambios deben existir al terminar.

**Limitaciones.** (opcional) Lo que queda fuera de alcance.

**Criterios de aceptación.**
- Condiciones comprobables. El revisor las usa para aprobar el PR.
```

Un issue no se empieza si alguna de sus **dependencias** no está cerrada, salvo que se acuerde en el issue.

### 2.3 Número ISS y número de GitHub

En este repo coinciden: ISS-01 es el #1, ISS-14 es el #14. En ramas y commits se usa `iss-XX`; en el PR, `Closes #XX`.

### 2.4 Etiquetas

Cada issue lleva **un tipo, una o más áreas, su nivel (si aplica) y una prioridad**.

| Grupo | Etiquetas |
|---|---|
| Tipo | `tipo:config`, `tipo:feature`, `tipo:test`, `tipo:docs`, `tipo:bug` |
| Área | `area:repo`, `area:comun`, `area:broker`, `area:proveedores`, `area:cliente`, `area:red` |
| Nivel | `nivel:1` (local), `nivel:2` (distribuido) |
| Prioridad | `prioridad:bloqueante` (frena a otros, va primero), `prioridad:alta`, `prioridad:media` |

### 2.5 Milestone

| Milestone | Fecha | Contenido |
|---|---|---|
| Tarea 05 — Broker | sábado 14 de noviembre (todo listo el viernes 13) | ISS-01 a ISS-14 |

**El nivel 1 vale el 50 %: debe estar completo y probado antes de empezar sockets.**

### 2.6 Tablero

GitHub Projects con cuatro columnas: **Backlog → En progreso → En revisión → Hecho**.
Cada quien mueve sus propios issues. Si algo te bloquea más de unas horas, coméntalo en el issue y avisa en el grupo.

---

## 3. Ramas

```
main      ← solo recibe develop en la entrega (etiqueta v1.0.0)
develop   ← rama de integración; aquí llegan todos los PR
  └── tipo/iss-XX-descripcion   ← una rama por issue
```

- **Nunca** se trabaja directo en `main` ni en `develop`. Ambas están protegidas: no aceptan push directo y piden 1 aprobación.
- Una rama por issue, siempre desde `develop` actualizado.
- Formato: `tipo/iss-XX-descripcion`, en minúsculas, sin acentos, con guiones.

| Tipo de rama | Uso | Ejemplo |
|---|---|---|
| `feature` | Código nuevo | `feature/iss-03-broker-registro` |
| `fix` | Corrección de un error | `fix/b-01-timeout-infinito` |
| `test` | Pruebas y evidencias | `test/iss-07-pruebas-nivel1` |
| `docs` | Documentación y diagramas | `docs/iss-12-diagramas` |
| `chore` | Configuración, Maven, estructura | `chore/iss-01-estructura` |

```bash
git switch develop
git pull
git switch -c feature/iss-03-broker-registro
```

---

## 4. Commits

Formato: **`tipo: descripción (iss-XX)`**

- `tipo` en minúsculas, de la tabla de abajo.
- Descripción en español, en imperativo, sin punto final, de 72 caracteres o menos.
- Siempre termina con el issue entre paréntesis.

| Tipo | Uso |
|---|---|
| `feat` | Funcionalidad nueva |
| `fix` | Corrección de un error |
| `test` | Pruebas |
| `docs` | Solo documentación |
| `chore` | Configuración, `pom.xml`, `.gitignore`, estructura |

Ejemplos:

```
chore: crea la estructura de paquetes y el pom (iss-01)
feat: agrega Solicitud y Respuesta con sus fabricas (iss-02)
feat: agrega RegistroServicios con registro y sustitucion (iss-03)
test: agrega prueba de servicio desconocido (iss-03)
feat: agrega MarshallerJson con Gson (iss-08)
```

---

## 5. Pull requests

### 5.1 Antes de abrirlo

```bash
git fetch origin
git merge origin/develop     # traer lo último y resolver conflictos en tu rama
mvn clean compile
mvn test
```

### 5.2 Al abrirlo

- **Título** con el mismo formato que los commits: `tipo: descripción (iss-XX)`. Se convierte en el mensaje del commit final.
- Llena la **plantilla** que aparece sola (`.github/pull_request_template.md`), incluido `Closes #N`.
- Asigna a tu **revisor** (§6) y mueve el issue a *En revisión*.

### 5.3 Revisión y fusión

1. El revisor baja la rama, la compila, corre las pruebas y revisa los criterios de aceptación del issue.
2. **Aprueba** o **pide cambios**. Si pide cambios, el autor corrige en la misma rama, contesta los comentarios y pide revisión otra vez (*Re-request review*).
3. Con la aprobación, **el autor fusiona su propio PR** con **Squash and merge**. Así cada issue queda como un solo commit en `develop` y el trabajo de cada quien queda a su nombre.
4. Se **borra la rama** (botón *Delete branch* y `git branch -d` en local).
5. El issue se cierra solo por el `Closes #N`.

---

## 6. Revisión cruzada

Nadie aprueba su propio PR. Cada integrante revisa al siguiente:

| PR de | Lo revisa |
|---|---|
| Diego | Luis |
| Luis | Oswaldo |
| Oswaldo | Adriel |
| Adriel | Paola |
| Paola | Diego |

- El dueño del repo (Luis) puede aprobar cualquier PR.
- Si el revisor asignado no contesta en **12 horas**, puede aprobar cualquier otro integrante (la entrega es en una semana).

---

## 7. Dueños de archivo

Quien crea un archivo es su dueño. Los demás **no reescriben** lo que ya existe sin avisar en el issue; si necesitan agregar algo, lo hacen **al final**, con un comentario de sección:

```java
// ===== ISS-11 · Timeouts y reintentos (Oswaldo) =====
```

Las clases copiadas de `v1.0-original` en `broker.abarrotes` son de quien hizo ISS-01; si un proveedor necesita cambiarlas, avisa en su issue.

---

## 8. Nombres en Java

| Elemento | Convención | Ejemplo |
|---|---|---|
| Paquetes | minúsculas, sin guiones | `broker.comun` |
| Clases e interfaces | `PascalCase`, sustantivo | `RegistroServicios`, `ProveedorServicio` |
| Métodos | `camelCase`, empiezan con verbo | `buscarPorId()`, `registrarVenta()` |
| Variables y atributos | `camelCase`, descriptivos | `idSolicitud`, `productosConStockBajo` |
| Constantes | `MAYUSCULAS_CON_GUION_BAJO` | `PUERTO_BROKER`, `TIMEOUT_MS` |
| Enums | `PascalCase`; valores en mayúsculas | `EstadoRespuesta.PARAMETRO_INVALIDO` |
| Pruebas | clase probada + `Test` | `RegistroServiciosTest` |

Los nombres van en español, sin acentos ni ñ (`anio`, `contrasenia`). Términos técnicos sin buena traducción se quedan en inglés (`socket`, `json`, `dao`).

### 8.1 Estructura de paquetes

```
src/main/java/broker/
├── comun/         Solicitud, Respuesta, EstadoRespuesta, ProveedorServicio
├── broker/        Broker, RegistroServicios, ServicioDesconocidoException
├── proveedores/   ServicioProductos, ServicioProductosMemoria, ServicioVentas, ServicioClientes
├── cliente/       ClienteApp, DemoNivel1
├── red/           MarshallerJson, ServidorBroker, NodoProveedores, ProveedorRemoto, ClienteRemoto
└── abarrotes/     modelos y DAO copiados de SistemaGestionAbarrotes v1.0-original
```

### 8.2 Evidencias y documentos

```
docs/
├── diagramas/           componentes y secuencia (imagen + fuente)
├── evidencias/nivel1/   prueba1_registro.png … prueba8_sustitucion.png
├── evidencias/nivel2/   capturas y extracto de logs/broker.log
└── protocolo.md         formato de los mensajes JSON
```

---

## 9. Estándares de código

- **Indentación de 4 espacios**, sin tabuladores. UTF-8 y salto de línea al final del archivo.
- **Sin números mágicos.** Puertos, tiempos, umbrales y textos repetidos van en constantes con nombre.
- **Métodos pequeños, con una sola responsabilidad.** Si pasa de ~30 líneas, probablemente hace demasiado.
- **Retorno temprano** en lugar de `if` anidados.
- **No repetir código:** si algo aparece en dos lugares, se extrae.
- **Los proveedores reciben sus DAO por constructor**, no hacen `new XxxDAO()` adentro.
- **El cliente nunca importa nada de `broker.proveedores`**: solo conoce `Broker`, `Solicitud` y `Respuesta`.
- **Excepciones:** nunca un `catch` vacío. El broker nunca deja escapar una excepción hacia el cliente: la convierte en una `Respuesta` de error.
- **Javadoc** en toda clase y método público: qué hace, parámetros, retorno y excepciones. Dentro del código se comenta el **porqué**, no el qué.
- **Nada de código comentado ni `System.out` de depuración** en lo que se fusiona (la bitácora del broker sí va).
- **Toda clase nueva lleva sus pruebas** en `src/test/java` con el mismo paquete.
