package com.jcuadrado.erplitebackend.infrastructure.in.web.advice;

import com.jcuadrado.erplitebackend.domain.exception.documenttypes.DocumentTypeNotFoundException;
import com.jcuadrado.erplitebackend.domain.exception.documenttypes.DuplicateCodeException;
import com.jcuadrado.erplitebackend.domain.exception.documenttypes.InvalidDocumentTypeException;
import com.jcuadrado.erplitebackend.domain.exception.geography.DepartmentNotFoundException;
import com.jcuadrado.erplitebackend.domain.exception.geography.DuplicateDepartmentCodeException;
import com.jcuadrado.erplitebackend.domain.exception.geography.DuplicateMunicipalityCodeException;
import com.jcuadrado.erplitebackend.domain.exception.geography.GeographyConstraintException;
import com.jcuadrado.erplitebackend.domain.exception.geography.InvalidGeographyException;
import com.jcuadrado.erplitebackend.domain.exception.geography.MunicipalityNotFoundException;
import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.DuplicatePaymentMethodCodeException;
import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.InvalidPaymentMethodCodeException;
import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.InvalidPaymentMethodDataException;
import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.PaymentMethodConstraintException;
import com.jcuadrado.erplitebackend.domain.exception.paymentmethod.PaymentMethodNotFoundException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.DuplicateTaxTypeCodeException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.InvalidTaxPercentageException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.InvalidTaxTypeCodeException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.InvalidTaxTypeDataException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.TaxTypeConstraintException;
import com.jcuadrado.erplitebackend.domain.exception.taxtype.TaxTypeNotFoundException;
import com.jcuadrado.erplitebackend.domain.exception.unitofmeasure.DuplicateUnitOfMeasureAbbreviationException;
import com.jcuadrado.erplitebackend.domain.exception.unitofmeasure.DuplicateUnitOfMeasureNameException;
import com.jcuadrado.erplitebackend.domain.exception.unitofmeasure.InvalidUnitOfMeasureDataException;
import com.jcuadrado.erplitebackend.domain.exception.unitofmeasure.UnitOfMeasureInUseException;
import com.jcuadrado.erplitebackend.domain.exception.unitofmeasure.UnitOfMeasureNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Test
    void handleDocumentTypeNotFound_shouldReturnNotFound() {
        // Given
        DocumentTypeNotFoundException exception = new DocumentTypeNotFoundException("CC");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleDocumentTypeNotFound(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains("CC");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleDuplicateCode_shouldReturnConflict() {
        // Given
        DuplicateCodeException exception = new DuplicateCodeException("CC");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleDuplicateCode(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).contains("CC");
    }

    @Test
    void handleInvalidDocumentType_shouldReturnBadRequest() {
        // Given
        InvalidDocumentTypeException exception = new InvalidDocumentTypeException("Invalid document type");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleInvalidDocumentType(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid document type");
    }

    @Test
    void handleValidationErrors_shouldReturnBadRequestWithValidationErrors() {
        // Given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", "field1", "error message 1");
        FieldError fieldError2 = new FieldError("object", "field2", "error message 2");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // When
        ResponseEntity<GlobalExceptionHandler.ValidationErrorResponse> response = 
            exceptionHandler.handleValidationErrors(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Validation Failed");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid input data");
        assertThat(response.getBody().getValidationErrors()).hasSize(2);
        assertThat(response.getBody().getValidationErrors()).containsEntry("field1", "error message 1");
        assertThat(response.getBody().getValidationErrors()).containsEntry("field2", "error message 2");
    }

    @Test
    void handleDepartmentNotFound_shouldReturnNotFound() {
        // Given
        DepartmentNotFoundException exception = new DepartmentNotFoundException("05");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleDepartmentNotFound(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains("05");
    }

    @Test
    void handleMunicipalityNotFound_shouldReturnNotFound() {
        // Given
        MunicipalityNotFoundException exception = new MunicipalityNotFoundException("001");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleMunicipalityNotFound(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains("001");
    }

    @Test
    void handleDuplicateDepartmentCode_shouldReturnConflict() {
        // Given
        DuplicateDepartmentCodeException exception = new DuplicateDepartmentCodeException("05");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleDuplicateDepartmentCode(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).contains("05");
    }

    @Test
    void handleDuplicateMunicipalityCode_shouldReturnConflict() {
        // Given
        DuplicateMunicipalityCodeException exception = 
            new DuplicateMunicipalityCodeException("001");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleDuplicateMunicipalityCode(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).contains("001");
    }

    @Test
    void handleGeographyConstraint_shouldReturnConflict() {
        // Given
        GeographyConstraintException exception = new GeographyConstraintException("Geography constraint violation");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleGeographyConstraint(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).isEqualTo("Geography constraint violation");
    }

    @Test
    void handleInvalidGeography_shouldReturnBadRequest() {
        // Given
        InvalidGeographyException exception = new InvalidGeographyException("Invalid geography");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleInvalidGeography(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid geography");
    }

    @Test
    void handlePaymentMethodNotFound_shouldReturnNotFound() {
        // Given
        PaymentMethodNotFoundException exception = new PaymentMethodNotFoundException("01");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handlePaymentMethodNotFound(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains("01");
    }

    @Test
    void handleDuplicatePaymentMethodCode_shouldReturnConflict() {
        // Given
        DuplicatePaymentMethodCodeException exception = new DuplicatePaymentMethodCodeException("01");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleDuplicatePaymentMethodCode(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).contains("01");
    }

    @Test
    void handleInvalidPaymentMethodCode_shouldReturnBadRequest() {
        // Given
        InvalidPaymentMethodCodeException exception = new InvalidPaymentMethodCodeException("invalid");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleInvalidPaymentMethodCode(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).contains("invalid");
    }

    @Test
    void handleInvalidPaymentMethodData_shouldReturnBadRequest() {
        // Given
        InvalidPaymentMethodDataException exception = new InvalidPaymentMethodDataException("Invalid data");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleInvalidPaymentMethodData(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid data");
    }

    @Test
    void handlePaymentMethodConstraint_shouldReturnConflict() {
        // Given
        PaymentMethodConstraintException exception = new PaymentMethodConstraintException("Constraint violation");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handlePaymentMethodConstraint(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).isEqualTo("Constraint violation");
    }

    @Test
    void handleIllegalState_shouldReturnBadRequest() {
        // Given
        IllegalStateException exception = new IllegalStateException("Illegal state");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleIllegalState(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Illegal state");
    }

    @Test
    void handleTaxTypeNotFound_shouldReturnNotFound() {
        // Given
        TaxTypeNotFoundException exception = new TaxTypeNotFoundException("IVA19");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            exceptionHandler.handleTaxTypeNotFound(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains("IVA19");
    }

    @Test
    void handleDuplicateTaxTypeCode_shouldReturnConflict() {
        // Given
        DuplicateTaxTypeCodeException exception = new DuplicateTaxTypeCodeException("IVA19");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            exceptionHandler.handleDuplicateTaxTypeCode(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).contains("IVA19");
    }

    @Test
    void handleInvalidTaxTypeCode_shouldReturnBadRequest() {
        // Given
        InvalidTaxTypeCodeException exception = new InvalidTaxTypeCodeException("invalid");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            exceptionHandler.handleInvalidTaxTypeCode(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).contains("invalid");
    }

    @Test
    void handleInvalidTaxPercentage_shouldReturnBadRequest() {
        // Given
        InvalidTaxPercentageException exception = new InvalidTaxPercentageException("percentage");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            exceptionHandler.handleInvalidTaxPercentage(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).contains("percentage");
    }

    @Test
    void handleInvalidTaxTypeData_shouldReturnBadRequest() {
        // Given
        InvalidTaxTypeDataException exception = new InvalidTaxTypeDataException("Invalid tax data");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            exceptionHandler.handleInvalidTaxTypeData(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid tax data");
    }

    @Test
    void handleTaxTypeConstraint_shouldReturnConflict() {
        // Given
        TaxTypeConstraintException exception = new TaxTypeConstraintException("Constraint violation");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            exceptionHandler.handleTaxTypeConstraint(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).isEqualTo("Constraint violation");
    }

    @Test
    void handleUnitOfMeasureNotFound_shouldReturnNotFound() {
        UnitOfMeasureNotFoundException exception = new UnitOfMeasureNotFoundException("uom-not-found");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            exceptionHandler.handleUnitOfMeasureNotFound(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(404);
        assertThat(response.getBody().getError()).isEqualTo("Not Found");
        assertThat(response.getBody().getMessage()).contains("uom-not-found");
    }

    @Test
    void handleDuplicateUnitOfMeasure_shouldReturnConflictForName() {
        DuplicateUnitOfMeasureNameException exception = new DuplicateUnitOfMeasureNameException("Caja");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            exceptionHandler.handleDuplicateUnitOfMeasure(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).contains("Caja");
    }

    @Test
    void handleDuplicateUnitOfMeasure_shouldReturnConflictForAbbreviation() {
        DuplicateUnitOfMeasureAbbreviationException exception = new DuplicateUnitOfMeasureAbbreviationException("KG");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            exceptionHandler.handleDuplicateUnitOfMeasure(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).contains("KG");
    }

    @Test
    void handleInvalidUnitOfMeasureData_shouldReturnBadRequest() {
        InvalidUnitOfMeasureDataException exception = new InvalidUnitOfMeasureDataException("Invalid unit");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            exceptionHandler.handleInvalidUnitOfMeasureData(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid unit");
    }

    @Test
    void handleUnitOfMeasureInUse_shouldReturnConflict() {
        UnitOfMeasureInUseException exception = new UnitOfMeasureInUseException("Unit in use");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            exceptionHandler.handleUnitOfMeasureInUse(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(409);
        assertThat(response.getBody().getError()).isEqualTo("Conflict");
        assertThat(response.getBody().getMessage()).isEqualTo("Unit in use");
    }

    @Test
    void handleGenericException_shouldReturnInternalServerError() {
        // Given
        Exception exception = new Exception("Unexpected error");

        // When
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            exceptionHandler.handleGenericException(exception);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void errorResponse_shouldHaveCorrectStructure() {
        // When
        GlobalExceptionHandler.ErrorResponse errorResponse = 
            GlobalExceptionHandler.ErrorResponse.builder()
                .status(404)
                .error("Not Found")
                .message("Resource not found")
                .build();

        // Then
        assertThat(errorResponse.getStatus()).isEqualTo(404);
        assertThat(errorResponse.getError()).isEqualTo("Not Found");
        assertThat(errorResponse.getMessage()).isEqualTo("Resource not found");
    }

    @Test
    void validationErrorResponse_shouldHaveCorrectStructure() {
        // When
        GlobalExceptionHandler.ValidationErrorResponse errorResponse = 
            GlobalExceptionHandler.ValidationErrorResponse.builder()
                .status(400)
                .error("Validation Failed")
                .message("Invalid input")
                .validationErrors(java.util.Map.of("field", "error"))
                .build();

        // Then
        assertThat(errorResponse.getStatus()).isEqualTo(400);
        assertThat(errorResponse.getError()).isEqualTo("Validation Failed");
        assertThat(errorResponse.getMessage()).isEqualTo("Invalid input");
        assertThat(errorResponse.getValidationErrors()).containsEntry("field", "error");
    }
}
