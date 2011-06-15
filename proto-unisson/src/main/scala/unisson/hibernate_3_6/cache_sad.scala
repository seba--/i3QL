package unisson.hibernate_3_6

import sae.bytecode.BytecodeDatabase
import sae.collections.QueryResult
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.model.dependencies.{invoke_interface}
import unisson.EnsembleDefinition
import unisson.Queries._

/**
 *
 * Author: Ralf Mitschke
 * Created: 07.06.11 09:53
 *
 */
class cache_sad(db: BytecodeDatabase)
    extends hibernate_3_6_ensemble_definitions(db)
    with EnsembleDefinition
{

    // element checks declared as values so the functions are created once and not one function for each application inside the selection
    val inCache = ∈(`org.hibernate.cache`)
    val notInCache = ∉(`org.hibernate.cache`)
    val notInAction = ∉(`org.hibernate.action`)
    val notInEvent = ∉(`org.hibernate.event`)
    val notInEngine = ∉(`org.hibernate.engine`)
    val notInLoader = ∉(`org.hibernate.loader`)
    val notInPersister = ∉(`org.hibernate.persister`)
    val notInStat = ∉(`org.hibernate.stat`)
    val notInSession = ∉(Session)
    val notInMetamodel = ∉(Metamodel_Configurator)
    val notInGlobalSettings = ∉(GlobalSettings)


    val incoming_invoke_interface_to_cache_violation: QueryResult[invoke_interface] =
        (
                σ(
                    target(_: invoke_interface)(inCache)
                )(db.invoke_interface)
                ) ∩ (
                σ(
                    source(_: invoke_interface)(notInCache)
                )(db.invoke_interface)
                ) ∩ (
                σ(
                    source(_: invoke_interface)(notInAction)
                )(db.invoke_interface)
                ) ∩ (
                σ(
                    source(_: invoke_interface)(notInEvent)
                )(db.invoke_interface)
                ) ∩ (
                σ(
                    source(_: invoke_interface)(notInEngine)
                )(db.invoke_interface)
                ) ∩ (
                σ(
                    source(_: invoke_interface)(notInLoader)
                )(db.invoke_interface)
                ) ∩ (
                σ(
                    source(_: invoke_interface)(notInPersister)
                )(db.invoke_interface)
                ) ∩ (
                σ(
                    source(_: invoke_interface)(notInStat)
                )(db.invoke_interface)
                ) ∩ (
                σ(
                    source(_: invoke_interface)(notInMetamodel)
                )(db.invoke_interface)
                ) ∩ (
                σ(
                    source(_: invoke_interface)(notInGlobalSettings)
                )(db.invoke_interface)
                )

    def printViolations()
    {
        incoming_invoke_interface_to_cache_violation.foreach(println)
    }
}
