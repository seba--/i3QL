/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package sae.bytecode.bat

import sae.bytecode.BytecodeDatabaseManipulation
import java.sql.DriverManager

/**
 * Created with IntelliJ IDEA.
 * User: Ralf Mitschke
 * Date: 23.08.12
 * Time: 16:04
 */

object SQLDatabaseFactory
{
    Class.forName("com.mysql.jdbc.Driver")

    val schema =
        """
          |SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
          |SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
          |SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
          |
          |DROP SCHEMA IF EXISTS `bytecode` ;
          |CREATE SCHEMA IF NOT EXISTS `bytecode` DEFAULT CHARACTER SET latin1 COLLATE latin1_german1_ci ;
          |USE `bytecode` ;
          |
          |-- -----------------------------------------------------
          |-- Table `bytecode`.`classes`
          |-- -----------------------------------------------------
          |DROP TABLE IF EXISTS `bytecode`.`classes` ;
          |
          |CREATE  TABLE IF NOT EXISTS `bytecode`.`classes` (
          |  `cid` INT NOT NULL AUTO_INCREMENT ,
          |  `packageName` VARCHAR(255) NOT NULL ,
          |  `simpleName` VARCHAR(100) NOT NULL ,
          |  PRIMARY KEY (`cid`) ,
          |  UNIQUE INDEX `cid_UNIQUE` (`cid` ASC) )
          |ENGINE = InnoDB;
          |
          |
          |-- -----------------------------------------------------
          |-- Table `bytecode`.`methods`
          |-- -----------------------------------------------------
          |DROP TABLE IF EXISTS `bytecode`.`methods` ;
          |
          |CREATE  TABLE IF NOT EXISTS `bytecode`.`methods` (
          |  `mid` INT NOT NULL AUTO_INCREMENT ,
          |  `cid` INT NOT NULL ,
          |  `name` VARCHAR(100) NULL ,
          |  PRIMARY KEY (`mid`, `cid`) ,
          |  UNIQUE INDEX `mid_UNIQUE` (`mid` ASC) ,
          |  UNIQUE INDEX `cid_UNIQUE` (`cid` ASC) )
          |ENGINE = InnoDB;
          |
          |
          |
          |SET SQL_MODE=@OLD_SQL_MODE;
          |SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
          |SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
          |
        """.stripMargin

    def create(url: String): BytecodeDatabaseManipulation = {
        val connection = DriverManager.getConnection (url)
        connection.createStatement().execute(schema)
        new SQLDatabase (connection)
    }

    def drop(url: String) {
        val connection = DriverManager.getConnection (url)
        connection.createStatement().execute(schema)
    }
}
