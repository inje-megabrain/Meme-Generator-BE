CREATE TABLE `meme_like_member` (
    `meme_id` BIGINT NOT NULL,
    `member_id` BIGINT NOT NULL,
    `liked_time` DATETIME NOT NULL,
    PRIMARY KEY (`meme_id`, `member_id`),
    CONSTRAINT `fk_meme_like_member_meme_id` FOREIGN KEY (`meme_id`) REFERENCES `meme` (`meme_id`),
    CONSTRAINT `fk_meme_like_member_member_id` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;