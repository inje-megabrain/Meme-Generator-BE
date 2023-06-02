ALTER TABLE `meme`
    ADD COLUMN `like_count` INT(11) NOT NULL DEFAULT 0 AFTER `view_count`;
