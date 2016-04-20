package idb.query.colors

/**
 * @author Mirko Köhler
 */
trait ColorId

case class StringColor(name : String) extends ColorId
