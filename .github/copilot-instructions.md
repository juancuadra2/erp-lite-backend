# Reglas de IA para Desarrollo Java/Spring Boot
> Versión 3.0 - Optimizada para Copilot Instructions

---

## 🎯 Quick Reference

### Jerarquía de Decisiones
```
1. ¿Está en specs/? → SI: implementar | NO: PREGUNTAR
2. ¿Hay conflictos? → SI: REPORTAR | NO: continuar
3. ¿Está claro? → SI: implementar | NO: CLARIFICAR
4. ¿Es inmutable? → SI: RECORD | NO: LOMBOK
5. ¿Es de negocio? → SI: domain service | NO: validación estructural
```

### Nomenclatura Rápida
```
Feature folder (snake_case): document_types

Domain (sin Spring):
    - model/document_types:      DocumentType (Record), DocumentTypeId (Record)
    - service/document_types:    DocumentTypeDomainService
    - exception/document_types:  DocumentTypeNotFoundException

Application:
    - port/document_types:       CreateDocumentTypeUseCase (Port In)
                                                             DocumentTypeRepository (Port Out)
    - usecase/document_types:    CreateDocumentTypeUseCaseImpl (@Service)

Infrastructure:
    - in/web/controller/document_types:  DocumentTypeController (@RestController)
    - in/web/dto/document_types:         CreateDocumentTypeRequestDto (Lombok)
                                                                             DocumentTypeResponseDto (Record)
    - out/persistence/entity/document_types:      DocumentTypeEntity (@Entity + Lombok)
    - out/persistence/repository/document_types:  DocumentTypeJpaRepository
    - out/persistence/adapter/document_types:     DocumentTypeRepositoryAdapter (@Repository)
```

### Anotaciones por Tipo de Clase
```java
// INMUTABLES → RECORDS (NO Lombok - puede usar @Builder)
public record DocumentType(String id, String code, String name) {}

// ENTIDADES JPA → LOMBOK ESPECÍFICO
@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DocumentTypeEntity {}

// SERVICIOS APPLICATION → LOMBOK + SPRING
@Service @RequiredArgsConstructor @Slf4j
public class CreateDocumentTypeUseCaseImpl {}

// SERVICIOS DOMAIN → SOLO LOMBOK (NO Spring)
@RequiredArgsConstructor @Slf4j
public class DocumentTypeDomainService {}

// DTOS REQUEST → LOMBOK COMPLETO
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateDocumentTypeRequestDto {}
```

---

## Principios Fundamentales

### 🗣️ Lenguaje
**Responde siempre en español.**

### 💎 Código Limpio
- **SOLID** en diseño de clases
- **DRY** - No repetir código
- **Separación de responsabilidades** clara entre capas
- **Código autoexplicativo** - si necesitas comentario dentro de método, refactoriza
- **Nombres descriptivos** - no abreviaturas salvo convenciones (id, dto, url)

### 📚 Documentación
**PERMITIDO:**
- JavaDoc en interfaces públicas (ports, contratos)
- JavaDoc en métodos públicos complejos
- Comentarios explicando "por qué" en decisiones no obvias
- `// TODO [ISSUE-123]: descripción`
- Advertencias sobre edge cases

**PROHIBIDO:**
- Comentarios que parafrasean código
- JavaDoc en getters/setters triviales
- Código comentado (usa git)
- Comentarios obsoletos

---

## Source of Truth: /specs/

### ⚠️ REGLA CRÍTICA
**NUNCA implementes nada que no esté especificado en `/specs/`.**

### Jerarquía de Precedencia (Mayor → Menor)
```
1. specs/wip/[feature]/        ← MÁXIMA PRIORIDAD (trabajo activo)
2. specs/features/[feature]/   ← Specs funcionales detalladas
3. specs/PROJECT_INFO.md       ← Info general del proyecto
4. specs/scaffolding.md        ← Estructura y convenciones
```

### En Caso de Conflicto
1. **DETENER** inmediatamente
2. **REPORTAR** con referencias exactas a documentos
3. **ESPERAR** clarificación
4. **NO ASUMIR** cuál es correcta

### En Caso de Ambigüedad
1. **NO ASUMIR** comportamiento
2. **PREGUNTAR** con opciones claras
3. **DOCUMENTAR** decisión
4. **ACTUALIZAR** contexto mental

### Checklist Pre-Implementación
- [ ] ¿Está en specs/wip/ o specs/features/?
- [ ] ¿Hay conflictos entre specs?
- [ ] ¿Está todo claro o hay ambigüedad?
- [ ] ¿Revisé PROJECT_INFO.md y scaffolding.md?

---

## Records vs Lombok

### REGLA DE ORO
```
INMUTABLE → RECORD
MUTABLE → LOMBOK
```

### ✅ USA RECORDS Para

#### 1. Modelos de Dominio Inmutables
```java
public record DocumentType(
    DocumentTypeId id,
    String code,
    String name,
    boolean enabled
) {
    // Constructor compacto para validaciones
    public DocumentType {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code cannot be blank");
        }
    }
    
    // Métodos de negocio retornan nuevas instancias
    public DocumentType disable() {
        return new DocumentType(id, code, name, false);
    }
}
```

#### 2. Value Objects
```java
public record DocumentTypeId(String value) {
    public DocumentTypeId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("DocumentTypeId cannot be blank");
        }
    }
}
```

