package sandbox.ast.sql

/**
 *
 * @author Ralf Mitschke
 */
object TableNames
{
  var counter: Int = 0

  def freshName (): String =
  {
    counter = counter + 1 // there was some omnious scala compiler error with +=
    "Table_" + (counter)
  }

}
