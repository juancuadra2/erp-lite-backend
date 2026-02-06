# Framework SDD - Fundamentos y Reglas

Esta carpeta contiene todos los fundamentos, reglas, plantillas y especificaciones del Framework SDD (Sistema de Documentación de Desarrollo). Es un framework genérico y reutilizable, no atado a ningún proyecto específico.

## 📋 Contenido

### Documentación del Framework

- **[proyecto-framework-sdd.md](proyecto-framework-sdd.md)** - Especificación completa del framework SDD
  - Estructura y organización de la documentación
  - Principios de simplicidad, trazabilidad y flujo
  - Descripción de carpetas y archivos
  - Workflow de desarrollo
  - Ejemplos y mejores prácticas

### Estándares y Reglas

- **[STANDARD_ERROR_FORMAT.md](STANDARD_ERROR_FORMAT.md)** - Formato estándar de errores API
  - Códigos de error estándar para todos los módulos
  - Formato JSON obligatorio para respuestas de error
  - Mapeo de errores a códigos HTTP
  - Ejemplos de implementación

### Plantillas (templates/)

- **[plan-template.md](templates/plan-template.md)** - Plantilla para planes de implementación
  - Estructura estándar de fases y tareas
  - Secciones obligatorias y opcionales
  - Formato de estimaciones y dependencias

- **[spec-template.md](templates/spec-template.md)** - Plantilla para especificaciones funcionales/técnicas
  - User stories y escenarios de aceptación
  - Requisitos funcionales y no funcionales
  - Arquitectura y diseño técnico

## 🎯 Propósito

Este framework sirve como:

1. **Guía de Documentación**: Define cómo documentar features de cualquier proyecto
2. **Estándar de Calidad**: Establece reglas y formatos obligatorios
3. **Plantillas Reutilizables**: Proporciona templates para acelerar la creación de specs
4. **Contexto para IA**: Sirve como referencia para la generación automática de especificaciones
5. **Single Source of Truth**: Fuente única de verdad para reglas de documentación
6. **Framework Reutilizable**: Puede aplicarse a cualquier proyecto de software

## 📚 Uso

### Para Crear un Nuevo Feature

1. **Revisar**: [proyecto-framework-sdd.md](proyecto-framework-sdd.md) para entender la estructura
2. **Usar plantillas**: Copiar templates desde `templates/` como punto de partida
3. **Seguir estándares**: Aplicar las reglas definidas en cada documento
4. **Validar formato de errores**: Usar [STANDARD_ERROR_FORMAT.md](STANDARD_ERROR_FORMAT.md) para APIs

### Para Mantener el Framework

- Actualizaciones al framework deben ser revisadas y aprobadas
- Cambios importantes requieren actualizar esta documentación
- Mantener consistencia con features ya implementados

## 🔄 Estructura del Proyecto

```
specs/
├── framework/                       # Esta carpeta - Fundamentos genéricos
│   ├── README.md                   # Este archivo
│   ├── proyecto-framework-sdd.md   # Especificación del framework
│   ├── STANDARD_ERROR_FORMAT.md    # Estándar de errores (para APIs REST)
│   └── templates/                  # Plantillas reutilizables
│       ├── plan-template.md
│       └── spec-template.md
│
├── features/                        # Features implementados del proyecto
│   └── XX-feature-name/
│
└── wip/                            # Features en desarrollo del proyecto
    └── feature-name/
```

## 🔗 Referencias Relativas al Proyecto

Una vez implementado en un proyecto específico, estos archivos estarán disponibles:

- [README del Proyecto](../../README.md) - Documentación específica del proyecto actual
- [Estado General del Proyecto](../STATUS.md) - Estado de features del proyecto
- [Información del Proyecto](../PROJECT_INFO.md) - Detalles del proyecto actual

---

**Última actualización**: Febrero 2026