#### 3. DTOs de Respuesta
```java
public record DocumentTypeResponseDto(
    String id,
    String code,
    String name,
    boolean enabled
) {}
```

#### 4. Commands/Queries (CQRS)
```java
public record CreateDocumentTypeCommand(
    String code,
    String name,
    boolean enabled
) {
    public CreateDocumentTypeCommand {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code is required");
        }
    }
}
```

**Ventajas:**
- ✅ Inmutabilidad garantizada
- ✅ Equals/hashCode/toString automáticos
- ✅ Sintaxis más concisa
- ✅ Compatible con pattern matching
- ✅ Semántica clara: "dato", no "objeto con comportamiento"

---

### 🔧 USA LOMBOK Para

#### 1. Entidades JPA (Mutables por Naturaleza)
```java
@Entity
@Table(name = "document_types")
@Getter
@Setter
@NoArgsConstructor  // REQUERIDO por JPA
@AllArgsConstructor
@Builder
public class DocumentTypeEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 10)
    private String code;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(nullable = false)
    private Boolean enabled;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

**⚠️ CRÍTICO con JPA:**
- ❌ NO: `@Data` (causa lazy loading issues)
- ❌ NO: `@ToString` (lazy initialization exceptions)
- ❌ NO: `@EqualsAndHashCode` (usar solo ID)
- ✅ SI: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`

#### 2. DTOs de Request (Mutables para Binding)
```java
@Getter
@Setter
@NoArgsConstructor  // REQUERIDO para deserialización
@AllArgsConstructor
@Builder
public class CreateDocumentTypeRequestDto {
    
    @NotBlank(message = "Code is required")
    @Size(max = 10)
    @Pattern(regexp = "^[A-Z0-9_]+$")
    private String code;
    
    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;
    
    @NotNull
    private Boolean enabled;
}
```

#### 3. Servicios/Controllers (Inyección de Dependencias)
```java
@Service
@RequiredArgsConstructor  // Constructor con campos final
@Slf4j                    // Logger: log.info(), log.debug()
public class CreateDocumentTypeUseCaseImpl implements CreateDocumentTypeUseCase {
    
    private final DocumentTypeRepository repository;
    private final DocumentTypeDomainService domainService;
    
    @Override
    @Transactional
    public DocumentType execute(CreateDocumentTypeCommand command) {
        log.debug("Creating document type: {}", command.code());
        
        domainService.validateUniqueCode(command.code());
        DocumentType saved = repository.save(documentType);
        
        log.info("Created with id: {}", saved.id());
        return saved;
    }
}
```

### Reglas Generales Lombok
- ✅ `@Slf4j` siempre (nunca declares logger manual)
- ❌ Evita `@Data` (usa combinaciones específicas)
- ✅ `@Builder` para construcción fluida
- ✅ `@RequiredArgsConstructor` para DI (campos final)
- ✅ `@NoArgsConstructor(access = AccessLevel.PROTECTED)` en JPA

### ❌ NUNCA Uses Lombok En
- Clases inmutables → **Usa Records**
- Clases con validaciones en constructor → **Manual**
- Casos con lógica en setters → **Manual**

---

## Arquitectura Hexagonal

### Principios
1. **Dominio es el centro** - Sin dependencias externas
2. **Dependencias apuntan hacia adentro** - Infra → App → Domain
3. **Ports = Interfaces** - Contratos sin implementación
4. **Adapters = Implementaciones** - Conectan con tecnologías

### Estructura de Capas
```
src/main/java/com/tuapp/
├── domain/                    ← Sin dependencias Spring/Jakarta
│   ├── model/[feature]/      ← Records inmutables
│   ├── service/[feature]/    ← Lógica negocio (NO @Service)
│   └── exception/[feature]/  ← Excepciones específicas
│
├── application/               ← Orquestación
│   ├── port/[feature]/      ← Interfaces (UseCase y Repository)
│   └── usecase/[feature]/    ← Implementaciones
│
└── infrastructure/            ← Detalles técnicos
    ├── in/web/
    │   ├── controller/[feature]/
    │   └── dto/[feature]/
    └── out/persistence/
        ├── entity/[feature]/
        ├── repository/[feature]/
        └── adapter/[feature]/
```

### Reglas de Dependencia
- ✅ Infrastructure → Application → Domain
- ❌ Domain NO debe importar Application ni Infrastructure
- ❌ Domain NO debe tener anotaciones Spring (@Service, @Component, etc.)
- ✅ Domain puede usar Lombok técnico (@Slf4j, @RequiredArgsConstructor)
- ❌ Application NO debe importar Infrastructure
- ✅ Ports (interfaces) definen contratos
- ✅ Adapters implementan ports

---

## Organización por Feature

### ⚠️ REGLA OBLIGATORIA
**Cada feature tiene su propia carpeta en TODAS las capas.**

