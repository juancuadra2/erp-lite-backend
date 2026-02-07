package com.jcuadrado.erplitebackend.infrastructure.config;

import com.jcuadrado.erplitebackend.domain.port.documenttypes.DocumentTypeRepository;
import com.jcuadrado.erplitebackend.domain.port.geography.DepartmentRepository;
import com.jcuadrado.erplitebackend.domain.port.geography.MunicipalityRepository;
import com.jcuadrado.erplitebackend.domain.service.documenttypes.DocumentTypeDomainService;
import com.jcuadrado.erplitebackend.domain.service.documenttypes.DocumentTypeValidator;
import com.jcuadrado.erplitebackend.domain.service.geography.GeographyDomainService;
import com.jcuadrado.erplitebackend.domain.service.geography.GeographyValidator;
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
}

