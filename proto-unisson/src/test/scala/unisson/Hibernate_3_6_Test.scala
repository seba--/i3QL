package unisson

import sae.collections.QueryResult
import org.junit.Test
import org.junit.Assert._
import sae.bytecode.BytecodeDatabase
import sae.syntax.RelationalAlgebraSyntax._
import sae.bytecode.model.dependencies.{create, parameter, Dependency, invoke_interface}

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.05.11 14:18
 *
 */

class Hibernate_3_6_Test
{

    class ensemble_definition(val db: BytecodeDatabase)
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

        val dep1 = db.create.∪[Dependency[_, _], invoke_interface](db.invoke_interface)

        val dep2 = dep1.∪[Dependency[_, _], parameter](db.parameter)

        // element checks declared as values so the functions are created once and not one function for each application inside the selection
        val inAction = ∈(`org.hibernate.action`)
        val notInAction = ∉(`org.hibernate.action`)
        val notInLock = ∉(lock)
        val notInEvent = ∉(`org.hibernate.event`)
        val notInHQL = ∉(HQL)
        val notInEngine = ∉(`org.hibernate.engine`)

        // val notAllowedIncoming = notInEngine && notInEvent && notInHQL && notInLock // currently can not be modelled as updateable function


        val incoming_engine_to_action_violation =
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

        val incoming_lock_to_action_violation =
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

        val incoming_event_to_action_violation =
            (
                    σ(
                        target(_: create)(inAction)
                    )(db.create)) ∩ (
                    σ(
                        source(_: create)(notInAction)
                    )(db.create)) ∩ (
                    σ(
                        source(_: create)(notInLock)
                    )(db.create)) ∩ (
                    σ(
                        source(_: create)(notInEvent)
                    )(db.create)) ∩ (
                    σ(
                        source(_: create)(notInHQL)
                    )(db.create)) ∩ (
                    σ(
                        source(_: create)(notInEngine)
                    )(db.create)
                    )


        val incoming_HQL_to_action_violation =
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


        def source(dependency: Dependency[_, _]): SourceElement[AnyRef] = new SourceElement[AnyRef](dependency.source.asInstanceOf[AnyRef])

        def target(dependency: Dependency[_, _]): SourceElement[AnyRef] = new SourceElement[AnyRef](dependency.target.asInstanceOf[AnyRef])

        // def sources(dependencies : LazyView[Dependency[_,_]]) = Π{ (_:Dependency[_,_]).source}(dependencies)

        // def targets(dependencies : LazyView[Dependency[_,_]]) = Π{ (_:Dependency[_,_]).target}(dependencies)
    }


    @Test
    def count_ensemble_elements()
    {
        val db = new BytecodeDatabase

        val ensembles = new ensemble_definition(db)

        import ensembles._

        db.addArchiveAsResource("hibernate-core-3.6.0.Final.jar")

        assertEquals(186, `org.hibernate.action`.size) // findall :- 188
        assertEquals(836, `org.hibernate.event`.size) // findall :- 839
        assertEquals(75, lock.size) // findall :- 839
        assertEquals(2315, HQL.size) // findall :- 2329
        assertEquals(1655, `org.hibernate.engine`.size) // findall :- 1688

    }

    @Test
    def find_violation_elements()
    {
        val db = new BytecodeDatabase

        val ensembles = new ensemble_definition(db)

        import ensembles._

        db.addArchiveAsResource("hibernate-core-3.6.0.Final.jar")

        incoming_engine_to_action_violation.foreach(println)
        assertEquals(0, incoming_engine_to_action_violation.size)

        incoming_event_to_action_violation.foreach(println)
        assertEquals(0, incoming_event_to_action_violation.size)

        incoming_HQL_to_action_violation.foreach(println)
        assertEquals(0, incoming_HQL_to_action_violation.size)

        incoming_lock_to_action_violation.foreach(println)
        assertEquals(0, incoming_lock_to_action_violation.size)

    }
}

