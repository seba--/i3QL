package sandbox.ast.sql.syntax

/**
 *
 * @author: Ralf Mitschke
 */
class SelectClause[Domain: Manifest, Range: Manifest]
{
  val ir = sandbox.ast.sql.ir

  var function: ir.Rep[Domain] => ir.Rep[Range] = null

  def this (fun: sandbox.ast.sql.ir.Rep[Domain] => sandbox.ast.sql.ir.Rep[Range]) = {
    this ()
    function = fun
  }

}
