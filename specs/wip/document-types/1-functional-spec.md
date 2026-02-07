# Especificación Funcional: Módulo de Tipos de Documento

**Creado:** 10 de enero de 2026  
**Actualizado:** 6 de febrero de 2026  
**Tipo de Módulo:** Catálogo Independiente (Sin dependencias)  
**Número de Funcionalidad:** 01

---

## Resumen General

Módulo para gestionar tipos de documentos de identificación (NIT, CC, CE, Pasaporte, etc.) utilizados para identificar personas naturales y jurídicas en el sistema. Actúa como un catálogo base independiente que proporciona información de tipos de documento a otros módulos.

### Objetivos de Negocio
- Mantener un catálogo estandarizado de tipos de documentos de identificación
- Soportar tipos de documento colombianos inicialmente
- Permitir validación e identificación adecuada de contactos, usuarios y empresas
- Proporcionar una base extensible para países adicionales

### Usuarios Objetivo
- Administradores del sistema que gestionan el catálogo de tipos de documento
- Otros módulos del sistema que consumen datos de tipos de documento (Contacto, Usuario, Empresa)

---

## Historias de Usuario y Escenarios de Aceptación

### Historia de Usuario 1 - Gestión de Tipos de Documento (Prioridad: P1)

**Como** administrador del sistema  
**Necesito** gestionar los tipos de documentos de identificación aceptados en el sistema  
**Para que** pueda validar correctamente la información de contactos y usuarios

#### Escenarios de Aceptación

**Escenario 1.1: Crear tipo de documento con validaciones**
- **Dado** que estoy autenticado como administrador
- **Cuando** envío POST /api/document-types con:
  ```json
  {
    "code": "NIT",
    "name": "Número de Identificación Tributaria",
    "description": "Documento de identificación para empresas en Colombia"
  }
  ```
- **Entonces** recibo **201 Created** con el tipo creado
- **Y** la respuesta incluye: id, uuid, code, name, description, active=true, timestamps

**Escenario 1.2: Validación de código único**
- **Dado** que existe un tipo de documento con código "NIT"
- **Cuando** intento crear otro con código "NIT"
- **Entonces** recibo **409 Conflict** con mensaje de error

**Escenario 1.3: Validación de datos de entrada**
- **Dado** que estoy autenticado como administrador
- **Cuando** envío POST con datos inválidos (código muy corto, nombre vacío, descripción muy larga)
- **Entonces** recibo **400 Bad Request** con errores de validación

**Escenario 1.4: Consultar tipo de documento inexistente**
- **Dado** que no existe ningún tipo de documento con el uuid dado
- **Cuando** envío GET /api/document-types/{uuid}
- **Entonces** recibo **404 Not Found**

**Escenario 1.5: Actualizar tipo de documento exitosamente**
- **Dado** que existe un tipo de documento
- **Cuando** envío PUT /api/document-types/{uuid} con datos actualizados
- **Entonces** recibo **200 OK** con el tipo actualizado
- **Y** se registran updatedBy y updatedAt

**Escenario 1.6: Desactivar tipo de documento**
- **Dado** que existe un tipo de documento activo
- **Cuando** envío PATCH /api/document-types/{uuid}/deactivate
- **Entonces** recibo **200 OK** con active=false

**Escenario 1.7: Eliminar tipo de documento (eliminación lógica)**
- **Dado** que existe un tipo de documento
- **Cuando** envío DELETE /api/document-types/{uuid}
- **Entonces** recibo **204 No Content**
- **Y** el registro tiene deletedAt poblado
- **Y** se registra deletedBy

**Escenario 1.8: Listar tipos activos con filtros avanzados**
- **Dado** que existen 5 tipos activos y 2 tipos inactivos
- **Cuando** envío GET /api/document-types con parámetros de consulta:
  - enabled=true
  - search=Cedula
  - page=0, limit=10
  - fields=id,uuid,code,name,active
  - sort.field=name, sort.order=ASC
  - populate=createdBy,updatedBy
- **Entonces** recibo **200 OK** con tipos coincidentes, ordenados por nombre ascendente
- **Y** solo se incluyen los campos especificados
- **Y** se pueblan las relaciones createdBy y updatedBy
- **Y** se incluyen metadatos de paginación

**Escenario 1.9: Búsqueda global**
- **Dado** que existen tipos de documento con nombres "Cédula de Ciudadanía", "Número de Identificación Tributaria"
- **Cuando** envío GET /api/document-types?search=Cedula
- **Entonces** recibo **200 OK** con tipos que contienen "Cedula" en code, name o description

