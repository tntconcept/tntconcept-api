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

UPDATE `AccountEntryType`
    SET `observations` = ""
    WHERE `observations` IS NULL;

ALTER TABLE `AccountEntryType`
    MODIFY `observations` VARCHAR(1024) NOT NULL;

UPDATE `AccountEntry`
    SET `observations` = ""
    WHERE `observations` IS NULL;

ALTER TABLE `AccountEntry`
    MODIFY `observations` VARCHAR(1024) NOT NULL;

UPDATE `IVAType`
    SET `iva` = 0
    WHERE `iva` IS NULL;

UPDATE `IVAType`
    SET `name` = ""
    WHERE `name` IS NULL;

ALTER TABLE `IVAType`
    MODIFY `iva` DECIMAL(4,2) NOT NULL,
    MODIFY `name` VARCHAR(30) NOT NULL;

UPDATE `Bill`
    SET `paymentMode` = "UNKNOWN"
    WHERE `paymentMode` IS NULL;

UPDATE `Bill`
    SET `observations` = ""
    WHERE `observations` IS NULL;

UPDATE `Bill`
    SET `orderNumber` = ""
    WHERE `orderNumber` IS NULL;

ALTER TABLE `Bill`
    MODIFY `paymentMode` VARCHAR(16) NOT NULL,
    MODIFY `observations` VARCHAR(4096) NOT NULL,
    MODIFY `orderNumber` VARCHAR(64) NOT NULL;

UPDATE `Objective`
    SET `state` = "PENDING"
    WHERE `state` IS NULL;

ALTER TABLE `Objective`
    MODIFY `state` VARCHAR(16) NOT NULL;

UPDATE `Magazine`
    SET `description` = ""
    WHERE `description` IS NULL;

ALTER TABLE `Magazine`
    MODIFY `description` VARCHAR(2048) NOT NULL;

UPDATE `Tutorial`
    SET `description` = ""
    WHERE `description` IS NULL;

ALTER TABLE `Tutorial`
    MODIFY `description` VARCHAR(2048) NOT NULL;