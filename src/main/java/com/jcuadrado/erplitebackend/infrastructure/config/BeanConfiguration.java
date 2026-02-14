package com.jcuadrado.erplitebackend.infrastructure.config;

import com.jcuadrado.erplitebackend.domain.port.documenttypes.DocumentTypeRepository;
import com.jcuadrado.erplitebackend.domain.port.geography.DepartmentRepository;
import com.jcuadrado.erplitebackend.domain.port.geography.MunicipalityRepository;
import com.jcuadrado.erplitebackend.domain.port.paymentmethod.PaymentMethodRepository;
import com.jcuadrado.erplitebackend.domain.port.taxtype.TaxTypeRepository;
import com.jcuadrado.erplitebackend.domain.service.documenttypes.DocumentTypeDomainService;
import com.jcuadrado.erplitebackend.domain.service.documenttypes.DocumentTypeValidator;
import com.jcuadrado.erplitebackend.domain.service.geography.GeographyDomainService;
import com.jcuadrado.erplitebackend.domain.service.geography.GeographyValidator;
import com.jcuadrado.erplitebackend.domain.service.paymentmethod.PaymentMethodDomainService;
import com.jcuadrado.erplitebackend.domain.service.paymentmethod.PaymentMethodValidator;
import com.jcuadrado.erplitebackend.domain.service.taxtype.TaxTypeDomainService;
import com.jcuadrado.erplitebackend.domain.service.taxtype.TaxTypeValidationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bean configuration for dependency injection
 */
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
}

