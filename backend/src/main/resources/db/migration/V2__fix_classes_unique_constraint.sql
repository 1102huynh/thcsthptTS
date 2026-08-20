-- =====================================================
-- Fix classes.class_name unique constraint
-- =====================================================
-- V1 baseline generated a UNIQUE key on `class_name` ALONE (Hibernate
-- ddl-auto=update artifact from before this table had a `section`
-- column), which means only one class named e.g. "10" could ever exist
-- at a time — a second row like ("10","B") silently violates it.
-- Confirmed in practice: TEST_DATA_CORRECTED.sql inserts 4 classes
-- ("10","A"), ("10","B"), ("9","A"), ("9","B") via INSERT IGNORE, but
-- only 2 survive because of this bug.
--
-- The real invariant is: class name + section + academic year together
-- identify a class. Replace the single-column key with that composite
-- unique key, and require academic_year (every row inserted so far
-- already has one).
-- =====================================================

ALTER TABLE classes DROP INDEX UK_mgg5753yel6celk0t48duc5jx;
ALTER TABLE classes MODIFY COLUMN academic_year VARCHAR(255) NOT NULL;
ALTER TABLE classes ADD CONSTRAINT uk_classes_name_section_year UNIQUE (class_name, section, academic_year);
