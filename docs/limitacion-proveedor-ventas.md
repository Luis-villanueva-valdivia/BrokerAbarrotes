# Limitación del proveedor de ventas (ISS-05)

El proveedor `ServicioVentas` utiliza `VentaDAO.listarTodos()` para consultar las ventas disponibles.

Actualmente, ese método recupera únicamente el identificador de la venta, su fecha, el total y el nombre del cliente. No carga el detalle de los productos vendidos ni el método de pago.

Por esta razón, las operaciones de ISS-05 se limitan a:

- Consultar una venta por su identificador, devolviendo los datos disponibles en `VentaModel`.
- Calcular la suma de los totales de las ventas de una fecha determinada.

## Tratamiento de fechas almacenadas

`VentaDAO.listarTodos()` intenta convertir la fecha almacenada al formato `yyyy-MM-dd HH:mm:ss`. Si la fecha es nula o no puede convertirse, el DAO asigna la fecha y hora actuales (`new Date()`) a esa venta.

Este comportamiento puede afectar `totalDelDia`: una venta con una fecha inválida en la base de datos puede contarse como si perteneciera al día actual, aunque originalmente correspondiera a otra fecha. El proveedor no puede detectar esa sustitución porque `listarTodos()` entrega la fecha actual en lugar del valor almacenado inválido. Corregir ese comportamiento requeriría un cambio independiente en `VentaDAO`.

## Dependencia de la base de datos en las pruebas

Las pruebas de `ServicioVentasTest` utilizan una instancia real de `VentaDAO` y consultan la base SQLite del proyecto. Para ejecutarlas se necesita que exista `data/tienda.db` con los datos de prueba, incluidas las 19 ventas esperadas.

Las pruebas deben ejecutarse desde la raíz del repositorio, donde está `pom.xml`, porque `ConexionDB` utiliza la ruta relativa `jdbc:sqlite:data/tienda.db`. Si el archivo no existe, no contiene los datos esperados o la conexión no está disponible, las pruebas dependientes de la base de datos pueden fallar.

No se modifica `VentaDAO` como parte de esta issue. La consulta de productos vendidos o del método de pago requeriría ampliar el DAO en una issue independiente.
