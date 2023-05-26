ALTER TABLE `item`
    ADD COLUMN `member_id` bigint NOT NULL,
    ADD CONSTRAINT `fk_item_member_id` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`);