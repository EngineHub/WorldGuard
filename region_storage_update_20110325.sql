SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

ALTER TABLE `region_cuboid` DROP FOREIGN KEY `fk_region_cuboid_region` ;

ALTER TABLE `region_flag` DROP FOREIGN KEY `fk_flags_region1` ;

ALTER TABLE `region_groups` DROP FOREIGN KEY `fk_region_groups_region` ;

ALTER TABLE `region_players` DROP FOREIGN KEY `fk_region_players_user` , DROP FOREIGN KEY `fk_region_players_region` ;

ALTER TABLE `region_poly2d` DROP FOREIGN KEY `fk_region_poly2d_region` ;

ALTER TABLE `region_poly2d_point` DROP FOREIGN KEY `fk_region_poly2d_point_region_poly2d` ;

ALTER TABLE `group` COLLATE = utf8_bin ;

ALTER TABLE `region_cuboid` COLLATE = utf8_bin , ADD COLUMN `world_id` INT(10) UNSIGNED NOT NULL  AFTER `region_id` , CHANGE COLUMN `min_x` `min_x` BIGINT(20) NOT NULL  AFTER `world_id` , CHANGE COLUMN `min_y` `min_y` BIGINT(20) NOT NULL  AFTER `min_x` , CHANGE COLUMN `max_x` `max_x` BIGINT(20) NOT NULL  AFTER `min_z` , CHANGE COLUMN `max_y` `max_y` BIGINT(20) NOT NULL  AFTER `max_x` , 
  ADD CONSTRAINT `fk_region_cuboid_region`
  FOREIGN KEY (`region_id` , `world_id` )
  REFERENCES `region` (`id` , `world_id` )
  ON DELETE CASCADE
  ON UPDATE CASCADE
, DROP PRIMARY KEY 
, ADD PRIMARY KEY (`region_id`, `world_id`) ;

ALTER TABLE `region_flag` COLLATE = utf8_bin , ADD COLUMN `world_id` INT(10) UNSIGNED NOT NULL  AFTER `region_id` , CHANGE COLUMN `flag` `flag` VARCHAR(45) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL  , 
  ADD CONSTRAINT `fk_flags_region`
  FOREIGN KEY (`region_id` , `world_id` )
  REFERENCES `region` (`id` , `world_id` )
  ON DELETE CASCADE
  ON UPDATE CASCADE
, DROP INDEX `fk_flags_region` 
, ADD INDEX `fk_flags_region` (`region_id` ASC, `world_id` ASC) ;

ALTER TABLE `region_groups` COLLATE = utf8_bin , ADD COLUMN `world_id` INT(10) UNSIGNED NOT NULL  AFTER `region_id` , DROP FOREIGN KEY `fk_region_groups_group` ;

ALTER TABLE `region_groups` 
  ADD CONSTRAINT `fk_region_groups_group`
  FOREIGN KEY (`group_id` )
  REFERENCES `group` (`id` )
  ON DELETE CASCADE
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_region_groups_region`
  FOREIGN KEY (`region_id` , `world_id` )
  REFERENCES `region` (`id` , `world_id` )
  ON DELETE CASCADE
  ON UPDATE CASCADE
, DROP PRIMARY KEY 
, ADD PRIMARY KEY (`region_id`, `world_id`, `group_id`) ;

ALTER TABLE `region_players` COLLATE = utf8_bin , ADD COLUMN `world_id` INT(10) UNSIGNED NOT NULL  AFTER `region_id` , 
  ADD CONSTRAINT `fk_region_users_region`
  FOREIGN KEY (`region_id` , `world_id` )
  REFERENCES `region` (`id` , `world_id` )
  ON DELETE CASCADE
  ON UPDATE CASCADE, 
  ADD CONSTRAINT `fk_region_users_user`
  FOREIGN KEY (`user_id` )
  REFERENCES `user` (`id` )
  ON DELETE CASCADE
  ON UPDATE CASCADE
, DROP PRIMARY KEY 
, ADD PRIMARY KEY (`region_id`, `world_id`, `user_id`) 
, ADD INDEX `fk_region_users_user` (`user_id` ASC) 
, DROP INDEX `fk_region_players_user` ;

ALTER TABLE `region_poly2d` COLLATE = utf8_bin , ADD COLUMN `world_id` INT(10) UNSIGNED NOT NULL  AFTER `region_id` , CHANGE COLUMN `min_y` `min_y` INT(11) NOT NULL  AFTER `world_id` , 
  ADD CONSTRAINT `fk_region_poly2d_region`
  FOREIGN KEY (`region_id` , `world_id` )
  REFERENCES `region` (`id` , `world_id` )
  ON DELETE CASCADE
  ON UPDATE CASCADE
, DROP PRIMARY KEY 
, ADD PRIMARY KEY (`region_id`, `world_id`) ;

ALTER TABLE `region_poly2d_point` COLLATE = utf8_bin , ADD COLUMN `world_id` INT(10) UNSIGNED NOT NULL  AFTER `region_id` , CHANGE COLUMN `x` `x` BIGINT(20) NOT NULL  AFTER `world_id` , 
  ADD CONSTRAINT `fk_region_poly2d_point_region_poly2d`
  FOREIGN KEY (`region_id` , `world_id` )
  REFERENCES `region_poly2d` (`region_id` , `world_id` )
  ON DELETE CASCADE
  ON UPDATE CASCADE
, DROP INDEX `fk_region_poly2d_point_region_poly2d` 
, ADD INDEX `fk_region_poly2d_point_region_poly2d` (`region_id` ASC, `world_id` ASC) ;

ALTER TABLE `user` COLLATE = utf8_bin ;

ALTER TABLE `world` COLLATE = utf8_bin ;


UPDATE `region_cuboid` AS c SET c.`world_id` = (SELECT p.`world_id` FROM `region` AS p WHERE p.`id` = c.`region_id`);

UPDATE `region_flag` AS c SET c.`world_id` = (SELECT p.`world_id` FROM `region` AS p WHERE p.`id` = c.`region_id`);

UPDATE `region_groups` AS c SET c.`world_id` = (SELECT p.`world_id` FROM `region` AS p WHERE p.`id` = c.`region_id`);

UPDATE `region_players` AS c SET c.`world_id` = (SELECT p.`world_id` FROM `region` AS p WHERE p.`id` = c.`region_id`);

UPDATE `region_poly2d` AS c SET c.`world_id` = (SELECT p.`world_id` FROM `region` AS p WHERE p.`id` = c.`region_id`);

UPDATE `region_poly2d_point` AS c SET c.`world_id` = (SELECT p.`world_id` FROM `region` AS p WHERE p.`id` = c.`region_id`);

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
