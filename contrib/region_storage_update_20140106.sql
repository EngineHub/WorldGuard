-- -----------------------------------------------------
-- Table `region_cylinder`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `region_cylinder` (
  `region_id` VARCHAR(128) CHARACTER SET 'utf8' COLLATE 'utf8_bin' NOT NULL ,
  `world_id` INT(10) UNSIGNED NOT NULL ,
  `min_y` INT(11) NOT NULL ,
  `max_y` INT(11) NOT NULL ,
  `center_x` INT(11) NOT NULL ,
  `center_z` INT(11) NOT NULL ,
  `radius_x` INT(11) NOT NULL ,
  `radius_z` INT(11) NOT NULL ,
  PRIMARY KEY (`region_id`, `world_id`) ,
  INDEX `fk_region_cylinder_region` (`region_id` ASC) ,
  CONSTRAINT `fk_region_cylinder_region`
    FOREIGN KEY (`region_id` , `world_id` )
    REFERENCES `region` (`id` , `world_id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;
