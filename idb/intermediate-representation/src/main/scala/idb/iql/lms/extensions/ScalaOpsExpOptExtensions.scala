package idb.iql.lms.extensions

/**
 *
 * @author Ralf Mitschke
 */
trait ScalaOpsExpOptExtensions
  extends FunctionsExpOptAlphaEquivalence
          with FunctionsExpOptBetaReduction
          with ScalaOpsExpConstantPropagation
          with ScalaOpsExpNormalization
{

}