### Ejemplo Completo: "document_types"
```
src/main/java/com/tuapp/
├── domain/
│   ├── model/document_types/
│   │   ├── DocumentType.java                    ← Record
│   │   └── DocumentTypeId.java                  ← Record
│   ├── service/document_types/
│   │   └── DocumentTypeDomainService.java       ← NO @Service
│   └── exception/document_types/
│       ├── DocumentTypeNotFoundException.java
│       └── DuplicateDocumentTypeException.java
│
├── application/
│   ├── port/document_types/
│   │   ├── CreateDocumentTypeUseCase.java       ← Interface
│   │   ├── FindDocumentTypeUseCase.java
│   │   ├── UpdateDocumentTypeUseCase.java
│   │   ├── DeleteDocumentTypeUseCase.java
│   │   └── DocumentTypeRepository.java          ← Interface
│   └── usecase/document_types/
│       ├── CreateDocumentTypeUseCaseImpl.java   ← @Service
│       ├── FindDocumentTypeUseCaseImpl.java
│       ├── UpdateDocumentTypeUseCaseImpl.java
│       └── DeleteDocumentTypeUseCaseImpl.java
│
└── infrastructure/
    ├── in/web/
    │   ├── controller/document_types/
    │   │   └── DocumentTypeController.java      ← @RestController
    │   └── dto/document_types/
    │       ├── CreateDocumentTypeRequestDto.java ← Lombok
    │       ├── UpdateDocumentTypeRequestDto.java ← Lombok
    │       └── DocumentTypeResponseDto.java      ← Record
    └── out/persistence/
        ├── entity/document_types/
        │   └── DocumentTypeEntity.java           ← @Entity + Lombok
        ├── repository/document_types/
        │   └── DocumentTypeJpaRepository.java    ← Spring Data
        └── adapter/document_types/
            ├── DocumentTypeRepositoryAdapter.java ← @Repository
            └── DocumentTypeMapper.java            ← @Mapper
```

### Reglas
- ❌ NO mezclar features en misma clase
- ✅ Compartidos en `domain/shared/` o `application/shared/`
- ✅ Una clase = un feature (Single Responsibility)
- ✅ Paquetes en `snake_case`: `document_types`, `user_management`

---

## Convenciones de Nomenclatura

### Clases

| Tipo | Convención | Ejemplo |
|------|-----------|---------|
| **Domain** |
| Modelo de dominio | `[Feature]` | `DocumentType` |
| Value Object | `[Feature]` | `DocumentTypeId` |
| Servicio dominio | `[Feature]DomainService` | `DocumentTypeDomainService` |
| Excepción | `[Feature][Concepto]Exception` | `DocumentTypeNotFoundException` |
| **Application** |
| Caso de Uso (Port In) | `[Action][Feature]UseCase` | `CreateDocumentTypeUseCase` |
| Caso de Uso (Impl) | `[Action][Feature]UseCaseImpl` | `CreateDocumentTypeUseCaseImpl` |
| Repositorio (Port Out) | `[Feature]Repository` | `DocumentTypeRepository` |
| Command | `[Action][Feature]Command` | `CreateDocumentTypeCommand` |
| Query | `[Action][Feature]Query` | `FindDocumentTypeByIdQuery` |
| **Infrastructure** |
| Entidad JPA | `[Feature]Entity` | `DocumentTypeEntity` |
| DTO Request | `[Action][Feature]RequestDto` | `CreateDocumentTypeRequestDto` |
| DTO Response | `[Feature]ResponseDto` | `DocumentTypeResponseDto` |
| Controller | `[Feature]Controller` | `DocumentTypeController` |
| Adapter | `[Feature]RepositoryAdapter` | `DocumentTypeRepositoryAdapter` |
| JPA Repository | `[Feature]JpaRepository` | `DocumentTypeJpaRepository` |
| Mapper | `[Feature]Mapper` | `DocumentTypeMapper` |

### Paquetes
**SIEMPRE `snake_case` con guiones bajos:**
- ✅ `document_types`, `user_management`, `payment_gateway`
- ❌ `documentTypes`, `userManagement`

### Variables y Métodos
- **camelCase** descriptivo
- Sin abreviaciones (salvo: `id`, `dto`, `url`)
- ✅ `documentTypeList`, `isEnabled`, `calculateTotal`
- ❌ `docTypes`, `e`, `calc`

### Constantes
- **UPPER_SNAKE_CASE**
- ✅ `MAX_RETRY_ATTEMPTS`, `DEFAULT_PAGE_SIZE`

### Booleanos
- Prefijos: `is`, `has`, `can`, `should`
- ✅ `isEnabled`, `hasPermission`, `canDelete`

---

## Testing

### Cobertura Mínima

| Tipo | Cobertura | Nota |
|------|-----------|------|
| Casos de uso | **100%** | Líneas y branches |
| Servicios dominio | **100%** | Líneas y branches |
| Validadores | **100%** | Todos los edge cases |
| Controllers | **100%** | Happy path + errores |
| Mappers | Básica | Tests de integración |
| Entities/DTOs | **NO** | Son POJOs |

