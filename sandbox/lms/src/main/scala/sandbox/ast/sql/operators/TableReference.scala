package sandbox.ast.sql.operators

/**
 *
 * @author Ralf Mitschke
 */
case class TableReference[Domain: Manifest] (table: sandbox.ast.sql.Table[Domain])
  extends Operator
{

  def children = Nil

}
