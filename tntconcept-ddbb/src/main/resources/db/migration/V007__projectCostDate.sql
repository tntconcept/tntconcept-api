ALTER TABLE ProjectCost ADD COLUMN allocationDate DATE;

UPDATE ProjectCost pc SET allocationDate = (SELECT startDate from Project p where p.id = pc.projectId);

ALTER TABLE ProjectCost MODIFY allocationDate DATE NOT NULL;