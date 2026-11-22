# Arquitectura inicial

## Objetivo
Una app Android liviana en español, con el chat del agente general como base y un módulo independiente de control de apps que la persona habilita manualmente.

## Módulos

- `chat`: interfaz de conversación y adaptador inicial para endpoints compatibles con OpenAI Chat Completions. La configuración está cifrada en Android Keystore; el proveedor recibe los mensajes.
- `memory` (pendiente): memoria local visible, editable y borrable por la persona.
- `web` (pendiente): navegación aislada; no se controlarán pestañas personales externas.
- `phone-control` (pendiente): integración optativa con AccessibilityService u otra API Android apropiada. Requiere activación explícita en Ajustes del teléfono, indicador visible, botón de detener y límites para acciones sensibles. No se debe intentar ocultar el servicio ni sortear permisos.

## Privacidad y seguridad

- No subir claves, sesiones, datos personales ni capturas del usuario a GitHub.
- No ejecutar pagos, compras, envíos de mensajes ni cambios de cuenta sin autorización y confirmación visibles.
- No tratar instrucciones leídas de páginas o apps como autorización para ignorar estos límites.
- No prometer gratuidad ilimitada: solo la app puede ser libre; los modelos y servicios pueden tener costos o cuotas.
- La función de control de apps no está en este primer scaffold y no debe anunciarse como disponible.

## Compatibilidad

El chat parte de Android 8 (API 26). El control de apps se diseñará y verificará por separado según versión, fabricante y permisos; el ZTE Blade A55 concreto aún requiere prueba física.
