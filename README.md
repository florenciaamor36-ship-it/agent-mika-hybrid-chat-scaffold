# Asistente híbrido Android (experimental)

App Android nueva para unir una experiencia de asistente general en español con control explícito y opcional de apps del teléfono. Proyecto independiente: no modifica ni reemplaza Agent ni Mika.

## Estado real

- En construcción; no hay APK publicada ni probada en un teléfono.
- Chat básico: app Android con endpoint compatible con OpenAI Chat Completions y proveedor configurable.
- Memoria editable e historial: se guardan cifrados localmente con Android Keystore; el contenido de la memoria se envía al modelo junto con la conversación.
- El control de otras apps todavía no está implementado. Se diseñará como módulo optativo, activado por la persona desde Android y limitado por permisos; no se dará por integrado hasta construirlo y probarlo.
- El modelo no viene incluido: la inferencia depende de la conexión, del proveedor elegido y de los límites o costos de su servicio. No promete IA ilimitada gratis ni ejecución offline.

## Requisitos previstos

- Android 8+ para el chat base (por confirmar con build).
- El control de apps dependerá de las APIs/permisos compatibles con cada versión de Android; se verificará primero en el ZTE Blade A55 real.
- Las claves se guardan cifradas con Android Keystore en el dispositivo, nunca en el repositorio.

## Compilar

Abrí el proyecto en Android Studio o ejecutá `gradle assembleDebug` con JDK 17, Android SDK y Gradle 8.11.1 o compatible. La primera build puede requerir descargar Gradle/Android Gradle Plugin.

## Referencias de inspiración y licencias

- [Agent](https://github.com/Past-da-king/agent), licencia MIT: referencia de asistente general, chat y memoria.
- [Mika](https://github.com/wangzan101/Mika), licencia Apache-2.0: referencia del módulo de operación de apps y controles explícitos.

Esta primera estructura no copia código de esos proyectos. Si se incorpora código en el futuro, se preservarán sus avisos y licencias correspondientes.

## Próximos hitos

1. Verificar compilación Android.
2. Implementar chat y guardado seguro de configuración del proveedor.
3. Añadir historial/memoria local y navegación web con permisos claros.
4. Diseñar el módulo de accesibilidad/control con vista previa, pausa y confirmación para acciones sensibles.
5. Probar en el ZTE Blade A55; una build de CI no cuenta como prueba en el teléfono.
