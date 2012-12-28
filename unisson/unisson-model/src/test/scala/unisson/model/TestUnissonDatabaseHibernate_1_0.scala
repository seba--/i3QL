package unisson.model

import mock.vespucci._
import org.scalatest.matchers.ShouldMatchers
import org.junit.{Assert, Test}
import unisson.query.code_model.SourceElementFactory
import de.tud.cs.st.bat.resolved.{IntegerType, ArrayType, VoidType, ObjectType}
import sae.bytecode.bat.BATDatabaseFactory
import sae.bytecode.structure.MethodDeclaration
import UnissonOrdering._

/**
 *
 * Author: Ralf Mitschke
 * Date: 24.02.12
 * Time: 09:53
 *
 */
class TestUnissonDatabaseHibernate_1_0
    extends ShouldMatchers
{

    def getStream(resource: String) = this.getClass.getClassLoader.getResourceAsStream (resource)

    @Test
    def testEnsembleElementsInDeprecatedLegacy() {

        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)

        val elements = sae.relationToResult (db.ensemble_elements)

        val deprecatedPersistenceMarkerInterface = Ensemble ("DeprecatedPersistenceMarkerInterface", "class_with_members('cirrus.hibernate','Persistent')")
        val deprecatedPersistenceLifecycleCallbacks = Ensemble ("DeprecatedPersistenceLifecycleCallbacks", "class_with_members('cirrus.hibernate','PersistentLifecycle')")
        val deprecatedLegacy = Ensemble ("DeprecatedLegacy", "derived", deprecatedPersistenceMarkerInterface, deprecatedPersistenceLifecycleCallbacks)

        val ensembles = Set (deprecatedLegacy)
        val global = Repository (ensembles)

        db.setRepository (global)


        bc.addArchive (getStream ("hibernate-1.0.jar"))

        Assert.assertEquals (
            List (
                (deprecatedLegacy, SourceElementFactory (ObjectType ("cirrus/hibernate/Persistent"))),
                (deprecatedLegacy, SourceElementFactory (ObjectType ("cirrus/hibernate/PersistentLifecycle"))),
                (deprecatedLegacy,
                    SourceElementFactory (MethodDeclaration (
                        ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                        "create",
                        VoidType,
                        Seq (ObjectType ("cirrus/hibernate/Session"))
                    ))
                    ),
                (deprecatedLegacy,
                    SourceElementFactory (MethodDeclaration (
                        ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                        "delete",
                        VoidType,
                        Seq (ObjectType ("cirrus/hibernate/Session"))
                    ))
                    ),
                (deprecatedLegacy,
                    SourceElementFactory (MethodDeclaration (
                        ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                        "load",
                        VoidType,
                        Seq (ObjectType ("cirrus/hibernate/Session"))
                    ))
                    ),
                (deprecatedLegacy,
                    SourceElementFactory (MethodDeclaration (
                        ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                        "store",
                        VoidType,
                        Seq ()
                    ))
                    ),
                (deprecatedPersistenceLifecycleCallbacks, SourceElementFactory (ObjectType ("cirrus/hibernate/PersistentLifecycle"))),
                (deprecatedPersistenceLifecycleCallbacks,
                    SourceElementFactory (MethodDeclaration (
                        ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                        "create",
                        VoidType,
                        Seq (ObjectType ("cirrus/hibernate/Session"))
                    ))
                    ),
                (deprecatedPersistenceLifecycleCallbacks,
                    SourceElementFactory (MethodDeclaration (
                        ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                        "delete",
                        VoidType,
                        Seq (ObjectType ("cirrus/hibernate/Session"))
                    ))
                    ),
                (deprecatedPersistenceLifecycleCallbacks,
                    SourceElementFactory (MethodDeclaration (
                        ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                        "load",
                        VoidType,
                        Seq (ObjectType ("cirrus/hibernate/Session"))
                    ))
                    ),
                (deprecatedPersistenceLifecycleCallbacks,
                    SourceElementFactory (MethodDeclaration (
                        ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                        "store",
                        VoidType,
                        Seq ()
                    ))
                    ),
                (deprecatedPersistenceMarkerInterface, SourceElementFactory (ObjectType ("cirrus/hibernate/Persistent")))
            ),
            elements.asList.sorted
        )
    }

    @Test
    def testViolationsToDeprecatedLegacy() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)

        val deprecatedPersistenceMarkerInterface = Ensemble ("DeprecatedPersistenceMarkerInterface", "class_with_members('cirrus.hibernate','Persistent')")
        val deprecatedPersistenceLifecycleCallbacks = Ensemble ("DeprecatedPersistenceLifecycleCallbacks", "class_with_members('cirrus.hibernate','PersistentLifecycle')")
        val deprecatedLegacy = Ensemble ("DeprecatedLegacy", "derived", deprecatedPersistenceMarkerInterface, deprecatedPersistenceLifecycleCallbacks)

        val classPersister = Ensemble ("ClassPersister", "class_with_members('cirrus.hibernate.impl','ClassPersister')")
        val relationalDatabaseSession = Ensemble ("RelationalDatabaseSession", "class_with_members('cirrus.hibernate.impl','RelationalDatabaseSession')")
        val usersOfPersistenceLifecycleCallbacks = Ensemble ("UsersOfPersistenceLifecycleCallbacks", "derived", classPersister, relationalDatabaseSession)

        val ensembles = Set (deprecatedLegacy, usersOfPersistenceLifecycleCallbacks)
        val emptyToDeprecatedPersistenceLifecycleCallbacks = GlobalIncomingConstraint ("all", EmptyEnsemble, deprecatedLegacy)
        val constraints = Set (emptyToDeprecatedPersistenceLifecycleCallbacks)
        val global = Repository (ensembles)
        val model = Concern (ensembles, constraints, "DeprecatedLegacy")

        val queryResult = sae.relationToResult (db.violations)

        db.setRepository (global)
        db.addSlice (model)


        bc.addArchive (getStream ("hibernate-1.0.jar"))

        val result = queryResult.asList.sorted.map (_.toString).foldLeft ("")(_ + "\n" + _)

        val expectedResult = List (
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                classPersister,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/ClassPersister"),
                    "assemble",
                    ObjectType ("java/lang/Object"),
                    Seq (ArrayType (1, ObjectType ("java/lang/Object")), ObjectType ("java/lang/Object"), ObjectType ("java/io/Serializable"), ObjectType ("cirrus/hibernate/impl/SessionImplementor"))
                )),
                SourceElementFactory (ObjectType ("cirrus/hibernate/PersistentLifecycle")),
                "class_cast",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                classPersister,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/ClassPersister"),
                    "assemble",
                    ObjectType ("java/lang/Object"),
                    Seq (ArrayType (1, ObjectType ("java/lang/Object")), ObjectType ("java/lang/Object"), ObjectType ("java/io/Serializable"), ObjectType ("cirrus/hibernate/impl/SessionImplementor"))
                )),
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                    "load",
                    VoidType,
                    Seq (ObjectType ("cirrus/hibernate/Session"))
                )),
                "invoke_interface",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "delete",
                    VoidType,
                    Seq (ObjectType ("java/lang/Object"))
                )),
                SourceElementFactory (ObjectType ("cirrus/hibernate/PersistentLifecycle")),
                "class_cast",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "delete",
                    VoidType,
                    Seq (ObjectType ("java/lang/Object"))
                )),
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                    "delete",
                    VoidType,
                    Seq (ObjectType ("cirrus/hibernate/Session"))
                )),
                "invoke_interface",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "doLoadCallbacks",
                    VoidType,
                    Seq (ObjectType ("java/util/List"), IntegerType)
                )),
                SourceElementFactory (ObjectType ("cirrus/hibernate/PersistentLifecycle")),
                "class_cast",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "doLoadCallbacks",
                    VoidType,
                    Seq (ObjectType ("java/util/List"), IntegerType)
                )),
                SourceElementFactory (ObjectType ("cirrus/hibernate/PersistentLifecycle")),
                "class_cast",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "doLoadCallbacks",
                    VoidType,
                    Seq (ObjectType ("java/util/List"), IntegerType)
                )),
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                    "load",
                    VoidType,
                    Seq (ObjectType ("cirrus/hibernate/Session"))
                )),
                "invoke_interface",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "doLoadCallbacks",
                    VoidType,
                    Seq (ObjectType ("java/util/List"), IntegerType)
                )),
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                    "load",
                    VoidType,
                    Seq (ObjectType ("cirrus/hibernate/Session"))
                )),
                "invoke_interface",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "doStoreCallbacks",
                    VoidType,
                    Seq ()
                )),
                SourceElementFactory (ObjectType ("cirrus/hibernate/PersistentLifecycle")),
                "class_cast",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "doStoreCallbacks",
                    VoidType,
                    Seq ()
                )),
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                    "store",
                    VoidType,
                    Seq ()
                )),
                "invoke_interface",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "save",
                    ObjectType ("java/io/Serializable"),
                    Seq (ObjectType ("java/lang/Object"), ObjectType ("java/io/Serializable"))
                )),
                SourceElementFactory (ObjectType ("cirrus/hibernate/PersistentLifecycle")),
                "class_cast",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "save",
                    ObjectType ("java/io/Serializable"),
                    Seq (ObjectType ("java/lang/Object"), ObjectType ("java/io/Serializable"))
                )),
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                    "create",
                    VoidType,
                    Seq (ObjectType ("cirrus/hibernate/Session"))
                )),
                "invoke_interface",
                "DeprecatedLegacy"),
            // from usersOfPersistentLifeCycleCallBack
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                usersOfPersistenceLifecycleCallbacks,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/ClassPersister"),
                    "assemble",
                    ObjectType ("java/lang/Object"),
                    Seq (ArrayType (1, ObjectType ("java/lang/Object")), ObjectType ("java/lang/Object"), ObjectType ("java/io/Serializable"), ObjectType ("cirrus/hibernate/impl/SessionImplementor"))
                )),
                SourceElementFactory (ObjectType ("cirrus/hibernate/PersistentLifecycle")),
                "class_cast",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                usersOfPersistenceLifecycleCallbacks,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/ClassPersister"),
                    "assemble",
                    ObjectType ("java/lang/Object"),
                    Seq (ArrayType (1, ObjectType ("java/lang/Object")), ObjectType ("java/lang/Object"), ObjectType ("java/io/Serializable"), ObjectType ("cirrus/hibernate/impl/SessionImplementor"))
                )),
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                    "load",
                    VoidType,
                    Seq (ObjectType ("cirrus/hibernate/Session"))
                )),
                "invoke_interface",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                usersOfPersistenceLifecycleCallbacks,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "delete",
                    VoidType,
                    Seq (ObjectType ("java/lang/Object"))
                )),
                SourceElementFactory (ObjectType ("cirrus/hibernate/PersistentLifecycle")),
                "class_cast",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                usersOfPersistenceLifecycleCallbacks,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "delete",
                    VoidType,
                    Seq (ObjectType ("java/lang/Object"))
                )),
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                    "delete",
                    VoidType,
                    Seq (ObjectType ("cirrus/hibernate/Session"))
                )),
                "invoke_interface",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                usersOfPersistenceLifecycleCallbacks,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "doLoadCallbacks",
                    VoidType,
                    Seq (ObjectType ("java/util/List"), IntegerType)
                )),
                SourceElementFactory (ObjectType ("cirrus/hibernate/PersistentLifecycle")),
                "class_cast",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                usersOfPersistenceLifecycleCallbacks,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "doLoadCallbacks",
                    VoidType,
                    Seq (ObjectType ("java/util/List"), IntegerType)
                )),
                SourceElementFactory (ObjectType ("cirrus/hibernate/PersistentLifecycle")),
                "class_cast",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                usersOfPersistenceLifecycleCallbacks,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "doLoadCallbacks",
                    VoidType,
                    Seq (ObjectType ("java/util/List"), IntegerType)
                )),
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                    "load",
                    VoidType,
                    Seq (ObjectType ("cirrus/hibernate/Session"))
                )),
                "invoke_interface",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                usersOfPersistenceLifecycleCallbacks,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "doLoadCallbacks",
                    VoidType,
                    Seq (ObjectType ("java/util/List"), IntegerType)
                )),
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                    "load",
                    VoidType,
                    Seq (ObjectType ("cirrus/hibernate/Session"))
                )),
                "invoke_interface",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                usersOfPersistenceLifecycleCallbacks,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "doStoreCallbacks",
                    VoidType,
                    Seq ()
                )),
                SourceElementFactory (ObjectType ("cirrus/hibernate/PersistentLifecycle")),
                "class_cast",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                usersOfPersistenceLifecycleCallbacks,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "doStoreCallbacks",
                    VoidType,
                    Seq ()
                )),
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                    "store",
                    VoidType,
                    Seq ()
                )),
                "invoke_interface",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                usersOfPersistenceLifecycleCallbacks,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "save",
                    ObjectType ("java/io/Serializable"),
                    Seq (ObjectType ("java/lang/Object"), ObjectType ("java/io/Serializable"))
                )),
                SourceElementFactory (ObjectType ("cirrus/hibernate/PersistentLifecycle")),
                "class_cast",
                "DeprecatedLegacy"),
            Violation (
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                usersOfPersistenceLifecycleCallbacks,
                deprecatedLegacy,
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "save",
                    ObjectType ("java/io/Serializable"),
                    Seq (ObjectType ("java/lang/Object"), ObjectType ("java/io/Serializable"))
                )),
                SourceElementFactory (MethodDeclaration (
                    ObjectType ("cirrus/hibernate/PersistentLifecycle"),
                    "create",
                    VoidType,
                    Seq (ObjectType ("cirrus/hibernate/Session"))
                )),
                "invoke_interface",
                "DeprecatedLegacy")
        ).map (_.toString).foldLeft ("")(_ + "\n" + _)

        Assert.assertEquals (expectedResult, result)
    }

    @Test
    def testViolationSummaryActionsImplementsInternalWithGlobal() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)

        val ActionInterface = Ensemble ("ActionInterface", "class_with_members(class('cirrus.hibernate.impl','RelationalDatabaseSession$Executable'))")
        val ScalarActions = Ensemble ("ScalarActions", "class_with_members(class('cirrus.hibernate.impl','ScheduledDeletion')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledInsertion')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledUpdate'))")
        val CollectionActions = Ensemble ("CollectionActions", "class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionDeletes')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionUpdates')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionInserts')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionRecreate')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionRemove'))")
        val Actions = Ensemble ("Actions", "derived", ActionInterface, ScalarActions, CollectionActions)


        val ensembles = Set (Actions)
        val EmptyToActions = GlobalIncomingConstraint ("all", EmptyEnsemble, Actions)
        val ActionsToEmpty = GlobalOutgoingConstraint ("all", Actions, EmptyEnsemble)
        val ScalarActionsToActionInterface = IncomingOutgoingConstraint ("implements", ScalarActions, ActionInterface)
        val CollectionActionsToActionInterface = IncomingOutgoingConstraint ("implements", CollectionActions, ActionInterface)

        val constraints = Set (EmptyToActions, ActionsToEmpty, ScalarActionsToActionInterface, CollectionActionsToActionInterface)
        val global = Repository (ensembles)
        val model = Concern (ensembles, constraints, "actions.sad")
        db.setRepository (global)
        db.addSlice (model)

        val queryResult = sae.relationToResult (db.violation_summary)

        bc.addArchive (getStream ("hibernate-1.0.jar"))

        queryResult.asList should be (Nil)
    }

    @Test
    def testViolationSummaryActionsAllInternalWithGlobal() {
        val bc = BATDatabaseFactory.create ()
        val db = new UnissonDatabase (bc)

        val ActionInterface = Ensemble ("ActionInterface", "class_with_members(class('cirrus.hibernate.impl','RelationalDatabaseSession$Executable'))")
        val ScalarActions = Ensemble ("ScalarActions", "class_with_members(class('cirrus.hibernate.impl','ScheduledDeletion')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledInsertion')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledUpdate'))")
        val CollectionActions = Ensemble ("CollectionActions", "class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionDeletes')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionUpdates')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionInserts')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionRecreate')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionRemove'))")
        val Actions = Ensemble ("Actions", "derived", ActionInterface, ScalarActions, CollectionActions)

        val Utilities = Ensemble ("Utilities", "package('cirrus.hibernate.helpers')")
        val Exceptions = Ensemble ("Exceptions", "class_with_members(class('cirrus.hibernate','HibernateException')) \nor class_with_members(transitive(supertype(class('cirrus.hibernate','HibernateException')))) \nor class_with_members(class('cirrus.hibernate','AssertionFailure'))\nor class_with_members(class('cirrus.hibernate','LazyInitializationException'))")
        val UtilitiesAndExceptions = Ensemble ("UtilitiesAndExceptions", "derived", Utilities, Exceptions)

        val ClassPersister = Ensemble ("ClassPersister", "class_with_members('cirrus.hibernate.impl','ClassPersister')")
        val CollectionPersister = Ensemble ("CollectionPersister", "class_with_members('cirrus.hibernate.impl','CollectionPersister')")
        val PersistentCollectionWrappers = Ensemble ("PersistentCollectionWrappers", "package('cirrus.hibernate.collections')")
        val ResultIterator = Ensemble ("ResultIterator", "class_with_members('cirrus.hibernate.impl','IdentifierIterator')")
        val ByIDQuery = Ensemble ("ByIDQuery", "class_with_members('cirrus.hibernate.impl','ByIDQuery')")
        val PersisterCache = Ensemble ("PersisterCache", "class_with_members('cirrus.hibernate.impl','PersisterCache')")
        val PersistenceManagement = Ensemble ("PersistenceManagement", "derived", ClassPersister, CollectionPersister, PersistentCollectionWrappers, ResultIterator, ByIDQuery, PersisterCache)

        val SessionInternalInterface = Ensemble ("SessionInternalInterface", "class_with_members('cirrus.hibernate.impl','SessionImplementor')")
        val SessionApplicationInterface = Ensemble ("SessionApplicationInterface", "class_with_members('cirrus.hibernate','Session')")
        val ConcreteSession = Ensemble ("ConcreteSession", "class_with_members('cirrus.hibernate.impl','RelationalDatabaseSession')\nwithout\nclass_with_members('cirrus.hibernate.impl','RelationalDatabaseSession$Executable')")
        val SessionFactory = Ensemble ("SessionFactory", "class_with_members('cirrus.hibernate','SessionFactory')")
        val ConcreteSessionFactory = Ensemble ("ConcreteSessionFactory", "class_with_members('cirrus.hibernate.impl','RelationalDatabaseSessionFactory')")
        val JNDIFactoryLookup = Ensemble ("JNDIFactoryLookup", "class_with_members('cirrus.hibernate.impl','SessionFactoryObjectFactory')")
        val SessionManagement = Ensemble ("SessionManagement", "derived", SessionInternalInterface, SessionApplicationInterface, ConcreteSession, SessionFactory, ConcreteSessionFactory, JNDIFactoryLookup)

        val ensembles = Set (
            Actions,
            UtilitiesAndExceptions,
            PersistenceManagement,
            SessionManagement
        )

        val ActionsToUtilAndExceptions = GlobalOutgoingConstraint ("all", Actions, UtilitiesAndExceptions)
        val ActionsToSession = GlobalOutgoingConstraint ("all", Actions, SessionManagement)
        val ActionToPersistence = GlobalOutgoingConstraint ("all", Actions, PersistenceManagement)
        val SessionToAction = GlobalIncomingConstraint ("all", SessionManagement, Actions)

        val constraints = Set (
            ActionsToUtilAndExceptions,
            ActionsToSession,
            ActionToPersistence,
            SessionToAction
        )
        val global = Repository (ensembles)
        val model = Concern (ensembles, constraints, "actions.sad")
        db.setRepository (global)
        db.addSlice (model)

        val queryResult = sae.relationToResult (db.violation_summary)

        bc.addArchive (getStream ("hibernate-1.0.jar"))

        println (queryResult.asList.sorted.map (_.toString).foldLeft ("")(_ + "\n" + _))
        Assert.assertEquals (Nil, queryResult.asList)
    }

}