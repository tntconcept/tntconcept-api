CREATE TABLE `Expense`
(
    `id`           int UNSIGNED  NOT NULL AUTO_INCREMENT,
    `userId`       int           NOT NULL DEFAULT 1,
    `date`         datetime      NOT NULL NOW(),
    `description`  varchar(1024) NOT NULL DEFAULT '',
    `amount`       decimal(10,2) NOT NULL DEFAULT 0.0,
    `type`         varchar(64)   NOT NULL DEFAULT 'STRUCTURE',
    `state`        varchar(64)   NOT NULL DEFAULT 'PENDING',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_expense_userId` FOREIGN KEY (`userId`) REFERENCES `User` (`id`)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_spanish_ci;