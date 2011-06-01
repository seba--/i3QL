package unisson

import sae.collections.QueryResult
import org.junit.Test
import org.junit.Assert._
import sae.bytecode.BytecodeDatabase
import sae.syntax.RelationalAlgebraSyntax._
import sae.LazyView
import sae.bytecode.model.dependencies.{Dependency, invoke_interface}

/**
 *
 * Author: Ralf Mitschke
 * Created: 30.05.11 14:18
 *
 */

class Hibernate_3_6_Test
{

    class ensemble_definition(val db : BytecodeDatabase)
    {
        val queries = new Queries(db)
        import queries._

        val `org.hibernate.event` : QueryResult[SourceElement[_]] =
            `package`("org.hibernate.event.def") ∪
            `package`("org.hibernate.event")

        val lock: QueryResult[SourceElement[_]] =
            `package`("org.hibernate.dialect.lock")

        val `org.hibernate.action` : QueryResult[SourceElement[_]] =
            `package`("org.hibernate.action")

        val HQL: QueryResult[SourceElement[_]] =
            `package`("org.hibernate.hql") ∪
            `package`("org.hibernate.hql.antlr") ∪
            `package`("org.hibernate.hql.ast") ∪
            `package`("org.hibernate.hql.ast.exec") ∪
            `package`("org.hibernate.hql.ast.tree")

        val `org.hibernate.engine` : QueryResult[SourceElement[_]] =
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

        def dep1 = db.create.∪[Dependency[_,_], invoke_interface] (db.invoke_interface)

        val incoming_lock_to_action_violation =
            σ(
                (SourceElement[_]((_:Dependency[_,_]).target) ∈ `org.hibernate.action`)
            )
            (dep1) ∩
            (
                σ(
                   SourceElement[_]((_:Dependency[_,_]).source) ∉ lock
                )
                (dep1)
            )


        def source(dependencies : LazyView[Dependency[_,_]]) = Π{ (_:Dependency[_,_]).source}(dependencies)

        def target(dependencies : LazyView[Dependency[_,_]]) = Π{ (_:Dependency[_,_]).target}(dependencies)
    }



    @Test
    def count_ensemble_elements()
    {
        val db = new BytecodeDatabase

        val ensembles = new ensemble_definition(db)

        import ensembles._

        db.addArchiveAsResource("hibernate-core-3.6.0.Final.jar")

        assertEquals(836, `org.hibernate.event`.size)
        assertEquals(75, lock.size)
        assertEquals(2315, HQL.size)
        assertEquals(2771, `org.hibernate.engine`.size)

    }

}