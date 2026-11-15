# Diagramas (ISS-12)

| Diagrama | Imagen | Fuente (draw.io) |
|---|---|---|
| Componentes · nivel 1 (con puertos) | [componentes-nivel1.svg](componentes-nivel1.svg) | [componentes-nivel1.drawio](componentes-nivel1.drawio) |
| Componentes · nivel 2 (con puertos TCP) | [componentes-nivel2.svg](componentes-nivel2.svg) | [componentes-nivel2.drawio](componentes-nivel2.drawio) |
| Secuencia · solicitud exitosa | [secuencia-exitosa.svg](secuencia-exitosa.svg) | [secuencia-exitosa.drawio](secuencia-exitosa.drawio) |
| Secuencia · solicitud con timeout | [secuencia-timeout.svg](secuencia-timeout.svg) | [secuencia-timeout.drawio](secuencia-timeout.drawio) |

Los `.drawio` se abren y editan en [app.diagrams.net](https://app.diagrams.net) (*Archivo → Abrir desde → Dispositivo*).
Los nombres de clases, operaciones, constantes y puertos coinciden con el código de `src/main/java/broker`.

- **Nivel 1:** `ClienteApp` → `Broker.enviar(Solicitud)` → `RegistroServicios.buscar(servicio)` → `ProveedorServicio.ejecutar(Solicitud)`, todo en la misma JVM.
- **Nivel 2:** `ClienteRemoto` → `ServidorBroker` (:5000) → `Broker` → `ProveedorRemoto` → `NodoProveedores` (:6001–:6005), con una línea JSON por mensaje (`MarshallerJson`).
- **Timeout:** `ProveedorRemoto` espera 2000 ms por intento y reintenta hasta `MAX_REINTENTOS = 2`; con `SocketTimeoutException` responde `TIMEOUT`.
