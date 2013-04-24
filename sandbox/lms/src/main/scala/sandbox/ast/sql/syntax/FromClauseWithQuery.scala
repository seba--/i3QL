package sandbox.ast.sql.syntax


/**
 *
 * @author Ralf Mitschke
 */
case class FromClauseWithQuery[Domain: Manifest, OtherDomain: Manifest] (selection: SelectClause[OtherDomain, Domain])
  extends FromClause[Domain]
{

  /*
  val ir = sandbox.ast.sql.IR

  def SELECT[Range: Manifest] (fun: ir.Rep[Domain] => ir.Rep[Range]): SelectClause[Domain, Range] =
    new SelectClause[Domain, Range](this, fun)
*/

}
