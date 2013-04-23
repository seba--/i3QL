package sandbox.ast.sql.operators

/**
 *
 * @author Ralf Mitschke
 */
trait Operator
{
  def children: Iterable[Operator]
}
