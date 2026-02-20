package com.jcuadrado.erplitebackend.domain.service.warehouse;

import com.jcuadrado.erplitebackend.domain.exception.warehouse.InvalidWarehouseDataException;

public class WarehouseValidator {

    private static final java.util.regex.Pattern CODE_PATTERN =
            java.util.regex.Pattern.compile("[A-Z0-9\\-]+");

    private static final java.util.regex.Pattern EMAIL_PATTERN =
            java.util.regex.Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private static final java.util.regex.Pattern PHONE_PATTERN =
            java.util.regex.Pattern.compile("[0-9+\\-\\s]{7,20}");

    public void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new InvalidWarehouseDataException("El código de la bodega es requerido");
        }
        if (code.length() < 3 || code.length() > 20) {
            throw new InvalidWarehouseDataException("El código debe tener entre 3 y 20 caracteres");
        }
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new InvalidWarehouseDataException("El código solo puede contener letras mayúsculas, números y guiones");
        }
    }

    public void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidWarehouseDataException("El nombre de la bodega es requerido");
        }
        if (name.length() < 3 || name.length() > 100) {
            throw new InvalidWarehouseDataException("El nombre debe tener entre 3 y 100 caracteres");
        }
    }

    public void validateType(com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType type) {
        if (type == null) {
            throw new InvalidWarehouseDataException("El tipo de bodega es requerido");
        }
    }

    public void validateEmail(String email) {
        if (email != null && !email.isBlank() && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidWarehouseDataException("El formato del email no es válido");
        }
    }

    public void validatePhone(String phone) {
        if (phone != null && !phone.isBlank() && !PHONE_PATTERN.matcher(phone).matches()) {
            throw new InvalidWarehouseDataException("El formato del teléfono no es válido");
        }
    }

    public void validateAll(String code, String name,
                            com.jcuadrado.erplitebackend.domain.model.warehouse.WarehouseType type,
                            String email, String phone) {
        validateCode(code);
        validateName(name);
        validateType(type);
        validateEmail(email);
        validatePhone(phone);
    }
}
