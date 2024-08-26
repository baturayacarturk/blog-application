-- V2__User_Table_Refactoring.sql
ALTER TABLE "user"
ADD COLUMN password VARCHAR(255) NOT NULL;

ALTER TABLE "user"
ADD COLUMN display_name VARCHAR(255);

ALTER TABLE "user"
DROP COLUMN first_name;

ALTER TABLE "user"
DROP COLUMN last_name;