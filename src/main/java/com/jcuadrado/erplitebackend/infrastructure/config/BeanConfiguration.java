package com.jcuadrado.erplitebackend.infrastructure.config;

import com.jcuadrado.erplitebackend.application.port.security.AuditLogUseCase;
import com.jcuadrado.erplitebackend.application.port.security.AuthUseCase;
import com.jcuadrado.erplitebackend.application.port.security.CompareRoleUseCase;
import com.jcuadrado.erplitebackend.application.port.security.CompareUserUseCase;
import com.jcuadrado.erplitebackend.application.port.security.ConditionEvaluator;
import com.jcuadrado.erplitebackend.application.port.security.ManagePermissionUseCase;
import com.jcuadrado.erplitebackend.application.port.security.ManageRoleUseCase;
import com.jcuadrado.erplitebackend.application.port.security.ManageUserUseCase;
import com.jcuadrado.erplitebackend.application.port.security.PasswordEncoder;
import com.jcuadrado.erplitebackend.application.port.security.TokenService;
import com.jcuadrado.erplitebackend.application.port.security.UserPermissionsUseCase;
import com.jcuadrado.erplitebackend.application.usecase.security.AuditLogUseCaseImpl;
import com.jcuadrado.erplitebackend.application.usecase.security.AuthUseCaseImpl;
import com.jcuadrado.erplitebackend.application.usecase.security.CompareRoleUseCaseImpl;
import com.jcuadrado.erplitebackend.application.usecase.security.CompareUserUseCaseImpl;
import com.jcuadrado.erplitebackend.application.usecase.security.ManagePermissionUseCaseImpl;
import com.jcuadrado.erplitebackend.application.usecase.security.ManageRoleUseCaseImpl;
import com.jcuadrado.erplitebackend.application.usecase.security.ManageUserUseCaseImpl;
import com.jcuadrado.erplitebackend.application.usecase.security.UserPermissionsUseCaseImpl;
import com.jcuadrado.erplitebackend.domain.port.documenttypes.DocumentTypeRepository;
import com.jcuadrado.erplitebackend.domain.port.geography.DepartmentRepository;
import com.jcuadrado.erplitebackend.domain.port.geography.MunicipalityRepository;
import com.jcuadrado.erplitebackend.domain.port.paymentmethod.PaymentMethodRepository;
import com.jcuadrado.erplitebackend.domain.port.security.AuditLogRepository;
import com.jcuadrado.erplitebackend.domain.port.security.PermissionRepository;
import com.jcuadrado.erplitebackend.domain.port.security.RefreshTokenRepository;
import com.jcuadrado.erplitebackend.domain.port.security.RoleRepository;
import com.jcuadrado.erplitebackend.domain.port.security.UserRepository;
import com.jcuadrado.erplitebackend.domain.port.taxtype.TaxTypeRepository;
import com.jcuadrado.erplitebackend.domain.port.unitofmeasure.UnitOfMeasureRepository;
import com.jcuadrado.erplitebackend.domain.service.documenttypes.DocumentTypeDomainService;
import com.jcuadrado.erplitebackend.domain.service.documenttypes.DocumentTypeValidator;
import com.jcuadrado.erplitebackend.domain.service.geography.GeographyDomainService;
import com.jcuadrado.erplitebackend.domain.service.geography.GeographyValidator;
import com.jcuadrado.erplitebackend.domain.service.paymentmethod.PaymentMethodDomainService;
import com.jcuadrado.erplitebackend.domain.service.paymentmethod.PaymentMethodValidator;
import com.jcuadrado.erplitebackend.domain.service.security.UserDomainService;
import com.jcuadrado.erplitebackend.domain.service.taxtype.TaxTypeDomainService;
import com.jcuadrado.erplitebackend.domain.service.taxtype.TaxTypeValidationService;
import com.jcuadrado.erplitebackend.domain.service.unitofmeasure.UnitOfMeasureDomainService;
import com.jcuadrado.erplitebackend.domain.service.unitofmeasure.UnitOfMeasureValidationService;
import com.jcuadrado.erplitebackend.domain.service.unitofmeasure.UnitOfMeasureValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    /**
     * Bean for DocumentTypeValidator
     */
    @Bean
    public DocumentTypeValidator documentTypeValidator() {
        return new DocumentTypeValidator();
    }

    /**
     * Bean for DocumentTypeDomainService
     */
    @Bean
    public DocumentTypeDomainService documentTypeDomainService(
            DocumentTypeRepository repository,
            DocumentTypeValidator validator) {
        return new DocumentTypeDomainService(repository, validator);
    }

    // ==================== Geography Beans ====================

    /**
     * Bean for GeographyValidator
     */
    @Bean
    public GeographyValidator geographyValidator() {
        return new GeographyValidator();
    }

    /**
     * Bean for GeographyDomainService
     */
    @Bean
    public GeographyDomainService geographyDomainService(
            DepartmentRepository departmentRepository,
            MunicipalityRepository municipalityRepository,
            GeographyValidator geographyValidator) {
        return new GeographyDomainService(departmentRepository, municipalityRepository, geographyValidator);
    }

    // ==================== Payment Method Beans ====================

    /**
     * Bean for PaymentMethodValidator
     */
    @Bean
    public PaymentMethodValidator paymentMethodValidator() {
        return new PaymentMethodValidator();
    }

    /**
     * Bean for PaymentMethodDomainService
     */
    @Bean
    public PaymentMethodDomainService paymentMethodDomainService(
            PaymentMethodRepository repository,
            PaymentMethodValidator validator) {
        return new PaymentMethodDomainService(repository, validator);
    }

    // ==================== Tax Type Beans ====================

    /**
     * Bean for TaxTypeDomainService
     */
    @Bean
    public TaxTypeDomainService taxTypeDomainService() {
        return new TaxTypeDomainService();
    }

    /**
     * Bean for TaxTypeValidationService
     */
    @Bean
    public TaxTypeValidationService taxTypeValidationService(TaxTypeRepository repository) {
        return new TaxTypeValidationService(repository);
    }

    // ==================== Unit Of Measure Beans ====================

    @Bean
    public UnitOfMeasureValidator unitOfMeasureValidator() {
        return new UnitOfMeasureValidator();
    }

    @Bean
    public UnitOfMeasureValidationService unitOfMeasureValidationService(UnitOfMeasureRepository repository) {
        return new UnitOfMeasureValidationService(repository);
    }

    @Bean
    public UnitOfMeasureDomainService unitOfMeasureDomainService(
            UnitOfMeasureValidator validator,
            UnitOfMeasureValidationService validationService) {
        return new UnitOfMeasureDomainService(validator, validationService);
    }

    // ==================== Security Beans ====================

    @Bean
    public UserDomainService userDomainService() {
        return new UserDomainService();
    }

    @Bean
    public AuthUseCase authUseCase(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            RefreshTokenRepository refreshTokenRepository,
            AuditLogRepository auditLogRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService) {
        return new AuthUseCaseImpl(userRepository, roleRepository, permissionRepository,
                refreshTokenRepository, auditLogRepository, passwordEncoder, tokenService);
    }

    @Bean
    public ManageUserUseCase manageUserUseCase(
            UserRepository userRepository,
            RoleRepository roleRepository,
            AuditLogRepository auditLogRepository,
            UserDomainService userDomainService,
            PasswordEncoder passwordEncoder) {
        return new ManageUserUseCaseImpl(userRepository, roleRepository, auditLogRepository,
                userDomainService, passwordEncoder);
    }

    @Bean
    public CompareUserUseCase compareUserUseCase(UserRepository userRepository) {
        return new CompareUserUseCaseImpl(userRepository);
    }

    @Bean
    public ManageRoleUseCase manageRoleUseCase(
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            AuditLogRepository auditLogRepository) {
        return new ManageRoleUseCaseImpl(roleRepository, permissionRepository, auditLogRepository);
    }

    @Bean
    public CompareRoleUseCase compareRoleUseCase(RoleRepository roleRepository) {
        return new CompareRoleUseCaseImpl(roleRepository);
    }

    @Bean
    public ManagePermissionUseCase managePermissionUseCase(
            PermissionRepository permissionRepository,
            AuditLogRepository auditLogRepository,
            ConditionEvaluator conditionEvaluator) {
        return new ManagePermissionUseCaseImpl(permissionRepository, auditLogRepository, conditionEvaluator);
    }

    @Bean
    public AuditLogUseCase auditLogUseCase(AuditLogRepository auditLogRepository) {
        return new AuditLogUseCaseImpl(auditLogRepository);
    }

    @Bean
    public UserPermissionsUseCase userPermissionsUseCase(
            UserRepository userRepository,
            PermissionRepository permissionRepository) {
        return new UserPermissionsUseCaseImpl(userRepository, permissionRepository);
    }
}

