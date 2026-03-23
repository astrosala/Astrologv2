# AstroLog — App Android de Astrofotografía

App Android 100% **offline** para controlar sesiones de astrofotografía.
Diseñada para usarse en la montaña sin conexión a internet.

---

## ⚡ INSTALACIÓN SIN PC — APK desde GitHub (recomendado)

### Paso 1 — Crear cuenta en GitHub
Ve a https://github.com → "Sign up" → cuenta gratuita.

### Paso 2 — Crear repositorio
1. Pulsa el botón "New" (repositorio nuevo)
2. Nombre: AstroLog
3. Visibilidad: Private (tus datos son tuyos)
4. Pulsa "Create repository"

### Paso 3 — Subir el proyecto
En la página del repositorio vacío verás "uploading an existing file".
1. Pulsa ese enlace
2. Arrastra todos los archivos del ZIP descomprimido
3. Pulsa "Commit changes"

### Paso 4 — Esperar la compilación (≈5 minutos)
- Ve a la pestaña "Actions" del repositorio
- Verás "Build AstroLog APK" en marcha
- Cuando el círculo sea verde, ha terminado
- También puedes lanzarla con: Actions → "Build AstroLog APK" → "Run workflow"

### Paso 5 — Descargar el APK en el móvil
1. Pulsa en el job completado
2. Baja hasta "Artifacts"
3. Pulsa "AstroLog-debug" para descargar
4. Abre el archivo en el móvil

### Paso 6 — Instalar en el móvil
Ajustes → Aplicaciones → (tu navegador) → Instalar apps desconocidas → Permitir
Abre el .apk → Instalar → Abrir

---

## Equipo configurado
- Refractor 80/380 f4.8 · ZWO ASI 533MC · Bortle 4
- Filtros: L-Pro · Askar C1 (Ha) · Askar C2 (OIII)
- Campo: 102'x102' · 2.04"/px · Temporada Marzo-Junio 2026 · Cataluña 41.5N

---

## Características principales
- Registro de sesiones con calculo automatico HH:MM (subs x exposicion)
- Seeing visual 1-5
- Importar tu Excel de control directamente
- Exportar a .xlsx y .csv
- Calendario de visibilidad mensual con codigo de colores
- Lista de deseos con alertas offline de visibilidad optima
- Tema oscuro forzado (esencial de noche)
- 100% offline: SQLite local, sin internet

## Arquitectura
MVVM + Room (SQLite) + LiveData + Coroutines + WorkManager
Sin permiso de internet.
