package unisson.model

import mock.vespucci._
import org.scalatest.matchers.ShouldMatchers
import org.junit.{Ignore, Test}
import sae.bytecode.BytecodeDatabase
import sae.collections.QueryResult
import de.tud.cs.st.vespucci.interfaces.IViolation
import de.tud.cs.st.vespucci.model.IEnsemble
import sae.bytecode.model.MethodDeclaration
import unisson.query.code_model.SourceElement
import de.tud.cs.st.bat.{VoidType, ObjectType}

/**
 *
 * Author: Ralf Mitschke
 * Date: 24.02.12
 * Time: 09:53
 *
 */
class TestUnissonDatabaseHibernate_1_2_3
        extends ShouldMatchers
{


    import UnissonOrdering._
    import sae.collections.Conversions._

    @Test
    @Ignore
    def testViolationsActionsToExceptions() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ActionInterface = Ensemble("ActionInterface", "class_with_members(class('cirrus.hibernate.impl','SessionImpl$Executable'))")
        val ScalarActions = Ensemble("ScalarActions", "class_with_members(class('cirrus.hibernate.impl','ScheduledDeletion')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledInsertion')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledUpdate'))\nor\nclass_with_members('cirrus.hibernate.impl','ScheduledEntityAction')")
        val CollectionActions = Ensemble("CollectionActions", "class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionDeletes')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionUpdates')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionInserts')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionRecreate')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionRemove')) \n\nor\nclass_with_members('cirrus.hibernate.impl','ScheduledCollectionAction')\nor\nclass_with_members('cirrus.hibernate.impl','ScheduledCollectionUpdate')")
        val Actions = Ensemble("Actions", "derived", ActionInterface, ScalarActions, CollectionActions)


        val Exceptions = Ensemble("Exceptions", "class_with_members(class('cirrus.hibernate','HibernateException')) \nor class_with_members(transitive(supertype(class('cirrus.hibernate','HibernateException')))) \nor class_with_members(class('cirrus.hibernate','AssertionFailure'))\nor class_with_members(class('cirrus.hibernate','LazyInitializationException'))")
        val Utilities = Ensemble("Utilities", "package('cirrus.hibernate.helpers') ")
        val UtilitiesAndExceptions = Ensemble("UtilitiesAndExceptions", "derived", Utilities, Exceptions)

        val Empty = Ensemble("Empty", "empty")

        val ensembles = Set(Actions, UtilitiesAndExceptions, Empty)
        val emptyToExceptions = GlobalOutgoingConstraint("all", Actions, Empty)
        val constraints = Set(emptyToExceptions)
        val global = GlobalArchitectureModel(ensembles)
        val model = ArchitectureModel(ensembles, constraints, "Actions.sad")
        db.addGlobalModel(global)
        db.addModel(model)

        val queryResult: QueryResult[IViolation] = db.violations

        bc.addArchiveAsResource("hibernate-1.2.3.jar")

        val result = queryResult.asList.sorted

        val expectedResult = List[IViolation](
            Violation(emptyToExceptions,
                ActionInterface,
                Exceptions,
                SourceElement(
                    MethodDeclaration(
                        ObjectType("cirrus/hibernate/impl/SessionImpl$Executable"),
                        "afterTransactionCompletion",
                        Seq(),
                        VoidType()
                    )
                ),
                SourceElement(ObjectType("cirrus/hibernate/HibernateException")),
                "throws", "Actions.sad"
            ),
            Violation(emptyToExceptions,
                ActionInterface,
                Exceptions,
                SourceElement(
                    MethodDeclaration(
                        ObjectType("cirrus/hibernate/impl/SessionImpl$Executable"),
                        "execute",
                        Seq(),
                        VoidType()
                    )
                ),
                SourceElement(ObjectType("cirrus/hibernate/HibernateException")),
                "throws", "Actions.sad"
            ),
            Violation(emptyToExceptions,
                CollectionActions,
                Exceptions,
                SourceElement(
                    MethodDeclaration(
                        ObjectType("cirrus/hibernate/impl/ScheduledCollectionAction"),
                        "afterTransactionCompletion",
                        Seq(),
                        VoidType()
                    )
                ),
                SourceElement(ObjectType("cirrus/hibernate/cache/CacheException")),
                "throws", "Actions.sad"
            ),
            Violation(emptyToExceptions,
                CollectionActions,
                Exceptions,
                SourceElement(
                    MethodDeclaration(
                        ObjectType("cirrus/hibernate/impl/ScheduledCollectionRecreate"),
                        "execute",
                        Seq(),
                        VoidType()
                    )
                ),
                SourceElement(ObjectType("cirrus/hibernate/HibernateException")),
                "throws", "Actions.sad"
            ),
            Violation(emptyToExceptions,
                CollectionActions,
                Exceptions,
                SourceElement(
                    MethodDeclaration(
                        ObjectType("cirrus/hibernate/impl/ScheduledCollectionRemove"),
                        "execute",
                        Seq(),
                        VoidType()
                    )
                ),
                SourceElement(ObjectType("cirrus/hibernate/HibernateException")),
                "throws", "Actions.sad"
            ),
            Violation(emptyToExceptions,
                CollectionActions,
                Exceptions,
                SourceElement(
                    MethodDeclaration(
                        ObjectType("cirrus/hibernate/impl/ScheduledCollectionUpdate"),
                        "execute",
                        Seq(),
                        VoidType()
                    )
                ),
                SourceElement(ObjectType("cirrus/hibernate/HibernateException")),
                "throws", "Actions.sad"
            ),
            Violation(emptyToExceptions,
                ScalarActions,
                Exceptions,
                SourceElement(
                    MethodDeclaration(
                        ObjectType("cirrus/hibernate/impl/ScheduledDeletion"),
                        "afterTransactionCompletion",
                        Seq(),
                        VoidType()
                    )
                ),
                SourceElement(ObjectType("cirrus/hibernate/cache/CacheException")),
                "throws", "Actions.sad"
            ),
            Violation(emptyToExceptions,
                ScalarActions,
                Exceptions,
                SourceElement(
                    MethodDeclaration(
                        ObjectType("cirrus/hibernate/impl/ScheduledUpdate"),
                        "afterTransactionCompletion",
                        Seq(),
                        VoidType()
                    )
                ),
                SourceElement(ObjectType("cirrus/hibernate/cache/CacheException")),
                "throws", "Actions.sad"
            ),
            Violation(emptyToExceptions,
                ScalarActions,
                Exceptions,
                SourceElement(
                    MethodDeclaration(
                        ObjectType("cirrus/hibernate/impl/ScheduledDeletion"),
                        "execute",
                        Seq(),
                        VoidType()
                    )
                ),
                SourceElement(ObjectType("cirrus/hibernate/HibernateException")),
                "throws", "Actions.sad"
            ),
            Violation(emptyToExceptions,
                ScalarActions,
                Exceptions,
                SourceElement(
                    MethodDeclaration(
                        ObjectType("cirrus/hibernate/impl/ScheduledUpdate"),
                        "execute",
                        Seq(),
                        VoidType()
                    )
                ),
                SourceElement(ObjectType("cirrus/hibernate/HibernateException")),
                "throws", "Actions.sad"
            ),
            Violation(emptyToExceptions,
                ScalarActions,
                Exceptions,
                SourceElement(
                    MethodDeclaration(
                        ObjectType("cirrus/hibernate/impl/ScheduledInsertion"),
                        "execute",
                        Seq(),
                        VoidType()
                    )
                ),
                SourceElement(ObjectType("cirrus/hibernate/HibernateException")),
                "throws", "Actions.sad"
            )
        ).sorted

        result should be(expectedResult)
    }

    @Test
    @Ignore
    def testActionsToExceptionsDependencies() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ActionInterface = Ensemble("ActionInterface", "class_with_members(class('cirrus.hibernate.impl','SessionImpl$Executable'))")
        val ScalarActions = Ensemble("ScalarActions", "class_with_members(class('cirrus.hibernate.impl','ScheduledDeletion')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledInsertion')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledUpdate'))\nor\nclass_with_members('cirrus.hibernate.impl','ScheduledEntityAction')")
        val CollectionActions = Ensemble("CollectionActions", "class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionDeletes')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionUpdates')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionInserts')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionRecreate')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionRemove')) \n\nor\nclass_with_members('cirrus.hibernate.impl','ScheduledCollectionAction')\nor\nclass_with_members('cirrus.hibernate.impl','ScheduledCollectionUpdate')")
        val Actions = Ensemble("Actions", "derived", ActionInterface, ScalarActions, CollectionActions)


        val Exceptions = Ensemble("Exceptions", "class_with_members(class('cirrus.hibernate','HibernateException')) \nor class_with_members(transitive(supertype(class('cirrus.hibernate','HibernateException')))) \nor class_with_members(class('cirrus.hibernate','AssertionFailure'))\nor class_with_members(class('cirrus.hibernate','LazyInitializationException'))")
        val Utilities = Ensemble("Utilities", "package('cirrus.hibernate.helpers') ")
        val UtilitiesAndExceptions = Ensemble("UtilitiesAndExceptions", "derived", Utilities, Exceptions)

        val ensembles = Set(Actions, UtilitiesAndExceptions)
        val global = GlobalArchitectureModel(ensembles)
        db.addGlobalModel(global)

        val queryResult: QueryResult[(IEnsemble, IEnsemble, Int)] = db.ensembleDependencies

        bc.addArchiveAsResource("hibernate-1.2.3.jar")

        val result = queryResult.asList.sorted

        //result.foreach(println)

    }

}