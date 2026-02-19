CREATE TABLE users (
    id                   BINARY(16)   NOT NULL,
    username             VARCHAR(50)  NOT NULL,
    email                VARCHAR(100) NOT NULL,
    password_hash        VARCHAR(72)  NOT NULL,
    first_name           VARCHAR(50),
    last_name            VARCHAR(50),
    document_type_id     BINARY(16),
    document_number      VARCHAR(20),
    active               BOOLEAN      NOT NULL DEFAULT TRUE,
    failed_attempts      INT          NOT NULL DEFAULT 0,
    locked_at            DATETIME,
    last_login           DATETIME,
    last_failed_login_at DATETIME,
    created_at           DATETIME     NOT NULL,
    created_by           BINARY(16),
    updated_at           DATETIME,
    updated_by           BINARY(16),
    deleted_at           DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_username UNIQUE (username),
    CONSTRAINT uk_user_email    UNIQUE (email),
    INDEX idx_user_username  (username),
    INDEX idx_user_email     (email),
    INDEX idx_user_active    (active),
    INDEX idx_user_deleted_at (deleted_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE roles (
    id          BINARY(16)   NOT NULL,
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  DATETIME     NOT NULL,
    updated_at  DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT uk_role_name UNIQUE (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE permissions (
    id             BINARY(16)   NOT NULL,
    entity         VARCHAR(50)  NOT NULL,
    action         VARCHAR(50)  NOT NULL,
    condition_expr VARCHAR(255),
    description    VARCHAR(255),
    PRIMARY KEY (id),
    INDEX idx_permission_entity_action (entity, action)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE user_roles (
    user_id BINARY(16) NOT NULL,
    role_id BINARY(16) NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE role_permissions (
    role_id       BINARY(16) NOT NULL,
    permission_id BINARY(16) NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_role_permissions_role       FOREIGN KEY (role_id)       REFERENCES roles (id)       ON DELETE CASCADE,
    CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE refresh_tokens (
    id         BINARY(16)   NOT NULL,
    user_id    BINARY(16)   NOT NULL,
    token      VARCHAR(36)  NOT NULL,
    expires_at DATETIME     NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at DATETIME     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token UNIQUE (token),
    INDEX idx_rt_user_id   (user_id),
    INDEX idx_rt_expires_at (expires_at),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE audit_logs (
    id         BINARY(16)   NOT NULL,
    user_id    BINARY(16),
    username   VARCHAR(50),
    entity     VARCHAR(50),
    entity_id  BINARY(16),
    action     VARCHAR(50)  NOT NULL,
    old_value  TEXT,
    new_value  TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    timestamp  DATETIME     NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_audit_user_id  (user_id),
    INDEX idx_audit_entity   (entity),
    INDEX idx_audit_action   (action),
    INDEX idx_audit_timestamp (timestamp)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
