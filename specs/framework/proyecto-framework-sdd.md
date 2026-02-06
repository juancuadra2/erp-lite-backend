# Framework SDD - Sistema de Documentación de Desarrollo

**Versión**: 2.0  
**Fecha**: Febrero 2026

---

## 🎯 Objetivo

Proporcionar un framework genérico y reutilizable para mantener una **fuente única de verdad** (Single Source of Truth) para todas las especificaciones técnicas y funcionales de cualquier proyecto de software, organizando el conocimiento de forma simple y eficiente.

**Características clave**:
- Framework **genérico** - No atado a ningún proyecto específico
- **Reutilizable** - Aplicable a cualquier tipo de proyecto de software
- **Estructurado** - Organización clara y predecible
- **Trazable** - Seguimiento completo del ciclo de vida de features

---

## Estructura del Framework

```
specs/
├── README.md                    # Este archivo - Guía del framework
├── PROJECT_INFO.md              # Información general del proyecto
├── STATUS.md                    # Estado general de TODOS los features
├── features/                    # Features IMPLEMENTADOS y CONFIRMADOS
│   └── 01-feature-example/      
│       ├── functional-spec.md   # Especificación funcional
│       ├── technical-spec.md    # Especificación técnica
│       ├── plan.md              # Plan de implementación detallado
│       ├── tasks.json           # Tareas en formato JSON
│       └── IMPLEMENTED.md       # Resumen de lo implementado
│
└── wip/                         # Work In Progress - Features en desarrollo
    └── feature-name/
        ├── functional-spec.md   # Especificación funcional
        ├── technical-spec.md    # Especificación técnica
        ├── plan.md              # Plan de implementación
        ├── tasks.json           # Tareas en formato JSON
        └── STATUS.md            # Estado actual del desarrollo
```

---

## Principios del Framework

### 🎯 Simplicidad
- **Separación clara**: Spec funcional y técnico en archivos independientes
- **Un solo archivo para el plan**: Todas las tareas en `plan.md`
- **Sin carpetas vacías**: No crear estructura hasta que sea necesaria

### 📊 Trazabilidad
- Todo feature tiene un número secuencial: `01-user-auth`, `02-product-catalog`, etc.
- Fácil encontrar qué está implementado (`features/`) vs qué está en desarrollo (`wip/`)

### 🔄 Flujo Simple
1. **Nueva feature** → Crear en `wip/feature-name/`
2. **En desarrollo** → Actualizar `STATUS.md` continuamente
3. **Completada** → Mover a `features/XX-feature-name/` con número secuencial

---

## Descripción de Carpetas y Archivos

### 📁 `features/` - Features Implementados

Contiene todas las funcionalidades **COMPLETADAS y CONFIRMADAS** en producción.

Estructura por feature:
```
features/
└── XX-feature-name/
    ├── functional-spec.md   # Especificación funcional
    ├── technical-spec.md    # Especificación técnica
    ├── plan.md              # Plan de implementación con todas las tareas
    ├── tasks.json           # Tareas en formato JSON estructurado
    └── IMPLEMENTED.md       # Documentación de lo implementado
```

**Archivos**:
- **`functional-spec.md`**: Especificación funcional con:
  - Overview y objetivos de negocio
  - User stories y escenarios
  - Requisitos funcionales
  - Reglas de negocio
  - Casos de uso
  - Criterios de aceptación
  - Out of scope

- **`technical-spec.md`**: Especificación técnica con:
  - Arquitectura y diseño técnico
  - Modelo de datos (entidades, relaciones)
  - API contracts (endpoints, DTOs)
  - Dependencias técnicas
  - Constraints técnicos (performance, seguridad)
  - Decisiones técnicas
  - Integraciones

- **`plan.md`**: Plan de implementación con:
  - Resumen ejecutivo
  - Fases de implementación
  - Tareas detalladas (checkboxes)
  - Estimaciones
  - Riesgos y mitigaciones

- **`tasks.json`**: Tareas en formato JSON estructurado:
  - Lista de todas las tareas con IDs únicos
  - Estado de cada tarea (pending, in-progress, completed, blocked)
  - Metadata (fase, estimación, prioridad, asignado)
  - Dependencias entre tareas
  - Formato consumible por herramientas y scripts

- **`IMPLEMENTED.md`**: Documentación de implementación:
  - Fecha de implementación
  - Resumen de lo desarrollado
  - Cambios vs plan original
  - Lecciones aprendidas
  - Tests coverage
  - Integration notes

### 📁 `wip/` - Work In Progress

