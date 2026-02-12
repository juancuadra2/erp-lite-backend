package com.jcuadrado.erplitebackend.domain.service.documenttypes;

import com.jcuadrado.erplitebackend.domain.exception.documenttypes.InvalidDocumentTypeException;

/**
 * Validator for Document Type domain rules
 */
public class DocumentTypeValidator {

    private static final int CODE_MIN_LENGTH = 2;
    private static final int CODE_MAX_LENGTH = 10;
    private static final int NAME_MIN_LENGTH = 1;
    private static final int NAME_MAX_LENGTH = 200;
    private static final int DESCRIPTION_MAX_LENGTH = 500;
    private static final String CODE_PATTERN = "^[A-Z0-9]+$";

    /**
     * Validate document type code
     * Rules: 2-10 alphanumeric characters, uppercase
     */
    public void validateCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new InvalidDocumentTypeException("code", "Code is required");
        }

        String trimmedCode = code.trim().toUpperCase();

        if (trimmedCode.length() < CODE_MIN_LENGTH) {
            throw new InvalidDocumentTypeException("code",
                String.format("Code must be at least %d characters", CODE_MIN_LENGTH));
        }

        if (trimmedCode.length() > CODE_MAX_LENGTH) {
            throw new InvalidDocumentTypeException("code",
                String.format("Code must not exceed %d characters", CODE_MAX_LENGTH));
        }

        if (!trimmedCode.matches(CODE_PATTERN)) {
            throw new InvalidDocumentTypeException("code",
                "Code must contain only alphanumeric characters (A-Z, 0-9)");
        }
    }

    /**
     * Validate document type name
     * Rules: 1-200 characters, required
     */
    public void validateName(String name) {
        if (name == null) {
            throw new InvalidDocumentTypeException("name", "Name is required");
        }

        String trimmedName = name.trim();

        if (trimmedName.length() < NAME_MIN_LENGTH) {
            throw new InvalidDocumentTypeException("name", "Name cannot be empty");
        }

        if (trimmedName.length() > NAME_MAX_LENGTH) {
            throw new InvalidDocumentTypeException("name",
                String.format("Name must not exceed %d characters", NAME_MAX_LENGTH));
        }
    }

    /**
     * Validate document type description
     * Rules: Optional, max 500 characters
     */
    public void validateDescription(String description) {
        if (description != null && description.trim().length() > DESCRIPTION_MAX_LENGTH) {
            throw new InvalidDocumentTypeException("description",
                String.format("Description must not exceed %d characters", DESCRIPTION_MAX_LENGTH));
        }
    }

    /**
     * Validate all fields of a document type
     */
    public void validateAll(String code, String name, String description) {
        validateCode(code);
        validateName(name);
        validateDescription(description);
    }
}

