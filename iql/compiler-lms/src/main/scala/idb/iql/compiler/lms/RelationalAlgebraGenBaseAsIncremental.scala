package idb.iql.compiler.lms


/**
 *
 * @author Ralf Mitschke
 */
trait RelationalAlgebraGenBaseAsIncremental
{

  val IR: RelationalAlgebraIRBase with RelationalAlgebraGenSAEBinding

  def compile[Domain: Manifest] (exp: IR.Rep[IR.Relation[Domain]]): sae.Relation[Domain] = exp match {
    // TODO fix variance in sae.relation to remove type cast
    case IR.BaseRelation (rel) => rel.asInstanceOf[sae.Relation[Domain]]
  }

}
