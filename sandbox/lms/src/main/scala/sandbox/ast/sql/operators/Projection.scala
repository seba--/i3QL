package sandbox.ast.sql.operators


/**
 *
 * @author Ralf Mitschke
 */
case class Projection[Domain: Manifest, Range: Manifest] (
  children: List[Operator],
  function: sandbox.ast.sql.IR.Rep[Domain] => sandbox.ast.sql.IR.Rep[Range]
)
  extends Operator
{

  def lambda: String =
  {
    //sandbox.ast.sql.IR.
    //val effects = sandbox.ast.sql.IR.reifyEffects (function)

    val e = effects

    "λ" // + sandbox.ast.sql.IR.quote(e)
  }


  def effects (): sandbox.ast.sql.IR.Rep[Domain => Range] =
  {
    import sandbox.ast.sql.IR._
    reifyEffects {
      function
    }.res
  }
}
