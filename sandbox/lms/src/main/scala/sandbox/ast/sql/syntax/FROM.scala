package sandbox.ast.sql.syntax

import sandbox.ast.sql.Table

/**
 *
 * @author Ralf Mitschke
 */
object FROM
{

  def apply[Domain: Manifest] (table: Table[Domain]): FromClause[Domain] =
    new FromClauseWithTable[Domain](table)

  def apply[Domain: Manifest, Range: Manifest] (query: SelectClause[Domain, Range]): FromClause[Range] =
    new FromClauseWithQuery[Range, Domain](query)

}
