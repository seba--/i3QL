package unisson.ast

/**
 * 
 * Author: Ralf Mitschke
 * Created: 30.08.11 11:08
 *
 */

case class UnresolvedEnsemble(name : String, query : UnissonQuery, subEnsembleNames : Seq[String])
    extends UnissonDefinition
{

}
