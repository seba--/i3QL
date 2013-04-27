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

    val t1 = new Table[(String, Int, Int)]

    val clause3 = FROM (t1) SELECT (projection2)

    val clause1 = FROM (t) SELECT (projection0)
    val clause2 = FROM (clause1) SELECT (projection1)

    val ast = SyntaxToAst (clause3)
    val dot = PrettyPrinter (ast)
    println (dot)
  }

  def projection0 (i: sql.IR.Rep[Int]): sql.IR.Rep[Int] =
  {
    import sql.IR._
    i + 1
  }

  def projection1 (i: sql.IR.Rep[Int]): sql.IR.Rep[Int] =
  {
    import sql.IR._
    i * i
  }

  def projection2 (i: sql.IR.Rep[(String, Int, Int)]): sql.IR.Rep[String] =
  {
    import sql.IR._
    if (i._3 < 0)
      i._1 + i._2
    else
      i._1 + i._3
  }
}
