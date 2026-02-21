package com.jcuadrado.erplitebackend.domain.model.security;

public enum AuditAction {
    LOGIN,
    LOGOUT,
    LOGIN_FAILED,
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED,
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED,
    ROLE_CREATED,
    ROLE_UPDATED,
    ROLE_DELETED,
    PERMISSION_DENIED,
    CREATE,
    UPDATE,
    DELETE
}
