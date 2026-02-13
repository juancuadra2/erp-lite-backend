# Rules for chat
Responde siempre en español.

# Rules for Specifications (SOURCE OF TRUTH)
NUNCA implementes nada que no esté especificado en los documentos del proyecto.
La fuente de la verdad son los specs funcionales, técnicos y tasks en la carpeta /specs/.
Antes de implementar, consulta:
  - specs/PROJECT_INFO.md - Información general del proyecto y features
  - specs/features/[feature]/ - Especificaciones funcionales detalladas del feature
  - specs/wip/[feature]/ - Features en trabajo activo
  - specs/scaffolding.md - Estructura de paquetes y convenciones arquitectónicas
Si algo no está especificado, pregunta al usuario antes de implementar.
No asumas requisitos, comportamientos o decisiones de diseño no documentadas.

# Rules for code
Siempre sigue las buenas prácticas de programación.
Sigue estrictamente las convenciones de nomenclatura del proyecto.
Aplica los principios SOLID en el diseño de clases y métodos.
Asegúrate de que el código sea limpio, legible y fácil de mantener.

# Rules for Lombok Usage (PRIORITY)
Usa Lombok para reducir código boilerplate en todas las clases.
Anotaciones obligatorias según tipo de clase:
  - Entidades JPA: @Entity, @Table, @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor, @Builder
  - DTOs: @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor, @Builder
  - Modelos de dominio: @Getter, @AllArgsConstructor, @Builder (inmutables, sin @Setter)
  - Servicios/Controllers: @RequiredArgsConstructor para inyección por constructor
Usa @Slf4j para logging automático en lugar de declarar logger manualmente.
Evita @Data (usa combinaciones específicas de @Getter/@Setter según necesidad).
Usa @Builder para construcción fluida de objetos complejos.
No uses @ToString ni @EqualsAndHashCode en entidades JPA (puede causar lazy loading issues).

# Rules for Code Documentation
No agregues comentarios explicativos dentro de métodos (el código debe ser autoexplicativo).
Usa JavaDoc solo en interfaces públicas (ports, casos de uso) para describir contratos.
No documentes lo obvio ("// suma dos números").
Los comentarios TODO/FIXME son aceptables temporalmente con issue tracker asociado.

# Rules for Architecture
Sigue la arquitectura hexagonal (Ports and Adapters) para organizar el código.
Sigue la sugerencia en el archivo scaffolding.md para la estructura de paquetes y clases.

# Rules for Feature Organization
Cada feature debe tener su propia carpeta en TODAS las capas.
Estructura obligatoria:
  - domain/model/[feature]/
  - domain/service/[feature]/
  - domain/exception/[feature]/
  - application/port/in/[feature]/
  - application/usecase/[feature]/
  - infrastructure/in/web/controller/[feature]/
  - infrastructure/in/web/dto/[feature]/
  - infrastructure/out/persistence/entity/[feature]/
No mezcles código de diferentes features en la misma clase.
Los elementos compartidos van en domain/shared/ o application/shared/.

# Rules for Naming Conventions
Clases:
  - Entidades JPA: [Feature]Entity (ej: DocumentTypeEntity)
  - Modelos de dominio: [Feature] (ej: DocumentType) sin sufijo
  - DTOs: [Feature][Tipo]Dto (ej: DocumentTypeResponseDto)
  - Servicios: [Feature]DomainService, [Feature]UseCaseImpl
  - Repositorios: [Feature]Repository (puerto), [Feature]RepositoryAdapter (implementación)
  - Controllers: [Feature]Controller
Paquetes: siempre en snake-case con guiones (ej: document-types)
Variables: camelCase descriptivo (ej: documentTypeList, isEnabled)
Constantes: UPPER_SNAKE_CASE (ej: MAX_RETRY_ATTEMPTS)

# Rules for Testing
Implementa tests unitarios para todos los casos de uso (mínimo 100% coverage).
Usa @DisplayName descriptivo en cada test para claridad.
Estructura los tests con patrón AAA (Arrange-Act-Assert).
Mockea las dependencias externas usando Mockito.
Nombra los tests siguiendo el patrón: should[ExpectedBehavior]When[StateUnderTest].
Evita lógica compleja dentro de los tests.

