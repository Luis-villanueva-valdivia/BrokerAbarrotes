# Salida completa de las consolas (ISS-13)

Salida sin editar de cada terminal durante la corrida del nivel 2. El orden está en `README.md`.

## Terminal 1 · ServidorBroker (toda la corrida)

```
ServidorBroker escuchando en el puerto 5000
[BROKER] Servicio 'productos' registrado
[BROKER] Servicio 'ventas' registrado
[BROKER] Servicio 'clientes' registrado
[BROKER] Servicio 'prueba' registrado
[BROKER] Servicio 'lento' registrado
[BROKER] 19:27:47 id=23c69469 productos.consultar → EXITO (39 ms)
[BROKER] 19:27:47 id=656bbe26 ventas.totalDelDia → EXITO (10 ms)
[BROKER] 19:27:47 id=6cf30a25 clientes.consultar → EXITO (3 ms)
[BROKER] 19:27:47 id=c21e0898 productos.buscar → EXITO (3 ms)
[BROKER] 19:27:47 id=01900c66 facturacion.consultar → SERVICIO_DESCONOCIDO (0 ms)
[BROKER] 19:27:47 id=59bc41f2 productos.stockBajo → PARAMETRO_INVALIDO (1 ms)
[BROKER] 19:27:47 id=29e7ee23 prueba.ejecutar → ERROR_PROVEEDOR (1 ms)
[BROKER] 19:27:47 id=ead7fdea productos.consultar → EXITO (2 ms)
[BROKER] 19:27:47 id=ad796e7c productos.consultar → ERROR_PROVEEDOR (2 ms)
[PROVEEDOR] Reintento 1/2 lento.ejecutar en localhost:6005 estado=TIMEOUT (Read timed out)
[PROVEEDOR] Reintento 2/2 lento.ejecutar en localhost:6005 estado=TIMEOUT (Read timed out)
[BROKER] 19:27:53 id=e2c93fc7 lento.ejecutar → TIMEOUT (6017 ms)
[BROKER] 19:27:53 id=a80bb16b productos.consultar → EXITO (15 ms)
[BROKER] 19:27:53 id=7d144c5d productos.consultar → EXITO (7 ms)
[BROKER] 19:27:53 id=c2d3a733 ventas.totalDelDia → EXITO (10 ms)
[BROKER] 19:27:53 id=65798216 ventas.totalDelDia → EXITO (11 ms)
[BROKER] 19:27:53 id=52bf0c46 clientes.consultar → EXITO (3 ms)
[BROKER] 19:27:53 id=59cd9246 productos.buscar → EXITO (9 ms)
[BROKER] 19:27:53 id=3143ce4a clientes.consultar → EXITO (18 ms)
[BROKER] 19:27:53 id=d673f9db facturacion.consultar → SERVICIO_DESCONOCIDO (0 ms)
[BROKER] 19:27:53 id=7972cdd2 productos.buscar → EXITO (2 ms)
[BROKER] 19:27:53 id=b3279eca facturacion.consultar → SERVICIO_DESCONOCIDO (0 ms)
[BROKER] 19:27:53 id=42d0390d productos.stockBajo → PARAMETRO_INVALIDO (3 ms)
[BROKER] 19:27:53 id=d9c0dd95 productos.stockBajo → PARAMETRO_INVALIDO (4 ms)
[BROKER] 19:27:53 id=d882f356 prueba.ejecutar → ERROR_PROVEEDOR (5 ms)
[BROKER] 19:27:53 id=1e3f5fa4 prueba.ejecutar → ERROR_PROVEEDOR (9 ms)
[BROKER] 19:27:53 id=86896dcb productos.consultar → EXITO (8 ms)
[BROKER] 19:27:53 id=8e43a973 productos.consultar → EXITO (5 ms)
[BROKER] 19:27:53 id=24f05738 productos.consultar → ERROR_PROVEEDOR (2 ms)
[BROKER] 19:27:53 id=b24dd230 productos.consultar → ERROR_PROVEEDOR (2 ms)
[PROVEEDOR] Reintento 1/2 lento.ejecutar en localhost:6005 estado=TIMEOUT (Read timed out)
[PROVEEDOR] Reintento 1/2 lento.ejecutar en localhost:6005 estado=TIMEOUT (Read timed out)
[PROVEEDOR] Reintento 2/2 lento.ejecutar en localhost:6005 estado=TIMEOUT (Read timed out)
[PROVEEDOR] Reintento 2/2 lento.ejecutar en localhost:6005 estado=TIMEOUT (Read timed out)
[BROKER] 19:27:59 id=071d555b lento.ejecutar → TIMEOUT (6009 ms)
[BROKER] 19:27:59 id=116d97dc lento.ejecutar → TIMEOUT (6010 ms)
[PROVEEDOR] Reintento 1/2 productos.consultar en localhost:6001 estado=ERROR_PROVEEDOR (Connection refused)
[PROVEEDOR] Reintento 2/2 productos.consultar en localhost:6001 estado=ERROR_PROVEEDOR (Connection refused)
[BROKER] 19:28:00 id=8880135b productos.consultar → ERROR_PROVEEDOR (1 ms)
[PROVEEDOR] Reintento 1/2 ventas.totalDelDia en localhost:6002 estado=ERROR_PROVEEDOR (Connection refused)
[PROVEEDOR] Reintento 2/2 ventas.totalDelDia en localhost:6002 estado=ERROR_PROVEEDOR (Connection refused)
[BROKER] 19:28:00 id=91b4038f ventas.totalDelDia → ERROR_PROVEEDOR (0 ms)
[PROVEEDOR] Reintento 1/2 clientes.consultar en localhost:6003 estado=ERROR_PROVEEDOR (Connection refused)
[PROVEEDOR] Reintento 2/2 clientes.consultar en localhost:6003 estado=ERROR_PROVEEDOR (Connection refused)
[BROKER] 19:28:00 id=7d2b9dd9 clientes.consultar → ERROR_PROVEEDOR (0 ms)
[PROVEEDOR] Reintento 1/2 productos.buscar en localhost:6001 estado=ERROR_PROVEEDOR (Connection refused)
[PROVEEDOR] Reintento 2/2 productos.buscar en localhost:6001 estado=ERROR_PROVEEDOR (Connection refused)
[BROKER] 19:28:00 id=afd4685b productos.buscar → ERROR_PROVEEDOR (0 ms)
[BROKER] 19:28:00 id=3683b4e4 facturacion.consultar → SERVICIO_DESCONOCIDO (0 ms)
[PROVEEDOR] Reintento 1/2 productos.stockBajo en localhost:6001 estado=ERROR_PROVEEDOR (Connection refused)
[PROVEEDOR] Reintento 2/2 productos.stockBajo en localhost:6001 estado=ERROR_PROVEEDOR (Connection refused)
[BROKER] 19:28:00 id=734e1bf3 productos.stockBajo → ERROR_PROVEEDOR (0 ms)
[PROVEEDOR] Reintento 1/2 prueba.ejecutar en localhost:6004 estado=ERROR_PROVEEDOR (Connection refused)
[PROVEEDOR] Reintento 2/2 prueba.ejecutar en localhost:6004 estado=ERROR_PROVEEDOR (Connection refused)
[BROKER] 19:28:00 id=9a48427f prueba.ejecutar → ERROR_PROVEEDOR (0 ms)
[PROVEEDOR] Reintento 1/2 productos.consultar en localhost:6001 estado=ERROR_PROVEEDOR (Connection refused)
[PROVEEDOR] Reintento 2/2 productos.consultar en localhost:6001 estado=ERROR_PROVEEDOR (Connection refused)
[BROKER] 19:28:00 id=348ffcf4 productos.consultar → ERROR_PROVEEDOR (0 ms)
[PROVEEDOR] Reintento 1/2 productos.consultar en localhost:6001 estado=ERROR_PROVEEDOR (Connection refused)
[PROVEEDOR] Reintento 2/2 productos.consultar en localhost:6001 estado=ERROR_PROVEEDOR (Connection refused)
[BROKER] 19:28:00 id=1e104586 productos.consultar → ERROR_PROVEEDOR (0 ms)
[PROVEEDOR] Reintento 1/2 lento.ejecutar en localhost:6005 estado=ERROR_PROVEEDOR (Connection refused)
[PROVEEDOR] Reintento 2/2 lento.ejecutar en localhost:6005 estado=ERROR_PROVEEDOR (Connection refused)
[BROKER] 19:28:00 id=8f85a3dc lento.ejecutar → ERROR_PROVEEDOR (0 ms)
[BROKER] Servicio 'productos' actualizado por nuevo registro
[BROKER] Servicio 'ventas' actualizado por nuevo registro
[BROKER] Servicio 'clientes' actualizado por nuevo registro
[BROKER] Servicio 'prueba' actualizado por nuevo registro
[BROKER] Servicio 'lento' actualizado por nuevo registro
[BROKER] 19:28:04 id=535b3f14 productos.consultar → EXITO (21 ms)
[BROKER] 19:28:04 id=f93d6066 ventas.totalDelDia → EXITO (10 ms)
[BROKER] 19:28:04 id=b352ef68 clientes.consultar → EXITO (3 ms)
[BROKER] 19:28:04 id=603e4b55 productos.buscar → EXITO (3 ms)
[BROKER] 19:28:04 id=5e187053 facturacion.consultar → SERVICIO_DESCONOCIDO (0 ms)
[BROKER] 19:28:04 id=25c684ae productos.stockBajo → PARAMETRO_INVALIDO (3 ms)
[BROKER] 19:28:04 id=655c0d26 prueba.ejecutar → ERROR_PROVEEDOR (1 ms)
[BROKER] 19:28:04 id=9cbdb322 productos.consultar → EXITO (2 ms)
[BROKER] 19:28:04 id=f9952b89 productos.consultar → ERROR_PROVEEDOR (7 ms)
[PROVEEDOR] Reintento 1/2 lento.ejecutar en localhost:6005 estado=TIMEOUT (Read timed out)
[PROVEEDOR] Reintento 2/2 lento.ejecutar en localhost:6005 estado=TIMEOUT (Read timed out)
[BROKER] 19:28:10 id=08a8b9ff lento.ejecutar → TIMEOUT (6005 ms)
```

