package unisson.hibernate_3_6

import sae.bytecode.BytecodeDatabase
import sae.collections.QueryResult
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.model.dependencies.{create, parameter, invoke_interface, Dependency}
import unisson.Queries._
import unisson.{SourceElement, EnsembleDefinition}

/**
 *
 * Author: Ralf Mitschke
 * Created: 07.06.11 09:53
 *
 */
/*
min:    1.777059369 (s)
mean:   2.224382917 (s)
median: 2.059122559 (s)
1.777059369;1.804316795;1.844760817;1.921462101;1.955943186;1.962274908;1.964442583;2.010956172;2.107288947;2.108043457;2.280071248;2.318227487;2.544517603;2.549527392;4.216851817;hibernate-core-3.6.0.Final.jar

 */
class action_sad(db: BytecodeDatabase)
        extends hibernate_3_6_ensemble_definitions(db)
                with EnsembleDefinition
{


    val dep1 = db.create.∪[Dependency[_, _], invoke_interface](db.invoke_interface)

    val dep2 = dep1.∪[Dependency[_, _], parameter](db.parameter)


    // val notAllowedIncoming = notInEngine && notInEvent && notInHQL && notInLock
    val incoming_violation_param_create_interface: QueryResult[Dependency[_, _]] =
        ( (dep2, target _) ⋉ (identity(_:SourceElement[AnyRef]), `org.hibernate.action`) ) ∩
        ( (dep2, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.action`) ) ∩
        ( (dep2, source _) ⊳ (identity(_:SourceElement[AnyRef]), lock) ) ∩
        ( (dep2, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.event`) ) ∩
        ( (dep2, source _) ⊳ (identity(_:SourceElement[AnyRef]), HQL) ) ∩
        ( (dep2, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.engine`) )


    val incoming_violation_create_interface: QueryResult[Dependency[_, _]] =
        ( (dep1, target _) ⋉ (identity(_:SourceElement[AnyRef]), `org.hibernate.action`) ) ∩
        ( (dep1, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.action`) ) ∩
        ( (dep1, source _) ⊳ (identity(_:SourceElement[AnyRef]), lock) ) ∩
        ( (dep1, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.event`) ) ∩
        ( (dep1, source _) ⊳ (identity(_:SourceElement[AnyRef]), HQL) ) ∩
        ( (dep1, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.engine`) )


    val incoming_violation_create: QueryResult[create] =
        ( (db.create, target _) ⋉ (identity(_:SourceElement[AnyRef]), `org.hibernate.action`) ) ∩
        ( (db.create, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.action`) ) ∩
        ( (db.create, source _) ⊳ (identity(_:SourceElement[AnyRef]), lock) ) ∩
        ( (db.create, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.event`) ) ∩
        ( (db.create, source _) ⊳ (identity(_:SourceElement[AnyRef]), HQL) ) ∩
        ( (db.create, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.engine`) )


    def printViolations()
    {
        incoming_violation_param_create_interface.foreach(println)

        incoming_violation_create.foreach(println)

        incoming_violation_create_interface.foreach(println)
    }

}
