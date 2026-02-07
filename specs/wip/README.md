# WIP - Work In Progress

Esta carpeta contiene features **en desarrollo activo**.

## 📊 Features en WIP

### 🔄 document-types
**Status**: 🔴 Planning & Documentation  
**Progress**: 0% (0/32 tasks)  
**Architecture**: Hexagonal (Aligned with Scaffolding)  
**Priority**: High  
**Docs**: [document-types/README.md](document-types/README.md)

Módulo de catálogos de tipos de documentos de identificación (NIT, CC, CE, etc.).

---

## 📋 Uso

### Cuando iniciar un nuevo feature:

1. **Mover de raíz a wip/**
   ```
   Ejemplo: specs/geography/ → specs/wip/geography/
   ```

2. **Crear STATUS.md**
   - Usar template del framework (ver framework/proyecto-framework-sdd.md)
   - Actualizar diariamente con progreso

3. **Archivos requeridos en wip/**
   ```
   wip/feature-name/
   ├── functional-spec.md    (puede estar en progreso)
   ├── technical-spec.md     (puede estar en progreso)
   ├── plan.md               (plan de implementación)
   ├── tasks.json            (tareas estructuradas)
   └── STATUS.md             (estado actual - CRÍTICO)
   ```

4. **Actualizar STATUS.md diariamente**
   - Estado general (🔴🟡🟢⚪)
   - Progreso por fase
   - Tareas actuales
   - Blockers activos
   - Notas y decisiones

5. **Al completar el feature**
   - Mover a `features/XX-feature-name/`
   - Crear `IMPLEMENTED.md`
   - Eliminar `STATUS.md`
   - Actualizar `STATUS.md` global en specs/

## ⚠️ Importante

- **NO dejar features más de 2 sprints en WIP**
- **Actualizar STATUS.md diariamente**
- Los features planeados pero NO iniciados quedan en la raíz de specs/

## 📚 Referencias

- Framework SDD: [../framework/proyecto-framework-sdd.md](../framework/proyecto-framework-sdd.md)
- Estado General: [../STATUS.md](../STATUS.md)
