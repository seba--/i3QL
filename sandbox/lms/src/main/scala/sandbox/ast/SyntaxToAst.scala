package sandbox.ast

import sandbox.ast.sql.Table
import sandbox.ast.sql.operators._
import sandbox.ast.sql.syntax._

/**
 *
 * @author Ralf Mitschke
 */
object SyntaxToAst
{

  def apply (clause: Clause): Operator =
  {
    transform (clause)
  }

  private def transform (clause: Clause): Operator =
    clause match {
      case (SelectClause (fromClause, function)) =>
        Selection (
          List (transform (fromClause)),
          function
        )

      case (FromClause (table: Table[_])) =>
        TableReference (table)

    }
}
