# Limitación del proveedor de ventas (ISS-05)

El proveedor `ServicioVentas` utiliza `VentaDAO.listarTodos()` para consultar las ventas disponibles.

Actualmente, ese método recupera únicamente el identificador de la venta, su fecha, el total y el nombre del cliente. No carga el detalle de los productos vendidos ni el método de pago.

Por esta razón, las operaciones de ISS-05 se limitan a:

- Consultar una venta por su identificador, devolviendo los datos disponibles en `VentaModel`.
- Calcular la suma de los totales de las ventas de una fecha determinada.

No se modifica `VentaDAO` como parte de esta issue. La consulta de productos vendidos o del método de pago requeriría ampliar el DAO en una issue independiente.
