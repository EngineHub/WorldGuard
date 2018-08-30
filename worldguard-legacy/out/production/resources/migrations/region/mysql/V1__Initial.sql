SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';

-- -----------------------------------------------------
-- Table `group`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `${tablePrefix}group` (
  `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(64) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `name` (`name` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;


-- -----------------------------------------------------
-- Table `world`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `${tablePrefix}world` (
  `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(128) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `name` (`name` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;


-- -----------------------------------------------------
-- Table `region`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `${tablePrefix}region` (
  `id` VARCHAR(128) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL ,
  `world_id` INT(10) UNSIGNED NOT NULL ,
  `type` ENUM('cuboid','poly2d','global') CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL ,
  `priority` SMALLINT(6) NOT NULL DEFAULT '0' ,
  `parent` VARCHAR(128) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NULL DEFAULT NULL ,
  PRIMARY KEY (`id`, `world_id`) ,
  INDEX `fk_region_world` (`world_id` ASC) ,
  INDEX `parent` (`parent` ASC) ,
  CONSTRAINT `fk_${tablePrefix}region_world1`
    FOREIGN KEY (`world_id` )
    REFERENCES `${tablePrefix}world` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `${tablePrefix}parent`
    FOREIGN KEY (`parent` )
    REFERENCES `${tablePrefix}region` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;


-- -----------------------------------------------------
-- Table `region_cuboid`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `${tablePrefix}region_cuboid` (
  `region_id` VARCHAR(128) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL ,
  `world_id` INT(10) UNSIGNED NOT NULL ,
  `min_x` BIGINT(20) NOT NULL ,
  `min_y` BIGINT(20) NOT NULL ,
  `min_z` BIGINT(20) NOT NULL ,
  `max_x` BIGINT(20) NOT NULL ,
  `max_y` BIGINT(20) NOT NULL ,
  `max_z` BIGINT(20) NOT NULL ,
  PRIMARY KEY (`region_id`, `world_id`) ,
  INDEX `fk_region_cuboid_region` (`region_id` ASC) ,
  CONSTRAINT `fk_${tablePrefix}region_cuboid_region`
    FOREIGN KEY (`region_id` , `world_id` )
    REFERENCES `${tablePrefix}region` (`id` , `world_id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;


-- -----------------------------------------------------
-- Table `region_flag`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `${tablePrefix}region_flag` (
  `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `region_id` VARCHAR(128) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL ,
  `world_id` INT(10) UNSIGNED NOT NULL ,
  `flag` VARCHAR(45) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL ,
  `value` VARCHAR(256) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_flags_region` (`region_id` ASC, `world_id` ASC) ,
  CONSTRAINT `fk_${tablePrefix}flags_region`
    FOREIGN KEY (`region_id` , `world_id` )
    REFERENCES `${tablePrefix}region` (`id` , `world_id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;


-- -----------------------------------------------------
-- Table `region_groups`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `${tablePrefix}region_groups` (
  `region_id` VARCHAR(128) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL ,
  `world_id` INT(10) UNSIGNED NOT NULL ,
  `group_id` INT(10) UNSIGNED NOT NULL ,
  `owner` TINYINT(1) NOT NULL ,
  PRIMARY KEY (`region_id`, `world_id`, `group_id`) ,
  INDEX `fk_region_groups_region` (`region_id` ASC) ,
  INDEX `fk_region_groups_group` (`group_id` ASC) ,
  CONSTRAINT `fk_${tablePrefix}region_groups_group`
    FOREIGN KEY (`group_id` )
    REFERENCES `${tablePrefix}group` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_${tablePrefix}region_groups_region`
    FOREIGN KEY (`region_id` , `world_id` )
    REFERENCES `${tablePrefix}region` (`id` , `world_id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;


-- -----------------------------------------------------
-- Table `user`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `${tablePrefix}user` (
  `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(64) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `name` (`name` ASC) )
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;


-- -----------------------------------------------------
-- Table `region_players`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `${tablePrefix}region_players` (
  `region_id` VARCHAR(128) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL ,
  `world_id` INT(10) UNSIGNED NOT NULL ,
  `user_id` INT(10) UNSIGNED NOT NULL ,
  `owner` TINYINT(1) NOT NULL ,
  PRIMARY KEY (`region_id`, `world_id`, `user_id`) ,
  INDEX `fk_region_players_region` (`region_id` ASC) ,
  INDEX `fk_region_users_user` (`user_id` ASC) ,
  CONSTRAINT `fk_${tablePrefix}region_users_region`
    FOREIGN KEY (`region_id` , `world_id` )
    REFERENCES `${tablePrefix}region` (`id` , `world_id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_${tablePrefix}region_users_user`
    FOREIGN KEY (`user_id` )
    REFERENCES `${tablePrefix}user` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;


-- -----------------------------------------------------
-- Table `region_poly2d`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `${tablePrefix}region_poly2d` (
  `region_id` VARCHAR(128) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL ,
  `world_id` INT(10) UNSIGNED NOT NULL ,
  `min_y` INT(11) NOT NULL ,
  `max_y` INT(11) NOT NULL ,
  PRIMARY KEY (`region_id`, `world_id`) ,
  INDEX `fk_region_poly2d_region` (`region_id` ASC) ,
  CONSTRAINT `fk_${tablePrefix}region_poly2d_region`
    FOREIGN KEY (`region_id` , `world_id` )
    REFERENCES `${tablePrefix}region` (`id` , `world_id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;


-- -----------------------------------------------------
-- Table `region_poly2d_point`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `${tablePrefix}region_poly2d_point` (
  `id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT ,
  `region_id` VARCHAR(128) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL ,
  `world_id` INT(10) UNSIGNED NOT NULL ,
  `x` BIGINT(20) NOT NULL ,
  `z` BIGINT(20) NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_region_poly2d_point_region_poly2d` (`region_id` ASC, `world_id` ASC) ,
  CONSTRAINT `fk_${tablePrefix}region_poly2d_point_region_poly2d`
    FOREIGN KEY (`region_id` , `world_id` )
    REFERENCES `${tablePrefix}region_poly2d` (`region_id` , `world_id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
