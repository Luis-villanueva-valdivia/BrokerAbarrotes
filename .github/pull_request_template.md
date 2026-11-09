<!--
Título del PR (se convierte en el commit final del squash merge):
  tipo: descripción (iss-XX)
  Ej.: feat: agrega RegistroServicios con registro y sustitucion (iss-03)

El PR va hacia develop.
-->

Closes #N

## Descripción

<!-- Qué resuelve este PR y por qué. Una o dos frases. -->

## Cambios

<!-- Qué se agregó, modificó o eliminó. -->
-
-

## Cómo probarlo

1. `git fetch && git switch tipo/iss-XX-descripcion`
2. `mvn clean compile && mvn test`
3. <!-- Clase main a correr (DemoNivel1, ServidorBroker…) y qué hacer -->
4. Resultado esperado:

## Criterios de aceptación

<!-- Copiados del issue. Marca los que se cumplen. -->
- [ ]
- [ ]

## Evidencia

<!-- Capturas de consola. Si es una de las 8 pruebas, guárdala también en docs/evidencias/. Si no aplica, escribe "No aplica". -->

## Checklist

- [ ] La rama sale de `develop` y sigue el formato `tipo/iss-XX-descripcion`.
- [ ] El título y los commits siguen el formato `tipo: descripción (iss-XX)`.
- [ ] El PR va a `develop`.
- [ ] `mvn clean compile` termina sin errores.
- [ ] `mvn test` pasa en verde.
- [ ] El cliente no importa clases de `broker.proveedores`.
- [ ] Clases y métodos públicos con Javadoc.
- [ ] Sin números mágicos, sin código comentado ni `System.out` de depuración.
- [ ] Solo se agregó al final de archivos ajenos, con comentario de sección.
- [ ] Revisor asignado según la revisión cruzada de `docs/convenciones.md`.

## Notas para el revisor

<!-- Dudas, decisiones tomadas o pendientes. Borrar si no hay. -->
