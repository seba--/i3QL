package unisson.hibernate_3_6

import sae.bytecode.BytecodeDatabase
import sae.collections.QueryResult
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.model.dependencies.invoke_interface
import unisson.{EnsembleDefinition, SourceElement, Queries}

/**
 *
 * Author: Ralf Mitschke
 * Created: 07.06.11 09:53
 *
 */

class bytecode_sad(val db: BytecodeDatabase) extends EnsembleDefinition
{
    val queries = new Queries(db)

    import queries._

    val `org.hibernate.intercept` : QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.intercept") ∪
                `package`("org.hibernate.intercept.cglib") ∪
                `package`("org.hibernate.intercept.javassist")


    val `org.hibernate.bytecode` : QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.bytecode.cglib") ∪
                `package`("org.hibernate.bytecode.buildtime") ∪
                `package`("org.hibernate.bytecode.util") ∪
                `package`("org.hibernate.bytecode.javassist") ∪
                `package`("org.hibernate.bytecode")


    val `org.hibernate.tool` : QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.tool") ∪
                `package`("org.hibernate.tool.hbm2ddl") ∪
                `package`("org.hibernate.tool.instrument") ∪
                `package`("org.hibernate.tool.instrument.cglib") ∪
                `package`("org.hibernate.tool.instrument.javassist")

    val `org.hibernate.tuple` : QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.tuple.component") ∪
                `package`("org.hibernate.tuple") ∪
                `package`("org.hibernate.tuple.entity")


    // element checks declared as values so the functions are created once and not one function for each application inside the selection
    val inBytecode = ∈(`org.hibernate.bytecode`)
    val notInBytecode = ∉(`org.hibernate.bytecode`)
    val notInIntercept = ∉(`org.hibernate.intercept`)
    val notInTool = ∉(`org.hibernate.tool`)
    val notInTuple = ∉(`org.hibernate.tuple`)

    // val notAllowedIncoming = notInEngine && notInEvent && notInHQL && notInLock // currently can not be modelled as updateable function

    val incoming_invoke_interface_to_bytecode_violation: QueryResult[invoke_interface] =
        (
                σ(
                    target(_: invoke_interface)(inBytecode)
                )(db.invoke_interface)) ∩ (
                σ(
                    source(_: invoke_interface)(notInBytecode)
                )(db.invoke_interface)) ∩ (
                σ(
                    source(_: invoke_interface)(notInIntercept)
                )(db.invoke_interface)) ∩ (
                σ(
                    source(_: invoke_interface)(notInTool)
                )(db.invoke_interface)) ∩ (
                σ(
                    source(_: invoke_interface)(notInTuple)
                )(db.invoke_interface)
                )

    def printViolations() {
        incoming_invoke_interface_to_bytecode_violation.foreach(println)
    }

}
