# Esquema de Base de Datos — GranaTour

**Motor**: PostgreSQL (alojado en Supabase)  
**Script completo**: `granaTour_supabase.sql` (en la raíz del proyecto)  
**Conexión**: `app/src/main/resources/config/database.properties`

## Diagrama de relaciones

```
USUARIOS
├── id_usuario (PK)
├── nombre, ap1, ap2
├── dni (UNIQUE)
├── email (UNIQUE)
├── telefono
├── rol: 'cliente' | 'guia' | 'admin'
├── password
├── valoracion
├── num_turnos
├── fecha_registro
└── ultimo_acceso
     │
     │ (id_guia FK, opcional)          (id_usuario FK)
     │                                         │
     ▼                                         │
EXCURSIONES                              RESERVAS
├── id_excursion (PK)                   ├── id_reserva (PK)
├── nombre_ruta                         ├── id_usuario → USUARIOS
├── zona                                ├── id_excursion → EXCURSIONES
├── duracion_horas                      ├── fecha_reserva
├── precio_persona                      ├── num_personas
├── fecha_inicio                        ├── estado: 'pendiente'|'confirmada'|'cancelada'
├── plazas_disponibles                  └── precio_total
├── id_guia → USUARIOS (nullable)
├── imagen (BYTEA, nullable)
└── descripcion (TEXT, nullable)
```

## Tabla: USUARIOS

```sql
CREATE TABLE USUARIOS (
    id_usuario        SERIAL PRIMARY KEY,
    nombre            VARCHAR(50) NOT NULL,
    ap1               VARCHAR(50) NOT NULL,
    ap2               VARCHAR(50),                              -- nullable
    dni               VARCHAR(9)  UNIQUE NOT NULL,
    email             VARCHAR(100) UNIQUE NOT NULL,
    telefono          VARCHAR(15),                              -- nullable
    rol               VARCHAR(10) NOT NULL DEFAULT 'cliente'
                      CHECK (rol IN ('cliente', 'guia', 'admin')),
    password          VARCHAR(255) NOT NULL,                    -- texto plano (legacy)
    valoracion        DECIMAL(3,2) DEFAULT NULL,
    num_turnos        INT DEFAULT 0,
    fecha_registro    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso     TIMESTAMP DEFAULT NULL
);
```

**Índices:** `idx_email (email)`, `idx_dni (dni)`, `idx_rol (rol)`

**Modelo Java:** `models.Usuario`  
**CRUD:** `app.granatour.crud.UsuarioCRUD`

### Queries habituales

```sql
-- Autenticación
SELECT * FROM USUARIOS WHERE email = ? AND password = ?;

-- Listar guías (para selector en ExcursionForm)
SELECT id_usuario, nombre, ap1, ap2 FROM USUARIOS WHERE rol = 'guia' ORDER BY nombre;

-- Búsqueda por texto
SELECT * FROM USUARIOS
WHERE nombre ILIKE '%texto%' OR dni ILIKE '%texto%' OR email ILIKE '%texto%'
ORDER BY nombre;
```

## Tabla: EXCURSIONES

```sql
CREATE TABLE EXCURSIONES (
    id_excursion        SERIAL PRIMARY KEY,
    nombre_ruta         VARCHAR(100) NOT NULL,
    zona                VARCHAR(100) NOT NULL,
    duracion_horas      DECIMAL(4,2) NOT NULL CHECK (duracion_horas > 0),
    precio_persona      DECIMAL(6,2) NOT NULL CHECK (precio_persona >= 0),
    fecha_inicio        DATE NOT NULL,
    plazas_disponibles  INT NOT NULL DEFAULT 0 CHECK (plazas_disponibles >= 0),
    id_guia             INT REFERENCES USUARIOS(id_usuario)
                        ON DELETE SET NULL ON UPDATE CASCADE,  -- nullable
    imagen              BYTEA,                                 -- nullable
    descripcion         TEXT                                   -- nullable
);
```

**Índices:** `idx_zona (zona)`, `idx_fecha (fecha_inicio)`, `idx_guia (id_guia)`

**Modelo Java:** `models.Excursion`  
**CRUD:** `app.granatour.crud.ExcursionCRUD`

### Queries habituales

