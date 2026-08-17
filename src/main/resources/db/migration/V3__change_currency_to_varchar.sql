-- char(3) blank-pads shorter values, so 'GB' stored in char(3) reads back as
-- 'GB '. varchar(3) with the existing regex check constraint gives the same
-- guarantee without padding, and avoids a Hibernate-specific type annotation
-- in the entity. V1 cannot be edited: Flyway checksums applied migrations, so
-- schema changes are additive.
ALTER TABLE account
    ALTER COLUMN currency TYPE varchar(3);