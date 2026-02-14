# Copilot Instructions (Transversal)
> Guía general aplicable a cualquier proyecto.

---

## 1) Rol de este documento
Este archivo define **comportamientos transversales** del agente (calidad, flujo de trabajo, comunicación y seguridad).
No define reglas funcionales ni arquitectónicas específicas del negocio.

## 2) Fuente de verdad del proyecto
Las reglas específicas del proyecto (precedencia, ambigüedad, comentarios, arquitectura, convenciones y decisiones funcionales) se leen en:
- `specs/RULES.md` (canónico)
- y luego en el resto de `specs/` según precedencia definida allí.

Si hay conflicto entre este archivo y `specs/RULES.md`, **prevalece `specs/RULES.md`**.

## 3) Principios transversales de implementación
- Responder en español.
- No implementar requisitos no especificados.
- No asumir cuando exista ambigüedad; pedir aclaración concreta.
- Priorizar cambios mínimos, precisos y reversibles.
- Mantener separación de responsabilidades y legibilidad.
- Evitar sobre-ingeniería.

## 4) Calidad de código (agnóstico)
- Mantener consistencia con el estilo existente del repositorio.
- Evitar duplicación innecesaria.
- Nombrar variables/métodos de forma descriptiva.
- Evitar valores mágicos hardcodeados (strings, números, patrones repetidos); usar constantes con el alcance adecuado.
- No introducir deuda técnica evitable.
- Corregir causa raíz antes que parches superficiales.

## 5) Documentación y comentarios (genérico)
- Comentar solo cuando aporte contexto no obvio (el “por qué”).
- Evitar comentarios que describen lo evidente del código.
- No dejar código comentado.
- Los TODO deben incluir identificador de issue cuando la política del proyecto lo exija.

> La política exacta de comentarios se define en `specs/RULES.md`.

## 6) Flujo de trabajo recomendado
1. Revisar `specs/RULES.md` y specs del feature.
2. Detectar conflictos/ambigüedades antes de codificar.
3. Implementar cambios mínimos y enfocados.
4. Validar con pruebas/build relevantes.
5. Reportar qué cambió, riesgos y próximos pasos.

## 7) Seguridad y operación
- No exponer secretos ni credenciales.
- No registrar datos sensibles en logs.
- No ejecutar acciones destructivas no solicitadas.
- No hacer commit sin solicitud explícita del usuario.

## 8) Checklist breve antes de entregar
- [ ] Se respetó `specs/RULES.md`.
- [ ] No hay ambigüedades pendientes.
- [ ] Cambios enfocados y consistentes.
- [ ] Validación técnica ejecutada (cuando aplica).
