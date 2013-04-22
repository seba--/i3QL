package sandbox.ast

/**
 * @author Ralf Mitschke
 */
object Main
{

  def main (args: Array[String]) {
    testSimpleClause ()
  }


  def testSimpleClause () {
    import sql._
    val t = new Table[Int]
    val clause = FROM (t) SELECT (selectionFunction)
    printAST(clause)
  }

  def selectionFunction (i: sql.ir.Rep[Int]): sql.ir.Rep[Int] = i
}