### Estructura AAA (Arrange-Act-Assert)
```java
@ExtendWith(MockitoExtension.class)
@DisplayName("CreateDocumentTypeUseCase - Unit Tests")
class CreateDocumentTypeUseCaseImplTest {
    
    @Mock private DocumentTypeRepository repository;
    @Mock private DocumentTypeDomainService domainService;
    @InjectMocks private CreateDocumentTypeUseCaseImpl useCase;
    
    @Test
    @DisplayName("should create document type when all data is valid")
    void shouldCreateDocumentTypeWhenAllDataIsValid() {
        // Arrange
        CreateDocumentTypeCommand command = new CreateDocumentTypeCommand("CC", "Cédula", true);
        DocumentType expected = new DocumentType(new DocumentTypeId("1"), "CC", "Cédula", true);
        
        when(repository.save(any())).thenReturn(expected);
        doNothing().when(domainService).validateUniqueCode(anyString());
        
        // Act
        DocumentType result = useCase.execute(command);
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result.code()).isEqualTo("CC");
        verify(domainService).validateUniqueCode("CC");
        verify(repository).save(any());
    }
    
    @Test
    @DisplayName("should throw exception when code already exists")
    void shouldThrowExceptionWhenCodeAlreadyExists() {
        // Arrange
        CreateDocumentTypeCommand command = new CreateDocumentTypeCommand("CC", "Cédula", true);
        doThrow(new DuplicateDocumentTypeException("CC"))
            .when(domainService).validateUniqueCode("CC");
        
        // Act & Assert
        assertThatThrownBy(() -> useCase.execute(command))
            .isInstanceOf(DuplicateDocumentTypeException.class)
            .hasMessageContaining("CC");
        
        verify(repository, never()).save(any());
    }
}
```

### Convenciones
- **Clase:** `[ClaseTesteada]Test`
- **Método:** `should[ExpectedBehavior]When[StateUnderTest]`
- **@DisplayName:** Descripción natural en español/inglés

### NO Testear
- ❌ Getters/setters de Lombok
- ❌ Records (generados por Java)
- ❌ Métodos triviales sin lógica
- ❌ Configuraciones Spring Boot
- ❌ Clases de config puras

### Herramientas
- JUnit 5
- Mockito
- AssertJ
- Testcontainers (tests integración BD)

---

## Manejo de Excepciones

### Nomenclatura
`[Feature][Concepto]Exception`

Ejemplos:
- `DocumentTypeNotFoundException`
- `DuplicateDocumentTypeException`
- `InvalidDocumentTypeException`

### Ubicación
`domain/exception/[feature]/`

### Estructura
```java
@Getter
public class DocumentTypeNotFoundException extends RuntimeException {
    private final String documentTypeId;
    private final String context;
    
    public DocumentTypeNotFoundException(String documentTypeId) {
        super(String.format("DocumentType with id '%s' not found", documentTypeId));
        this.documentTypeId = documentTypeId;
        this.context = null;
    }
    
    public DocumentTypeNotFoundException(String documentTypeId, String context) {
        super(String.format("DocumentType '%s' not found. Context: %s", documentTypeId, context));
        this.documentTypeId = documentTypeId;
        this.context = context;
    }
}
```

### Mapeo a HTTP

| Excepción | HTTP | Descripción |
|-----------|------|-------------|
| `*NotFoundException` | 404 | Recurso no encontrado |
| `Duplicate*Exception` | 409 | Conflicto por duplicado |
| `Invalid*Exception` | 400 | Datos inválidos |
| `*ValidationException` | 400 | Validación fallida |
| `Unauthorized*Exception` | 401 | No autenticado |
| `Forbidden*Exception` | 403 | No autorizado |
| `*BusinessRuleException` | 422 | Regla negocio violada |
| `*TechnicalException` | 500 | Error técnico |