Contiene features **EN DESARROLLO ACTIVO**. Una vez completadas, se mueven a `features/`.

Estructura por feature:
```
wip/
└── feature-name/
    ├── functional-spec.md   # Especificación funcional (puede estar incompleta)
    ├── technical-spec.md    # Especificación técnica (puede estar incompleta)
    ├── plan.md              # Plan de implementación
    ├── tasks.json           # Tareas en formato JSON estructurado
    └── STATUS.md            # Estado actual - CRÍTICO para seguimiento
```

**Archivos**:
- **`functional-spec.md`** y **`technical-spec.md`**: Igual que en `features/`, pero pueden estar en progreso
- **`plan.md`**: Plan de implementación con todas las tareas
- **`tasks.json`**: Tareas en formato JSON, sincronizado con plan.md y STATUS.md
- **`STATUS.md`**: **Archivo clave para seguimiento**, contiene:

```markdown
# Status - Feature Name

**Última actualización**: YYYY-MM-DD HH:MM  
**Developer**: @developer  
**Estado general**: 🟡 En Progreso

---

## 📊 Progreso General

- **Completado**: 15/30 tareas (50%)
- **En progreso**: 3 tareas
- **Bloqueado**: 1 tarea
- **Por hacer**: 11 tareas

---

## 🎯 Estado por Fase

### ✅ Phase 1: [Nombre Fase] (COMPLETADO)
- [x] T001: [Nombre tarea]
- [x] T002: [Nombre tarea]
- [x] T003: [Nombre tarea]

### 🟡 Phase 2: [Nombre Fase] (EN PROGRESO - XX%)
- [x] T004: [Nombre tarea completada]
- [x] T005: [Nombre tarea completada]
- [ ] T006: [Nombre tarea] → **EN PROGRESO** (Developer: @developer)

### 🔴 Phase 3: [Nombre Fase] (BLOQUEADO)
- [ ] T007: [Nombre tarea] → **BLOQUEADO** (Razón)

### ⚪ Phase 4: [Nombre Fase] (NO INICIADO)
- [ ] T008: [Nombre tarea]
- [ ] T009: [Nombre tarea]

---

## 🚧 Tareas Actuales (Hoy)

1. **TXXX: [Nombre de tarea]** 
   - Estado: En progreso (XX%)
   - Developer: @developer
   - Blocker: [Ninguno / Descripción]
   - ETA: YYYY-MM-DD

2. **TYYY: Code review pendiente**
   - Estado: Esperando review
   - Reviewer: @reviewer

---

## 🔴 Blockers Activos

1. **Dependency externa**: [Descripción del blocker]
   - Reportado: YYYY-MM-DD
   - Owner: @developer
   - Impacto: Bloquea [tareas afectadas]

---

## 📝 Notas y Decisiones

### YYYY-MM-DD
- [Decisión técnica o cambio importante]
- [Impacto y justificación]

### YYYY-MM-DD
- [Problema resuelto y solución aplicada]

---

## 🎯 Próximos Pasos

1. [Tarea prioritaria 1]
2. [Tarea prioritaria 2]
3. [Actividad de seguimiento]

---

## 📅 Timeline

- **Inicio**: YYYY-MM-DD
- **ETA Completado**: YYYY-MM-DD
- **Días trabajados**: X/Y
- **Días restantes**: Z
```

---

## 📄 `PROJECT_INFO.md` - Información del Proyecto

Archivo en la raíz de `specs/` que mantiene información general:

```markdown
# [Project Name] - Project Information

**Última actualización**: YYYY-MM-DD  
**Versión**: X.Y.Z  
**Estado**: [Estado del proyecto]

---

## 📋 Información General

- **Nombre**: [Nombre del Proyecto]
- **Repositorio**: [URL del repositorio]
- **Stack**: [Tecnologías principales]
- **Arquitectura**: [Patrón arquitectónico]

---

## 📊 Features Overview

### ✅ Implementados (Production)
1. **01-feature-name** - Descripción del feature
   - Fecha: YYYY-MM-DD
   - Endpoints: X REST APIs
   - Tests: XX% coverage

### 🟡 En Desarrollo (WIP)
1. **feature-name** - Descripción del feature
   - Developer: @developer
   - ETA: YYYY-MM-DD
   - Estado: XX% completado

### 📋 Planeados (Backlog)
1. **feature-1** - Descripción breve
2. **feature-2** - Descripción breve
3. **feature-3** - Descripción breve

---

## 🏗️ Arquitectura General

- **Patrón**: [Patrón arquitectónico]
- **Capas**: [Descripción de capas]
- **Base de datos**: [Base de datos y herramientas]
- **Testing**: [Frameworks de testing]

---

## 🔗 Enlaces Importantes

- [README Principal](../README.md) - Documentación general del proyecto
- [Error Format Standard](framework/STANDARD_ERROR_FORMAT.md)

---

## 👥 Team

- Tech Lead: @name
- Developers: @dev1, @dev2
- QA: @name

---

## 📈 Métricas del Proyecto

- **Total Features**: X planeados
- **Completados**: X
- **En desarrollo**: X
- **Test Coverage**: XX%
- **Technical Debt**: [Nivel]

---

## 🎯 Objetivos [Periodo]

- [ ] Objetivo 1
- [ ] Objetivo 2
- [ ] Objetivo 3
```

