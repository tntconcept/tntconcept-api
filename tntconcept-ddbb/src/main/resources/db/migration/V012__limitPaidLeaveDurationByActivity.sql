ALTER TABLE ProjectRole
    RENAME COLUMN maxAllowed TO maxTimeAllowedByYear;

ALTER TABLE ProjectRole
    ADD maxTimeAllowedByActivity INT NOT NULL DEFAULT 0;

ALTER TABLE ProjectRole
    MODIFY COLUMN maxTimeAllowedByActivity INT NOT NULL;