### Global Exception Handler
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(DocumentTypeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            DocumentTypeNotFoundException ex, WebRequest request) {
        log.warn("DocumentType not found: {}", ex.getDocumentTypeId());
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .message(ex.getMessage())
            .path(request.getDescription(false))
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, WebRequest request) {
        log.error("Unexpected error", ex);
        
        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .message("An unexpected error occurred")
            .path(request.getDescription(false))
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### Reglas de Captura
- ❌ NO capturar `Exception` o `RuntimeException` salvo:
  - Global Exception Handler
  - Boundary (controllers, scheduled tasks)
- ✅ Captura solo si puedes manejar específicamente
- ✅ Mensajes descriptivos y contextuales
- ✅ Logea apropiadamente (ERROR críticos, WARN esperados)

---

## Validaciones

### Dos Tipos de Validación

#### 1. Validaciones de Negocio → Servicios de Dominio
```java
// NO @Service en dominio (sin dependencias Spring)
@RequiredArgsConstructor
@Slf4j
public class DocumentTypeDomainService {
    
    private final DocumentTypeRepository repository;
    
    public void validateUniqueCode(String code) {
        if (repository.existsByCode(code)) {
            log.warn("Duplicate code attempt: {}", code);
            throw new DuplicateDocumentTypeException(code);
        }
    }
    
    public void validateCanUpdate(DocumentType existing, DocumentType updated) {
        if (!existing.enabled() && updated.enabled()) {
            log.debug("Reactivating: {}", updated.code());
            // Lógica de validación de reactivación
        }
    }
}
```

#### 2. Validaciones Estructurales → DTOs (Jakarta Validation)
```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateDocumentTypeRequestDto {
    
    @NotBlank(message = "Code is required")
    @Size(min = 2, max = 10, message = "Code: 2-10 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Code: uppercase, numbers, underscores")
    private String code;
    
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name: max 100 characters")
    private String name;
    
    @NotNull(message = "Enabled is required")
    private Boolean enabled;
}
```

### Reglas
- ✅ Negocio → Domain services
- ✅ Estructural → DTOs con anotaciones
- ❌ NO mezclar infra con negocio
- ✅ Lanzar excepciones específicas
- ✅ Usar `Optional` para nullables

---

## REST API

### Convenciones
- **Plural:** `/api/v1/document-types` (no singular)
- **Versionado:** `/api/v1/`, `/api/v2/`

### Verbos HTTP

| Verbo | Uso | Idempotente | Safe |
|-------|-----|-------------|------|
| GET | Consultar | ✅ | ✅ |
| POST | Crear | ❌ | ❌ |
| PUT | Actualizar completo | ✅ | ❌ |
| PATCH | Actualizar parcial | ❌ | ❌ |
| DELETE | Eliminar | ✅ | ❌ |

### Códigos HTTP Comunes

| Código | Uso |
|--------|-----|
| 200 OK | Operación exitosa con respuesta |
| 201 Created | Recurso creado (+ header Location) |
| 204 No Content | Exitosa sin respuesta |
| 400 Bad Request | Datos inválidos |
| 401 Unauthorized | No autenticado |
| 403 Forbidden | No autorizado |
| 404 Not Found | No encontrado |
| 409 Conflict | Conflicto (duplicado) |
| 422 Unprocessable | Regla negocio violada |
| 500 Internal Error | Error servidor |

### Controller REST Completo
```java
@RestController
@RequestMapping("/api/v1/document-types")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Document Types", description = "Gestión de tipos de documento")
public class DocumentTypeController {
    
    private final CreateDocumentTypeUseCase createUseCase;
    private final FindDocumentTypeUseCase findUseCase;
    private final DocumentTypeMapper mapper;
    
    @PostMapping
    @Operation(summary = "Crear tipo de documento")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Creado"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "409", description = "Código duplicado")
    })
    public ResponseEntity<DocumentTypeResponseDto> create(
            @Valid @RequestBody CreateDocumentTypeRequestDto request) {
        
        log.info("Creating document type: {}", request.getCode());
        
        CreateDocumentTypeCommand command = new CreateDocumentTypeCommand(
            request.getCode(), request.getName(), request.getEnabled());
        
        DocumentType created = createUseCase.execute(command);
        DocumentTypeResponseDto response = mapper.toResponseDto(created);
        
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}").buildAndExpand(response.id()).toUri();
        
        return ResponseEntity.created(location).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Obtener por ID")
    public ResponseEntity<DocumentTypeResponseDto> findById(@PathVariable String id) {
        log.debug("Finding: {}", id);
        
        DocumentType found = findUseCase.findById(new DocumentTypeId(id));
        return ResponseEntity.ok(mapper.toResponseDto(found));
    }
    
    @GetMapping
    @Operation(summary = "Listar todos")
    public ResponseEntity<List<DocumentTypeResponseDto>> findAll() {
        List<DocumentType> all = findUseCase.findAll();
        List<DocumentTypeResponseDto> response = all.stream()
            .map(mapper::toResponseDto).toList();
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.info("Deleting: {}", id);
        deleteUseCase.execute(new DocumentTypeId(id));
        return ResponseEntity.noContent().build();
    }
}
```

### Reglas
- ✅ `ResponseEntity<T>` para control HTTP explícito
- ✅ Documentar con SpringDoc (`@Operation`, `@ApiResponse`)
- ✅ Header `Location` en 201 Created
- ✅ `@Valid` en request bodies
- ❌ NO exponer entities ni domain directamente

---

## Transacciones

### Principio
**Transacciones a nivel de caso de uso (application layer).**

### Reglas
- ✅ `@Transactional(readOnly = true)` para **consultas**
- ✅ `@Transactional` para **escritura**
- ❌ NO en:
  - Controllers
  - Domain services
  - Capa de dominio

### Ejemplo: Consulta
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class FindDocumentTypeUseCaseImpl implements FindDocumentTypeUseCase {
    
    private final DocumentTypeRepository repository;
    
    @Override
    @Transactional(readOnly = true)  // Optimización lectura
    public DocumentType findById(DocumentTypeId id) {
        log.debug("Finding: {}", id.value());
        return repository.findById(id)
            .orElseThrow(() -> new DocumentTypeNotFoundException(id.value()));
    }
}
```

### Ejemplo: Escritura
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class CreateDocumentTypeUseCaseImpl implements CreateDocumentTypeUseCase {
    
    private final DocumentTypeRepository repository;
    private final DocumentTypeDomainService domainService;
    
    @Override
    @Transactional  // Rollback automático en excepciones
    public DocumentType execute(CreateDocumentTypeCommand command) {
        log.debug("Creating: {}", command.code());
        
        domainService.validateUniqueCode(command.code());
        DocumentType saved = repository.save(documentType);
        
        log.info("Created: {}", saved.id());
        return saved;
    }
}
```

### Operaciones Batch
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class BulkImportUseCaseImpl {
    
    private static final int BATCH_SIZE = 100;
    
    @Override
    public BulkImportResult execute(List<CreateDocumentTypeCommand> commands) {
        log.info("Bulk import: {} items", commands.size());
        
        List<DocumentType> success = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        
        for (int i = 0; i < commands.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, commands.size());
            List<CreateDocumentTypeCommand> batch = commands.subList(i, end);
            processBatch(batch, success, failures);
        }
        
        return new BulkImportResult(success, failures);
    }
    
    @Transactional  // Cada batch = una transacción
    private void processBatch(List<CreateDocumentTypeCommand> batch,
                             List<DocumentType> success,
                             List<String> failures) {
        // Procesar batch...
    }
}
```

---

## Seguridad

### Defensa en Profundidad - TODAS las Capas

#### 1. Controllers - Validación y Autorización
```java
@RestController
@RequestMapping("/api/v1/document-types")
@RequiredArgsConstructor
@Slf4j
public class DocumentTypeController {
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")  // Control acceso
    public ResponseEntity<DocumentTypeResponseDto> create(
            @Valid @RequestBody CreateDocumentTypeRequestDto request) {  // @Valid OBLIGATORIO
        
        // Sanitizar para logs
        log.info("Creating: {}", sanitizeForLog(request.getCode()));
        // ...
    }
    
