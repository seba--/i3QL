package sae.syntax.sql

/**
 *
 * Author: Ralf Mitschke
 * Date: 03.08.12
 * Time: 21:12
 *
 * This trait is a work around because we can not write *.type in expressions due to '*' being a token for multiple parameter passing
 */
trait STAR
{

}

object * extends STAR{

}