---

## 📄 `STATUS.md` - Estado General del Proyecto

Archivo en la raíz de `specs/` que mantiene el estado global de todos los features:

```markdown
# [Project Name] - Estado General

**Última actualización**: YYYY-MM-DD  
**Sprint actual**: Sprint X

---

## 📊 Dashboard General

### Resumen de Features
- **Total features planeados**: X
- **Completados**: X (XX%)
- **En desarrollo**: X (XX%)
- **Pendientes**: X (XX%)

### Progreso General
```
████████░░░░░░░░░░░░░░░░░░ XX% completado
```

---

## ✅ Features Completados

### 01-feature-name
- **Estado**: ✅ Completado
- **Fecha**: YYYY-MM-DD
- **Coverage**: XX%
- **Ver**: [features/01-feature-name/](features/01-feature-name/)

---

## 🟡 Features En Desarrollo (WIP)

### feature-name
- **Estado**: 🟡 En Progreso (XX%)
- **Developer**: @developer
- **ETA**: YYYY-MM-DD
- **Blocker**: [Ninguno / Descripción]
- **Ver**: [wip/feature-name/STATUS.md](wip/feature-name/STATUS.md)

---

## 📋 Features Pendientes (Backlog)

1. **feature-1** - [Prioridad]
2. **feature-2** - [Prioridad]
3. **feature-3** - [Prioridad]

---

## 🔴 Blockers Globales

_No hay blockers globales actualmente_

---

## 📅 Timeline del Sprint

- **Sprint X**: YYYY-MM-DD → YYYY-MM-DD
- **Objetivo**: [Objetivo del sprint]
- **Estado**: [On track / At risk / Delayed]

---

## 📝 Decisiones Recientes

### YYYY-MM-DD
- [Decisión importante 1]

### YYYY-MM-DD
- [Decisión importante 2]
```

---

## 🔄 Workflow de Desarrollo

### Fases de Implementación

Toda feature nueva debe seguir este proceso estructurado en 5 fases:

> **⚠️ REGLA CRÍTICA**: Para avanzar a la siguiente fase, la fase anterior debe estar **REVISADA Y APROBADA**. No se puede iniciar una fase sin la aprobación formal de la anterior.

#### **PHASE 1: Functional Spec**
```
Draft → Clarify → Approve
```
1. **Draft**: Escribir especificación funcional inicial en `functional-spec.md`
   - Overview y objetivos de negocio
   - User stories y escenarios
   - Requisitos funcionales
   - Reglas de negocio
   - Criterios de aceptación

2. **Clarify**: Revisión con stakeholders
   - Resolver ambigüedades
   - Validar requisitos
   - Ajustar scope si es necesario

3. ****✅ GATE: Aprobación requerida para avanzar a PHASE 2**ión formal del Product Owner
   - ✅ Functional spec firmada
   - Proceder a Phase 2

---

#### **PHASE 2: Technical Spec**
```
Draft → Clarify → Approve
```
1. **Draft**: Escribir especificación técnica en `technical-spec.md`
   - Arquitectura y diseño técnico
   - Modelo de datos
   - API contracts
   - Dependencias técnicas
   - Decisiones técnicas

2. **Clarify**: Revisión técnica con el equipo
   - Tech review con Tech Lead
   - Validar decisiones de arquitectura
   - Identificar riesgos técnicos

3. **Approve**: Aprobación del Tech Lead
   - ✅ Technical spec aprobada
   - **✅ GATE: Aprobación requerida para avanzar a PHASE 3**

---

#### **PHASE 3: Plan**
```
Draft → Clarify → Approve
```
1. **Draft**: Crear plan de implementación en `plan.md`
   - Dividir en fases lógicas
   - Listar todas las tareas
   - Estimar esfuerzo
   - Identificar dependencias

