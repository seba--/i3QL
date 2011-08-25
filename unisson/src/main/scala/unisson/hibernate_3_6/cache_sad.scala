package unisson.hibernate_3_6

import sae.bytecode.BytecodeDatabase
import sae.collections.QueryResult
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.model.dependencies.{invoke_interface}
import unisson.Queries._
import unisson.{SourceElement, EnsembleDefinition}

/**
 *
 * Author: Ralf Mitschke
 * Created: 07.06.11 09:53
 *
 */
/*

max:    1.44658265 (s)
min:    1.00149824 (s)
mean:   1.088503064 (s)
median: 1.062571168 (s)
1.00149824;1.004017763;1.02827486;1.033512157;1.042666745;1.048680494;1.056849215;1.058942594;1.066199743;1.067252207;1.067554396;1.075253473;1.139595429;1.190666101;1.44658265;

 */
class cache_sad(db: BytecodeDatabase)
    extends hibernate_3_6_ensemble_definitions(db)
    with EnsembleDefinition
{

    val incoming_invoke_interface_to_cache_violation: QueryResult[invoke_interface] =
        ( (db.invoke_interface, target _) ⋉ (identity(_:SourceElement[AnyRef]), `org.hibernate.cache`) ) ∩
        ( (db.invoke_interface, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.cache`) ) ∩
        ( (db.invoke_interface, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.action`) ) ∩
        ( (db.invoke_interface, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.event`) ) ∩
        ( (db.invoke_interface, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.engine`) ) ∩
        ( (db.invoke_interface, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.loader`) ) ∩
        ( (db.invoke_interface, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.engine`) ) ∩
        ( (db.invoke_interface, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.persister`) ) ∩
        ( (db.invoke_interface, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.stat`) ) ∩
        ( (db.invoke_interface, source _) ⊳ (identity(_:SourceElement[AnyRef]), Session) ) ∩
        ( (db.invoke_interface, source _) ⊳ (identity(_:SourceElement[AnyRef]), Metamodel_Configurator) ) ∩
        ( (db.invoke_interface, source _) ⊳ (identity(_:SourceElement[AnyRef]), GlobalSettings) )

    def printViolations()
    {
        incoming_invoke_interface_to_cache_violation.foreach(println)
    }
}
