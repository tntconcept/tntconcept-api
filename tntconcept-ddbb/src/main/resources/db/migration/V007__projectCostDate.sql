ALTER TABLE ProjectCost
ADD COLUMN date Date;

UPDATE ProjectCost pc
SET date = (SELECT startDate from Project p where p.id = pc.projectId);

ALTER TABLE ProjectCost
MODIFY date Date NOT NULL;