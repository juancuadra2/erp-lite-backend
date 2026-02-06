package com.jcuadrado.erplitebackend.infrastructure.in.api.documenttype.constant;

/**
 * Constants for Document Type API endpoints and messages.
 */
public final class DocumentTypeApiConstants {

    private DocumentTypeApiConstants() {
        // Private constructor to prevent instantiation
    }

    // API Paths
    public static final String BASE_PATH = "/api/document-types";
    public static final String BY_UUID_PATH = "/{uuid}";
    public static final String DEACTIVATE_PATH = "/{uuid}/deactivate";
    public static final String ACTIVATE_PATH = "/{uuid}/activate";

    // API Tags
    public static final String API_TAG = "Document Types";
    public static final String API_DESCRIPTION = "Operations for managing document types (NIT, CC, CE, PA, etc.)";

    // Validation Messages
    public static final String CODE_REQUIRED = "Code is required";
    public static final String CODE_SIZE = "Code must be between 2 and 10 characters";
    public static final String CODE_PATTERN = "Code must contain only alphanumeric characters";
    public static final String NAME_REQUIRED = "Name is required";
    public static final String NAME_SIZE = "Name must be between 1 and 200 characters";
    public static final String DESCRIPTION_SIZE = "Description must not exceed 500 characters";

    // Query Parameters
    public static final String PARAM_ENABLED = "enabled";
    public static final String PARAM_SEARCH = "search";
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_LIMIT = "limit";
    public static final String PARAM_SORT_FIELD = "sort.field";
    public static final String PARAM_SORT_ORDER = "sort.order";
    public static final String PARAM_FIELDS = "fields";
    public static final String PARAM_POPULATE = "populate";

    // Default Values
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_LIMIT = 10;
    public static final int MAX_LIMIT = 100;
    public static final String DEFAULT_SORT_FIELD = "id";
    public static final String DEFAULT_SORT_ORDER = "ASC";

    // OpenAPI Descriptions
    public static final String DESC_CREATE = "Create a new document type";
    public static final String DESC_GET_BY_UUID = "Get document type by UUID";
    public static final String DESC_UPDATE = "Update an existing document type";
    public static final String DESC_DELETE = "Delete a document type (soft delete)";
    public static final String DESC_LIST = "List document types with advanced filters and pagination";
    public static final String DESC_DEACTIVATE = "Deactivate a document type";
    public static final String DESC_ACTIVATE = "Activate a document type";
}

