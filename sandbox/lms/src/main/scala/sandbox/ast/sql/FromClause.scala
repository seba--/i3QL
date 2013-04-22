package sandbox.ast.sql

/**
 *
 * @author: Ralf Mitschke
 */
class FromClause[Domain] (table: Table[Domain])
{

  val ir = sandbox.ast.sql.ir

  def SELECT[Range] (fun: ir.Rep[Domain] => ir.Rep[Range]): SelectClause[Domain, Range] =
    new SelectClause[Domain, Range]

}
