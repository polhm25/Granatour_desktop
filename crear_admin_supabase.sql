-- ============================
-- CREAR USUARIO ADMIN PARA GRANATOUR (Supabase)
-- ============================
-- Ejecuta este script en el SQL Editor de Supabase

INSERT INTO USUARIOS (nombre, ap1, ap2, dni, email, telefono, rol, password, valoracion, num_turnos, fecha_registro, ultimo_acceso)
VALUES ('Admin', 'Sistema', '', '00000000A', 'admin@granatour.com', '600000000', 'admin', 'admin123', NULL, 0, NOW(), NOW());

-- ============================
-- CREDENCIALES DE ACCESO:
-- Email: admin@granatour.com
-- Password: admin123
-- ============================
