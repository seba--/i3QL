package sandbox.ast.sql.ast.operators

/**
 *
 * @author: Ralf Mitschke
 */
case class Selection[Domain: Manifest, Range: Manifest]
  (
  fun: sandbox.ast.sql.ir.Rep[Domain] => sandbox.ast.sql.ir.Rep[Range]
)
{

}