**Escenario 1.10: Ordenamiento y paginación**
- **Dado** que existen 15 tipos de documento
- **Cuando** envío GET /api/document-types?page=1&limit=5&sort.field=code&sort.order=DESC
- **Entonces** recibo **200 OK** con 5 tipos de la segunda página, ordenados por código descendente
- **Y** la respuesta incluye metadatos de paginación

**Escenario 1.11: Límite de paginación excedido**
- **Dado** que existe el endpoint de listado
- **Cuando** envío GET /api/document-types?limit=150
- **Entonces** recibo **400 Bad Request** con "Limit must not exceed 100"

---

### Historia de Usuario 2 - Datos Iniciales de Colombia (Prioridad: P1)

**Como** administrador del sistema  
**Necesito** que el sistema incluya tipos de documentos colombianos precargados  
**Para que** pueda empezar a usarlos inmediatamente

#### Escenarios de Aceptación

**Escenario 2.1: Tipos de documento precargados**
- **Dado** que el sistema ejecuta la migración V1.4__insert_colombia_document_types.sql
- **Entonces** se cargan los siguientes tipos:
  - NIT - Número de Identificación Tributaria
  - CC - Cédula de Ciudadanía
  - CE - Cédula de Extranjería
  - PA - Pasaporte
  - TI - Tarjeta de Identidad
  - RC - Registro Civil

**Escenario 2.2: Consultar tipos precargados**
- **Dado** que se han ejecutado las migraciones
- **Cuando** envío GET /api/document-types
- **Entonces** recibo **200 OK** con los 6 tipos precargados

---

## Requisitos Funcionales

### Funcionalidad Principal
- **RF-001**: El sistema DEBE permitir crear, leer, actualizar y desactivar tipos de documento
- **RF-002**: El sistema DEBE validar la unicidad del código
- **RF-003**: El sistema DEBE filtrar tipos por estado activo/inactivo

### Filtrado Avanzado
- **RF-010**: El sistema DEBE soportar filtros avanzados en los listados:
  - **enabled**: Filtro booleano para estado activo/inactivo
  - **search**: Búsqueda global en código, nombre y descripción (insensible a mayúsculas)
  - **page**: Número de página (basado en 0)
  - **limit**: Resultados por página (predeterminado: 10, máximo: 100)
  - **fields**: Selección de campos para la respuesta
  - **sort.field**: Campo por el cual ordenar (code, name, createdAt, etc.)
  - **sort.order**: Orden de clasificación (ASC o DESC)
  - **populate**: Relaciones a poblar (createdBy, updatedBy, deletedBy)
  - **filters**: Filtros dinámicos adicionales (p.ej., country, type)

### Validación de Datos
- **RF-004**: El código DEBE ser alfanumérico, de 2-10 caracteres, único
- **RF-005**: El nombre DEBE ser requerido, de 1-200 caracteres
- **RF-006**: La descripción DEBE ser opcional, máximo 500 caracteres

### Auditoría
- **RF-008**: El sistema DEBE auditar operaciones con:
  - createdBy, updatedBy, deletedBy (IDs de usuario)
  - createdAt, updatedAt, deletedAt (marcas de tiempo)

### Formato de Respuesta
- **RF-009**: Las respuestas DEBEN incluir paginación y metadatos para listados:
  - totalElements, totalPages, currentPage, pageSize
- **RF-011**: El sistema DEBE permitir población opcional de relaciones de auditoría

---

## Reglas de Negocio

### Gestión de Códigos
- **RN-001**: Los códigos de documento DEBEN normalizarse a mayúsculas antes del almacenamiento
- **RN-002**: Los códigos de documento DEBEN ser únicos en registros activos y eliminados
- **RN-003**: La validación de unicidad de código DEBE ser insensible a mayúsculas

### Eliminación Lógica
- **RN-004**: Las operaciones de eliminación DEBEN ser eliminaciones lógicas (establecer marca de tiempo deletedAt)
- **RN-005**: Los registros eliminados lógicamente NO DEBEN aparecer en consultas predeterminadas
- **RN-006**: Los registros eliminados lógicamente DEBEN mantener la integridad de datos históricos

### Gestión de Estado
- **RN-007**: Los tipos de documento recién creados DEBEN estar activos por defecto
- **RN-008**: La desactivación DEBE preservar todos los datos y relaciones
- **RN-009**: Solo los tipos de documento activos DEBERÍAN estar disponibles para selección

