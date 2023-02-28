-- Country table

ALTER TABLE `Country` 
    MODIFY `code`   smallint(6) NOT NULL,
    MODIFY `iso3166a1`  char(2) COLLATE utf8_spanish_ci NOT NULL,
    MODIFY `iso3166a2`  char(3) COLLATE utf8_spanish_ci NOT NULL,
    MODIFY `name`  varchar(128) COLLATE utf8_spanish_ci NOT NULL;

-- Organization table

UPDATE `Organization`
    SET `documentNumber` = ''
    WHERE `documentNumber` IS NULL;

UPDATE `Organization`
    SET `phone` = ''
    WHERE `phone` IS NULL;

UPDATE `Organization`
    SET `street` = ''
    WHERE `street` IS NULL;

UPDATE `Organization`
    SET `number` = ''
    WHERE `number` IS NULL;

UPDATE `Organization`
    SET `locator` = ''
    WHERE `locator` IS NULL;

UPDATE `Organization`
    SET `postalCode` = ''
    WHERE `postalCode` IS NULL;

UPDATE `Organization`
    SET `city` = ''
    WHERE `city` IS NULL;

UPDATE `Organization`
    SET `state` = ''
    WHERE `state` IS NULL;

UPDATE `Organization`
    SET `fax` = ''
    WHERE `fax` IS NULL;

UPDATE `Organization`
    SET `email` = ''
    WHERE `email` IS NULL;

UPDATE `Organization`
    SET `website` = ''
    WHERE `website` IS NULL;

UPDATE `Organization` 
    SET `ftpsite` = ''
    WHERE `ftpsite` IS NULL;

UPDATE `Organization`
    SET `notes` = ''
    WHERE `notes` IS NULL;
    
ALTER TABLE `Organization`
    MODIFY `documentNumber`  varchar(50)   NOT NULL,
    MODIFY `phone`   varchar(15)    NOT NULL,
    MODIFY `street`  varchar(256)   NOT NULL,
    MODIFY `number`  varchar(16) COMMENT 'Building number in street'    NOT NULL,
    MODIFY `locator` varchar(256) COMMENT 'Location information inside building'    NOT NULL,
    MODIFY `postalCode` varchar(32) NOT NULL,
    MODIFY `city`  varchar(256)   NOT NULL,
    MODIFY `state`  varchar(256)   NOT NULL,
    MODIFY `fax`  varchar(16) NOT NULL,
    MODIFY `email`  varchar(256)   NOT NULL,
    MODIFY `website`  varchar(256)   NOT NULL,
    MODIFY `ftpsite`  varchar(256)   NOT NULL,
    MODIFY `notes`  varchar(1024)   NOT NULL;

-- Contact table

UPDATE `Contact`
    SET `email` = ''
    WHERE `email` IS NULL;

UPDATE `Contact`
    SET `phone` = ''
    WHERE `phone` IS NULL;

UPDATE `Contact`
    SET `mobile` = ''
    WHERE `mobile` IS NULL;

UPDATE `Contact`
    SET `email2` = ''
    WHERE `email2` IS NULL;

UPDATE `Contact`
    SET `phone2` = ''
    WHERE `phone2` IS NULL;

UPDATE `Contact`
    SET `fax` = ''
    WHERE `fax` IS NULL;

UPDATE `Contact`
    SET `address` = ''
    WHERE `address` IS NULL;

UPDATE `Contact`
    SET `postalCode` = ''
    WHERE `postalCode` IS NULL;

UPDATE `Contact`
    SET `city` = ''
    WHERE `city` IS NULL;

UPDATE `Contact`
    SET `country` = ''
    WHERE `country` IS NULL;

ALTER TABLE `Contact`
    MODIFY `email`   varchar(128)    NOT NULL,
    MODIFY `phone`  varchar(15)   NOT NULL,
    MODIFY `mobile`  varchar(15)   NOT NULL,
    MODIFY `email2`  varchar(128)   NOT NULL,
    MODIFY `phone2`  varchar(15)   NOT NULL,
    MODIFY `fax`  varchar(15)   NOT NULL,
    MODIFY `address`  varchar(100)   NOT NULL,
    MODIFY `postalCode`  varchar(5)   NOT NULL,
    MODIFY `city`  varchar(100)   NOT NULL,
    MODIFY `country`  varchar(100)   NOT NULL;