ALTER TABLE ProjectRole
    ADD maxAllowedByActivity INT NOT NULL DEFAULT 0;

ALTER TABLE ProjectRole
    MODIFY COLUMN maxAllowedByActivity INT NOT NULL;