### Integridad de Datos
- **RN-010**: El tipo de documento no puede eliminarse permanentemente si está referenciado por otros módulos
- **RN-011**: Las actualizaciones DEBEN preservar la unicidad del código

---

## Puntos de Integración

### Módulos Consumidores
Este módulo proporciona datos a:
- **Módulo de Contacto**: Usa DocumentType para validación de taxId de clientes y proveedores
- **Módulo de Usuario**: Puede usar DocumentType para identificación personal
- **Módulo de Empresa**: Usa DocumentType (típicamente NIT) para identificación fiscal

### Contrato de API
Todos los módulos consumidores deberán:
1. Llamar a GET /api/document-types para recuperar tipos disponibles
2. Almacenar referencia de ID o UUID de tipo de documento
3. Validar tipo de documento seleccionado por usuario contra tipos activos

---

## Fuera de Alcance

1. ❌ Verificación en línea con entidades gubernamentales (DIAN, Registraduría)
2. ❌ OCR de documentos físicos
3. ❌ Validación de vigencia/vencimiento de documentos
4. ❌ Validación de formato de número de documento
5. ❌ Cálculo de dígito de verificación
6. ❌ Soporte multiidioma para nombres
7. ❌ Versionado de tipos de documento
8. ❌ Reglas de validación personalizadas por tipo de documento

---

## Criterios de Éxito

### Completitud Funcional
- ✅ Operaciones CRUD completas implementadas y verificadas
- ✅ Datos iniciales colombianos cargados vía Flyway
- ✅ Todos los filtros avanzados funcionando correctamente
- ✅ Eliminación lógica funcionando correctamente
- ✅ Pista de auditoría completa para todas las operaciones

### Métricas de Calidad
- **Cobertura de Pruebas**: > 80% en capas de dominio y aplicación
- **Documentación de API**: Documentación completa de Swagger/OpenAPI
- **Manejo de Errores**: Todos los escenarios de error manejados apropiadamente con códigos HTTP adecuados

### Benchmarks de Rendimiento
- **Endpoint de Listado**: < 100ms p95
- **Operaciones CRUD**: < 150ms p95

---

## Supuestos

1. **SUPUESTO**: Solo se implementan tipos de documento colombianos inicialmente
2. **SUPUESTO**: Otros países se agregarán bajo demanda
3. **SUPUESTO**: La validación de formato de número de documento NO se realiza en este módulo
4. **SUPUESTO**: La validación de formato de documento se implementará en módulos consumidores si se requiere

---

## Preguntas Abiertas

1. **P**: ¿Se requieren validaciones de formato específicas por tipo de documento?
   - **PENDIENTE**: Se implementará en módulos consumidores si es necesario

2. **P**: ¿Debemos permitir eliminación permanente de tipos de documento?
   - **DECISIÓN**: Solo se permite eliminación lógica para preservar integridad de datos

3. **P**: ¿Se necesita soporte multiidioma para nombres de tipos de documento?
   - **DECISIÓN**: No en la versión inicial, se puede agregar después

---

## Notas y Casos Especiales

### Casos Especiales

**Caso 1: Códigos duplicados**
- **Problema**: Prevenir duplicación de códigos
- **Solución**: Restricción UNIQUE en columna code + validación en servicio de dominio

**Caso 2: Eliminación lógica**
- **Problema**: Los tipos eliminados deben permanecer en el sistema para datos históricos
- **Solución**: Marca de tiempo deletedAt con cláusula @Where en entidad JPA

**Caso 3: Normalización de código**
- **Problema**: Asegurar consistencia en códigos
- **Solución**: Normalizar a mayúsculas antes de guardar en servicio de dominio

**Caso 4: Filtrado Activo/Inactivo**
- **Problema**: Los usuarios típicamente solo quieren tipos activos
- **Solución**: Proporcionar filtro enabled con predeterminado mostrando todos (dejar que la UI decida)

**Caso 5: Límites de paginación**
- **Problema**: Prevenir recuperación excesiva de datos
- **Solución**: Límite máximo de 100 registros por página con validación

---

## Documentación Relacionada

- Especificación Técnica: [technical-spec.md](2-technical-spec.md)
- Plan de Implementación: [plan.md](3-plan.md)
- Seguimiento de Tareas: [tasks.json](4-tasks.json)
- Estado: [STATUS.md](status.md)
- README Principal: [../../README.md](../../README.md)
- Referencia de Scaffolding: [../../scaffolding.md](../../scaffolding.md)