2. **Clarify**: Revisión del plan
   - Validar estimaciones
   - Ajustar secuencia de tareas
   - Identificar riesgos

3. **Approve**: Aprobación del plan
   - ✅ Plan aprobado
   - **✅ GATE: Aprobación requerida para avanzar a PHASE 4**

---

#### **PHASE 4: Tasks**
```
Generate → Refine → Approve
```
1. **Generate**: Crear `tasks.json` desde `plan.md`
   - Convertir tareas a formato JSON estructurado
   - Asignar IDs únicos (T001, T002, etc.)
   - Definir estados y metadata

2. **Refine**: Refinar tareas
   - Agregar estimaciones detalladas
   - Definir dependencias entre tareas
   - Asignar prioridades

3. **Approve**: Validación final
   - ✅ Tasks.json sincronizado con plan.md
   - Inicializar STATUS.md
   - **✅ GATE: Aprobación requerida para avanzar a PHASE 5**

---

#### **PHASE 5: Implement**
```
Code → Test → Complete
```
1. **Code**: Desarrollo de la feature
   - Implementar tareas según plan
   - Actualizar STATUS.md diariamente
   - Marcar tareas completadas en plan.md y tasks.json

2. **Test**: Validación y testing
   - Unit tests
   - Integration tests
   - E2E tests
   - Code review

3. **Complete**: Finalización
   - Todas las tareas ✅
   - Tests pasando
   - Crear IMPLEMENTED.md
   - Mover de `wip/` a `features/XX-feature-name/`

---

### 1. Inicio de Feature Nueva

```bash
# 1. Crear estructura en wip/
specs/wip/
└── nueva-feature/
    ├── functional-spec.md
    ├── technical-spec.md
    ├── plan.md
    ├── tasks.json
    └── STATUS.md

# 2. Escribir especificación funcional en functional-spec.md
# 3. Escribir especificación técnica en technical-spec.md
# 4. Crear plan de tareas en plan.md
# 5. Generar tasks.json desde plan.md (o crear manualmente)
# 6. Inicializar STATUS.md con template
# 7. Actualizar STATUS.md general en specs/STATUS.md
```

### 2. Durante el Desarrollo

- **Actualizar `STATUS.md` del feature DIARIAMENTE**
- **Actualizar `STATUS.md` general SEMANALMENTE**
- **Actualizar `tasks.json`** cuando cambien estados de tareas
- Marcar tareas completadas con `[x]` en plan.md
- Documentar blockers inmediatamente
- Agregar notas de decisiones importantes

### 3. Completar Feature

```bash
# 1. Verificar que todas las tareas estén completadas
# 2. Crear IMPLEMENTED.md con resumen
# 3. Mover de wip/ a features/ con número secuencial
mv wip/nueva-feature/ features/0X-nueva-feature/

# 4. Eliminar STATUS.md del feature (ya no se necesita)
rm features/05-nueva-feature/STATUS.md

# 5. Actualizar PROJECT_INFO.md con la nueva feature
# 6. Actualizar STATUS.md general (mover de WIP a Completados)
```

---

## 📋 Templates

### Template: tasks.json

```json
{
  "feature": "feature-name",
  "version": "1.0.0",
  "lastUpdated": "YYYY-MM-DDTHH:MM:SSZ",
  "summary": {
    "total": 30,
    "completed": 15,
    "inProgress": 3,
    "blocked": 1,
    "pending": 11
  },
  "phases": [
    {
      "id": "phase-1",
      "name": "Foundation & Domain Models",
      "order": 1,
      "status": "completed",
      "tasks": [
        {
          "id": "T001",
          "title": "Crear entidad de dominio Entity",
          "description": "Crear modelo de dominio con atributos principales",
          "phase": "phase-1",
          "status": "completed",
          "priority": "high",
          "estimatedHours": 2,
          "actualHours": 1.5,
          "assignedTo": "@developer",
          "dependencies": [],
          "tags": ["domain", "model"],
          "completedAt": "YYYY-MM-DDTHH:MM:SSZ"
        },
        {
          "id": "T002",
          "title": "Crear excepciones del dominio",
          "description": "EntityNotFoundException, DuplicateException, etc.",
          "phase": "phase-1",
          "status": "completed",
          "priority": "high",
          "estimatedHours": 1,
          "actualHours": 1,
          "assignedTo": "@developer",
          "dependencies": ["T001"],
          "tags": ["domain", "exception"],
          "completedAt": "YYYY-MM-DDTHH:MM:SSZ"
        }
      ]
    },
    {
      "id": "phase-2",
      "name": "Database Schema",
      "order": 2,
      "status": "in-progress",
      "tasks": [
        {
          "id": "T006",
          "title": "Repository tests con herramienta",
          "description": "Tests de integración para el repository",
          "phase": "phase-2",
          "status": "in-progress",
          "priority": "medium",
          "estimatedHours": 4,
          "actualHours": 2,
          "assignedTo": "@developer",
          "dependencies": ["T004", "T005"],
          "tags": ["test", "integration"],
          "startedAt": "YYYY-MM-DDTHH:MM:SSZ",
          "progress": 70
        },
        {
          "id": "T007",
          "title": "Implementar Use Cases",
          "description": "CreateEntityUseCase, etc.",
          "phase": "phase-2",
          "status": "blocked",
          "priority": "high",
          "estimatedHours": 6,
          "assignedTo": "@developer",
          "dependencies": ["T006"],
          "tags": ["application", "use-case"],
          "blocker": {
            "reason": "Esperando completar T006",
            "since": "YYYY-MM-DD"
          }
        }
      ]
    }
  ]
}
```

