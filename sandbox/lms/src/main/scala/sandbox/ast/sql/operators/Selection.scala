package sandbox.ast.sql.operators

/**
 *
 * @author Ralf Mitschke
 */
case class Selection[Domain: Manifest] (
  children: List[Operator],
  function: sandbox.ast.sql.IR.Rep[Domain] => sandbox.ast.sql.IR.Rep[Boolean]
)
  extends Operator
{

}
