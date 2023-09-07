CREATE TABLE `ActivityAttachment` (
    activityId      INT NOT NULL,
    attachmentId    VARCHAR(36)  NOT NULL,
    insertDate      DATETIME  NOT NULL DEFAULT NOW(),
    PRIMARY KEY (activityId, attachmentId),
    CONSTRAINT FK_ACTIVITY_ID FOREIGN KEY (activityId) REFERENCES Activity(id),
    CONSTRAINT FK_ATTACHMENT_ID FOREIGN KEY (attachmentId) REFERENCES Attachment(id)
) ENGINE = innodb
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_spanish_ci;