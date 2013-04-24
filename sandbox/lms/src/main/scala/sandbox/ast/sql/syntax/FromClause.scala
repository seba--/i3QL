package sandbox.ast.sql.syntax


/**
 *
 * @author Ralf Mitschke
 */
trait FromClause[Domain]
  extends Clause
{

  val ir = sandbox.ast.sql.IR

  def SELECT[Range] (fun: ir.Rep[Domain] => ir.Rep[Range])
      (implicit m0: Manifest[Domain], m1: Manifest[Range]): SelectClause[Domain, Range] =
    new SelectClause[Domain, Range](this, fun)

}
