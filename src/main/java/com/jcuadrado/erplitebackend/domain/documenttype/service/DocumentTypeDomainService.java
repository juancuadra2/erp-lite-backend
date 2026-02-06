package com.jcuadrado.erplitebackend.domain.documenttype.service;

import com.jcuadrado.erplitebackend.domain.documenttype.exception.DocumentTypeConstraintException;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Domain service for document type business rules and validations.
 */
@Service
public class DocumentTypeDomainService {
    
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z0-9]{2,10}$");
    
    /**
     * Validates that a document type code follows the required format.
     * Code must be 2-10 alphanumeric characters (uppercase).
     * 
     * @param code the code to validate
     * @throws DocumentTypeConstraintException if the code is invalid
     */
    public void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new DocumentTypeConstraintException("Document type code cannot be null or blank");
        }
        
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new DocumentTypeConstraintException(
                "Document type code must be 2-10 uppercase alphanumeric characters. Got: " + code
            );
        }
    }
    
    /**
     * Normalizes a code to uppercase and trims whitespace.
     *
     * @param code the code to normalize
     * @return normalized code
     */
    public String normalizeCode(String code) {
        return code != null ? code.trim().toUpperCase() : null;
    }
    
    /**
     * Validates that a name is not empty and within size limits.
     * 
     * @param name the name to validate
     * @throws DocumentTypeConstraintException if the name is invalid
     */
    public void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new DocumentTypeConstraintException("Document type name cannot be null or blank");
        }
        
        if (name.length() > 200) {
            throw new DocumentTypeConstraintException(
                "Document type name cannot exceed 200 characters. Got: " + name.length()
            );
        }
    }
}
