ALTER TABLE ProjectRole
    ADD isWorkingTime boolean NOT NULL DEFAULT 1;

ALTER TABLE ProjectRole
    MODIFY isWorkingTime boolean NOT NULL;