# WIP - Work In Progress

Esta carpeta contiene features **en desarrollo activo**.

## 📊 Features en WIP

### ✅ 01-document-types
**Status**: ✅ Completed  
**Progress**: 100%  
**Architecture**: Hexagonal (Aligned with Scaffolding)  
**Priority**: High  
**Docs**: [01-document-types/status.md](01-document-types/status.md)

Módulo de catálogos de tipos de documentos de identificación (NIT, CC, CE, etc.).

### ✅ 02-geography
**Status**: ✅ Completed  
**Progress**: 100%  
**Architecture**: Hexagonal (Aligned with Scaffolding)  
**Docs**: [02-geography/status.md](02-geography/status.md)

Módulo de geografía (departamentos y municipios de Colombia).

### ⚪ 03-payment-methods
**Status**: ⚪ Pendiente de implementación  
**Progress**: 0% (0/29 tasks)  
**Architecture**: Hexagonal (Aligned with Scaffolding)  
**Docs**: [03-payment-methods/STATUS.md](03-payment-methods/STATUS.md)

Módulo de métodos de pago.

### ⚪ 04-tax-types
**Status**: ⚪ Pendiente de aprobación  
**Progress**: 0% (0/38 tasks)  
**Architecture**: Hexagonal (Aligned with Scaffolding)  
**Docs**: [04-tax-types/STATUS.md](04-tax-types/STATUS.md)

Módulo de tipos de impuestos.

### ⚪ 05-units-of-measure
**Status**: ⚪ Pendiente de aprobación  
**Progress**: 0% (0/38 tasks)  
**Architecture**: Hexagonal (Aligned with Scaffolding)  
**Docs**: [05-units-of-measure/STATUS.md](05-units-of-measure/STATUS.md)

Módulo de unidades de medida.

### ⏳ 06-security
**Status**: ⏳ Not Started  
**Progress**: 0% (0/92 tasks)  
**Architecture**: Hexagonal (Aligned with Scaffolding)  
**Docs**: [06-security/STATUS.md](06-security/STATUS.md)

Módulo de seguridad y control de acceso.

### ⏳ 07-inventory
**Status**: ⏳ Not Started  
**Progress**: 0% (0/128 tasks)  
**Architecture**: Hexagonal (Aligned with Scaffolding)  
**Docs**: [07-inventory/STATUS.md](07-inventory/STATUS.md)

Módulo de gestión integral de inventarios.

---

## 📋 Uso

### Cuando iniciar un nuevo feature:

1. **Mover de raíz a wip/**
   ```
   Ejemplo: specs/08-new-feature/ → specs/wip/08-new-feature/
   ```

2. **Crear STATUS.md**
   - Usar template del framework (ver framework/proyecto-framework-sdd.md)
   - Actualizar diariamente con progreso

3. **Archivos requeridos en wip/**
   ```
   wip/XX-feature-name/
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
