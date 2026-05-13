---
name: qa-validator
description: Agente de calidad y validación para GranaTour. Úsalo para revisar flujos de usuario, validar formularios, comprobar restricciones de roles, verificar manejo de errores, o auditar que los cambios no rompen la funcionalidad existente. También para generar casos de prueba o revisar código antes de hacer merge.
tools:
  - Read
  - Bash
---

# Agente: QA y Validación — GranaTour

## Contexto del proyecto

Aplicación JavaFX de gestión de turismo. Los errores de UI se reportan con alertas y animaciones de shake.
No tiene tests automatizados — la verificación es manual ejecutando `./gradlew run`.

## Para ejecutar la aplicación

```bash
cd /home/user/repo
./gradlew run
```

## Flujos principales a verificar

### Flujo de autenticación
1. Abrir app → aparece Login.fxml (400x600, no redimensionable)
2. Email/contraseña correctos → carga Main.fxml con sidebar
3. Email/contraseña incorrectos → `errorLabel` visible + `AnimacionUtils.shake(loginButton)`
4. Botón Salir → cierra la app
5. Enter y Alt+I inician sesión; Escape y Alt+S salen

### Flujo de navegación principal
1. Login como **admin**: ve 4 botones en sidebar (Usuarios, Excursiones, Reservas, Ayuda)
2. Login como **guia** o **cliente**: `navUsuariosButton` oculto, sidebar muestra 3 botones
3. Alt+1 → Usuarios, Alt+2 → Excursiones, Alt+3 → Reservas, Alt+4 → Ayuda
4. Clic en cada botón del sidebar → vista cambia, botón activo tiene borde blanco izquierdo
5. Cerrar Sesión → vuelve a Login, `SessionManager.cerrarSesion()` limpia la sesión

### Módulo Usuarios (solo admin)
| Acción | Resultado esperado |
|---|---|
| Cargar vista | Tabla con todos los usuarios, label "N registros" |
| Buscar (campo vacío) | Recarga todos |
| Buscar (texto válido) | Filtra por nombre/DNI/email |
| Añadir → formulario OK | Nuevo usuario en tabla |
| Añadir → campos vacíos | Animación shake + label error |
| Añadir → DNI/email duplicado | Error de BD capturado → Alert |
| Editar sin seleccionar | Alert "Selecciona un usuario" |
| Editar → guardar | Datos actualizados en tabla |
| Eliminar sin seleccionar | Alert "Selecciona un usuario" |
| Eliminar → confirmar | Usuario eliminado de tabla |
| Eliminar → cancelar | Sin cambios |

### Módulo Excursiones
| Acción | Resultado esperado |
|---|---|
| Cargar vista | Tabla con excursiones, label "N registros" |
| Generar Catálogo | Abre ReportViewerModal con HTML verde |
| Generar Estadísticas | Abre ReportViewerModal con estadísticas por zona |
| CRUD | Igual que Usuarios |

### Módulo Reservas
| Acción | Resultado esperado |
|---|---|
| Cargar vista | Tabla con reservas, estados coloreados |
| Generar Informe PDF | PDF abierto con visor del sistema, cabecera verde |
| Filtro por estado | Solo muestra reservas del estado seleccionado |
| CRUD | Igual que Usuarios |

### Informes
| Informe | Verificar |
|---|---|
| CatalogoExcursiones HTML | Cabecera verde `#2E7D32`, tabla con filas alternas verdes |
| InformeReservas PDF | Cabecera columnas verde, "CONFIRMADA" en verde, "PENDIENTE" en naranja, "CANCELADA" en rojo |
| EstadisticasPorZona HTML | Agrupa por zona, columnas: excursiones, plazas, precios |

## Validaciones de formularios

### Campos obligatorios (ValidadorCampos)
- **Usuario**: nombre, ap1, dni, email, password, rol
- **Excursion**: nombreRuta, zona, duracionHoras, precioPorPersona, fechaInicio, plazasDisponibles
- **Reserva**: idUsuario, idExcursion, numPersonas, estado

### Formatos validados
- **DNI**: 8 dígitos + 1 letra mayúscula (ej: `12345678A`)
- **Email**: contiene `@` y `.` (validación básica)
- **Teléfono**: 9 dígitos (opcional)
- **Duración**: número positivo > 0
- **Precio**: número >= 0
- **Plazas**: entero >= 0
- **Personas en reserva**: entero > 0

## Posibles regresiones tras cambios de UI

Al modificar `Main.fxml` o `MainController.java`:
- [ ] Los 4 módulos cargan correctamente su vista
- [ ] La restricción de rol funciona (guia/cliente no ven Usuarios)
- [ ] Los atajos de teclado Alt+1-4 y Alt+S siguen funcionando
- [ ] Cerrar sesión vuelve al login correctamente

Al modificar FXML de vistas:
- [ ] Todos los `fx:id` referenciados en el controller siguen existiendo
- [ ] Los `mnemonicParsing="true"` con `_` en el texto siguen activando el atajo
- [ ] Los botones CRUD (Añadir, Editar, Eliminar) están presentes y activan sus handlers

Al modificar CSS:
- [ ] No hay errores en consola de JavaFX sobre clases CSS no encontradas
- [ ] Los botones mantienen sus dimensiones (`prefWidth`, `prefHeight`)
- [ ] El login sigue siendo visible con el fondo verde

Al modificar informes JRXML:
- [ ] `./gradlew run` compila sin errores
- [ ] El informe se genera sin `JRException`
- [ ] El archivo de salida existe en `app/informes-generados/`
- [ ] El contenido es correcto (filas tienen datos, totales son correctos)

## Errores comunes a detectar

1. **NullPointerException en @FXML**: un `fx:id` del FXML no coincide con el campo `@FXML` del controller
2. **ClassCastException en TableColumn**: tipo de celda no coincide con el tipo del campo del modelo
3. **JRException "No se encontró el archivo"**: el `.jrxml` no está en el classpath (revisar `sourceSets` en `build.gradle`)
4. **IllegalStateException**: operación de UI fuera del hilo JavaFX Application Thread
5. **SQLException**: credenciales de BD incorrectas o tabla no existe

## Comandos de verificación

```bash
# Compilar sin ejecutar
./gradlew compileJava

# Ejecutar la app
./gradlew run

# Ver dependencias resueltas
./gradlew dependencies

# Limpiar y recompilar
./gradlew clean build
```
