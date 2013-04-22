package sandbox.ast.sql

/**
 *
 * @author: Ralf Mitschke
 */
class SelectClause[Domain, Range]
{
  val ir = sandbox.ast.sql.ir

  var function: ir.Rep[Domain] => ir.Rep[Range] = null

  def this (fun: ir.Rep[Domain] => ir.Rep[Range]) = {
    this ()
    function = fun
  }

}