## Terminal 2 · NodoProveedores (se detiene)

```
Conexión establecida
Proveedor productos escuchando en 6001 (broker localhost)
Proveedor ventas escuchando en 6002 (broker localhost)
Proveedor clientes escuchando en 6003 (broker localhost)
Proveedor prueba escuchando en 6004 (broker localhost)
Proveedor lento escuchando en 6005 (broker localhost)
Proveedores registrados en el broker.
[NodoProveedores detenido]
```

## Terminal 2 · NodoProveedores (se levanta otra vez)

```
Conexión establecida
Proveedor productos escuchando en 6001 (broker localhost)
Proveedor ventas escuchando en 6002 (broker localhost)
Proveedor clientes escuchando en 6003 (broker localhost)
Proveedor prueba escuchando en 6004 (broker localhost)
Proveedor lento escuchando en 6005 (broker localhost)
Proveedores registrados en el broker.
```

## Terminal 3 · ClienteRemoto, demo completa

```
=== Caso 1 · Registro ===
Los proveedores se registran al iniciar NodoProveedores.

=== Caso 2 · Localización ===
productos.consultar codigo=CC001: {id=1.0, nombre=Coca Cola, precio=10.0, cantidad=14.0, vencimiento=null, descripcion=Refresco cola, categoria=Bebidas, proveedor=Coca Cola Co, codigo=CC001}

=== Caso 3 · Una operación por servicio ===
ventas.totalDelDia fecha=2026-05-12: 440.5
clientes.consultar id=1: {id=1.0, nombre=Juan Pérez, email=juan.perez@example.com, telefono=5512345678, direccion=Av. Siempre Viva 742}

=== Caso 4 · Respuesta exitosa ===
productos.buscar nombre=Sabritas: [{id=4.0, nombre=Sabritas, precio=10.0, cantidad=20.0, vencimiento=null, descripcion=Botana salada, categoria=Botanas, proveedor=Pepsico Sabritas, codigo=SAB001}]

=== Caso 5 · Servicio desconocido ===
facturacion.consultar: SERVICIO_DESCONOCIDO · El servicio 'facturacion' no está registrado

=== Caso 6 · Parámetro inválido ===
productos.stockBajo umbral=abc: PARAMETRO_INVALIDO · El parámetro umbral debe ser numérico

=== Caso 7 · Excepción del proveedor ===
prueba.ejecutar: ERROR_PROVEEDOR · fallo simulado del proveedor de prueba
productos.consultar codigo=CC001 (el broker sigue activo): {id=1.0, nombre=Coca Cola, precio=10.0, cantidad=14.0, vencimiento=null, descripcion=Refresco cola, categoria=Bebidas, proveedor=Coca Cola Co, codigo=CC001}

=== Caso 8 · Sustitución del proveedor ===
Se demuestra en el nivel 1 (DemoNivel1). En el nivel 2, el broker sustituye
al proveedor cuando NodoProveedores se reinicia y se vuelve a registrar.

=== Extra · Producto no encontrado ===
productos.consultar codigo=NO-EXISTE: ERROR_PROVEEDOR · producto no encontrado

=== Extra · Proveedor lento (timeout y reintentos) ===
lento.ejecutar: TIMEOUT · Sin respuesta del proveedor después de 2 reintentos: Read timed out
```

