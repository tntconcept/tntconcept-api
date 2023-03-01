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
