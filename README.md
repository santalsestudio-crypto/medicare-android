# MEDICARE Android 2.0

Aplicación Android nativa que reutiliza el diseño web original dentro de un WebView y usa AlarmManager para programar avisos nativos.

## Funciones implementadas
- Registro local de nombre, sin correo ni contraseña.
- Alta, edición y eliminación de medicamentos.
- Alarmas nativas con notificación y acción «Ya la tomé».
- Ventana de 10 minutos para obtener 10 puntos. Las confirmaciones posteriores quedan registradas como tardías y no suman puntos.
- Racha calculada con días en que todas las tomas programadas se confirmaron a tiempo. Un día incumplido rompe la racha.
- Recuperación de horarios después de reiniciar el teléfono.
- Consejos de salud y buscador de síntomas de la primera versión.

## Requisitos y limitaciones
- Android 8.0 (API 26) o posterior.
- Android debe permitir notificaciones y alarmas exactas. Si no concede el permiso de alarmas exactas, el sistema usa una alarma inexacta como alternativa, que puede retrasarse.
- El usuario debe permitir notificaciones. En algunos fabricantes, la optimización de batería puede retrasar alarmas; se recomienda permitir actividad en segundo plano para MEDICARE.
- La aplicación no reemplaza las alarmas del reloj ni las indicaciones médicas. No usar como único mecanismo crítico para tratamientos.
- La ventana de 10 minutos se mide desde la hora programada. La acción desde la notificación también registra la toma.
- Los datos se guardan localmente en el almacenamiento de la aplicación y en WebView; desinstalar o borrar datos puede eliminarlos.

## Crear el APK con GitHub Actions
1. Crea un repositorio nuevo en GitHub.
2. Sube el contenido de esta carpeta a la rama `main` (no subas la carpeta ZIP dentro de otro proyecto).
3. Abre la pestaña **Actions** del repositorio y ejecuta **Build MEDICARE APK** (o espera a que termine el flujo al subir a `main`).
4. Cuando termine correctamente, abre la ejecución y descarga el artefacto `MEDICARE-Android-debug`.
5. Descomprime el artefacto y abre `app-debug.apk` en tu teléfono Android. Autoriza la instalación desde esa fuente si Android lo solicita.
6. Abre MEDICARE y concede notificaciones y alarmas exactas. Revisa también Ajustes > Aplicaciones > MEDICARE > Batería y permite el uso en segundo plano si el fabricante lo restringe.

El APK generado por el flujo es de depuración (debug), no está firmado para distribución en Play Store.
