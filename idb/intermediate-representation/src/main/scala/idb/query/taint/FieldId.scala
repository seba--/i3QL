package idb.query.taint

/**
 * @author Mirko Köhler
 */
trait FieldId

case class FieldName(name : String) extends FieldId

