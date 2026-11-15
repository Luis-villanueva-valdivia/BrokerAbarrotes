-- Schema for SistemaGestionAbarrotes
PRAGMA foreign_keys = ON;

-- Clientes
CREATE TABLE IF NOT EXISTS cliente (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    direccion TEXT,
    telefono TEXT,
    email TEXT,
    fecha_registro TEXT DEFAULT (datetime('now'))
);

-- Productos
CREATE TABLE IF NOT EXISTS producto (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    precio REAL DEFAULT 0.0,
    cantidad INTEGER DEFAULT 0,
    vencimiento TEXT,
    descripcion TEXT,
    categoria TEXT,
    proveedor TEXT,
    codigo TEXT UNIQUE
);
CREATE INDEX IF NOT EXISTS idx_producto_categoria ON producto(categoria);
CREATE INDEX IF NOT EXISTS idx_producto_codigo ON producto(codigo);

-- Promociones
CREATE TABLE IF NOT EXISTS promocion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    descripcion TEXT,
    porcentajeDescuento REAL DEFAULT 0.0,
    fecha_inicio TEXT,
    fecha_fin TEXT,
    activo INTEGER DEFAULT 1
);

-- Ventas (encabezado)
CREATE TABLE IF NOT EXISTS venta (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha TEXT DEFAULT (datetime('now')),
    total REAL DEFAULT 0.0,
    cliente_id INTEGER,
    metodo_pago TEXT,
    numero_tarjeta TEXT,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id) ON DELETE SET NULL
);

-- Detalle de venta (productos vendidos)
CREATE TABLE IF NOT EXISTS venta_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    venta_id INTEGER NOT NULL,
    producto_id INTEGER NOT NULL,
    cantidad INTEGER NOT NULL DEFAULT 1,
    precio_unitario REAL DEFAULT 0.0,
    descuento REAL DEFAULT 0.0,
    FOREIGN KEY (venta_id) REFERENCES venta(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE RESTRICT
);

-- Movimientos de inventario
CREATE TABLE IF NOT EXISTS movimiento (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha TEXT DEFAULT (datetime('now')),
    tipo TEXT NOT NULL, -- ej: ENTRADA, SALIDA, AJUSTE
    cantidad INTEGER NOT NULL,
    producto_id INTEGER NOT NULL,
    nota TEXT,
    FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE
);

-- Opcional: tabla que relaciona promociones y productos (si aplica)
CREATE TABLE IF NOT EXISTS promocion_producto (
    promocion_id INTEGER NOT NULL,
    producto_id INTEGER NOT NULL,
    PRIMARY KEY (promocion_id, producto_id),
    FOREIGN KEY (promocion_id) REFERENCES promocion(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES producto(id) ON DELETE CASCADE
);

-- End of schema