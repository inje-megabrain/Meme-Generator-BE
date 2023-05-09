ALTER TABLE `wanted`
    ADD COLUMN `member_id` bigint NOT NULL,
    ADD CONSTRAINT `fk_wanted_member_id` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`);