    private String sanitizeForLog(String value) {
        return value != null && value.length() > 3 
            ? value.substring(0, 3) + "***" 
            : "***";
    }
}
```

#### 2. DTOs - Validación Estructural
```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CreateDocumentTypeRequestDto {
    
    @NotBlank(message = "Code required")
    @Size(max = 10)
    @Pattern(regexp = "^[A-Z0-9_]+$")  // Prevenir injection
    private String code;
    
    @NotBlank @Size(max = 100)
    private String name;
}
```

#### 3. Domain - Validar Invariantes SIEMPRE
```java
public record DocumentType(DocumentTypeId id, String code, String name, boolean enabled) {
    public DocumentType {
        // NO confiar en validaciones de otras capas
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code cannot be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
    }
}
```

#### 4. Exception Handler - NO Exponer Detalles
```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @Value("${app.debug-mode:false}")
    private boolean debugMode;
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest req) {
        log.error("Error", ex);  // Log completo interno
        
        // Respuesta sin stack trace en producción
        ErrorResponse error = ErrorResponse.builder()
            .status(500)
            .message(debugMode ? ex.getMessage() : "An error occurred")
            .timestamp(LocalDateTime.now())
            .build();
        
        return ResponseEntity.status(500).body(error);
    }
}
```

#### 5. Persistencia - Prevenir SQL Injection
```java
// ❌ PELIGRO
String query = "SELECT * FROM users WHERE email = '" + email + "'";

// ✅ SEGURO - JPA con parámetros
@Query("SELECT u FROM UserEntity u WHERE u.email = :email")
Optional<UserEntity> findByEmail(@Param("email") String email);
```

#### 6. Logging - NO Exponer Datos Sensibles
```java
// ❌ PELIGRO
log.info("Login: email={}, password={}", email, password);

// ✅ SEGURO
log.info("Login: email={}", maskEmail(email));

private String maskEmail(String email) {
    if (email == null || !email.contains("@")) return "***";
    String[] parts = email.split("@");
    return parts[0].substring(0, Math.min(2, parts[0].length())) + "***@" + parts[1];
}
```

### Checklist de Seguridad
- [ ] Validar TODOS los inputs con `@Valid`
- [ ] Sanitizar datos antes de loguear
- [ ] NO exponer stack traces en producción
- [ ] `@PreAuthorize` para control de acceso
- [ ] Validar invariantes en dominio
- [ ] Usar PreparedStatements (JPA lo hace)
- [ ] Encriptar datos sensibles en BD
- [ ] Enmascarar PII en logs

---

## Workflow de Implementación

### FASE 1: VALIDACIÓN (BLOQUEANTE ⛔)

```
┌─────────────────────────────────────────┐
│ ¿Está especificado en /specs/?         │
│  NO → DETENER y PREGUNTAR              │
│  SÍ → Continuar                         │
└─────────────────────────────────────────┘
          ↓
┌─────────────────────────────────────────┐
│ ¿Hay conflictos entre specs?            │
│  SÍ → DETENER y REPORTAR               │
│  NO → Continuar                         │
└─────────────────────────────────────────┘
          ↓
┌─────────────────────────────────────────┐
│ ¿El feature está en /wip/ o /features/? │
│  Usar versión más reciente (prioridad wip)│
└─────────────────────────────────────────┘
          ↓