# Rules for Exception Handling
Crea excepciones de dominio específicas por feature (hereda de RuntimeException).
Nomenclatura: [Feature][Concepto]Exception (ej: DocumentTypeNotFoundException).
Las excepciones de dominio van en domain/exception/[feature]/.
Usa @RestControllerAdvice centralizado para traducir excepciones a respuestas HTTP.
No captures excepciones genéricas (Exception, RuntimeException) salvo casos muy específicos.
Incluye mensajes descriptivos y contextuales en las excepciones.

# Rules for Database Migrations
Toda modificación de esquema debe hacerse vía Flyway (archivos en db/migration/).
Nomenclatura: V[major].[minor]__[descripcion_snake_case].sql
Las migraciones deben ser idempotentes cuando sea posible.
Nunca modifiques una migración ya aplicada en producción.
Incluye datos de catálogo en la misma versión que crea las tablas.
Usa transacciones en las migraciones cuando sea posible.

# Rules for DTOs and Mapping
Usa MapStruct para mapeo entre entidades-dominio y DTOs-dominio.
Los DTOs deben estar en infrastructure/in/web/dto/[feature]/.
Sufijo obligatorio: RequestDto (entrada), ResponseDto (salida), Dto (compartido).
Los mappers de MapStruct deben usar @Mapper(componentModel = "spring").
No expongas entidades JPA directamente en los controllers.
Valida los DTOs de entrada con Jakarta Validation (@Valid, @NotNull, etc.).

# Rules for Validation
Validaciones de negocio van en los servicios de dominio (domain/service/[feature]/).
Validaciones estructurales van en DTOs con anotaciones Jakarta Validation.
Los validadores de dominio deben lanzar excepciones específicas del dominio.
No mezcles validaciones de infraestructura con validaciones de negocio.
Usa Optional para valores que pueden ser nulos en dominio.

# Rules for REST API
Usa plural para recursos REST (ej: /api/v1/document-types).
Verbos HTTP: GET (consulta), POST (crear), PUT (actualizar completo), PATCH (actualizar parcial), DELETE (eliminar).
Códigos HTTP: 200 (OK), 201 (Created con Location header), 204 (No Content), 400 (Bad Request), 404 (Not Found), 409 (Conflict).
Usa ResponseEntity<T> para control explícito de códigos HTTP.
Versionado en URL: /api/v1/, /api/v2/.
Documenta con anotaciones de SpringDoc (@Operation, @ApiResponse).

# Rules for Dependency Injection
Usa inyección por constructor (evita @Autowired en campos).
Marca las clases de servicio con @Service, @Component o @Repository según corresponda.
Los ports (interfaces) no llevan anotaciones Spring.
Registra beans manualmente en BeanConfiguration si requieren lógica de inicialización.
Prefiere interfaces sobre implementaciones en las dependencias.

# Rules for Logging
Usa SLF4J con Logback para todos los logs.
Niveles: ERROR para errores críticos, WARN para situaciones anormales, INFO para eventos importantes, DEBUG para detalles de depuración.
No uses System.out.println() ni printStackTrace().
Incluye contexto relevante en los mensajes (IDs, operación, usuario si aplica).
Los logs en capa de dominio deben ser excepcionales (solo validaciones críticas).

# Rules for Implementation Workflow
ANTES de implementar cualquier código:
  1. VALIDA que lo que vas a implementar esté especificado en los specs (funcionales, técnicos, tasks) - LA FUENTE DE LA VERDAD
  2. Lee y comprende TODAS estas reglas
  3. Identifica el feature y su ubicación en la estructura hexagonal
  4. Verifica convenciones de nomenclatura aplicables
  5. Planifica qué anotaciones de Lombok necesitarás

DURANTE la implementación:
  1. Aplica Lombok en todas las clases nuevas
  2. Sigue estrictamente la estructura de paquetes por feature
  3. Implementa validaciones apropiadas según la capa
  4. Mantén la separación de responsabilidades (ports, adapters, domain)

DESPUÉS de implementar:
  1. Revisa que se usen las anotaciones de Lombok correctas
  2. Verifica nomenclatura de clases, métodos y variables
  3. Confirma que las excepciones sean específicas del dominio
  4. Valida que los tests tengan @DisplayName descriptivos
  5. Asegura cobertura de tests mínima del 100%
  6. Verifica que no haya código comentado ni imports no usados
  7. Confirma que los logs usen SLF4J (@Slf4j)
  8. Valida que los DTOs usen MapStruct para mapeo

CUMPLIMIENTO:
Estas reglas deben cumplirse al 100%. No hay excepciones salvo justificación técnica crítica documentada.

# Rules for commits
No hagas commits sin antes preguntar.