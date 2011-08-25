package unisson.hibernate.v3_6_6

import sae.bytecode.BytecodeDatabase
import unisson.{SourceElement, Queries}
import sae.syntax.RelationalAlgebraSyntax._
import sae.collections.QueryResult

/**
 *
 * Author: Ralf Mitschke
 * Created: 07.06.11 15:02
 *
 * This trait defines all ensembles used throughout the various sad files for hibernate 3.6
 * Each ensemble is defined as a lazy val in order to allow subclasses to use only a fraction
 * of the defined ensembles, without instantiating all ensemble queries.
 */
class ensemble_definitions(val db: BytecodeDatabase)
{

    val queries = new Queries(db)

    import queries._

    lazy val ScalarTypes: QueryResult[SourceElement[AnyRef]] = `package`("org.hibernate.type.descriptor.sql") or
            `package`("org.hibernate.type") or
            `package`("org.hibernate.type.descriptor.java") or
            `package`("org.hibernate.type.descriptor")

    lazy val CollectionTypes: QueryResult[SourceElement[AnyRef]] = `package`("org.hibernate.collection")

    lazy val UserTypes: QueryResult[SourceElement[AnyRef]] = `package`("org.hibernate.usertype")


    lazy val DataTypes: QueryResult[SourceElement[AnyRef]] = ScalarTypes or CollectionTypes or UserTypes


    lazy val Mapping: QueryResult[SourceElement[AnyRef]] = `package`("org.hibernate.mapping")

    lazy val HQL: QueryResult[SourceElement[AnyRef]] = `package`("org.hibernate.hql") or
            `package`("org.hibernate.hql.classic") or
            `package`("org.hibernate.hql.ast.util") or
            `package`("org.hibernate.hql.ast.exec") or
            `package`("org.hibernate.hql.ast") or
            `package`("org.hibernate.hql.antlr") or
            `package`("org.hibernate.hql.ast.tree")

    lazy val SQL: QueryResult[SourceElement[AnyRef]] = `package`("org.hibernate.sql") or
            `package`("org.hibernate.sql.ordering.antlr") or
            class_with_members("org.hibernate", "SQLQueryResultMappingBuilder") or
            class_with_members("org.hibernate", "SQLQuery")


    lazy val Criterion: QueryResult[SourceElement[AnyRef]] = class_with_members("org.hibernate", "Criteria") or
            class_with_members("org.hibernate.impl", "CriteriaImpl") or
            `package`("org.hibernate.criterion") or
            `package`("org.hibernate.loader.criteria")

    lazy val QueryLanguages: QueryResult[SourceElement[AnyRef]] =
        HQL or SQL or Criterion


    lazy val CollectionPersister: QueryResult[SourceElement[AnyRef]] = `package`("org.hibernate.persister.collection")

    lazy val EntityPersister: QueryResult[SourceElement[AnyRef]] = `package`("org.hibernate.persister.entity")

    lazy val PersiterClassProvider: QueryResult[SourceElement[AnyRef]] = class_with_members(
        "org.hibernate.persister",
        "PersisterClassProvider"
    )


    lazy val PersisterFactory: QueryResult[SourceElement[AnyRef]] = class_with_members(
        "org.hibernate.persister",
        "PersisterFactory"
    )

    lazy val Persister: QueryResult[SourceElement[AnyRef]] = CollectionPersister or EntityPersister or PersiterClassProvider or PersisterFactory

    lazy val LoaderCore: QueryResult[SourceElement[AnyRef]] = `package`("org.hibernate.mapping")

    lazy val CollectionLoader: QueryResult[SourceElement[AnyRef]] = `package`("org.hibernate.mapping")

    lazy val EntityLoader: QueryResult[SourceElement[AnyRef]] = `package`("org.hibernate.mapping")

    lazy val CriteriaLoader: QueryResult[SourceElement[AnyRef]] = `package`("org.hibernate.mapping")

    lazy val HQLLoader: QueryResult[SourceElement[AnyRef]] = `package`("org.hibernate.mapping")

    lazy val CustomLoader: QueryResult[SourceElement[AnyRef]] = `package`("org.hibernate.mapping")

    lazy val Loader: QueryResult[SourceElement[AnyRef]] = LoaderCore or CollectionLoader or EntityLoader or CriteriaLoader or HQLLoader or CustomLoader

    lazy val PersistenceManager: QueryResult[SourceElement[AnyRef]] = Persister or Loader


}