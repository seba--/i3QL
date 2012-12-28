package unisson.model

import mock.vespucci._
import org.scalatest.matchers.ShouldMatchers
import org.junit.{Ignore, Test}
import de.tud.cs.st.vespucci.interfaces.IViolation
import unisson.query.code_model.SourceElementFactory
import de.tud.cs.st.bat.resolved.{VoidType, ObjectType}
import sae.bytecode.bat.BATDatabaseFactory
import UnissonOrdering._
import sae.bytecode.structure.MethodDeclaration

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


    def getStream(resource: String) = this.getClass.getClassLoader.getResourceAsStream (resource)

    @Test
    @Ignore
    def testViolationsActionsToExceptions() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)

        val ActionInterface = Ensemble ("ActionInterface", "class_with_members(class('cirrus.hibernate.impl','SessionImpl$Executable'))")
        val ScalarActions = Ensemble ("ScalarActions", "class_with_members(class('cirrus.hibernate.impl','ScheduledDeletion')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledInsertion')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledUpdate'))\nor\nclass_with_members('cirrus.hibernate.impl','ScheduledEntityAction')")
        val CollectionActions = Ensemble ("CollectionActions", "class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionDeletes')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionUpdates')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionInserts')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionRecreate')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionRemove')) \n\nor\nclass_with_members('cirrus.hibernate.impl','ScheduledCollectionAction')\nor\nclass_with_members('cirrus.hibernate.impl','ScheduledCollectionUpdate')")
        val Actions = Ensemble ("Actions", "derived", ActionInterface, ScalarActions, CollectionActions)


        val Exceptions = Ensemble ("Exceptions", "class_with_members(class('cirrus.hibernate','HibernateException')) \nor class_with_members(transitive(supertype(class('cirrus.hibernate','HibernateException')))) \nor class_with_members(class('cirrus.hibernate','AssertionFailure'))\nor class_with_members(class('cirrus.hibernate','LazyInitializationException'))")
        val Utilities = Ensemble ("Utilities", "package('cirrus.hibernate.helpers') ")
        val UtilitiesAndExceptions = Ensemble ("UtilitiesAndExceptions", "derived", Utilities, Exceptions)

        val Empty = Ensemble ("Empty", "empty")

        val ensembles = Set (Actions, UtilitiesAndExceptions, Empty)
        val emptyToExceptions = GlobalOutgoingConstraint ("all", Actions, Empty)
        val constraints = Set (emptyToExceptions)
        val global = Repository (ensembles)
        val model = Concern (ensembles, constraints, "Actions.sad")
        db.setRepository (global)
        db.addSlice (model)

        val queryResult = sae.relationToResult (db.violations)

        bc.addArchive (getStream ("hibernate-1.2.3.jar"))

        val result = queryResult.asList.sorted

        val expectedResult = List[IViolation](
            Violation (emptyToExceptions,
                ActionInterface,
                Exceptions,
                SourceElementFactory (
                    MethodDeclaration (
                        ObjectType ("cirrus/hibernate/impl/SessionImpl$Executable"),
                        "afterTransactionCompletion",
                        VoidType,
                        Seq ()
                    )
                ),
                SourceElementFactory (ObjectType ("cirrus/hibernate/HibernateException")),
                "throws", "Actions.sad"
            ),
            Violation (emptyToExceptions,
                ActionInterface,
                Exceptions,
                SourceElementFactory (
                    MethodDeclaration (
                        ObjectType ("cirrus/hibernate/impl/SessionImpl$Executable"),
                        "execute",
                        VoidType,
                        Seq ()
                    )
                ),
                SourceElementFactory (ObjectType ("cirrus/hibernate/HibernateException")),
                "throws", "Actions.sad"
            ),
            Violation (emptyToExceptions,
                CollectionActions,
                Exceptions,
                SourceElementFactory (
                    MethodDeclaration (
                        ObjectType ("cirrus/hibernate/impl/ScheduledCollectionAction"),
                        "afterTransactionCompletion",
                        VoidType,
                        Seq ()
                    )
                ),
                SourceElementFactory (ObjectType ("cirrus/hibernate/cache/CacheException")),
                "throws", "Actions.sad"
            ),
            Violation (emptyToExceptions,
                CollectionActions,
                Exceptions,
                SourceElementFactory (
                    MethodDeclaration (
                        ObjectType ("cirrus/hibernate/impl/ScheduledCollectionRecreate"),
                        "execute",
                        VoidType,
                        Seq ()

                    )
                ),
                SourceElementFactory (ObjectType ("cirrus/hibernate/HibernateException")),
                "throws", "Actions.sad"
            ),
            Violation (emptyToExceptions,
                CollectionActions,
                Exceptions,
                SourceElementFactory (
                    MethodDeclaration (
                        ObjectType ("cirrus/hibernate/impl/ScheduledCollectionRemove"),
                        "execute",
                        VoidType,
                        Seq ()

                    )
                ),
                SourceElementFactory (ObjectType ("cirrus/hibernate/HibernateException")),
                "throws", "Actions.sad"
            ),
            Violation (emptyToExceptions,
                CollectionActions,
                Exceptions,
                SourceElementFactory (
                    MethodDeclaration (
                        ObjectType ("cirrus/hibernate/impl/ScheduledCollectionUpdate"),
                        "execute",
                        VoidType,
                        Seq ()

                    )
                ),
                SourceElementFactory (ObjectType ("cirrus/hibernate/HibernateException")),
                "throws", "Actions.sad"
            ),
            Violation (emptyToExceptions,
                ScalarActions,
                Exceptions,
                SourceElementFactory (
                    MethodDeclaration (
                        ObjectType ("cirrus/hibernate/impl/ScheduledDeletion"),
                        "afterTransactionCompletion",
                        VoidType,
                        Seq ()

                    )
                ),
                SourceElementFactory (ObjectType ("cirrus/hibernate/cache/CacheException")),
                "throws", "Actions.sad"
            ),
            Violation (emptyToExceptions,
                ScalarActions,
                Exceptions,
                SourceElementFactory (
                    MethodDeclaration (
                        ObjectType ("cirrus/hibernate/impl/ScheduledUpdate"),
                        "afterTransactionCompletion",
                        VoidType,
                        Seq ()

                    )
                ),
                SourceElementFactory (ObjectType ("cirrus/hibernate/cache/CacheException")),
                "throws", "Actions.sad"
            ),
            Violation (emptyToExceptions,
                ScalarActions,
                Exceptions,
                SourceElementFactory (
                    MethodDeclaration (
                        ObjectType ("cirrus/hibernate/impl/ScheduledDeletion"),
                        "execute",
                        VoidType,
                        Seq ()

                    )
                ),
                SourceElementFactory (ObjectType ("cirrus/hibernate/HibernateException")),
                "throws", "Actions.sad"
            ),
            Violation (emptyToExceptions,
                ScalarActions,
                Exceptions,
                SourceElementFactory (
                    MethodDeclaration (
                        ObjectType ("cirrus/hibernate/impl/ScheduledUpdate"),
                        "execute",
                        VoidType,
                        Seq ()

                    )
                ),
                SourceElementFactory (ObjectType ("cirrus/hibernate/HibernateException")),
                "throws", "Actions.sad"
            ),
            Violation (emptyToExceptions,
                ScalarActions,
                Exceptions,
                SourceElementFactory (
                    MethodDeclaration (
                        ObjectType ("cirrus/hibernate/impl/ScheduledInsertion"),
                        "execute",
                        VoidType,
                        Seq ()

                    )
                ),
                SourceElementFactory (ObjectType ("cirrus/hibernate/HibernateException")),
                "throws", "Actions.sad"
            )
        ).sorted

        result should be (expectedResult)
    }

    @Test
    @Ignore
    def testActionsToExceptionsDependencies() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)

        val ActionInterface = Ensemble ("ActionInterface", "class_with_members(class('cirrus.hibernate.impl','SessionImpl$Executable'))")
        val ScalarActions = Ensemble ("ScalarActions", "class_with_members(class('cirrus.hibernate.impl','ScheduledDeletion')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledInsertion')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledUpdate'))\nor\nclass_with_members('cirrus.hibernate.impl','ScheduledEntityAction')")
        val CollectionActions = Ensemble ("CollectionActions", "class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionDeletes')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionUpdates')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionInserts')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionRecreate')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionRemove')) \n\nor\nclass_with_members('cirrus.hibernate.impl','ScheduledCollectionAction')\nor\nclass_with_members('cirrus.hibernate.impl','ScheduledCollectionUpdate')")
        val Actions = Ensemble ("Actions", "derived", ActionInterface, ScalarActions, CollectionActions)


        val Exceptions = Ensemble ("Exceptions", "class_with_members(class('cirrus.hibernate','HibernateException')) \nor class_with_members(transitive(supertype(class('cirrus.hibernate','HibernateException')))) \nor class_with_members(class('cirrus.hibernate','AssertionFailure'))\nor class_with_members(class('cirrus.hibernate','LazyInitializationException'))")
        val Utilities = Ensemble ("Utilities", "package('cirrus.hibernate.helpers') ")
        val UtilitiesAndExceptions = Ensemble ("UtilitiesAndExceptions", "derived", Utilities, Exceptions)

        val ensembles = Set (Actions, UtilitiesAndExceptions)
        val global = Repository (ensembles)
        db.setRepository (global)

        /*
        val queryResult: QueryResult[(IEnsemble, IEnsemble, Int)] = db.ensembleDependencies

        bc.addArchiveAsResource("hibernate-1.2.3.jar")

        val result = queryResult.asList.sorted

        result.foreach(println)
        */
    }

}