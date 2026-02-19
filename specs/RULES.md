# RULES - Fuente Única de Verdad Operativa

Este documento define las reglas específicas de este proyecto para implementación, revisión y validación.

## 0) Convenciones normativas
- **MUST**: obligatorio, sin excepción salvo decisión explícita documentada.
- **SHOULD**: recomendado fuerte; si no se cumple, debe justificarse.
- **MAY**: opcional.
- **Bloqueante**: impide cierre de tarea hasta resolverse.

## 1) Precedencia normativa (mayor → menor)
1. `specs/RULES.md` (este documento)
2. `specs/wip/[feature]/` (trabajo activo)
3. `specs/features/[feature]/` (funcionalidad implementada)
4. `specs/PROJECT_INFO.md`
5. `specs/scaffolding.md`
6. `.github/copilot-instructions.md` (solo guía transversal)

Si dos fuentes se contradicen, se aplica la de mayor precedencia.

## 2) Regla crítica de implementación
- MUST: no implementar requerimientos no especificados en `specs/`.
- MUST: si falta información funcional/técnica, detener y pedir clarificación.
- MUST: no asumir reglas de negocio no documentadas.

## 3) Protocolo de conflicto
Ante conflicto entre documentos:
1. Detener cambios.
2. Reportar conflicto con rutas exactas.
3. Solicitar decisión explícita.
4. Reanudar solo con decisión documentada.

## 4) Protocolo de ambigüedad
Ante ambigüedad:
1. No asumir comportamiento.
2. Proponer 2-3 interpretaciones concretas.
3. Solicitar selección del usuario.
4. Documentar la decisión en el artefacto correspondiente.

## 5) Principios de código
- MUST: aplicar SOLID y DRY.
- MUST: priorizar legibilidad, cohesión y separación de responsabilidades.
- MUST: corregir causa raíz antes que parches superficiales.
- SHOULD: evitar sobreingeniería.

## 6) Arquitectura y organización
- MUST: mantener arquitectura hexagonal (Domain, Application, Infrastructure).
- MUST: seguir `specs/scaffolding.md` para estructura de paquetes.
- MUST: organizar por feature en todas las capas; no mezclar features en una misma clase.

## 7) Reglas de comentarios y documentación
### Permitidos
- MAY: JavaDoc en interfaces públicas (ports/contratos).
- MAY: JavaDoc en métodos públicos complejos si aporta contexto real.
- MAY: comentarios de razón técnica no obvia (el "por qué").
- MAY: `TODO` solo con formato `// TODO [ISSUE-123]: descripción`.

### Prohibidos
- MUST NOT: comentarios que parafrasean el código (`// validar datos`, `// guardar`).
- MUST NOT: comentarios decorativos o separadores visuales.
- MUST NOT: comentarios inline obvios de atributos/campos.
- MUST NOT: dejar código comentado.
- MUST NOT: TODOs sin identificador de issue.

### Regla de desempate
Si existe duda entre comentar o refactorizar, prevalece refactorizar y eliminar comentario.

## 8) Records y Lombok
### Regla base
- MUST: tipo inmutable → `record`.
- MUST: tipo mutable → Lombok específico.

### Lombok en este proyecto
- MUST: usar `@Slf4j` en clases que emiten logs (principalmente infraestructura y casos de uso).
- MUST NOT: usar `@Data` en entidades JPA.
- MUST: en JPA usar `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`.

## 9) Nomenclatura
- Clases: nombres descriptivos por responsabilidad (`[Feature]Controller`, `[Feature]RepositoryAdapter`, etc.).
- Variables/métodos: `camelCase` descriptivo.
- Constantes: `UPPER_SNAKE_CASE`.
- MUST: evitar valores mágicos (strings, números, patrones repetidos) hardcodeados; definir constantes con alcance adecuado (`private static final` en clase o constantes compartidas por feature cuando corresponda).
- Mantener consistencia con convenciones ya presentes en el repositorio.

## 10) Validaciones
- MUST: validaciones de negocio en servicios de dominio.
- MUST: validaciones estructurales en DTOs (Jakarta Validation).
- MUST NOT: mezclar validación de infraestructura con reglas de negocio.
- MUST: en modelos de dominio de catálogos (ej. document types, geography, payment methods, tax types, units of measure), mantener en la entidad solo estado y transiciones de ciclo de vida; reglas de aplicabilidad, validación y decisión de negocio deben vivir en `domain/service/[feature]`.

## 11) Excepciones
- MUST: usar excepciones específicas por dominio/feature.
- MUST NOT: capturas genéricas sin necesidad real y justificada.
- MUST: mapear excepciones a HTTP en handler centralizado.

## 12) DTOs y mapeo
- MUST NOT: exponer entidades JPA directamente en controllers.
- MUST: usar MapStruct cuando exista transformación entre capas (Entity↔Domain, Domain↔DTO).
- MUST: separar DTO request/response de modelos de dominio.

