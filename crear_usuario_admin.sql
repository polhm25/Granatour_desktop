-- ============================
-- CREAR USUARIO ADMINISTRADOR
-- ============================
-- Este script agrega un usuario administrador a la base de datos GranaTour
-- Ejecuta este archivo en tu Docker MySQL para crear el usuario admin

USE grana_tour;

-- Insertar usuario administrador
INSERT INTO USUARIOS (nombre, ap1, ap2, dni, email, telefono, rol, password, valoracion, num_turnos, fecha_registro, ultimo_acceso)
VALUES
('Admin', 'GranaTour', '', '00000000A', 'admin@granatour.com', '000000000', 'admin', 'admin123', NULL, 0, NOW(), NULL);

-- Verificar que el usuario admin fue creado correctamente
SELECT id_usuario, nombre, ap1, email, rol
FROM USUARIOS
WHERE rol = 'admin';

-- ============================
-- CREDENCIALES DEL ADMIN:
-- ============================
-- Email: admin@granatour.com
-- Password: admin123
-- Rol: admin
-- ============================
