-- liquibase formatted sql

-- changeset darya:1
CREATE INDEX IDX_STUDENT_NAME ON student(name)

-- changeset darya:2
CREATE  INDEX IND_FACULTY_NAME_COLOR ON faculty(name, color)