```sql
-- Obtener todas con nombre del guía
SELECT e.*, CONCAT(u.nombre, ' ', u.ap1) as nombre_guia
FROM EXCURSIONES e
LEFT JOIN USUARIOS u ON e.id_guia = u.id_usuario
ORDER BY e.zona, e.nombre_ruta;

-- Búsqueda por texto
SELECT e.*, CONCAT(u.nombre, ' ', u.ap1) as nombre_guia
FROM EXCURSIONES e
LEFT JOIN USUARIOS u ON e.id_guia = u.id_usuario
WHERE e.nombre_ruta ILIKE '%texto%' OR e.zona ILIKE '%texto%';

-- Estadísticas por zona (informe EstadisticasPorZona)
SELECT zona,
       COUNT(*) AS total_excursiones,
       SUM(plazas_disponibles) AS total_plazas,
       AVG(precio_persona) AS precio_promedio,
       MIN(precio_persona) AS precio_min,
       MAX(precio_persona) AS precio_max
FROM EXCURSIONES
GROUP BY zona
ORDER BY zona;
```

## Tabla: RESERVAS

```sql
CREATE TABLE RESERVAS (
    id_reserva      SERIAL PRIMARY KEY,
    id_usuario      INT NOT NULL REFERENCES USUARIOS(id_usuario)
                    ON DELETE CASCADE ON UPDATE CASCADE,
    id_excursion    INT NOT NULL REFERENCES EXCURSIONES(id_excursion)
                    ON DELETE CASCADE ON UPDATE CASCADE,
    fecha_reserva   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    num_personas    INT NOT NULL DEFAULT 1 CHECK (num_personas > 0),
    estado          VARCHAR(15) NOT NULL DEFAULT 'pendiente'
                    CHECK (estado IN ('pendiente', 'confirmada', 'cancelada')),
    precio_total    DECIMAL(8,2) NOT NULL CHECK (precio_total >= 0)
);
```

**Índices:** `idx_usuario (id_usuario)`, `idx_excursion (id_excursion)`, `idx_estado (estado)`, `idx_fecha_reserva (fecha_reserva)`

**Modelo Java:** `models.Reserva`  
**CRUD:** `app.granatour.crud.ReservaCRUD`

### Queries habituales

```sql
-- Obtener todas con nombres de cliente y excursión
SELECT r.id_reserva,
       CONCAT(u.nombre, ' ', u.ap1) as nombre_cliente,
       e.nombre_ruta,
       r.fecha_reserva, r.num_personas, r.estado, r.precio_total
FROM RESERVAS r
INNER JOIN USUARIOS u ON r.id_usuario = u.id_usuario
INNER JOIN EXCURSIONES e ON r.id_excursion = e.id_excursion
ORDER BY r.fecha_reserva DESC;

-- Filtrar por estado
SELECT ... WHERE r.estado = 'confirmada';

-- Calcular precio total al crear reserva
precio_total = num_personas * excursion.precio_persona
```

## Datos de prueba (del script SQL)

```
USUARIOS de prueba:
- Juan Garcia Lopez   (guia,  id=1) — guía con 15 turnos
- Maria Rodriguez     (cliente, id=2)
- Carlos Lopez        (cliente, id=3)
- Ana Martinez        (guia,  id=4) — guía con 22 turnos
- Pedro Jimenez       (cliente, id=5)

EXCURSIONES de prueba:
- Ruta de los Picos (Sierra Nevada, 6.5h, 45€, guía=Juan)
- Senderismo en las Alpujarras (4h, 30€, guía=Ana)
- Trekking por el Mulhacén (Sierra Nevada, 8h, 55€, guía=Juan)
- Paseo por la Costa Tropical (3.5h, 25€, guía=Ana)

RESERVAS de prueba:
- Maria → Ruta Picos (2 personas, confirmada, 90€)
- Carlos → Alpujarras (1 persona, pendiente, 30€)
- Pedro → Mulhacén (3 personas, confirmada, 165€)
- Maria → Costa Tropical (2 personas, confirmada, 50€)
```

## Administrador (script separado)

```sql
-- crear_admin_supabase.sql
-- Crea el usuario admin por defecto para acceder al sistema
```

## Nota sobre contraseñas

Las contraseñas se almacenan en **texto plano** (sistema legacy). La autenticación hace comparación directa:
```sql
SELECT * FROM USUARIOS WHERE email = ? AND password = ?
```
Si en el futuro se migra a hashing, habrá que actualizar `UsuarioCRUD.autenticar()`.
