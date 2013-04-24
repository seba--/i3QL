package sandbox.ast.sql.syntax

import sandbox.ast.sql.Table

/**
 *
 * @author Ralf Mitschke
 */
case class FromClauseWithTable[Domain: Manifest] (table: Table[Domain])
  extends FromClause[Domain]
{

  /*
  val ir = sandbox.ast.sql.IR

  def SELECT[Range: Manifest] (fun: ir.Rep[Domain] => ir.Rep[Range]): SelectClause[Domain, Range] =
    new SelectClause[Domain, Range](this, fun)
  */
}
