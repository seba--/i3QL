package unisson.hibernate.v3_6_6.fragments

import sae.bytecode.BytecodeDatabase
import sae.collections.QueryResult
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.model.dependencies.{create, parameter, invoke_interface, Dependency}
import unisson.Queries._
import unisson.{SourceElement, EnsembleDefinition}
import unisson.hibernate.v3_6_6.ensemble_definitions

/**
 *
 * Author: Ralf Mitschke
 * Created: 17.08.11 09:53
 *
 */
class PersistenceManagerFragment(db: BytecodeDatabase)
{

    val ensembles = new ensemble_definitions(db)

    import ensembles._



    val dep1 = db.create.∪[Dependency[_, _], invoke_interface](db.invoke_interface)

    val dep2 = dep1.∪[Dependency[_, _], parameter](db.parameter)

    val incoming_violation : QueryResult[Dependency[_, _]] = null

    def printViolations()
    {
        incoming_violation.foreach(println)

    }

}
