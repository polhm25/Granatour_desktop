-- ============================
-- GRANATOUR - BASE DE DATOS PARA SUPABASE (PostgreSQL)
-- ============================

-- Nota: En Supabase, la base de datos ya existe como 'postgres'
-- Ejecuta este script en el SQL Editor de Supabase

-- ============================
-- ELIMINAR TABLAS SI EXISTEN (en orden inverso por FK)
-- ============================
DROP TABLE IF EXISTS RESERVAS CASCADE;
DROP TABLE IF EXISTS EXCURSIONES CASCADE;
DROP TABLE IF EXISTS USUARIOS CASCADE;

-- ============================
-- TABLA 1: USUARIOS
-- ============================
CREATE TABLE USUARIOS (
    id_usuario SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    ap1 VARCHAR(50) NOT NULL,
    ap2 VARCHAR(50),
    dni VARCHAR(9) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    telefono VARCHAR(15),
    rol VARCHAR(10) NOT NULL DEFAULT 'cliente' CHECK (rol IN ('cliente', 'guia', 'admin')),
    password VARCHAR(255) NOT NULL,
    valoracion DECIMAL(3,2) DEFAULT NULL,
    num_turnos INT DEFAULT 0,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso TIMESTAMP DEFAULT NULL
);

-- Índices para USUARIOS
CREATE INDEX idx_email ON USUARIOS(email);
CREATE INDEX idx_dni ON USUARIOS(dni);
CREATE INDEX idx_rol ON USUARIOS(rol);

-- ============================
-- TABLA 2: EXCURSIONES
-- ============================
CREATE TABLE EXCURSIONES (
    id_excursion SERIAL PRIMARY KEY,
    nombre_ruta VARCHAR(100) NOT NULL,
    zona VARCHAR(100) NOT NULL,
    duracion_horas DECIMAL(4,2) NOT NULL CHECK (duracion_horas > 0),
    precio_persona DECIMAL(6,2) NOT NULL CHECK (precio_persona >= 0),
    fecha_inicio DATE NOT NULL,
    plazas_disponibles INT NOT NULL DEFAULT 0 CHECK (plazas_disponibles >= 0),
    id_guia INT REFERENCES USUARIOS(id_usuario) ON DELETE SET NULL ON UPDATE CASCADE,
    imagen BYTEA,
    descripcion TEXT
);

-- Índices para EXCURSIONES
CREATE INDEX idx_zona ON EXCURSIONES(zona);
CREATE INDEX idx_fecha ON EXCURSIONES(fecha_inicio);
CREATE INDEX idx_guia ON EXCURSIONES(id_guia);

-- ============================
-- TABLA 3: RESERVAS (tabla intermedia N:M)
-- ============================
CREATE TABLE RESERVAS (
    id_reserva SERIAL PRIMARY KEY,
    id_usuario INT NOT NULL REFERENCES USUARIOS(id_usuario) ON DELETE CASCADE ON UPDATE CASCADE,
    id_excursion INT NOT NULL REFERENCES EXCURSIONES(id_excursion) ON DELETE CASCADE ON UPDATE CASCADE,
    fecha_reserva TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    num_personas INT NOT NULL DEFAULT 1 CHECK (num_personas > 0),
    estado VARCHAR(15) NOT NULL DEFAULT 'pendiente' CHECK (estado IN ('pendiente', 'confirmada', 'cancelada')),
    precio_total DECIMAL(8,2) NOT NULL CHECK (precio_total >= 0)
);

-- Índices para RESERVAS
CREATE INDEX idx_usuario ON RESERVAS(id_usuario);
CREATE INDEX idx_excursion ON RESERVAS(id_excursion);
CREATE INDEX idx_estado ON RESERVAS(estado);
CREATE INDEX idx_fecha_reserva ON RESERVAS(fecha_reserva);

-- ============================
-- DATOS DE PRUEBA
-- ============================

-- USUARIOS DE PRUEBA
INSERT INTO USUARIOS (nombre, ap1, ap2, dni, email, telefono, rol, password, valoracion, num_turnos, fecha_registro, ultimo_acceso) VALUES
('Juan', 'Garcia', 'Lopez', '12345678A', 'juan.garcia@email.com', '600111222', 'guia', 'password123', 4.5, 15, NOW(), NOW()),
('Maria', 'Rodriguez', 'Martinez', '23456789B', 'maria.rodriguez@email.com', '600333444', 'cliente', 'password123', 4.8, 8, NOW(), NOW()),
('Carlos', 'Lopez', 'Fernandez', '34567890C', 'carlos.lopez@email.com', '600555666', 'cliente', 'password123', 4.2, 5, NOW(), NOW()),
('Ana', 'Martinez', 'Garcia', '45678901D', 'ana.martinez@email.com', '600777888', 'guia', 'password123', 4.9, 22, NOW(), NOW()),
('Pedro', 'Jimenez', 'Ruiz', '56789012E', 'pedro.jimenez@email.com', '600999000', 'cliente', 'password123', 3.9, 3, NOW(), NOW());

-- EXCURSIONES DE PRUEBA
INSERT INTO EXCURSIONES (nombre_ruta, zona, duracion_horas, precio_persona, fecha_inicio, plazas_disponibles, id_guia, descripcion) VALUES
('Ruta de los Picos', 'Sierra Nevada', 6.5, 45.00, '2025-12-01', 15, 1, 'Una emocionante ruta por los picos más altos de Sierra Nevada'),
('Senderismo en las Alpujarras', 'Las Alpujarras', 4.0, 30.00, '2025-12-05', 20, 4, 'Recorre los pueblos blancos de Las Alpujarras con vistas espectaculares'),
('Trekking por el Mulhacén', 'Sierra Nevada', 8.0, 55.00, '2025-12-10', 10, 1, 'Ascensión al pico más alto de la Península Ibérica'),
('Paseo por la Costa Tropical', 'Costa Tropical', 3.5, 25.00, '2025-11-30', 25, 4, 'Relajante paseo por las playas y calas de la costa tropical');

-- RESERVAS DE PRUEBA
INSERT INTO RESERVAS (id_usuario, id_excursion, fecha_reserva, num_personas, estado, precio_total) VALUES
(2, 1, NOW(), 2, 'confirmada', 90.00),
(3, 2, NOW(), 1, 'pendiente', 30.00),
(5, 3, NOW(), 3, 'confirmada', 165.00),
(2, 4, NOW(), 2, 'confirmada', 50.00);

-- ============================
-- USUARIO ADMIN (si lo necesitas)
-- ============================
-- INSERT INTO USUARIOS (nombre, ap1, ap2, dni, email, telefono, rol, password)
-- VALUES ('Admin', 'Sistema', '', '00000000A', 'admin@granatour.com', '600000000', 'admin', 'admin123');