## 13) REST API
- MUST: recursos en plural.
- MUST: uso correcto de verbos HTTP (`GET`, `POST`, `PUT`, `PATCH`, `DELETE`).
- MUST: códigos HTTP consistentes; en controllers usar `ResponseEntity` para control explícito de status.

## 14) Persistencia y migraciones
- MUST: cambios de esquema vía Flyway.
- MUST NOT: modificar migraciones ya aplicadas.
- MUST: scripts de catálogos/seed deben ser idempotentes.
- MUST: toda nueva migración/versionado en `src/main/resources/db/migration` debe tener su equivalente en `docker/mysql-init` para entornos locales con Docker.
- MUST NOT: cerrar un feature con cambios de BD sin validar sincronía entre Flyway y `docker/mysql-init`.

## 15) Testing y validación técnica
- MUST: priorizar tests unitarios por caso de uso y reglas de negocio.
- MUST: nomenclatura descriptiva en tests (`@DisplayName` y nombres claros).
- MUST: ejecutar validación técnica antes de cerrar tarea.

### Definición de “validación técnica”
- Si hay cambios en `src/main/java`: MUST ejecutar tests relacionados + build de compilación.
- Si hay cambios en `src/test/java` únicamente: MUST ejecutar al menos los tests modificados.
- Si hay cambios en migraciones SQL: MUST validar build y coherencia de scripts (sin errores sintácticos evidentes).

## 16) Logging y seguridad
- MUST NOT: usar `System.out.println`.
- MUST NOT: exponer secretos o datos sensibles en logs.
- SHOULD: incluir contexto útil en logs sin sobrecargar.
- MUST: los logs deben propagarse hasta infraestructura; la escritura efectiva (appenders/sinks) se realiza en infraestructura.
- MUST NOT: Domain y Application acoplarse a mecanismos concretos de salida de logs.

## 17) Flujo de implementación obligatorio
Antes de implementar:
1. Revisar `specs/RULES.md` y specs del feature.
2. Confirmar que no hay conflicto/ambigüedad.
3. Planificar cambios mínimos y enfocados.

Durante implementación:
1. Respetar arquitectura y nomenclatura.
2. Aplicar validaciones en la capa correcta.
3. Mantener separación de responsabilidades.

Después de implementar:
1. Revisar cumplimiento de estas reglas.
2. Ejecutar validación técnica (tests/build aplicable).
3. Reportar cambios, riesgos y próximos pasos.
4. MUST: ejecutar una auditoría final de cumplimiento "al pie de la letra" contra `specs/RULES.md` y specs del feature antes de cerrar la tarea.

## 18) Criterio de bloqueo automático
Un cambio se considera incompleto si:
- Implementa algo no definido en `specs/`.
- Tiene conflictos/ambigüedades no resueltas.
- Incumple la política de comentarios.
- No ejecuta validación técnica según la sección 15.
- No ejecuta auditoría final de cumplimiento estricto según la sección 17.

## 19) Checklist obligatorio antes de entregar
- [ ] El requerimiento existe en `specs/wip` o `specs/features`.
- [ ] No hay conflictos de precedencia.
- [ ] No hay ambigüedad sin decisión explícita.
- [ ] Se cumple la política de comentarios.
- [ ] Se respeta arquitectura y nomenclatura.
- [ ] La validación técnica (tests/build aplicable) fue ejecutada.
- [ ] Si hubo cambios de BD, Flyway y `docker/mysql-init` están sincronizados.
- [ ] Se ejecutó auditoría final de cumplimiento estricto contra `specs/RULES.md` y specs del feature.

## 20) Estándar de estructura y naming en `specs/wip/[feature]/`

### Estructura mínima requerida
- MUST: cada feature en WIP debe contener exactamente estos artefactos base:
	1. `1-functional-spec.md`
	2. `2-technical-spec.md`
	3. `3-tasks.json`
	4. `STATUS.md`

### Convención de nombres
- MUST: usar prefijo numérico para preservar orden de revisión (`1-`, `2-`, `3-`).
- MUST NOT: usar nombres alternos para los artefactos base (`functional-spec.md`, `technical-spec.md`, `tasks.json`, `plan.md`) una vez estandarizado el feature.

### Enlaces internos y consistencia
- MUST: todos los links y referencias internas del feature deben apuntar a los nombres canónicos anteriores.
- SHOULD: si existe contenido de planificación narrativa, integrarlo en `3-tasks.json` y/o `STATUS.md` en lugar de crear `plan.md` adicional.

## 21) Política de estimación para planeación

### Unidad de estimación
- MUST: toda estimación de trabajo en artefactos de planeación (`3-tasks.json`, `STATUS.md`, documentación de plan) debe expresarse en **story points**.
- MUST NOT: usar estimaciones en horas como unidad principal de planeación.

### JSON de tareas
- MUST: en `3-tasks.json` cada tarea debe usar el campo `storyPoints`.
- MUST NOT: usar `estimatedHours` en tareas nuevas o actualizadas.
- SHOULD: mantener `estimatedStoryPoints` a nivel global del feature para total consolidado.
