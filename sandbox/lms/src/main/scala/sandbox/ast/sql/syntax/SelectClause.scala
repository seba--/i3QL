package sandbox.ast.sql.syntax

/**
 *
 * @author Ralf Mitschke
 */
case class SelectClause[Domain: Manifest, Range: Manifest] (
  fromClause: FromClause[Domain],
  function: sandbox.ast.sql.ir.Rep[Domain] => sandbox.ast.sql.ir.Rep[Range]
)
  extends Clause
{


}
