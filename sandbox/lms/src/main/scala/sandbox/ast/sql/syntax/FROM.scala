package sandbox.ast.sql.syntax

import sandbox.ast.sql.Table

/**
 *
 * @author: Ralf Mitschke
 */
object FROM
{

  def apply[Domain: Manifest] (table: Table[Domain]): FromClause[Domain] =
    new FromClause[Domain](table)

}