### Template: STATUS.md (para WIP)

Ver ejemplo completo arriba. Secciones clave:
- Estado general con emoji visual (🔴🟡🟢⚪)
- Progreso por fase
- Tareas actuales
- Blockers activos
- Notas y decisiones
- Próximos pasos

### Template: IMPLEMENTED.md (para features/)

```markdown
# Implementation Summary - Feature Name

**Fecha implementación**: YYYY-MM-DD  
**Developer(s)**: @developer  
**Versión**: X.Y.Z

---

## ✅ Completado

### Funcionalidades Implementadas
- Feature 1: Descripción
- Feature 2: Descripción

### Endpoints Creados
- POST /api/resource
- GET /api/resource/{id}
- PUT /api/resource/{id}
- DELETE /api/resource/{id}

---

## 📊 Métricas

- **Tests**: XX% coverage
- **Tareas completadas**: XX/XX
- **Tiempo desarrollo**: XX días
- **LOC**: ~X,XXX líneas

---

## 🔄 Cambios vs Plan Original

### Cambios realizados:
1. Se cambió X por Y debido a Z
2. Se agregó feature adicional W

### Scope reductions:
1. Feature Q postponed para v2.0

---

## 📝 Lecciones Aprendidas

1. [Lección aprendida 1]
2. [Lección aprendida 2]
3. [Lección aprendida 3]

---

##  Documentación Adicional
- Especificación Funcional: `functional-spec.md` (siempre)
- Especificación Técnica: `technical-spec.md` (siempre)
- [API Documentation](http://localhost:PORT/api-docs)
- [Postman Collection](../postman/feature-name.json)
```

---

## 🎨 Convenciones de Nombres

### Features
- Usar kebab-case: `user-auth`, `product-catalog`
- Número secuencial solo en `features/`: `01-user-auth`
- No usar números en `wip/`: solo `product-catalog`

### Archivos
- Especificación Funcional: `functional-spec.md` (siempre)
- Especificación Técnica: `technical-spec.md` (siempre)
- Plan: `plan.md` (siempre)
- Tareas JSON: `tasks.json` (siempre)
- Estado WIP: `STATUS.md` (solo en wip/)
- Implementación: `IMPLEMENTED.md` (solo en features/)
- Estado General: `STATUS.md` (raíz de specs/)

---

## 💡 Mejores Prácticas

### ✅ DO
- Actualizar `STATUS.md` diariamente cuando está en WIP
- Mover a `features/` solo cuando esté 100% completo
- Documentar decisiones importantes en STATUS notes
- Usar emojis para visualización rápida del estado
- Mantener `PROJECT_INFO.md` actualizado

### ❌ DON'T
- No crear carpetas vacías "por si acaso"
- No duplicar información entre archivos
- No dejar features en WIP más de 2 sprints
- No mover a features/ si quedan TODOs
- No olvidar actualizar el número secuencial

---

## 🔍 Búsqueda Rápida

### ¿Qué features están implementados?
```bash
ls specs/features/
```

### ¿Qué se está desarrollando ahora?
```bash
ls specs/wip/
cat specs/wip/*/STATUS.md
```

### ¿Cuál es el próximo número de feature?
```bash
ls specs/features/ | tail -1  # Ver último número
```

---

## 📞 Soporte

Para preguntas sobre este framework:
- Tech Lead: @lead
- Documentación: Este README.md
- Ejemplos: Ver features implementados en `features/`