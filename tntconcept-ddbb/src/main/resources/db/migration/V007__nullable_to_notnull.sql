-- DocumentCategory table
UPDATE `DocumentCategory`
    SET `description` = ''
    WHERE `description` IS NULL;

    -- `code` breaks logic of creating user
    -- `documentslastupdate` is equivalent to updateDate

ALTER TABLE `DocumentCategory`
    MODIFY COLUMN `description` varchar(4096) NOT NULL;

-- Project table
UPDATE `Project`
    SET `description` = ''
    WHERE `description` IS NULL;

UPDATE `Project`
    SET `open` = FALSE
    WHERE `open` IS NULL;
    -- TinyInt¿?

ALTER TABLE `Project`
    MODIFY COLUMN `description` varchar(4096) NOT NULL,
    MODIFY COLUMN `open` boolean NOT NULL;

-- Activity table
UPDATE `Activity`
    SET `description` = ''
    WHERE `description` IS NULL;

ALTER TABLE `Activity`
    MODIFY COLUMN `description` varchar(2048) NOT NULL;

-- Idea table
UPDATE `Idea`
    SET `cost` = ''
    WHERE `cost` IS NULL;

UPDATE `Idea`
    SET `benefits` = ''
    WHERE `benefits` IS NULL;

ALTER TABLE `Idea`
    MODIFY COLUMN `cost`  varchar(500) NOT NULL,
    MODIFY COLUMN `benefits` varchar(2048) NOT NULL;

-- Inventory table
UPDATE `Inventory`
    SET `cost` = 0
    WHERE `cost` IS NULL;

UPDATE `Inventory`
    SET `provider` = ''
    WHERE `provider` IS NULL;

UPDATE `Inventory`
    SET `trademark` = ''
    WHERE `trademark` IS NULL;

UPDATE `Inventory`
    SET `model` = ''
    WHERE `model` IS NULL;

UPDATE `Inventory`
    SET `speed` = ''
    WHERE `speed` IS NULL;

UPDATE `Inventory`
    SET `storage` = ''
    WHERE `storage` IS NULL;

UPDATE `Inventory`
    SET `ram` = ''
    WHERE `ram` IS NULL;

UPDATE `Inventory`
    SET `location` = ''
    WHERE `location` IS NULL;

UPDATE `Inventory`
    SET `description` = ''
    WHERE `description` IS NULL;

ALTER TABLE `Inventory`
    MODIFY COLUMN `cost` decimal(10, 2) NOT NULL,
    MODIFY COLUMN `provider` varchar(128) NOT NULL,
    MODIFY COLUMN `trademark` varchar(128) NOT NULL,
    MODIFY COLUMN `model` varchar(128) NOT NULL,
    MODIFY COLUMN `speed` varchar(10) NOT NULL,
    MODIFY COLUMN `storage` varchar(10) NOT NULL,
    MODIFY COLUMN `ram` varchar(10) NOT NULL,
    MODIFY COLUMN `location` varchar(128) NOT NULL,
    MODIFY COLUMN `description` varchar(256) NOT NULL;

-- Frequency table
UPDATE `Frequency`
    SET `months` = 0
    WHERE `months` IS NULL;

ALTER TABLE `Frequency`
    MODIFY COLUMN `months` INTEGER NOT NULL;

-- PeriodicalAccountEntry table
UPDATE `PeriodicalAccountEntry`
    SET `rise` = 0
    WHERE `rise` IS NULL;

UPDATE `PeriodicalAccountEntry`
    SET `observations` = ''
    WHERE `observations` IS NULL;

ALTER TABLE `PeriodicalAccountEntry`
    MODIFY COLUMN `rise` decimal(4, 2) NOT NULL,
    MODIFY COLUMN `observations` varchar(1024) NOT NULL;

-- RequestHoliday table
UPDATE `RequestHoliday`
    SET `observations` = ''
    WHERE `observations` IS NULL;

UPDATE `RequestHoliday`
    SET `userComment` = ''
    WHERE `userComment` IS NULL;

