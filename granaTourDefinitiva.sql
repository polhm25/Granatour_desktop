-- ============================
-- GRANATOUR - BASE DE DATOS CORREGIDA
-- ============================

DROP DATABASE IF EXISTS grana_tour;
CREATE DATABASE grana_tour CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE grana_tour;

-- ============================
-- TABLA 1: USUARIOS
-- ============================
CREATE TABLE USUARIOS (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    ap1 VARCHAR(50) NOT NULL,
    ap2 VARCHAR(50),
    dni VARCHAR(9) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    telefono VARCHAR(15),
    rol ENUM('cliente','guia','admin') NOT NULL DEFAULT 'cliente',
    password VARCHAR(255) NOT NULL,
    valoracion DECIMAL(3,2) DEFAULT NULL,
    num_turnos INT DEFAULT 0,
    fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso DATETIME DEFAULT NULL,
    INDEX idx_email (email),
    INDEX idx_dni (dni),
    INDEX idx_rol (rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================
-- TABLA 2: EXCURSIONES
-- ============================
CREATE TABLE EXCURSIONES (
    id_excursion INT AUTO_INCREMENT PRIMARY KEY,
    nombre_ruta VARCHAR(100) NOT NULL,
    zona VARCHAR(100) NOT NULL,
    duracion_horas DECIMAL(4,2) NOT NULL,
    precio_persona DECIMAL(6,2) NOT NULL,
    fecha_inicio DATE NOT NULL,
    plazas_disponibles INT NOT NULL DEFAULT 0,
    id_guia INT,
    imagen LONGBLOB,
    descripcion TEXT,
    FOREIGN KEY (id_guia) REFERENCES USUARIOS(id_usuario)
        ON DELETE SET NULL
        ON UPDATE CASCADE,
    CHECK (duracion_horas > 0),
    CHECK (precio_persona >= 0),
    CHECK (plazas_disponibles >= 0),
    INDEX idx_zona (zona),
    INDEX idx_fecha (fecha_inicio),
    INDEX idx_guia (id_guia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ============================
-- TABLA 3: RESERVAS (tabla intermedia N:M)
-- ============================
CREATE TABLE RESERVAS (
    id_reserva INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    id_excursion INT NOT NULL,
    fecha_reserva DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    num_personas INT NOT NULL DEFAULT 1,
    estado ENUM('pendiente','confirmada','cancelada') NOT NULL DEFAULT 'pendiente',
    precio_total DECIMAL(8,2) NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES USUARIOS(id_usuario)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (id_excursion) REFERENCES EXCURSIONES(id_excursion)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CHECK (num_personas > 0),
    CHECK (precio_total >= 0),
    INDEX idx_usuario (id_usuario),
    INDEX idx_excursion (id_excursion),
    INDEX idx_estado (estado),
    INDEX idx_fecha_reserva (fecha_reserva)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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