┌─────────────────────────────────────────┐
│ ¿Entiendes completamente el requisito?  │
│  NO → Pedir clarificación               │
│  SÍ → Continuar a FASE 2                │
└─────────────────────────────────────────┘
```

### FASE 2: PLANIFICACIÓN 📋

- [ ] Identificar feature y capas involucradas
- [ ] Listar clases nuevas con nomenclatura correcta
- [ ] Planificar Records vs Lombok
- [ ] Identificar excepciones necesarias
- [ ] Determinar si necesita migración BD

### FASE 3: IMPLEMENTACIÓN 🔨

**Orden: Domain → Application → Infrastructure**

#### 3.1 Domain
- [ ] Estructura paquetes: `model/`, `service/`, `exception/`
- [ ] Modelos con **Records**
- [ ] Value Objects con **Records**
- [ ] Servicios con **Lombok** (solo `@RequiredArgsConstructor`, `@Slf4j` - NO @Service)
- [ ] Excepciones específicas (NO anotaciones Spring)

#### 3.2 Application
- [ ] Estructura: `port/[feature]/`, `usecase/[feature]/`
- [ ] **Ports** (interfaces casos de uso y repositorios)
- [ ] Implementar casos de uso con **Lombok** + `@Transactional`

#### 3.3 Infrastructure
- [ ] Estructura: `controller/`, `dto/`, `entity/`, `repository/`, `adapter/`
- [ ] Entities con **Lombok** (JPA)
- [ ] DTOs request con **Lombok**
- [ ] DTOs response con **Records**
- [ ] Mappers con **MapStruct**
- [ ] Controllers con **Lombok**
- [ ] Adapters

#### 3.4 Validaciones y Logging
- [ ] Validaciones negocio en domain services
- [ ] Validaciones estructurales en DTOs
- [ ] Logging con `@Slf4j` (INFO eventos, DEBUG detalles)

#### 3.5 Migraciones BD
- [ ] Archivo Flyway: `V{major}.{minor}__{descripcion}.sql`
- [ ] Datos catálogo si aplica

### FASE 4: VERIFICACIÓN ✅

#### 4.1 Records y Lombok
- [ ] Inmutables → Records
- [ ] Mutables → Lombok apropiado
- [ ] JPA: `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`
- [ ] Servicios: `@RequiredArgsConstructor`, `@Slf4j`

#### 4.2 Nomenclatura
- [ ] Clases cumplen 100% convenciones
- [ ] Paquetes en `snake_case`
- [ ] Variables `camelCase`
- [ ] Constantes `UPPER_SNAKE_CASE`

#### 4.3 Excepciones
- [ ] Específicas del dominio
- [ ] En `domain/exception/[feature]/`
- [ ] Global handler mapea a HTTP

#### 4.4 Testing
- [ ] `@DisplayName` descriptivos
- [ ] Cobertura por tipo (UC 100%, services 100%, controllers ≥80%)
- [ ] Patrón AAA

#### 4.5 Código Limpio
- [ ] Sin código comentado
- [ ] Sin imports sin usar
- [ ] Sin warnings
- [ ] Logging con `@Slf4j`

#### 4.6 Mapping y Transacciones
- [ ] MapStruct para mapeo
- [ ] `@Transactional` en application layer
- [ ] Migraciones si tocó BD

### FASE 5: PRE-COMMIT 🚀

```bash
mvn clean test           # Tests
mvn jacoco:report        # Cobertura
mvn checkstyle:check     # Estilo
mvn clean install        # Build completo
```

**❗ PREGUNTAR AL USUARIO antes de commitear**

---

## Database Migrations (Flyway)

### Nomenclatura
`V{MAJOR}.{MINOR}__{descripcion_snake_case}.sql`

### Versionado
- **MAJOR:** Cambio en esquema existente (ALTER, DROP)
- **MINOR:** Agregar nuevas tablas/columnas (CREATE, ADD)

### Ejemplos
```
V1.0__create_document_types_table.sql
V1.1__add_description_column.sql
V2.0__alter_code_column_type.sql
```

### Reglas
1. Idempotentes cuando sea posible
2. NUNCA modificar migración aplicada en prod
3. Incluir datos de catálogo en misma versión
4. Usar transacciones
5. Probar rollback

### Ejemplo
```sql
-- V1.0__create_document_types_table.sql

CREATE TABLE document_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_document_types_enabled ON document_types(enabled);

COMMENT ON TABLE document_types IS 'Tipos de documento de identidad';

-- Datos iniciales
INSERT INTO document_types (code, name, enabled) VALUES
    ('CC', 'Cédula de Ciudadanía', true),
    ('CE', 'Cédula de Extranjería', true),
    ('PA', 'Pasaporte', true);
```

---

## DTOs y Mapping

### DTOs

#### Response → Records
```java
public record DocumentTypeResponseDto(
    String id,
    String code,
    String name,
    boolean enabled
) {}
```

#### Request → Lombok
```java
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateDocumentTypeRequestDto {
    @NotBlank @Size(max = 10) private String code;
    @NotBlank @Size(max = 100) private String name;
    @NotNull private Boolean enabled;
}
```

### Mappers (MapStruct)
```java
@Mapper(componentModel = "spring")
public interface DocumentTypeMapper {
    
    @Mapping(target = "id", source = "id", qualifiedByName = "toDocumentTypeId")
    DocumentType toDomain(DocumentTypeEntity entity);
    
    @Mapping(target = "id", source = "id.value")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DocumentTypeEntity toEntity(DocumentType domain);
    
    @Mapping(target = "id", source = "id.value")
    DocumentTypeResponseDto toResponseDto(DocumentType domain);
    
    @Named("toDocumentTypeId")
    default DocumentTypeId toDocumentTypeId(Long id) {
        return id != null ? new DocumentTypeId(String.valueOf(id)) : null;
    }
}
```

### Reglas
- ✅ MapStruct para mapeo
- ❌ NO exponer entities en controllers
- ✅ Validar DTOs entrada con Jakarta Validation
- ✅ Centralizar lógica compleja en `@Named`

---

## Logging

### Niveles

| Nivel | Uso |
|-------|-----|
| ERROR | Errores críticos inmediatos |
| WARN | Anormal, no impide funcionar |
| INFO | Eventos importantes negocio |
| DEBUG | Detalles depuración |
| TRACE | Muy detallado (no producción) |

### Uso con @Slf4j
```java
@Service
@RequiredArgsConstructor
@Slf4j  // Genera: private static final Logger log
public class CreateDocumentTypeUseCaseImpl {
    
