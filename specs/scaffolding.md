# Scaffolding Base - Arquitectura Hexagonal Organizada por Features

## Principio de Organización
Cada feature/módulo tiene su propia carpeta dentro de cada capa (domain, application, infrastructure).
Esto mantiene el código organizado y escalable cuando crece el número de módulos.

````
erp-lite-backend/
├─ pom.xml
├─ README.md
├─ docker/
│  ├─ Dockerfile
│  ├─ docker-compose.yml
│  └─ .dockerignore
└─ src/
   ├─ main/
   │  ├─ java/com/jcuadrado/erplitebackend/
   │  │
   │  │  ├─ ErpLiteBackendApplication.java
   │  │
   │  │  ├─ domain/
   │  │  │  ├─ model/
   │  │  │  │  ├─ document-types/
   │  │  │  │  │  └─ DocumentType.java
   │  │  │  │  ├─ contacts/
   │  │  │  │  │  └─ Contact.java
   │  │  │  │  └─ [otros-features]/
   │  │  │  │
   │  │  │  ├─ service/
   │  │  │  │  ├─ document-types/
   │  │  │  │  │  ├─ DocumentTypeDomainService.java
   │  │  │  │  │  └─ DocumentTypeValidator.java
   │  │  │  │  ├─ contacts/
   │  │  │  │  │  ├─ ContactDomainService.java
   │  │  │  │  │  └─ ContactValidator.java
   │  │  │  │  └─ [otros-features]/
   │  │  │  │
   │  │  │  ├─ port/
   │  │  │  │  └─ out/
   │  │  │  │     ├─ document-types/
   │  │  │  │     │  └─ DocumentTypeRepository.java
   │  │  │  │     ├─ contacts/
   │  │  │  │     │  └─ ContactRepository.java
   │  │  │  │     └─ [otros-features]/
   │  │  │  │
   │  │  │  └─ exception/
   │  │  │     ├─ document-types/
   │  │  │     │  ├─ DocumentTypeNotFoundException.java
   │  │  │     │  ├─ DuplicateCodeException.java
   │  │  │     │  └─ DocumentTypeDomainException.java
   │  │  │     ├─ contacts/
   │  │  │     │  └─ [excepciones de contacto]
   │  │  │     └─ [otros-features]/
   │  │  │
   │  │  ├─ application/
   │  │  │  ├─ port/
   │  │  │  │  └─ in/
   │  │  │  │     ├─ document-types/
   │  │  │  │     │  ├─ CompareDocumentTypesUseCase.java
   │  │  │  │     │  └─ ManageDocumentTypeUseCase.java
   │  │  │  │     ├─ contacts/
   │  │  │  │     │  ├─ ManageContactUseCase.java
   │  │  │  │     │  └─ SearchContactUseCase.java
   │  │  │  │     └─ [otros-features]/
   │  │  │  │
   │  │  │  └─ usecase/
   │  │  │     ├─ document-types/
   │  │  │     │  ├─ CompareDocumentTypesUseCaseImpl.java
   │  │  │     │  └─ ManageDocumentTypeUseCaseImpl.java
   │  │  │     ├─ contacts/
   │  │  │     │  ├─ ManageContactUseCaseImpl.java
   │  │  │     │  └─ SearchContactUseCaseImpl.java
   │  │  │     └─ [otros-features]/
   │  │  │
   │  │  └─ infrastructure/
   │  │     ├─ config/
   │  │     │  ├─ BeanConfiguration.java
   │  │     │  ├─ SecurityConfig.java
   │  │     │  └─ DatabaseConfig.java
   │  │     │
   │  │     ├─ in/
   │  │     │  └─ web/
   │  │     │     ├─ controller/
   │  │     │     │  ├─ document-types/
   │  │     │     │  │  └─ DocumentTypeController.java
   │  │     │     │  ├─ contacts/
   │  │     │     │  │  └─ ContactController.java
   │  │     │     │  └─ [otros-features]/
   │  │     │     │
   │  │     │     ├─ dto/
   │  │     │     │  ├─ document-types/
   │  │     │     │  │  ├─ DocumentTypeDto.java
   │  │     │     │  │  ├─ CreateDocumentTypeRequestDto.java
   │  │     │     │  │  └─ DocumentTypeResponseDto.java
   │  │     │     │  ├─ contacts/
   │  │     │     │  │  └─ [DTOs de contacto]
   │  │     │     │  └─ [otros-features]/
   │  │     │     │
   │  │     │     ├─ mapper/
   │  │     │     │  ├─ document-types/
   │  │     │     │  │  ├─ DocumentTypeDtoMapper.java
   │  │     │     │  │  └─ DocumentTypeComparisonDtoMapper.java
   │  │     │     │  ├─ contacts/
   │  │     │     │  │  └─ ContactDtoMapper.java
   │  │     │     │  └─ [otros-features]/
   │  │     │     │
   │  │     │     └─ advice/
   │  │     │        └─ GlobalExceptionHandler.java (compartido)
   │  │     │
   │  │     └─ out/
   │  │        └─ persistence/
   │  │           ├─ adapter/
   │  │           │  ├─ document-types/
   │  │           │  │  └─ DocumentTypeRepositoryAdapter.java
   │  │           │  ├─ contacts/
   │  │           │  │  └─ ContactRepositoryAdapter.java
   │  │           │  └─ [otros-features]/
   │  │           │
   │  │           ├─ entity/
   │  │           │  ├─ document-types/
   │  │           │  │  └─ DocumentTypeEntity.java
   │  │           │  ├─ contacts/
   │  │           │  │  └─ ContactEntity.java
   │  │           │  └─ [otros-features]/
   │  │           │
   │  │           ├─ mapper/
   │  │           │  ├─ document-types/
   │  │           │  │  └─ DocumentTypeEntityMapper.java
   │  │           │  ├─ contacts/
   │  │           │  │  └─ ContactEntityMapper.java
   │  │           │  └─ [otros-features]/
   │  │           │
   │  │           └─ util/
   │  │              ├─ document-types/
   │  │              │  └─ DocumentTypeSpecificationUtil.java
   │  │              ├─ contacts/
   │  │              │  └─ ContactSpecificationUtil.java
   │  │              └─ [otros-features]/
   │  │
   │  └─ resources/
   │     ├─ application.properties
   │     └─ db/
   │        └─ migration/
   │           ├─ V1.3__create_document_types_tables.sql
   │           ├─ V1.4__insert_colombia_document_types.sql
   │           └─ [otras migraciones]/
   │
   └─ test/
      └─ java/com/jcuadrado/erplitebackend/
         ├─ application/
         │  └─ usecase/
         │     ├─ document-types/
         │     │  ├─ CompareDocumentTypesUseCaseTest.java
         │     │  └─ ManageDocumentTypeUseCaseTest.java
         │     ├─ contacts/
         │     │  └─ [tests de casos de uso de contacto]
         │     └─ [otros-features]/
         │
         ├─ domain/
         │  ├─ model/
         │  │  ├─ document-types/
         │  │  │  └─ DocumentTypeTest.java
         │  │  └─ [otros-features]/
         │  │
         │  └─ service/
         │     ├─ document-types/
         │     │  ├─ DocumentTypeDomainServiceTest.java
         │     │  └─ DocumentTypeValidatorTest.java
         │     └─ [otros-features]/
         │
         └─ infrastructure/
            ├─ in/web/
            │  ├─ controller/
            │  │  ├─ document-types/
            │  │  │  └─ DocumentTypeControllerTest.java
            │  │  └─ [otros-features]/
            │  │
            │  ├─ advice/
            │  │  └─ GlobalExceptionHandlerTest.java
            │  │
            │  └─ mapper/
            │     ├─ document-types/
            │     │  ├─ DocumentTypeDtoMapperTest.java
            │     │  └─ DocumentTypeComparisonDtoMapperTest.java
            │     └─ [otros-features]/
            │
            └─ out/persistence/
               ├─ adapter/
               │  ├─ document-types/
               │  │  └─ DocumentTypeRepositoryAdapterTest.java
               │  └─ [otros-features]/
               │
               ├─ mapper/
               │  ├─ document-types/
               │  │  └─ DocumentTypeEntityMapperTest.java
               │  └─ [otros-features]/
               │
               └─ util/
                  ├─ document-types/
                  │  └─ DocumentTypeSpecificationUtilTest.java
                  └─ [otros-features]/
````

## Ventajas de esta Estructura

1. **Escalabilidad**: Fácil agregar nuevos features sin mezclar código
2. **Cohesión**: Todo el código relacionado con un feature está junto en cada capa
3. **Mantenibilidad**: Fácil encontrar y modificar código de un feature específico
4. **Claridad**: La estructura de carpetas refleja claramente los módulos del sistema
5. **Separación**: Cada feature mantiene su independencia dentro de cada capa

## Archivos Compartidos

Algunos archivos permanecen compartidos entre todos los features:
- `GlobalExceptionHandler.java` - Manejo centralizado de excepciones
- `BeanConfiguration.java` - Configuración de beans
- `SecurityConfig.java` - Configuración de seguridad
- `DatabaseConfig.java` - Configuración de base de datos
