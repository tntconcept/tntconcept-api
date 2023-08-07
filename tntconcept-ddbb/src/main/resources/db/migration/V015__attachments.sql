CREATE TABLE Attachment (
    id              VARCHAR(36)  NOT NULL,
    type            VARCHAR(25)  NOT NULL,
    path            VARCHAR(255) NOT NULL,
    fileName        VARCHAR(100) NOT NULL,
    mimeType        VARCHAR(100) NOT NULL,
    uploadTimestamp DATETIME     NOT NULL,
    isTemporary     BOOLEAN      NOT NULL,

    PRIMARY KEY (id)

) ENGINE = innodb
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_spanish_ci;