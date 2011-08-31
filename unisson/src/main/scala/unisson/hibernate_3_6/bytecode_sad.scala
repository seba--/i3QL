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
max:    0.665182926 (s)
min:    0.443588863 (s)
mean:   0.515432484 (s)
median: 0.514079879 (s)
0.443588863;0.454436093;0.461411071;0.464746697;0.476252967;0.489902275;0.50100242;0.509499506;0.518660253;0.522322319;0.529348111;0.542540095;0.558692758;0.593901022;0.665182926;hibernate-core-3.6.0.Final.jar
 */
class bytecode_sad(db: BytecodeDatabase)
    extends hibernate_3_6_ensemble_definitions(db)
    with EnsembleDefinition
{

    val incoming_invoke_interface_to_bytecode_violation: QueryResult[invoke_interface] =
        ( (db.invoke_interface, target _) ⋉ (identity(_:SourceElement[AnyRef]), `org.hibernate.bytecode`) ) ∩
        ( (db.invoke_interface, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.bytecode`) ) ∩
        ( (db.invoke_interface, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.intercept`) ) ∩
        ( (db.invoke_interface, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.tool`) ) ∩
        ( (db.invoke_interface, source _) ⊳ (identity(_:SourceElement[AnyRef]), `org.hibernate.tuple`) )

    def printViolations() {
        incoming_invoke_interface_to_bytecode_violation.foreach(println)
    }

}
