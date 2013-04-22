package sandbox.ast.sql

/**
 *
 * @author: Ralf Mitschke
 */
object FROM
{

  def apply[Domain] (table: Table[Domain]): FromClause[Domain] =
    new FromClause[Domain](table)

}
