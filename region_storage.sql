SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';


-- -----------------------------------------------------
-- Table `user`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `user` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(45) NOT NULL UNIQUE,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `group`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `group` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(45) NOT NULL UNIQUE,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `world`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `world` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `name` VARCHAR(64) NOT NULL UNIQUE,
  PRIMARY KEY (`id`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `region`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `region` (
  `id` VARCHAR(64) NOT NULL ,
  `world_id` INT UNSIGNED NOT NULL ,
  `type` ENUM('cuboid','poly2d','global') NOT NULL ,
  `priority` SMALLINT NOT NULL DEFAULT 0 ,
  `parent` VARCHAR(64) NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `parent` (`parent` ASC) ,
  INDEX `fk_region_world` (`world_id` ASC) ,
  CONSTRAINT `parent`
    FOREIGN KEY (`parent` )
    REFERENCES `region` (`id` )
    ON DELETE SET NULL
    ON UPDATE CASCADE,
  CONSTRAINT `fk_region_world1`
    FOREIGN KEY (`world_id` )
    REFERENCES `world` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `region_flag`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `region_flag` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `region_id` VARCHAR(64) NOT NULL ,
  `flag` VARCHAR(45) NOT NULL ,
  `value` VARCHAR(45) NOT NULL ,
  INDEX `fk_flags_region` (`region_id` ASC) ,
  PRIMARY KEY (`id`) ,
  CONSTRAINT `fk_flags_region1`
    FOREIGN KEY (`region_id` )
    REFERENCES `region` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `region_cuboid`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `region_cuboid` (
  `region_id` VARCHAR(64) NOT NULL ,
  `min_z` BIGINT NOT NULL ,
  `min_y` BIGINT NOT NULL ,
  `min_x` BIGINT NOT NULL ,
  `max_z` BIGINT NOT NULL ,
  `max_y` BIGINT NOT NULL ,
  `max_x` BIGINT NOT NULL ,
  PRIMARY KEY (`region_id`) ,
  INDEX `fk_region_cuboid_region` (`region_id` ASC) ,
  CONSTRAINT `fk_region_cuboid_region`
    FOREIGN KEY (`region_id` )
    REFERENCES `region` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `region_poly2d`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `region_poly2d` (
  `region_id` VARCHAR(64) NOT NULL ,
  `max_y` INT NOT NULL ,
  `min_y` INT NOT NULL ,
  PRIMARY KEY (`region_id`) ,
  INDEX `fk_region_poly2d_region` (`region_id` ASC) ,
  CONSTRAINT `fk_region_poly2d_region`
    FOREIGN KEY (`region_id` )
    REFERENCES `region` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `region_players`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `region_players` (
  `region_id` VARCHAR(64) NOT NULL ,
  `user_id` INT UNSIGNED NOT NULL ,
  `owner` TINYINT(1)  NOT NULL ,
  PRIMARY KEY (`region_id`, `user_id`) ,
  INDEX `fk_region_players_region` (`region_id` ASC) ,
  INDEX `fk_region_players_user` (`user_id` ASC) ,
  CONSTRAINT `fk_region_players_region`
    FOREIGN KEY (`region_id` )
    REFERENCES `region` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_region_players_user`
    FOREIGN KEY (`user_id` )
    REFERENCES `user` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `region_groups`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `region_groups` (
  `region_id` VARCHAR(64) NOT NULL ,
  `group_id` INT UNSIGNED NOT NULL ,
  `owner` TINYINT(1)  NOT NULL ,
  PRIMARY KEY (`region_id`, `group_id`) ,
  INDEX `fk_region_groups_region` (`region_id` ASC) ,
  INDEX `fk_region_groups_group` (`group_id` ASC) ,
  CONSTRAINT `fk_region_groups_region`
    FOREIGN KEY (`region_id` )
    REFERENCES `region` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `fk_region_groups_group`
    FOREIGN KEY (`group_id` )
    REFERENCES `group` (`id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `region_poly2d_point`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `region_poly2d_point` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `region_id` VARCHAR(64) NOT NULL ,
  `z` BIGINT NOT NULL ,
  `x` BIGINT NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `fk_region_poly2d_point_region_poly2d` (`region_id` ASC) ,
  CONSTRAINT `fk_region_poly2d_point_region_poly2d`
    FOREIGN KEY (`region_id` )
    REFERENCES `region_poly2d` (`region_id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