ALTER TABLE `RequestHoliday`
    MODIFY COLUMN `observations` varchar(1024) NOT NULL,
    MODIFY COLUMN `userComment` varchar(1024) NOT NULL;

-- CreditTitle table
UPDATE `CreditTitle`
    SET `concept` = ''
    WHERE `concept` IS NULL;

UPDATE `CreditTitle`
    SET `state` = ''
    WHERE `state` IS NULL;

UPDATE `CreditTitle`
    SET `type` = ''
    WHERE `type` IS NULL;

UPDATE `CreditTitle`
    SET `observations` = ''
    WHERE `observations` IS NULL;

ALTER TABLE `CreditTitle`
    MODIFY COLUMN `concept` varchar(1024) NOT NULL,
    MODIFY COLUMN `state` varchar(16) NOT NULL,
    MODIFY COLUMN `type` varchar(16) NOT NULL,
    MODIFY COLUMN `observations` varchar(1024) NOT NULL;

-- Commissioning table
UPDATE `Commissioning`
    SET `scope` = ''
    WHERE `scope` IS NULL;

UPDATE `Commissioning`
    SET `content` = ''
    WHERE `content` IS NULL;

UPDATE `Commissioning`
    SET `products` = ''
    WHERE `products` IS NULL;

UPDATE `Commissioning`
    SET `budget` = 0
    WHERE `budget` IS NULL;

UPDATE `Commissioning`
    SET `notes` = ''
    WHERE `notes` IS NULL;

UPDATE `Commissioning`
    SET `developedActivities` = ''
    WHERE `developedActivities` IS NULL;

UPDATE `Commissioning`
    SET `difficultiesAppeared` = ''
    WHERE `difficultiesAppeared` IS NULL;

UPDATE `Commissioning`
    SET `results` = ''
    WHERE `results` IS NULL;

UPDATE `Commissioning`
    SET `conclusions` = ''
    WHERE `conclusions` IS NULL;

UPDATE `Commissioning`
    SET `evaluation` = ''
    WHERE `evaluation` IS NULL;

UPDATE `Commissioning`
    SET `status` = ''
    WHERE `status` IS NULL;

ALTER TABLE `Commissioning`
    MODIFY COLUMN `scope` varchar(1024) NOT NULL,
    MODIFY COLUMN `content` varchar(1024) NOT NULL,
    MODIFY COLUMN `products` varchar(1024) NOT NULL,
    MODIFY COLUMN `budget` decimal(10, 2) NOT NULL,
    MODIFY COLUMN `notes` varchar(1024) NOT NULL,
    MODIFY COLUMN `developedActivities` varchar(1024) NOT NULL,
    MODIFY COLUMN `difficultiesAppeared` varchar(1024) NOT NULL,
    MODIFY COLUMN `results` varchar(1024) NOT NULL,
    MODIFY COLUMN `conclusions` varchar(1024) NOT NULL,
    MODIFY COLUMN `evaluation` varchar(1024) NOT NULL,
    MODIFY COLUMN `status` varchar(20) NOT NULL;

-- CommissioningDelay table
UPDATE `CommissioningDelay`
    SET `status` = ''
    WHERE `status` IS NULL;

ALTER TABLE `CommissioningDelay`
    MODIFY COLUMN `status` varchar(20) NOT NULL;

-- CommissioningPaymentData table
UPDATE `CommissioningPaymentData`
    SET `paymentMode` = ''
    WHERE `paymentMode` IS NULL;

UPDATE `CommissioningPaymentData`
    SET `bankAccount` = ''
    WHERE `bankAccount` IS NULL;

UPDATE `CommissioningPaymentData`
    SET `billNumber` = ''
    WHERE `billNumber` IS NULL;

ALTER TABLE `CommissioningPaymentData`
    MODIFY COLUMN `paymentMode` varchar(32) NOT NULL,
    MODIFY COLUMN `bankAccount` varchar(50) NOT NULL,
    MODIFY COLUMN `billNumber` varchar(50) NOT NULL;