## Dos clientes a la vez · cliente A

```
=== Caso 1 · Registro ===
Los proveedores se registran al iniciar NodoProveedores.

=== Caso 2 · Localización ===
productos.consultar codigo=CC001: {id=1.0, nombre=Coca Cola, precio=10.0, cantidad=14.0, vencimiento=null, descripcion=Refresco cola, categoria=Bebidas, proveedor=Coca Cola Co, codigo=CC001}

=== Caso 3 · Una operación por servicio ===
ventas.totalDelDia fecha=2026-05-12: 440.5
clientes.consultar id=1: {id=1.0, nombre=Juan Pérez, email=juan.perez@example.com, telefono=5512345678, direccion=Av. Siempre Viva 742}

=== Caso 4 · Respuesta exitosa ===
productos.buscar nombre=Sabritas: [{id=4.0, nombre=Sabritas, precio=10.0, cantidad=20.0, vencimiento=null, descripcion=Botana salada, categoria=Botanas, proveedor=Pepsico Sabritas, codigo=SAB001}]

=== Caso 5 · Servicio desconocido ===
facturacion.consultar: SERVICIO_DESCONOCIDO · El servicio 'facturacion' no está registrado

=== Caso 6 · Parámetro inválido ===
productos.stockBajo umbral=abc: PARAMETRO_INVALIDO · El parámetro umbral debe ser numérico

=== Caso 7 · Excepción del proveedor ===
prueba.ejecutar: ERROR_PROVEEDOR · fallo simulado del proveedor de prueba
productos.consultar codigo=CC001 (el broker sigue activo): {id=1.0, nombre=Coca Cola, precio=10.0, cantidad=14.0, vencimiento=null, descripcion=Refresco cola, categoria=Bebidas, proveedor=Coca Cola Co, codigo=CC001}

=== Caso 8 · Sustitución del proveedor ===
Se demuestra en el nivel 1 (DemoNivel1). En el nivel 2, el broker sustituye
al proveedor cuando NodoProveedores se reinicia y se vuelve a registrar.

=== Extra · Producto no encontrado ===
productos.consultar codigo=NO-EXISTE: ERROR_PROVEEDOR · producto no encontrado

=== Extra · Proveedor lento (timeout y reintentos) ===
lento.ejecutar: TIMEOUT · Sin respuesta del proveedor después de 2 reintentos: Read timed out

real	0m6.471s
user	0m0.294s
sys	0m0.038s
```

