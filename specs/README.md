# Features - Implementados y en Producción

Esta carpeta contiene features **COMPLETADOS y CONFIRMADOS** en producción.

## 📦 Features Implementados

### 01-document-types
- **Fecha**: 2026-01-15
- **Descripción**: Catálogo de tipos de documento (NIT, CC, CE, etc.)
- **Status**: ✅ Producción
- **Coverage**: 85%+

## 📂 Estructura de cada Feature

Cada feature completado tiene la siguiente estructura:

```
features/XX-feature-name/
├── functional-spec.md    # Especificación funcional
├── technical-spec.md     # Especificación técnica
├── plan.md               # Plan de implementación
├── tasks.json            # Tareas en formato JSON
└── IMPLEMENTED.md        # Resumen de implementación
```

## ✅ Criterios para estar en esta carpeta

Un feature SOLO puede estar aquí cuando:

1. ✅ Todas las tareas completadas (100%)
2. ✅ Tests > 80% coverage
3. ✅ Code review aprobado
4. ✅ Feature integrado y funcionando
5. ✅ No hay blockers ni TODOs pendientes
6. ✅ Documentación completa (IMPLEMENTED.md creado)

## 🔄 Workflow

1. Feature en desarrollo → `wip/feature-name/`
2. Feature completado → `features/XX-feature-name/`
3. Número secuencial asignado (01, 02, 03...)

## 📚 Referencias

- Framework SDD: [../framework/proyecto-framework-sdd.md](../framework/proyecto-framework-sdd.md)
- Estado General: [../STATUS.md](../STATUS.md)
