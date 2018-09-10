-- Fix WORLDGUARD-3117
-- Otherwise, you can't be both an owner and a member of a region

ALTER TABLE `${tablePrefix}region_players`
	DROP PRIMARY KEY,
	ADD PRIMARY KEY (`region_id`, `world_id`, `user_id`, `owner`);

ALTER TABLE `${tablePrefix}region_groups`
	DROP PRIMARY KEY,
	ADD PRIMARY KEY (`region_id`, `world_id`, `group_id`, `owner`);

-- Fix WORLDGUARD-3030
-- Adds UUID support

ALTER TABLE `${tablePrefix}user`
	ALTER `name` DROP DEFAULT;

ALTER TABLE `${tablePrefix}user`
	CHANGE COLUMN `name` `name` VARCHAR(64) NULL COLLATE 'utf8_bin' AFTER `id`,
	ADD COLUMN `uuid` CHAR(36) NULL AFTER `name`,
	ADD UNIQUE INDEX `uuid` (`uuid`);

-- Strings with differing numbers of trailing spaces are equal in MySQL
-- The domains have been updated to trim strings

UPDATE `${tablePrefix}user` SET `name` = TRIM(`name`);
UPDATE `${tablePrefix}group` SET `name` = TRIM(`name`);