## Dos clientes a la vez · cliente B

```
=== Caso 1 · Registro ===
Los proveedores se registran al iniciar NodoProveedores.

=== Caso 2 · Localización ===
productos.consultar codigo=CC001: {id=1.0, nombre=Coca Cola, precio=10.0, cantidad=14.0, vencimiento=null, descripcion=Refresco cola, categoria=Bebidas, proveedor=Coca Cola Co, codigo=CC001}

=== Caso 3 · Una operación por servicio ===
ventas.totalDelDia fecha=2026-05-12: 440.5
clientes.consultar id=1: {id=1.0, nombre=Juan Pérez, email=juan.perez@example.com, telefono=5512345678, direccion=Av. Siempre Viva 742}

=== Caso 4 · Respuesta exitosa ===
productos.buscar nombre=Sabritas: [{id=4.0, nombre=Sabritas, precio=10.0, cantidad=20.0, vencimiento=null, descripcion=Botana salada, categoria=Botanas, proveedor=Pepsico Sabritas, codigo=SAB001}]

=== Caso 5 · Servicio desconocido ===
facturacion.consultar: SERVICIO_DESCONOCIDO · El servicio 'facturacion' no está registrado

=== Caso 6 · Parámetro inválido ===
productos.stockBajo umbral=abc: PARAMETRO_INVALIDO · El parámetro umbral debe ser numérico

=== Caso 7 · Excepción del proveedor ===
prueba.ejecutar: ERROR_PROVEEDOR · fallo simulado del proveedor de prueba
productos.consultar codigo=CC001 (el broker sigue activo): {id=1.0, nombre=Coca Cola, precio=10.0, cantidad=14.0, vencimiento=null, descripcion=Refresco cola, categoria=Bebidas, proveedor=Coca Cola Co, codigo=CC001}

=== Caso 8 · Sustitución del proveedor ===
Se demuestra en el nivel 1 (DemoNivel1). En el nivel 2, el broker sustituye
al proveedor cuando NodoProveedores se reinicia y se vuelve a registrar.

=== Extra · Producto no encontrado ===
productos.consultar codigo=NO-EXISTE: ERROR_PROVEEDOR · producto no encontrado

=== Extra · Proveedor lento (timeout y reintentos) ===
lento.ejecutar: TIMEOUT · Sin respuesta del proveedor después de 2 reintentos: Read timed out

real	0m6.471s
user	0m0.308s
sys	0m0.055s
```

