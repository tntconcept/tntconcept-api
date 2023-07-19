ALTER TABLE Activity
    ADD approvedByUserId int;

ALTER TABLE Activity
    ADD approvalDate date;

ALTER TABLE Activity ADD CONSTRAINT `fk_activity_approvedUserId` FOREIGN KEY (`approvedByUserId`) REFERENCES `User` (`id`);