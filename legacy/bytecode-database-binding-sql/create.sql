SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

DROP SCHEMA IF EXISTS `bytecode` ;
CREATE SCHEMA IF NOT EXISTS `bytecode` DEFAULT CHARACTER SET latin1 COLLATE latin1_german1_ci ;
USE `bytecode` ;

-- -----------------------------------------------------
-- Table `bytecode`.`classes`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `bytecode`.`classes` (
  `cid` INT NOT NULL AUTO_INCREMENT ,
  `packageName` VARCHAR(255) NOT NULL ,
  `simpleName` VARCHAR(100) NOT NULL ,
  PRIMARY KEY (`cid`) )
ENGINE = InnoDB;

CREATE UNIQUE INDEX `cid_UNIQUE` ON `bytecode`.`classes` (`cid` ASC) ;


-- -----------------------------------------------------
-- Table `bytecode`.`methods`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `bytecode`.`methods` (
  `mid` INT NOT NULL AUTO_INCREMENT ,
  `cid` INT NOT NULL ,
  `name` VARCHAR(100) NULL ,
  PRIMARY KEY (`mid`, `cid`) )
ENGINE = InnoDB;

CREATE UNIQUE INDEX `mid_UNIQUE` ON `bytecode`.`methods` (`mid` ASC) ;

CREATE UNIQUE INDEX `cid_UNIQUE` ON `bytecode`.`methods` (`cid` ASC) ;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
