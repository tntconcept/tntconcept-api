ALTER TABLE Project
    ADD blockDate date;

ALTER TABLE Project
    ADD blockedByUser int;

ALTER TABLE Project ADD CONSTRAINT `fk_user_id` FOREIGN KEY (`blockedByUser`) REFERENCES `User` (`id`);

INSERT INTO archimedes_security_role (name) VALUES ('project-blocker');