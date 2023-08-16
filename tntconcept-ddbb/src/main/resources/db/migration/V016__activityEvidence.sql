CREATE TABLE ActivityEvidence
(
    activityId   int         NOT NULL,
    attachmentId VARCHAR(36) NOT NULL,
    insertDate   DATETIME    NOT NULL DEFAULT NOW(),

    PRIMARY KEY (activityId),
    CONSTRAINT fk_activity_id FOREIGN KEY (activityId) REFERENCES Activity (id),
    CONSTRAINT fk_attachment_id FOREIGN KEY (attachmentId) REFERENCES Attachment (id)
) ENGINE = innodb
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_spanish_ci;

ALTER TABLE Activity DROP COLUMN hasEvidences;