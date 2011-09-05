package unisson

import ast._
import sae.LazyView
import sae.collections.{QueryResult, Conversions}
import sae.bytecode.BytecodeDatabase
import sae.syntax.RelationalAlgebraSyntax

/**
 *
 * Author: Ralf Mitschke
 * Created: 31.08.11 09:42
 *
 */

class ArchitectureChecker(val db: BytecodeDatabase)
{

    private var ensembles: Map[Ensemble, QueryResult[SourceElement[AnyRef]]] = Map()

    private var constraintViolations: Map[DependencyConstraint, QueryResult[Violation]] = Map()

    private var allViolations : QueryResult[Violation] = null

    def getEnsembles = ensembles.keySet

    def getConstraints = constraintViolations.keySet

    def ensembleStatistic(ensemble : Ensemble) =

        ensemble.name + ":\n" +
        "   " + "#elements: " + ensembleElements(ensemble).size + "\n" +
        "   " + "constraints:\n" +
        (ensemble.outgoingConnections.map( _ match
            {
                case OutgoingConstraint(_,targets,kind) => "   " + "outgoing    " + kind + " to " + targets +"\n"
                case NotAllowedConstraint(_,target,kind) => "   " + "not_allowed " + kind +  " to " + target +"\n"
                case ExpectedConstraint(_,target,kind) => "   " + "expected    " + kind +  " to " + target +"\n"
                case _ => ""
            }
        ).foldLeft("")(_ + _)) +
        (ensemble.incomingConnections.map( _ match
            {
                case IncomingConstraint(sources,_,kind) => "   " + "incoming    " + kind + " from " + sources +"\n"
                case NotAllowedConstraint(source,_,kind) => "   " + "not_allowed " + kind +  " from " + source +"\n"
                case ExpectedConstraint(source,_,kind) => "   " + "expected    " + kind +  " from " + source +"\n"
                case _ => ""
            }
        ).foldLeft("")(_ + _))


    def addEnsemble(ensemble: Ensemble, query: LazyView[SourceElement[AnyRef]])
    {
        ensembles += {
            ensemble -> Conversions.lazyViewToResult(query)
        }
    }

    def hasEnsemble(ensemble: Ensemble) = ensembles.isDefinedAt(ensemble)

    // TODO in the long run, make this return an iterable
    def ensembleElements(ensemble: Ensemble): QueryResult[SourceElement[AnyRef]] =
    {
        ensembles.get(ensemble).get
    }

    def getEnsemble(name : String) : Option[Ensemble] =
    {
        ensembles.keySet.collectFirst( { case e @ Ensemble(n,_,_,_) if( n == name) => e } )
    }

    def addConstraint(constraint: DependencyConstraint, query: LazyView[Violation])
    {
        constraintViolations += {
            constraint -> Conversions.lazyViewToResult(query)
        }
    }

    def updateConstraint(constraint: DependencyConstraint, query: LazyView[Violation])
    {

        constraintViolations = (constraintViolations - constraint)
        constraintViolations += {  constraint -> Conversions.lazyViewToResult(query) }
    }



    def hasConstraint(constraint: DependencyConstraint) = constraintViolations.isDefinedAt(constraint)

    def violations(constraint: DependencyConstraint): QueryResult[Violation] = constraintViolations(
        constraint
    )

    def violations: QueryResult[Violation] =
    {
                import RelationalAlgebraSyntax._
        if( allViolations == null ){
            allViolations =
            constraintViolations.values.foldLeft[QueryResult[Violation]](new ArchitectureChecker.EmptyResult[Violation])(
                _ ∪ _ // union should yield all elements in foreach, as it just delegates to the underlying views, which are materialized
            )
        }

        allViolations
    }

}

object ArchitectureChecker
{

    class EmptyResult[V <: AnyRef] extends QueryResult[V]
    {
        def lazyInitialize {}
        protected def materialized_foreach[T](f : (V) => T) {}
        protected def materialized_size : Int = 0
        protected def materialized_singletonValue : Option[V] = None
        protected def materialized_contains(v : V) : Boolean = false
    }
}