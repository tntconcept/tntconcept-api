UPDATE `Document`
    SET `name` = ""
    WHERE `name` IS NULL;

UPDATE `Document`
    SET `description` = ""
    WHERE `description` IS NULL;

UPDATE `Document`
    SET `creationDate` = `insertDate`
    WHERE `creationDate` IS NULL;

ALTER TABLE `Document`
    MODIFY `name` VARCHAR(256) NOT NULL,
    MODIFY `description` VARCHAR(4096) NOT NULL,
    MODIFY `creationDate` DATETIME NOT NULL;

UPDATE `OrganizationType`
    SET `description` = ""
    WHERE `description` IS NULL;

ALTER TABLE `OrganizationType`
    MODIFY `description` VARCHAR(1024) NOT NULL;

UPDATE `OrganizationISOCategory`
    SET `description` = ""
    WHERE `description` IS NULL;

ALTER TABLE `OrganizationISOCategory`
    MODIFY `description` VARCHAR(1024) NOT NULL;

UPDATE `WorkingAgreement`
    SET `description` = ""
    WHERE `description` IS NULL;

ALTER TABLE `WorkingAgreement`
    MODIFY `description` VARCHAR(2048) NOT NULL;

UPDATE `Account`
    SET `description` = ""
    WHERE `description` IS NULL;

ALTER TABLE `Account`
    MODIFY `description` VARCHAR(2048) NOT NULL;

UPDATE `AccountEntryGroup`
    SET `description` = ""
    WHERE `description` IS NULL;

ALTER TABLE `AccountEntryGroup`
    MODIFY `description` VARCHAR(2048) NOT NULL;