DROP DATABASE IF EXISTS blogdb;
CREATE DATABASE IF NOT EXISTS blogdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

DROP USER IF EXISTS `bloguser`@`localhost`;
CREATE USER IF NOT EXISTS `bloguser`@`localhost` IDENTIFIED WITH caching_sha2_password BY 'baturayacarturk';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, REFERENCES, INDEX, ALTER, EXECUTE, CREATE VIEW, SHOW VIEW
ON blogdb.* TO `bloguser`@`localhost`;

DROP DATABASE IF EXISTS sonardb;
CREATE DATABASE IF NOT EXISTS sonardb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

DROP USER IF EXISTS `sonaruser`@`%`;
CREATE USER IF NOT EXISTS `sonaruser`@`%` IDENTIFIED WITH caching_sha2_password BY 'baturayacarturk';

GRANT ALL PRIVILEGES ON sonardb.* TO `sonaruser`@`%`;

FLUSH PRIVILEGES;
