DROP TABLE IF EXISTS `Attachment`;

CREATE TABLE `Attachment` (
    id              VARCHAR(36)  NOT NULL,
    userId          INT          NOT NULL,
    path            VARCHAR(255) NOT NULL,
    fileName        VARCHAR(100) NOT NULL,
    mimeType        VARCHAR(100) NOT NULL,
    uploadDate      DATETIME     NOT NULL,
    isTemporary     BOOLEAN      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_ATTACHMENT_USER_ID FOREIGN KEY (userId) REFERENCES User(id)
) ENGINE = innodb
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_spanish_ci;