## Cliente con NodoProveedores apagado

```
=== Caso 1 · Registro ===
Los proveedores se registran al iniciar NodoProveedores.

=== Caso 2 · Localización ===
productos.consultar codigo=CC001: ERROR_PROVEEDOR · Sin respuesta del proveedor después de 2 reintentos: Connection refused

=== Caso 3 · Una operación por servicio ===
ventas.totalDelDia fecha=2026-05-12: ERROR_PROVEEDOR · Sin respuesta del proveedor después de 2 reintentos: Connection refused
clientes.consultar id=1: ERROR_PROVEEDOR · Sin respuesta del proveedor después de 2 reintentos: Connection refused

=== Caso 4 · Respuesta exitosa ===
productos.buscar nombre=Sabritas: ERROR_PROVEEDOR · Sin respuesta del proveedor después de 2 reintentos: Connection refused

=== Caso 5 · Servicio desconocido ===
facturacion.consultar: SERVICIO_DESCONOCIDO · El servicio 'facturacion' no está registrado

=== Caso 6 · Parámetro inválido ===
productos.stockBajo umbral=abc: ERROR_PROVEEDOR · Sin respuesta del proveedor después de 2 reintentos: Connection refused

=== Caso 7 · Excepción del proveedor ===
prueba.ejecutar: ERROR_PROVEEDOR · Sin respuesta del proveedor después de 2 reintentos: Connection refused
productos.consultar codigo=CC001 (el broker sigue activo): ERROR_PROVEEDOR · Sin respuesta del proveedor después de 2 reintentos: Connection refused

=== Caso 8 · Sustitución del proveedor ===
Se demuestra en el nivel 1 (DemoNivel1). En el nivel 2, el broker sustituye
al proveedor cuando NodoProveedores se reinicia y se vuelve a registrar.

=== Extra · Producto no encontrado ===
productos.consultar codigo=NO-EXISTE: ERROR_PROVEEDOR · Sin respuesta del proveedor después de 2 reintentos: Connection refused

=== Extra · Proveedor lento (timeout y reintentos) ===
lento.ejecutar: ERROR_PROVEEDOR · Sin respuesta del proveedor después de 2 reintentos: Connection refused
```

