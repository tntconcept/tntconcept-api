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