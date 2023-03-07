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

UPDATE `Publication`
    SET `accepted` = 0
    WHERE `accepted` IS NULL;

ALTER TABLE `Publication`
    MODIFY `accepted` BOOLEAN NOT NULL;

UPDATE `BulletinBoard`
    SET `documentPath` = ""
    WHERE `documentPath` IS NULL;

UPDATE `BulletinBoard`
    SET `documentContentType` = ""
    WHERE `documentContentType` IS NULL;

ALTER TABLE `BulletinBoard`
    MODIFY `documentPath` VARCHAR(128) NOT NULL,
    MODIFY `documentContentType` VARCHAR(128) NOT NULL;

UPDATE `Book`
    SET `author` = ""
    WHERE `author` IS NULL;

UPDATE `Book`
    SET `ISBN` = ""
    WHERE `ISBN` IS NULL;

UPDATE `Book`
    SET `URL` = ""
    WHERE `URL` IS NULL;

UPDATE `Book`
    SET `price` = 0
    WHERE `price` IS NULL;

ALTER TABLE `Book`
    MODIFY `author` VARCHAR(255) COLLATE utf8mb4_spanish_ci NOT NULL,
    MODIFY `ISBN` VARCHAR(13) COLLATE utf8mb4_spanish_ci NOT NULL,
    MODIFY `URL` VARCHAR(255) COLLATE utf8mb4_spanish_ci NOT NULL,
    MODIFY `price` DECIMAL(10,2) NOT NULL;

UPDATE `InteractionType`
    SET `description` = ""
    WHERE `description` IS NULL;

ALTER TABLE `InteractionType`
    MODIFY `description` VARCHAR(1024) NOT NULL;

UPDATE `Interaction`
    SET `file` = ""
    WHERE `file` IS NULL;

UPDATE `Interaction`
    SET `fileMime` = ""
    WHERE `fileMime` IS NULL;

ALTER TABLE `Interaction`
    MODIFY `file` VARCHAR(400) NOT NULL,
    MODIFY `fileMime` VARCHAR(128) NOT NULL;

UPDATE `Setting`
    SET `value` = ""
    WHERE `value` IS NULL;

ALTER TABLE `Setting`
    MODIFY `value` varchar(4096) COLLATE utf8mb4_spanish_ci NOT NULL;

UPDATE `Occupation`
    SET `description` = ""
    WHERE `description` IS NULL;

ALTER TABLE `Occupation`
    MODIFY `description` VARCHAR(1024) NOT NULL;

