package sandbox.ast.sql.operators

/**
 *
 * @author Ralf Mitschke
 */
case class Selection[Domain: Manifest, Range: Manifest] (
  children: List[Operator],
  function: sandbox.ast.sql.ir.Rep[Domain] => sandbox.ast.sql.ir.Rep[Range]
)
  extends Operator
{

}
