INSERT INTO archimedes_security_role (name) VALUES ('subcontracted-activity-manager');

INSERT INTO User (id, login, password, passwordExpireDate, roleId, active, name, nif,
                  academicQualification, phone, mobile, street, city, postalCode,
                  provinceId, married, childrenNumber, drivenLicenseType,vehicleType,
                  licensePlate, startDate, categoryId, socialSecurityNumber, bank, account,
                  travelAvailability, workingInClient, email, genre, salary, salaryExtras,
                  securityCard, healthInsurance, notes, photo, contractObservations, agreementId,
                  dayDuration)
VALUES (5000, 'subcontratado', MD5('password'), NULL, 1, 1, 'subcontratado', '', '',
        '', '', ' ', 'Madrid', '', 28, 1, 2,
        '', '', '', curdate(), 1, '', '', '',
        '', 1, 'subcontratado@autentia.com','MAN',40000,10000,'','','','','', 1, 480);
