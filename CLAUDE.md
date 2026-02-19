# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Comandos esenciales

```bash
# Compilar
mvn clean compile

# Ejecutar todos los tests
mvn test

# Tests con reporte de cobertura
mvn clean test jacoco:report

# Compilar y ejecutar la aplicación
mvn spring-boot:run

# Build completo
mvn clean install
```

Para ejecutar un único test de clase:
```bash
mvn test -Dtest=NombreDeClaseTest
```

Para ejecutar un único método de test:
```bash
mvn test -Dtest=NombreDeClaseTest#nombreDelMetodo
```

## Arquitectura Hexagonal

El proyecto implementa **Hexagonal Architecture (Ports & Adapters)** organizada por feature en cada capa. Dependencias unidireccionales: `infrastructure → application → domain`.

```
domain/         → Lógica de negocio pura, sin dependencias externas
  model/        → Entidades de dominio (Lombok: @Getter @Setter @Builder, NO @Data en JPA)
  port/         → Interfaces de repositorio (puertos de salida del dominio)
  service/      → DomainService (reglas de negocio) + Validator (validación estructural de dominio)
  exception/    → Excepciones específicas por feature

application/
  port/         → Interfaces de casos de uso (ManageXxxUseCase, CompareXxxUseCase)
  usecase/      → Implementaciones de casos de uso
  command/      → Comandos de entrada hacia el dominio (donde aplique)

infrastructure/
  config/       → BeanConfiguration.java (wiring manual de beans de dominio)
  in/web/       → Controllers, DTOs (request/response), Mappers DTO↔Domain, Filters, Advice
  out/persistence/ → Adapters, JPA Entities, Entity Mappers, JpaRepositories, SpecificationUtils
```

### Patrón por feature

Cada feature replica esta estructura en todas las capas. Por ejemplo, el feature `unitofmeasure` tiene:
- `domain/model/unitofmeasure/UnitOfMeasure.java`
- `domain/service/unitofmeasure/UnitOfMeasureDomainService.java` + `UnitOfMeasureValidator.java` + `UnitOfMeasureValidationService.java`
- `domain/port/unitofmeasure/UnitOfMeasureRepository.java`
- `application/port/unitofmeasure/ManageUnitOfMeasureUseCase.java`
- `application/usecase/unitofmeasure/ManageUnitOfMeasureUseCaseImpl.java`
- `infrastructure/in/web/controller/unitofmeasure/UnitOfMeasureController.java`
- `infrastructure/out/persistence/adapter/unitofmeasure/UnitOfMeasureRepositoryAdapter.java`
- Y sus entidades, mappers (MapStruct), y SpecificationUtil para filtros.

### Wiring de beans

Los beans de dominio (`DomainService`, `Validator`) **no usan `@Component`**. Se declaran manualmente en `BeanConfiguration.java`. Los adaptadores de infraestructura sí usan `@Component`/`@RequiredArgsConstructor`.

### Filtros y paginación

Los listados usan `JPA Specification` para filtros dinámicos. Cada feature tiene un `[Feature]SpecificationUtil` en `infrastructure/out/persistence/util/[feature]/`.

## Reglas críticas del proyecto

La fuente de verdad es **`specs/RULES.md`**. Antes de implementar cualquier feature, revisar ese archivo y los specs en `specs/wip/[feature]/`.

Reglas clave:

- **Validaciones de negocio** van en `domain/service`, no en controllers ni adapters.
- **Validaciones estructurales** (formato, nulos, longitud) van en DTOs con Jakarta Validation.
- **Nunca** exponer entidades JPA en controllers; siempre usar DTOs + MapStruct.
- **Tipos inmutables** → `record`; **tipos mutables** → Lombok específico (no `@Data` en JPA entities).
- **Comentarios**: solo se permiten los que explican el "por qué", no el "qué". No comentarios decorativos ni código comentado. `TODO` solo con formato `// TODO [ISSUE-123]: descripción`.
- **Logging**: usar `@Slf4j` en infraestructura y casos de uso. Dominio y Application no se acoplan a logging concreto. Nunca `System.out.println`.
- **Excepciones**: específicas por dominio/feature; se mapean a HTTP en `GlobalExceptionHandler`.
- **Cambios de BD**: siempre vía Flyway (`src/main/resources/db/migration/`). Nunca modificar migraciones ya aplicadas. Sincronizar con `docker/mysql-init/` al cerrar un feature.
- **Recursos REST** en plural. Usar `ResponseEntity` para control explícito de status.
- **No implementar** nada que no esté especificado en `specs/`.

## Testing

- Los tests usan **H2 en memoria** (perfil `test`). Flyway está deshabilitado en tests; el esquema se crea con `ddl-auto=create-drop`.
- Cobertura mínima: **90% total**; **100% en archivos modificados** (validado por GitHub Actions en PRs a `main`).
- Tests organizados por capa y feature, espejando la estructura `src/main`.
- Usar `@DisplayName` con descripciones claras en español o inglés.

## Configuración local

- **BD producción**: MySQL en `localhost:3306`, base de datos `cs_solutions_erp_lite`.
- **BD test**: H2 en memoria con `MODE=MySQL`.
- Credenciales por defecto en `application.properties` (sobreescribir para entornos reales).
- Swagger UI disponible en `http://localhost:8080/swagger-ui.html`.

## Documentación del proyecto

- `specs/RULES.md` — Reglas operativas (máxima precedencia).
- `specs/STATUS.md` — Estado de todos los features.
- `specs/wip/[feature]/` — Specs activas (ver estándar de nombrado abajo).
- `specs/features/[feature]/` — Features completados.
- `specs/scaffolding.md` — Estructura canónica de paquetes.

### Estándar de nombrado de specs

**Directorio del feature:** `specs/wip/NN-feature-name/` — prefijo numérico de dos dígitos (`01`, `02`, …) y nombre en kebab-case.

**Archivos del directorio (exactamente estos cuatro):**
```
1-functional-spec.md   → Especificación funcional
2-technical-spec.md    → Especificación técnica
3-tasks.json           → Listado de tareas
STATUS.md              → Estado del feature
```

Reglas:
- Los archivos numerados llevan prefijo `N-` seguido de guion, sin excepción.
- `STATUS.md` siempre en **MAYÚSCULAS**.
- No se crean archivos extra (plan, notas, etc.) dentro del directorio de specs.

## Flujo obligatorio antes de cerrar una tarea

1. Revisar `specs/RULES.md` y specs del feature.
2. Ejecutar validación técnica (tests + build).
3. Si hay cambios de BD: verificar sincronía entre Flyway y `docker/mysql-init/`.
4. Ejecutar auditoría final contra `specs/RULES.md` y specs del feature.
