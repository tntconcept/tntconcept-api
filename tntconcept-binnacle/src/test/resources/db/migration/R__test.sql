-- User
INSERT INTO User(id, name, login, password, passwordExpireDate, roleId, categoryId, startDate, workingInClient, email,
                 photo, married, childrenNumber, dayDuration, agreementId, nif, academicQualification, phone, mobile,
                 street, city,
                 postalCode, drivenLicenseType, vehicleType, licensePlate, socialSecurityNumber, bank, account,
                 travelAvailability,
                 genre, salary, salaryExtras, securityCard, healthInsurance, notes, contractObservations)
VALUES (11, 'Usuario de prueba 1', 'usuario.prueba1', 'BMS0Tp2pdyCiYnI8amMaU1QX', NULL, 1, 1, '2022-04-04', 0,
        'usuario.prueba1@example.com', 'photo', 0, 0, 480, 1, '12345678X', 'OK', '666222333', '666555777', '', '', '',
        '', '', '', '', '', '', '', 'MAN', 0.0, 0.0, '', '', '', '');

INSERT INTO User(id, name, login, password, passwordExpireDate, roleId, categoryId, startDate, workingInClient, email,
                 photo, married, childrenNumber, dayDuration, agreementId, nif, academicQualification, phone, mobile,
                 street, city,
                 postalCode, drivenLicenseType, vehicleType, licensePlate, socialSecurityNumber, bank, account,
                 travelAvailability,
                 genre, salary, salaryExtras, securityCard, healthInsurance, notes, contractObservations)
VALUES (12, 'Usuario de prueba 2', 'usuario.prueba2', 'BMS0Tp2pdyCiYnI8amMaU1QX', NULL, 1, 1, '2022-04-04', 0,
        'usuario.prueba2@example.com', 'photo', 0, 0, 480, 1, '12345677X', 'OK', '666222333', '666555777', '', '', '',
        '', '', '', '', '', '', '', 'MAN', 0.0, 0.0, '', '', '', '');


-- Organization id = 3
INSERT INTO Organization (id, organizationTypeId, organizationISOCategoryId, name, documentNumber, phone,
                          street,
                          number, locator, postalCode, city, provinceId, state, countryId, fax, email,
                          website, ftpsite,
                          notes, ownerId, departmentId, evaluationCriteria, organizationDocCategoryId,
                          freelance,
                          insertDate, updateDate)
VALUES (3, 1, 1, 'Hermiston, Zieme and Monahan', '', '', '', '', '', '', '', 53, '', 1, '', '', '', '', '', 1, 1, null,
        1, 0, '2023-04-24 06:33:16', DEFAULT);

-- Project id = 6
INSERT INTO Project (id, organizationId, startDate, endDate, open, name, description, ownerId, departmentId,
                     billable, offerId, insertDate, updateDate)
VALUES (6, 3, '2023-04-24', null, 1, 'Seize distributed niches', '', null, null, DEFAULT, null, '2023-04-24 06:33:16',
        '2023-04-24 06:33:21');

-- Project role with weekly evidence
INSERT INTO ProjectRole (id, projectId, name, costPerHour, expectedHours, requireEvidence, ownerId,
                         departmentId, insertDate, updateDate, maxAllowed, timeUnit, isWorkingTime,
                         isApprovalRequired)
VALUES (6, 6, 'Project 6 weekly', 0.00, 0, 'WEEKLY', null, null, '2023-04-24 06:33:16', '2023-04-24 06:33:20', 0,
        'MINUTES', 1, 0);

-- Project role with evidence once
INSERT INTO ProjectRole (id, projectId, name, costPerHour, expectedHours, requireEvidence, ownerId,
                         departmentId, insertDate, updateDate, maxAllowed, timeUnit, isWorkingTime,
                         isApprovalRequired)
VALUES (7, 6, 'Project 6 once', 0.00, 0, 'ONCE', null, null, '2023-04-24 06:33:16', '2023-04-24 06:33:20', 0,
        'DAYS', 1, 0);

-- Project id = 7
INSERT INTO Project (id, organizationId, startDate, endDate, open, name, description, ownerId, departmentId, billable,
                     offerId, insertDate, updateDate)
VALUES (7, 3, '2023-04-24', null, 1, 'Envisioneer one-to-one e-services', '', null, null, DEFAULT, null,
        '2023-04-24 06:33:16', '2023-04-24 06:33:21');

-- Project weekly
INSERT INTO ProjectRole (id, projectId, name, costPerHour, expectedHours, requireEvidence, ownerId, departmentId,
                         insertDate, updateDate, maxAllowed, timeUnit, isWorkingTime, isApprovalRequired)
VALUES (8, 7, 'Project 7 weekly', 0.00, 0, 'WEEKLY', null, null, '2023-04-24 06:33:16', '2023-04-24 06:33:20', 0,
        'MINUTES', 1, 0);

-- Project once
INSERT INTO ProjectRole (id, projectId, name, costPerHour, expectedHours, requireEvidence, ownerId, departmentId,
                         insertDate, updateDate, maxAllowed, timeUnit, isWorkingTime, isApprovalRequired)
VALUES (9, 7, 'Project 7 once', 0.00, 0, 'ONCE', null, null, '2023-04-24 06:33:16', '2023-04-24 06:33:20', 0, 'MINUTES',
        1, 0);

-- Project none
INSERT INTO ProjectRole (id, projectId, name, costPerHour, expectedHours, requireEvidence, ownerId, departmentId,
                         insertDate, updateDate, maxAllowed, timeUnit, isWorkingTime, isApprovalRequired)
VALUES (10, 7, 'Project 7 none', 0.00, 0, 'NO', null, null, '2023-04-24 06:33:16', '2023-04-24 06:33:20', 0, 'MINUTES',
        1, 0);


-- PROJECT-ROLE for ProjectRoleDaoIT
-- Project id 8
INSERT INTO Project (id, organizationId, startDate, endDate, open, name, description, ownerId, departmentId, billable,
                     offerId, insertDate, updateDate)
VALUES (8, 3, '2023-04-24', null, 1, 'Project with roles for testing', '', null, null, DEFAULT, null,
        '2023-04-24 06:33:16', '2023-04-24 06:33:21');

INSERT INTO ProjectRole (id, projectId, name, costPerHour, expectedHours, requireEvidence, ownerId, departmentId,
                         insertDate, updateDate, maxAllowed, timeUnit, isWorkingTime, isApprovalRequired)
VALUES (11, 8, 'Project no working time', 0.00, 0, 'WEEKLY', null, null, '2023-04-24 06:33:16', '2023-04-24 06:33:20',
        0, 'MINUTES', 0, 0);

INSERT INTO ProjectRole (id, projectId, name, costPerHour, expectedHours, requireEvidence, ownerId, departmentId,
                         insertDate, updateDate, maxAllowed, timeUnit, isWorkingTime, isApprovalRequired)
VALUES (12, 8, 'Project no working time 2', 0.00, 0, 'WEEKLY', null, null, '2023-04-24 06:33:16', '2023-04-24 06:33:20',
        0, 'MINUTES', 0, 0);