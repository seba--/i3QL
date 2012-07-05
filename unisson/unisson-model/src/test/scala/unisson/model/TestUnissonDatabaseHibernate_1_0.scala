package unisson.model

import mock.vespucci._
import org.scalatest.matchers.ShouldMatchers
import org.junit.Test
import de.tud.cs.st.vespucci.model.IEnsemble
import unisson.query.code_model.SourceElement
import sae.bytecode.model.MethodDeclaration
import de.tud.cs.st.bat.constants.ACC_PUBLIC
import sae.bytecode.BytecodeDatabase
import de.tud.cs.st.bat.{ArrayType, IntegerType, VoidType, ObjectType}
import sae.collections.QueryResult
import de.tud.cs.st.vespucci.interfaces.{ICodeElement, IViolationSummary, IViolation}

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


    import UnissonOrdering._
    import sae.collections.Conversions._

    @Test
    def testEnsembleElementsInDeprecatedLegacy() {

        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val elements: QueryResult[(IEnsemble, ICodeElement)] = db.ensemble_elements

        val deprecatedPersistenceMarkerInterface = Ensemble("DeprecatedPersistenceMarkerInterface", "class_with_members('cirrus.hibernate','Persistent')")
        val deprecatedPersistenceLifecycleCallbacks = Ensemble("DeprecatedPersistenceLifecycleCallbacks", "class_with_members('cirrus.hibernate','PersistentLifecycle')")
        val deprecatedLegacy = Ensemble("DeprecatedLegacy", "derived", deprecatedPersistenceMarkerInterface, deprecatedPersistenceLifecycleCallbacks)

        val ensembles = Set(deprecatedLegacy)
        val global = GlobalArchitectureModel(ensembles)

        db.addGlobalModel(global)

        bc.addArchiveAsResource("hibernate-1.0.jar")

        elements.asList.sorted should be(
            List(
                (deprecatedPersistenceLifecycleCallbacks, SourceElement(ObjectType("cirrus/hibernate/PersistentLifecycle"))),
                (deprecatedPersistenceLifecycleCallbacks,
                        SourceElement(MethodDeclaration(
                            ObjectType("cirrus/hibernate/PersistentLifecycle"),
                            "create",
                            Seq(ObjectType("cirrus/hibernate/Session")),
                            VoidType(),
                            ACC_PUBLIC.mask,
                            isDeprecated = false,
                            isSynthetic = false
                        ))
                        ),
                (deprecatedPersistenceLifecycleCallbacks,
                        SourceElement(MethodDeclaration(
                            ObjectType("cirrus/hibernate/PersistentLifecycle"),
                            "delete",
                            Seq(ObjectType("cirrus/hibernate/Session")),
                            VoidType(),
                            ACC_PUBLIC.mask,
                            isDeprecated = false,
                            isSynthetic = false
                        ))
                        ),
                (deprecatedPersistenceLifecycleCallbacks,
                        SourceElement(MethodDeclaration(
                            ObjectType("cirrus/hibernate/PersistentLifecycle"),
                            "load",
                            Seq(ObjectType("cirrus/hibernate/Session")),
                            VoidType(),
                            ACC_PUBLIC.mask,
                            isDeprecated = false,
                            isSynthetic = false
                        ))
                        ),
                (deprecatedPersistenceLifecycleCallbacks,
                        SourceElement(MethodDeclaration(
                            ObjectType("cirrus/hibernate/PersistentLifecycle"),
                            "store",
                            Seq(),
                            VoidType(),
                            ACC_PUBLIC.mask,
                            isDeprecated = false,
                            isSynthetic = false
                        ))
                        ),
                (deprecatedPersistenceMarkerInterface, SourceElement(ObjectType("cirrus/hibernate/Persistent")))
            )
        )
    }

    @Test
    def testViolationsToDeprecatedLegacy() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val deprecatedPersistenceMarkerInterface = Ensemble("DeprecatedPersistenceMarkerInterface", "class_with_members('cirrus.hibernate','Persistent')")
        val deprecatedPersistenceLifecycleCallbacks = Ensemble("DeprecatedPersistenceLifecycleCallbacks", "class_with_members('cirrus.hibernate','PersistentLifecycle')")
        val deprecatedLegacy = Ensemble("DeprecatedLegacy", "derived", deprecatedPersistenceMarkerInterface, deprecatedPersistenceLifecycleCallbacks)

        val classPersister = Ensemble("ClassPersister", "class_with_members('cirrus.hibernate.impl','ClassPersister')")
        val relationalDatabaseSession = Ensemble("RelationalDatabaseSession", "class_with_members('cirrus.hibernate.impl','RelationalDatabaseSession')")
        val usersOfPersistenceLifecycleCallbacks = Ensemble("UsersOfPersistenceLifecycleCallbacks", "derived", classPersister, relationalDatabaseSession)

        val ensembles = Set(deprecatedLegacy, usersOfPersistenceLifecycleCallbacks)
        val emptyToDeprecatedPersistenceLifecycleCallbacks = GlobalIncomingConstraint("all", EmptyEnsemble, deprecatedLegacy)
        val constraints = Set(emptyToDeprecatedPersistenceLifecycleCallbacks)
        val global = GlobalArchitectureModel(ensembles)
        val model = ArchitectureModel(ensembles, constraints, "DeprecatedLegacy")
        db.addGlobalModel(global)
        db.addModel(model)

        val queryResult: QueryResult[IViolation] = db.violations

        bc.addArchiveAsResource("hibernate-1.0.jar")

        val result = queryResult.asList.sorted

        val expectedResult = List(
            Violation(
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                classPersister,
                deprecatedPersistenceLifecycleCallbacks,
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/impl/ClassPersister"),
                    "assemble",
                    Seq(ArrayType(1, ObjectType("java/lang/Object")), ObjectType("java/lang/Object"), ObjectType("java/io/Serializable"), ObjectType("cirrus/hibernate/impl/SessionImplementor")),
                    ObjectType("java/lang/Object"),
                    0,
                    isDeprecated = false,
                    isSynthetic = false
                )),
                SourceElement(ObjectType("cirrus/hibernate/PersistentLifecycle")),
                "class_cast",
                "DeprecatedLegacy"),
            Violation(
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                classPersister,
                deprecatedPersistenceLifecycleCallbacks,
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/impl/ClassPersister"),
                    "assemble",
                    Seq(ArrayType(1, ObjectType("java/lang/Object")), ObjectType("java/lang/Object"), ObjectType("java/io/Serializable"), ObjectType("cirrus/hibernate/impl/SessionImplementor")),
                    ObjectType("java/lang/Object"),
                    0,
                    isDeprecated = false,
                    isSynthetic = false
                )),
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/PersistentLifecycle"),
                    "load",
                    Seq(ObjectType("cirrus/hibernate/Session")),
                    VoidType(),
                    0,
                    isDeprecated = false,
                    isSynthetic = false
                )),
                "invoke_interface",
                "DeprecatedLegacy"),
            Violation(
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedPersistenceLifecycleCallbacks,
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "delete",
                    Seq(ObjectType("java/lang/Object")),
                    VoidType(),
                    0,
                    isDeprecated = false,
                    isSynthetic = false
                )),
                SourceElement(ObjectType("cirrus/hibernate/PersistentLifecycle")),
                "class_cast",
                "DeprecatedLegacy"),
            Violation(
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedPersistenceLifecycleCallbacks,
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "delete",
                    Seq(ObjectType("java/lang/Object")),
                    VoidType(),
                    0,
                    isDeprecated = false,
                    isSynthetic = false
                )),
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/PersistentLifecycle"),
                    "delete",
                    Seq(ObjectType("cirrus/hibernate/Session")),
                    VoidType(),
                    0,
                    isDeprecated = false,
                    isSynthetic = false
                )),
                "invoke_interface",
                "DeprecatedLegacy"),
            Violation(
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedPersistenceLifecycleCallbacks,
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "doLoadCallbacks",
                    Seq(ObjectType("java/util/List"), IntegerType()),
                    VoidType(),
                    0,
                    isDeprecated = false,
                    isSynthetic = false
                )),
                SourceElement(ObjectType("cirrus/hibernate/PersistentLifecycle")),
                "class_cast",
                "DeprecatedLegacy"),
            Violation(
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedPersistenceLifecycleCallbacks,
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "doLoadCallbacks",
                    Seq(ObjectType("java/util/List"), IntegerType()),
                    VoidType(),
                    0,
                    isDeprecated = false,
                    isSynthetic = false
                )),
                SourceElement(ObjectType("cirrus/hibernate/PersistentLifecycle")),
                "class_cast",
                "DeprecatedLegacy"),
            Violation(
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedPersistenceLifecycleCallbacks,
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "doLoadCallbacks",
                    Seq(ObjectType("java/util/List"), IntegerType()),
                    VoidType(),
                    0,
                    isDeprecated = false,
                    isSynthetic = false
                )),
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/PersistentLifecycle"),
                    "load",
                    Seq(ObjectType("cirrus/hibernate/Session")),
                    VoidType(),
                    0,
                    isDeprecated = false,
                    isSynthetic = false
                )),
                "invoke_interface",
                "DeprecatedLegacy"),
            Violation(
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedPersistenceLifecycleCallbacks,
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "doLoadCallbacks",
                    Seq(ObjectType("java/util/List"), IntegerType()),
                    VoidType(),
                    0,
                    isDeprecated = false,
                    isSynthetic = false
                )),
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/PersistentLifecycle"),
                    "load",
                    Seq(ObjectType("cirrus/hibernate/Session")),
                    VoidType(),
                    0,
                    isDeprecated = false,
                    isSynthetic = false
                )),
                "invoke_interface",
                "DeprecatedLegacy"),
            Violation(
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedPersistenceLifecycleCallbacks,
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "doStoreCallbacks",
                    Seq(),
                    VoidType(),
                    0,
                    isDeprecated = false,
                    isSynthetic = false
                )),
                SourceElement(ObjectType("cirrus/hibernate/PersistentLifecycle")),
                "class_cast",
                "DeprecatedLegacy"),
            Violation(
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedPersistenceLifecycleCallbacks,
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "doStoreCallbacks",
                    Seq(),
                    VoidType(),
                    0,
                    isDeprecated = false,
                    isSynthetic = false
                )),
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/PersistentLifecycle"),
                    "store",
                    Seq(),
                    VoidType(),
                    0,
                    isDeprecated = false,
                    isSynthetic = false
                )),
                "invoke_interface",
                "DeprecatedLegacy"),
            Violation(
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedPersistenceLifecycleCallbacks,
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "save",
                    Seq(ObjectType("java/lang/Object"), ObjectType("java/io/Serializable")),
                    ObjectType("java/io/Serializable"),
                    0,
                    isDeprecated = false,
                    isSynthetic = false
                )),
                SourceElement(ObjectType("cirrus/hibernate/PersistentLifecycle")),
                "class_cast",
                "DeprecatedLegacy"),
            Violation(
                emptyToDeprecatedPersistenceLifecycleCallbacks,
                relationalDatabaseSession,
                deprecatedPersistenceLifecycleCallbacks,
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/impl/RelationalDatabaseSession"),
                    "save",
                    Seq(ObjectType("java/lang/Object"), ObjectType("java/io/Serializable")),
                    ObjectType("java/io/Serializable"),
                    0,
                    isDeprecated = false,
                    isSynthetic = false
                )),
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/PersistentLifecycle"),
                    "create",
                    Seq(ObjectType("cirrus/hibernate/Session")),
                    VoidType(),
                    0,
                    isDeprecated = false,
                    isSynthetic = false
                )),
                "invoke_interface",
                "DeprecatedLegacy")
        )

        result should be(expectedResult)

    }

    @Test
    def testViolationSummaryActionsImplementsInternalWithGlobal() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ActionInterface = Ensemble("ActionInterface", "class_with_members(class('cirrus.hibernate.impl','RelationalDatabaseSession$Executable'))")
        val ScalarActions = Ensemble("ScalarActions", "class_with_members(class('cirrus.hibernate.impl','ScheduledDeletion')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledInsertion')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledUpdate'))")
        val CollectionActions = Ensemble("CollectionActions", "class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionDeletes')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionUpdates')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionInserts')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionRecreate')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionRemove'))")
        val Actions = Ensemble("Actions", "derived", ActionInterface, ScalarActions, CollectionActions)


        val ensembles = Set(Actions)
        val EmptyToActions = GlobalIncomingConstraint("all", EmptyEnsemble, Actions)
        val ActionsToEmpty = GlobalOutgoingConstraint("all", Actions, EmptyEnsemble)
        val ScalarActionsToActionInterface = IncomingOutgoingConstraint("implements", ScalarActions, ActionInterface)
        val CollectionActionsToActionInterface = IncomingOutgoingConstraint("implements", CollectionActions, ActionInterface)

        val constraints = Set(EmptyToActions, ActionsToEmpty, ScalarActionsToActionInterface, CollectionActionsToActionInterface)
        val global = GlobalArchitectureModel(ensembles)
        val model = ArchitectureModel(ensembles, constraints, "actions.sad")
        db.addGlobalModel(global)
        db.addModel(model)

        val queryResult: QueryResult[IViolationSummary] = db.violation_summary

        bc.addArchiveAsResource("hibernate-1.0.jar")

        queryResult.asList should be (Nil)
    }

    @Test
    def testViolationSummaryActionsAllInternalWithGlobal() {
        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val ActionInterface = Ensemble("ActionInterface", "class_with_members(class('cirrus.hibernate.impl','RelationalDatabaseSession$Executable'))")
        val ScalarActions = Ensemble("ScalarActions", "class_with_members(class('cirrus.hibernate.impl','ScheduledDeletion')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledInsertion')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledUpdate'))")
        val CollectionActions = Ensemble("CollectionActions", "class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionDeletes')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionUpdates')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionInserts')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionRecreate')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionRemove'))")
        val Actions = Ensemble("Actions", "derived", ActionInterface, ScalarActions, CollectionActions)

        val Utilities = Ensemble("Utilities", "package('cirrus.hibernate.helpers')")
        val Exceptions = Ensemble("Exceptions", "class_with_members(class('cirrus.hibernate','HibernateException')) \nor class_with_members(transitive(supertype(class('cirrus.hibernate','HibernateException')))) \nor class_with_members(class('cirrus.hibernate','AssertionFailure'))\nor class_with_members(class('cirrus.hibernate','LazyInitializationException'))")
        val UtilitiesAndExceptions = Ensemble("UtilitiesAndExceptions", "derived", Utilities, Exceptions)

        val ClassPersister = Ensemble("ClassPersister", "class_with_members('cirrus.hibernate.impl','ClassPersister')")
        val CollectionPersister = Ensemble("CollectionPersister", "class_with_members('cirrus.hibernate.impl','CollectionPersister')")
        val PersistentCollectionWrappers = Ensemble("PersistentCollectionWrappers", "package('cirrus.hibernate.collections')")
        val ResultIterator = Ensemble("ResultIterator", "class_with_members('cirrus.hibernate.impl','IdentifierIterator')")
        val ByIDQuery = Ensemble("ByIDQuery", "class_with_members('cirrus.hibernate.impl','ByIDQuery')")
        val PersisterCache = Ensemble("PersisterCache", "class_with_members('cirrus.hibernate.impl','PersisterCache')")
        val PersistenceManagement = Ensemble("PersistenceManagement", "derived",ClassPersister, CollectionPersister, PersistentCollectionWrappers, ResultIterator, ByIDQuery, PersisterCache)

        val SessionInternalInterface = Ensemble("SessionInternalInterface", "class_with_members('cirrus.hibernate.impl','SessionImplementor')")
        val SessionApplicationInterface = Ensemble("SessionApplicationInterface", "class_with_members('cirrus.hibernate','Session')")
        val ConcreteSession = Ensemble("ConcreteSession", "class_with_members('cirrus.hibernate.impl','RelationalDatabaseSession')\nwithout\nclass_with_members('cirrus.hibernate.impl','RelationalDatabaseSession$Executable')")
        val SessionFactory = Ensemble("SessionFactory", "class_with_members('cirrus.hibernate','SessionFactory')")
        val ConcreteSessionFactory = Ensemble("ConcreteSessionFactory", "class_with_members('cirrus.hibernate.impl','RelationalDatabaseSessionFactory')")
        val JNDIFactoryLookup = Ensemble("JNDIFactoryLookup", "class_with_members('cirrus.hibernate.impl','SessionFactoryObjectFactory')")
        val SessionManagement = Ensemble("SessionManagement", "derived", SessionInternalInterface, SessionApplicationInterface, ConcreteSession, SessionFactory, ConcreteSessionFactory, JNDIFactoryLookup)

        val ensembles = Set(
            Actions,
            UtilitiesAndExceptions,
            PersistenceManagement,
            SessionManagement
        )

        val ActionsToExceptions = GlobalOutgoingConstraint("all", Actions, Exceptions)
        val ActionsToSessionInternalInterface = GlobalOutgoingConstraint("all", Actions, SessionInternalInterface)
        val ScalarActionsToClassPersister = GlobalOutgoingConstraint("all", ScalarActions, ClassPersister)
        val CollectionActionsToCollectionPersister = GlobalOutgoingConstraint("all", CollectionActions, CollectionPersister)
        val CollectionActionsToPersistentCollectionWrappers = GlobalOutgoingConstraint("all", CollectionActions, PersistentCollectionWrappers)
        val ScalarActionsToActionInterface = IncomingOutgoingConstraint("implements", ScalarActions, ActionInterface)
        val CollectionActionsToActionInterface = IncomingOutgoingConstraint("implements", CollectionActions, ActionInterface)
        val ConcreteSessionToActions = GlobalIncomingConstraint("all", ConcreteSession, Actions)

        val constraints = Set(
            ActionsToExceptions,
            ActionsToSessionInternalInterface,
            ScalarActionsToClassPersister,
            CollectionActionsToCollectionPersister,
            CollectionActionsToPersistentCollectionWrappers,
            ScalarActionsToActionInterface,
            CollectionActionsToActionInterface,
            ConcreteSessionToActions
        )
        val global = GlobalArchitectureModel(ensembles)
        val model = ArchitectureModel(ensembles, constraints, "actions.sad")
        db.addGlobalModel(global)
        db.addModel(model)

        val queryResult: QueryResult[IViolationSummary] = db.violation_summary

        bc.addArchiveAsResource("hibernate-1.0.jar")

        queryResult.asList should be (Nil)
    }

}