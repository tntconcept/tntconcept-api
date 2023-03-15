-- -------------------------------------------------------------------------------------------------------
-- Security authentication and authorization
--

CREATE TABLE archimedes_security_role
(
    role             varchar(32) NOT NULL PRIMARY KEY,

    audit_created_at datetime    NOT NULL DEFAULT NOW(),
    audit_updated_at datetime    NOT NULL DEFAULT NOW()
);

CREATE TRIGGER archimedes_security_role_audit
    BEFORE UPDATE
    ON archimedes_security_role
    FOR EACH ROW SET NEW.audit_updated_at = NOW();


CREATE TABLE archimedes_security_subject
(
    id               integer     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    principal_name   varchar(64) NOT NULL UNIQUE,
    attributes       varchar(64) NOT NULL,

    audit_created_at datetime    NOT NULL DEFAULT NOW(),
    audit_updated_at datetime    NOT NULL DEFAULT NOW()
);

CREATE TRIGGER archimedes_security_subject_audit
    BEFORE UPDATE
    ON archimedes_security_subject
    FOR EACH ROW SET NEW.audit_updated_at = NOW();


CREATE TABLE archimedes_security_subject_role_relation
(
    subject_id       integer     NOT NULL REFERENCES archimedes_security_subject (id),
    role             varchar(32) NOT NULL REFERENCES archimedes_security_role (role),

    audit_created_at datetime    NOT NULL DEFAULT NOW(),
    audit_updated_at datetime    NOT NULL DEFAULT NOW(),

    PRIMARY KEY (subject_id, role),
    INDEX (subject_id)
);

CREATE TRIGGER archimedes_security_subject_role_relation_audit
    BEFORE UPDATE
    ON archimedes_security_subject_role_relation
    FOR EACH ROW SET NEW.audit_updated_at = NOW();


CREATE TABLE archimedes_security_password
(
    subject_id       integer     NOT NULL PRIMARY KEY REFERENCES archimedes_security_subject (id),
    secret           varchar(64) NOT NULL,

    audit_created_at datetime    NOT NULL DEFAULT NOW(),
    audit_updated_at datetime    NOT NULL DEFAULT NOW()
);

CREATE TRIGGER archimedes_security_password_audit
    BEFORE UPDATE
    ON archimedes_security_password
    FOR EACH ROW SET NEW.audit_updated_at = NOW();


CREATE TABLE archimedes_security_refresh_token
(
    refresh_token    varchar(128) NOT NULL PRIMARY KEY,
    principal_name   varchar(64)  NOT NULL UNIQUE REFERENCES archimedes_security_subject (principal_name),

    audit_created_at datetime     NOT NULL DEFAULT NOW(),
    audit_updated_at datetime     NOT NULL DEFAULT NOW()
);

CREATE TRIGGER archimedes_security_refresh_token_audit
    BEFORE UPDATE
    ON archimedes_security_refresh_token
    FOR EACH ROW SET NEW.audit_updated_at = NOW();
