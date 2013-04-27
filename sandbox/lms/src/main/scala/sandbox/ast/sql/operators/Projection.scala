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

    //val b = body(e)

    // "λ" +
    // sandbox.ast.sql.ScalaCompilation // .quote (e.res)


    import sandbox.ast.sql.ScalaCompilation._
    emitSource(function, "F", new java.io.PrintWriter(System.out))
    println ("-------------------------------")
    withStream(new java.io.PrintWriter(System.out))(emitBlock(e))

    ""
  }


  def effects (): sandbox.ast.sql.IR.Block[Range] =
  {
    import sandbox.ast.sql.IR._
    val sym = fresh[Domain]

    reifyEffects {
      function (sym)
    }
  }

  /*
  def body (function: sandbox.ast.sql.IR.Rep[Domain => Range]): Option[sandbox.ast.sql.IR.Stm] =
  {
    import sandbox.ast.sql.IR._



    val definition = findDefinition (function.asInstanceOf[Sym[Domain => Range]])
    //val summary = summarizeEffects(function)
    //val ref = reflectEffect(function)
    definition
  }
  */
}
