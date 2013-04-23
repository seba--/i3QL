package sandbox.ast.sql.syntax

import sandbox.ast.sql.Table

/**
 *
 * @author Ralf Mitschke
 */
case class FromClause[Domain: Manifest] (table: Table[Domain])
  extends Clause
{

  val ir = sandbox.ast.sql.ir

  def SELECT[Range: Manifest] (fun: ir.Rep[Domain] => ir.Rep[Range]): SelectClause[Domain, Range] =
    new SelectClause[Domain, Range](this, fun)

}