    @Override
    @Transactional
    public DocumentType execute(CreateDocumentTypeCommand command) {
        log.debug("Creating: {}", command.code());
        
        try {
            domainService.validateUniqueCode(command.code());
            DocumentType created = repository.save(documentType);
            
            log.info("Created ID: {}, Code: {}", created.id(), created.code());
            return created;
            
        } catch (DuplicateDocumentTypeException e) {
            log.warn("Duplicate code: {}", command.code());
            throw e;
            
        } catch (Exception e) {
            log.error("Error creating: {}", command.code(), e);
            throw new TechnicalException("Error creating", e);
        }
    }
}
```

### Reglas
- ❌ NO `System.out` ni `printStackTrace()`
- ✅ `@Slf4j` siempre
- ✅ Incluir contexto (IDs, operación)
- ✅ Placeholders `{}` (lazy eval)
- ✅ Sanitizar datos sensibles
- ✅ Domain: excepcional (solo crítico)
- ✅ Controllers: request/response importantes
- ✅ Adapters: interacciones externas

---

## Dependency Injection

### Constructor Injection (RECOMENDADO)
```java
@Service
@RequiredArgsConstructor  // Constructor auto para final
public class CreateDocumentTypeUseCaseImpl {
    
    private final DocumentTypeRepository repository;
    private final DocumentTypeDomainService domainService;
    
    // Constructor generado automáticamente
}
```

### Reglas
- ✅ Inyección por constructor (no `@Autowired` en campos)
- ✅ Dependencias `final`
- ✅ `@RequiredArgsConstructor` de Lombok
- ✅ Anotaciones apropiadas:
    - `@Service` - Servicios de application (casos de uso)
  - `@Repository` - Adapters persistencia
  - `@RestController` - Controllers
  - `@Component` - Genéricos
- ❌ Servicios de dominio NO llevan anotaciones Spring (`@Service`, `@Component`)
- ❌ Ports (interfaces) NO llevan anotaciones Spring
- ✅ Beans manuales en `@Configuration` si lógica inicialización
- ✅ Preferir interfaces sobre implementaciones

---

## Versionado

### API REST - URL (OBLIGATORIO)
```
/api/v1/document-types
/api/v2/document-types
```

#### Reglas
- ✅ Versionado obligatorio en URL
- ✅ Mantener v1 mientras haya clientes
- ✅ Marcar obsoletas con `@Deprecated` + warning header
- ✅ Incrementar MAJOR ante breaking changes

#### Deprecación
```java
@RestController
@RequestMapping("/api/v1/document-types")
@Deprecated
public class DocumentTypeControllerV1 {
    
    @GetMapping
    public ResponseEntity<List<DocumentTypeResponseDto>> findAll(HttpServletResponse res) {
        res.setHeader("Warning", 
            "299 - \"Deprecated. Use /api/v2/document-types\"");
        // ...
    }
}
```

### Base de Datos
Ver sección "Database Migrations"

---

## Commits

### ⚠️ CRÍTICO
**NO hacer commits sin preguntar al usuario.**

### Proceso
1. Completar TODAS las fases del workflow
2. Verificar TODOS los checks Fase 4 ✅
3. Ejecutar comandos Fase 5
4. **PREGUNTAR:** "¿Deseas commit?"
5. Si aprueba → commit con mensaje descriptivo

### Formato
```
[TIPO] Descripción corta en español (≤50 chars)

Descripción detallada (≤72 chars/línea):
- Qué se implementó
- Por qué así
- Referencias

Refs: #ISSUE-123
```

### Tipos
- `feat` - Nueva funcionalidad
- `fix` - Corrección bug
- `refactor` - Refactorización
- `test` - Tests
- `docs` - Documentación
- `style` - Formateo
- `chore` - Mantenimiento

### Ejemplo
```
feat: Implementar creación de tipos de documento

- Caso de uso CreateDocumentTypeUseCase
- Validación código único
- Controller REST POST /api/v1/document-types
- Migración V1.0__create_document_types_table.sql

Refs: #FEAT-001
```

---

## 🎯 Cumplimiento Obligatorio

**Estas reglas son OBLIGATORIAS al 100%.**

**NO hay excepciones** salvo justificación técnica crítica **DOCUMENTADA**.

### Si Encuentras un Caso Especial
1. **DETENER** implementación
2. **DOCUMENTAR** caso y justificación técnica
3. **PREGUNTAR** al usuario
4. **ACTUALIZAR** reglas si usuario aprueba

---

## 📚 Recursos y Referencias

### Consulta Obligatoria
- `specs/PROJECT_INFO.md` - Info general
- `specs/scaffolding.md` - Estructura y convenciones
- `specs/features/[feature]/` - Specs funcionales
- `specs/wip/[feature]/` - Features en desarrollo (PRIORIDAD)

### Stack Tecnológico
- Java 17+
- Spring Boot 3.x
- Spring Data JPA
- Lombok
- MapStruct
- Flyway
- JUnit 5 + Mockito + AssertJ
- SpringDoc OpenAPI

---

**FIN DEL DOCUMENTO**
