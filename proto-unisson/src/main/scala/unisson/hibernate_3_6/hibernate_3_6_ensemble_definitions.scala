package unisson.hibernate_3_6

import sae.bytecode.BytecodeDatabase
import sae.collections.QueryResult
import unisson.{SourceElement, Queries}
import sae.syntax.RelationalAlgebraSyntax._

/**
 * 
 * Author: Ralf Mitschke
 * Created: 07.06.11 15:02
 *
 * This trait defines all ensembles used throughout the various sad files for hibernate 3.6
 * Each ensemble is defined as a lazy val in order to allow subclasses to use only a fraction
 * of the defined ensembles, without instantiating all ensemble queries.
 */
class hibernate_3_6_ensemble_definitions(val db : BytecodeDatabase)
{

    val queries = new Queries(db)

    import queries._

    lazy val `org.hibernate.action` : QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.action")

    lazy val `org.hibernate.bytecode` : QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.bytecode.cglib") ∪
                `package`("org.hibernate.bytecode.buildtime") ∪
                `package`("org.hibernate.bytecode.util") ∪
                `package`("org.hibernate.bytecode.javassist") ∪
                `package`("org.hibernate.bytecode")

    lazy val `org.hibernate.cache` : QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.cache.impl.bridge") ∪
                `package`("org.hibernate.cache") ∪
                `package`("org.hibernate.cache.impl") ∪
                `package`("org.hibernate.cache.access") ∪
                `package`("org.hibernate.cache.entry")


    lazy val `org.hibernate.engine` : QueryResult[SourceElement[AnyRef]] =
        (
            `package`("org.hibernate.engine")
                ∖ class_with_members("org.hibernate.engine", "SessionImplementor")
                ∖ class_with_members("org.hibernate.engine", "SessionFactoryImplementor")
        ) ∪
        `package`("org.hibernate.engine.profile") ∪
        `package`("org.hibernate.engine.jdbc") ∪
        `package`("org.hibernate.engine.transaction") ∪
        `package`("org.hibernate.engine.query") ∪
        `package`("org.hibernate.engine.query.sql") ∪
        `package`("org.hibernate.engine.loading")

    lazy val `org.hibernate.event` : QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.event.def") ∪
        `package`("org.hibernate.event")

    lazy val `org.hibernate.intercept` : QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.intercept") ∪
                `package`("org.hibernate.intercept.cglib") ∪
                `package`("org.hibernate.intercept.javassist")

    lazy val `org.hibernate.loader` : QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.loader") ∪
                `package`("org.hibernate.loader.hql") ∪
                `package`("org.hibernate.loader.collection") ∪
                `package`("org.hibernate.loader.entity") ∪
                `package`("org.hibernate.loader.custom") ∪
                `package`("org.hibernate.loader.custom.sql") ∪
                `package`("org.hibernate.loader.criteria")

    lazy val `org.hibernate.persister` : QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.persister") ∪
                `package`("org.hibernate.persister.collection") ∪
                `package`("org.hibernate.persister.entity")

    lazy val `org.hibernate.stat` : QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.stat")

    lazy val `org.hibernate.tool` : QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.tool") ∪
                `package`("org.hibernate.tool.hbm2ddl") ∪
                `package`("org.hibernate.tool.instrument") ∪
                `package`("org.hibernate.tool.instrument.cglib") ∪
                `package`("org.hibernate.tool.instrument.javassist")

    lazy val `org.hibernate.tuple` : QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.tuple.component") ∪
                `package`("org.hibernate.tuple") ∪
                `package`("org.hibernate.tuple.entity")

    lazy val GlobalSettings: QueryResult[SourceElement[AnyRef]] =
        class_with_members("org.hibernate.cfg", "Settings") ∪
                class_with_members("org.hibernate.cfg", "SettingsFactory") ∪
                class_with_members("org.hibernate.cfg", "Environment") ∪
                class_with_members("org.hibernate.cfg", "Configuration")

    lazy val HQL: QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.hql") ∪
        `package`("org.hibernate.hql.antlr") ∪
        `package`("org.hibernate.hql.ast") ∪
        `package`("org.hibernate.hql.ast.exec") ∪
        `package`("org.hibernate.hql.ast.tree")

    lazy val lock: QueryResult[SourceElement[AnyRef]] =
        `package`("org.hibernate.dialect.lock")


    lazy val Metamodel_Configurator: QueryResult[SourceElement[AnyRef]] =
        ((((`package`("org.hibernate.cfg") ∖ class_with_members("org.hibernate.cfg", "Settings")) ∖ class_with_members(
            "org.hibernate.cfg",
            "SettingsFactory"
        )) ∖ class_with_members("org.hibernate.cfg", "Environment")) ∖ class_with_members(
            "org.hibernate.cfg",
            "Configuration"
        )) ∪
                `package`("org.hibernate.cfg.annotations") ∪
                `package`("org.hibernate.cfg.annotations.reflection") ∪
                `package`("org.hibernate.cfg.beanvalidation") ∪
                `package`("org.hibernate.cfg.search")


    lazy val Session: QueryResult[SourceElement[AnyRef]] =
        class_with_members("org.hibernate", "SessionFactory") ∪
                class_with_members("org.hibernate.impl", "SessionFactoryImpl") ∪
                class_with_members("org.hibernate.impl", "AbstractSessionImpl") ∪
                class_with_members("org.hibernate.jmx", "SessionFactoryStub") ∪
                class_with_members("org.hibernate.impl", "SessionImpl") ∪
                class_with_members("org.hibernate", "Session") ∪
                class_with_members("org.hibernate.classic", "Session") ∪
                class_with_members("org.hibernate.impl", "StatelessSessionImpl") ∪
                class_with_members("org.hibernate.engine", "SessionImplementor") ∪
                class_with_members("org.hibernate.engine", "SessionFactoryImplementor") ∪
                class_with_members("org.hibernate", "SessionFactoryObserver") ∪
                class_with_members("org.hibernate", "StatelessSession")

}