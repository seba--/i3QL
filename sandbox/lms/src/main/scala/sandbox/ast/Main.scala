package sandbox.ast

import sandbox.ast.sql.Table
import sandbox.ast.sql.syntax.FROM

/**
 * @author Ralf Mitschke
 */
object Main
{

  def main (args: Array[String])
  {
    testSimpleClause ()
  }


  def testSimpleClause ()
  {
    val t = new Table[Int]
    val clause1 = FROM (t) SELECT (projection0)
    val clause2 = FROM (clause1) SELECT (projection1)

    val ast = SyntaxToAst (clause2)
    val dot = PrettyPrinter (ast)
    println (dot)
  }

  def projection0 (i: sql.ir.Rep[Int]): sql.ir.Rep[Int] =
  {
    import sql.ir._
    i + 1
  }

  def projection1 (i: sql.ir.Rep[Int]): sql.ir.Rep[Int] =
  {
    import sql.ir._
    i * i
  }
}
