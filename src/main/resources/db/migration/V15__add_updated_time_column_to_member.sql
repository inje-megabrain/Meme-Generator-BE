ALTER TABLE `member`
    ADD COLUMN `updated_time` DATETIME NULL DEFAULT NULL AFTER `created_time`;