## Cliente después de levantar NodoProveedores otra vez

```
=== Caso 1 · Registro ===
Los proveedores se registran al iniciar NodoProveedores.

=== Caso 2 · Localización ===
productos.consultar codigo=CC001: {id=1.0, nombre=Coca Cola, precio=10.0, cantidad=14.0, vencimiento=null, descripcion=Refresco cola, categoria=Bebidas, proveedor=Coca Cola Co, codigo=CC001}

=== Caso 3 · Una operación por servicio ===
ventas.totalDelDia fecha=2026-05-12: 440.5
clientes.consultar id=1: {id=1.0, nombre=Juan Pérez, email=juan.perez@example.com, telefono=5512345678, direccion=Av. Siempre Viva 742}

=== Caso 4 · Respuesta exitosa ===
productos.buscar nombre=Sabritas: [{id=4.0, nombre=Sabritas, precio=10.0, cantidad=20.0, vencimiento=null, descripcion=Botana salada, categoria=Botanas, proveedor=Pepsico Sabritas, codigo=SAB001}]

=== Caso 5 · Servicio desconocido ===
facturacion.consultar: SERVICIO_DESCONOCIDO · El servicio 'facturacion' no está registrado

=== Caso 6 · Parámetro inválido ===
productos.stockBajo umbral=abc: PARAMETRO_INVALIDO · El parámetro umbral debe ser numérico

=== Caso 7 · Excepción del proveedor ===
prueba.ejecutar: ERROR_PROVEEDOR · fallo simulado del proveedor de prueba
productos.consultar codigo=CC001 (el broker sigue activo): {id=1.0, nombre=Coca Cola, precio=10.0, cantidad=14.0, vencimiento=null, descripcion=Refresco cola, categoria=Bebidas, proveedor=Coca Cola Co, codigo=CC001}

=== Caso 8 · Sustitución del proveedor ===
Se demuestra en el nivel 1 (DemoNivel1). En el nivel 2, el broker sustituye
al proveedor cuando NodoProveedores se reinicia y se vuelve a registrar.

=== Extra · Producto no encontrado ===
productos.consultar codigo=NO-EXISTE: ERROR_PROVEEDOR · producto no encontrado

=== Extra · Proveedor lento (timeout y reintentos) ===
lento.ejecutar: TIMEOUT · Sin respuesta del proveedor después de 2 reintentos: Read timed out
```

## Cliente con el broker apagado (pregunta 3)

```
=== Caso 1 · Registro ===
Los proveedores se registran al iniciar NodoProveedores.

=== Caso 2 · Localización ===
Exception in thread "main" java.net.ConnectException: Connection refused
```

## Cliente con el broker reiniciado y el registro vacío (pregunta 3)

```
=== Caso 2 · Localización ===
productos.consultar codigo=CC001: SERVICIO_DESCONOCIDO · El servicio 'productos' no está registrado
```
