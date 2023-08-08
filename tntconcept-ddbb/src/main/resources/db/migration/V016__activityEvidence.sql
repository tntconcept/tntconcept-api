CREATE TABLE Activity_Evidence
(
    activity_id      int         NOT NULL,
    attachment_id    VARCHAR(36) NOT NULL,
    insert_timestamp DATETIME    NOT NULL,

    PRIMARY KEY (activity_id)

) ENGINE = innodb
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_spanish_ci;

ALTER TABLE Activity_Evidence
    ADD CONSTRAINT fk_activity_id FOREIGN KEY (activity_id) REFERENCES Activity (id);
ALTER TABLE Activity_Evidence
    ADD CONSTRAINT fk_attachment_id FOREIGN KEY (attachment_id) REFERENCES Attachment (id);