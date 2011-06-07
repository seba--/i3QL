package unisson.hibernate_3_6

import sae.bytecode.BytecodeDatabase
import sae.collections.QueryResult
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.model.dependencies.{create, parameter, invoke_interface, Dependency}
import unisson.{EnsembleDefinition, SourceElement, Queries}

/**
 *
 * Author: Ralf Mitschke
 * Created: 07.06.11 09:53
 *
 */

class action_sad(val db: BytecodeDatabase) extends EnsembleDefinition
{
    val queries = new Queries(db)

    import queries._

    val `org.hibernate.event` : QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.event.def") ∪
        `package`("org.hibernate.event")

    val lock: QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.dialect.lock")

    val `org.hibernate.action` : QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.action")

    val HQL: QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.hql") ∪
        `package`("org.hibernate.hql.antlr") ∪
        `package`("org.hibernate.hql.ast") ∪
        `package`("org.hibernate.hql.ast.exec") ∪
        `package`("org.hibernate.hql.ast.tree")

    val `org.hibernate.engine` : QueryResult[SourceElement[AnyRef]] =
        (
        `package`("org.hibernate.engine")
                ∖ class_with_members("org.hibernate.engine.SessionImplementor")
                ∖ class_with_members("org.hibernate.engine.SessionFactoryImplementor")
        ) ∪
        `package`("org.hibernate.engine.profile") ∪
        `package`("org.hibernate.engine.jdbc") ∪
        `package`("org.hibernate.engine.transaction") ∪
        `package`("org.hibernate.engine.query") ∪
        `package`("org.hibernate.engine.query.sql") ∪
        `package`("org.hibernate.engine.loading")

    val dep1 = db.create.∪[Dependency[_, _], invoke_interface]( db.invoke_interface )

    val dep2 = dep1.∪[Dependency[_, _], parameter]( db.parameter )

    // element checks declared as values so the functions are created once and not one function for each application inside the selection
    val inAction = ∈(`org.hibernate.action`)
    val notInAction = ∉(`org.hibernate.action`)
    val notInLock = ∉(lock)
    val notInEvent = ∉(`org.hibernate.event`)
    val notInHQL = ∉(HQL)
    val notInEngine = ∉(`org.hibernate.engine`)

    // val notAllowedIncoming = notInEngine && notInEvent && notInHQL && notInLock // currently can not be modelled as updateable function

    val incoming_engine_to_action_violation : QueryResult[Dependency[_, _]] =
        (
                σ(
                    target(_: Dependency[_, _])(inAction)
                )(dep2)) ∩ (
                σ(
                    source(_: Dependency[_, _])(notInAction)
                )(dep2)) ∩ (
                σ(
                    source(_: Dependency[_, _])(notInLock)
                )(dep2)) ∩ (
                σ(
                    source(_: Dependency[_, _])(notInEvent)
                )(dep2)) ∩ (
                σ(
                    source(_: Dependency[_, _])(notInHQL)
                )(dep2)) ∩ (
                σ(
                    source(_: Dependency[_, _])(notInEngine)
                )(dep2)
                )

    val incoming_lock_to_action_violation : QueryResult[Dependency[_, _]] =
        (
                σ(
                    target(_: Dependency[_, _])(inAction)
                )(dep1)) ∩ (
                σ(
                    source(_: Dependency[_, _])(notInAction)
                )(dep1)) ∩ (
                σ(
                    source(_: Dependency[_, _])(notInLock)
                )(dep1)) ∩ (
                σ(
                    source(_: Dependency[_, _])(notInEvent)
                )(dep1)) ∩ (
                σ(
                    source(_: Dependency[_, _])(notInHQL)
                )(dep1)) ∩ (
                σ(
                    source(_: Dependency[_, _])(notInEngine)
                )(dep1)
                )

    val incoming_event_to_action_violation : QueryResult[create] =
        (
                σ(
                    target(_: create)(inAction)
                )( db.create )) ∩ (
                σ(
                    source(_: create)(notInAction)
                )( db.create )) ∩ (
                σ(
                    source(_: create)(notInLock)
                )( db.create )) ∩ (
                σ(
                    source(_: create)(notInEvent)
                )( db.create )) ∩ (
                σ(
                    source(_: create)(notInHQL)
                )( db.create )) ∩ (
                σ(
                    source(_: create)(notInEngine)
                )( db.create )
                )

    // FIXME we do not model individual arrows but individual relationship constraints, thus this is redundant
    val incoming_HQL_to_action_violation : QueryResult[Dependency[_, _]] =
        (
                σ(
                    target(_: Dependency[_, _])(inAction)
                )(dep1)) ∩ (
                σ(
                    source(_: Dependency[_, _])(notInAction)
                )(dep1)) ∩ (
                σ(
                    source(_: Dependency[_, _])(notInLock)
                )(dep1)) ∩ (
                σ(
                    source(_: Dependency[_, _])(notInEvent)
                )(dep1)) ∩ (
                σ(
                    source(_: Dependency[_, _])(notInHQL)
                )(dep1)) ∩ (
                σ(
                    source(_: Dependency[_, _])(notInEngine)
                )(dep1)
                )


    def printViolations() {
        incoming_engine_to_action_violation.foreach(println)

        incoming_event_to_action_violation.foreach(println)

        incoming_HQL_to_action_violation.foreach(println)

        incoming_lock_to_action_violation.foreach(println)
    }
}
