ALTER TABLE Activity
    MODIFY COLUMN approvalDate datetime;

UPDATE Activity SET approvalDate = addtime(approvalDate, '12:00:00') WHERE approvalState = 'ACCEPTED';