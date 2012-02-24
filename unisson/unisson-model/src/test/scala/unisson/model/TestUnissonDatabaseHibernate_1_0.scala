package unisson.model

import mock.vespucci._
import org.scalatest.matchers.ShouldMatchers
import org.junit.Test
import de.tud.cs.st.vespucci.model.IEnsemble
import unisson.query.code_model.SourceElement
import sae.bytecode.model.MethodDeclaration
import de.tud.cs.st.bat.constants.ACC_PUBLIC
import sae.bytecode.BytecodeDatabase
import de.tud.cs.st.vespucci.interfaces.IViolation
import de.tud.cs.st.bat.{ArrayType, IntegerType, VoidType, ObjectType}
import sae.collections.QueryResult

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


    import Ordering._
    import sae.collections.Conversions._

    @Test
    def testEnsembleElementsInDeprecatedLegacy() {

        val bc = new BytecodeDatabase()
        val db = new UnissonDatabase(bc)

        val elements: QueryResult[(IEnsemble, SourceElement[AnyRef])] = db.leaf_ensemble_elements

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
                            false,
                            false
                        ))
                        ),
                (deprecatedPersistenceLifecycleCallbacks,
                        SourceElement(MethodDeclaration(
                            ObjectType("cirrus/hibernate/PersistentLifecycle"),
                            "delete",
                            Seq(ObjectType("cirrus/hibernate/Session")),
                            VoidType(),
                            ACC_PUBLIC.mask,
                            false,
                            false
                        ))
                        ),
                (deprecatedPersistenceLifecycleCallbacks,
                        SourceElement(MethodDeclaration(
                            ObjectType("cirrus/hibernate/PersistentLifecycle"),
                            "load",
                            Seq(ObjectType("cirrus/hibernate/Session")),
                            VoidType(),
                            ACC_PUBLIC.mask,
                            false,
                            false
                        ))
                        ),
                (deprecatedPersistenceLifecycleCallbacks,
                        SourceElement(MethodDeclaration(
                            ObjectType("cirrus/hibernate/PersistentLifecycle"),
                            "store",
                            Seq(),
                            VoidType(),
                            ACC_PUBLIC.mask,
                            false,
                            false
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
                    false,
                    false
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
                    false,
                    false
                )),
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/PersistentLifecycle"),
                    "load",
                    Seq(ObjectType("cirrus/hibernate/Session")),
                    VoidType(),
                    0,
                    false,
                    false
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
                    false,
                    false
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
                    false,
                    false
                )),
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/PersistentLifecycle"),
                    "delete",
                    Seq(ObjectType("cirrus/hibernate/Session")),
                    VoidType(),
                    0,
                    false,
                    false
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
                    false,
                    false
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
                    false,
                    false
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
                    false,
                    false
                )),
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/PersistentLifecycle"),
                    "load",
                    Seq(ObjectType("cirrus/hibernate/Session")),
                    VoidType(),
                    0,
                    false,
                    false
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
                    false,
                    false
                )),
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/PersistentLifecycle"),
                    "load",
                    Seq(ObjectType("cirrus/hibernate/Session")),
                    VoidType(),
                    0,
                    false,
                    false
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
                    false,
                    false
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
                    false,
                    false
                )),
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/PersistentLifecycle"),
                    "store",
                    Seq(),
                    VoidType(),
                    0,
                    false,
                    false
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
                    false,
                    false
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
                    false,
                    false
                )),
                SourceElement(MethodDeclaration(
                    ObjectType("cirrus/hibernate/PersistentLifecycle"),
                    "create",
                    Seq(ObjectType("cirrus/hibernate/Session")),
                    VoidType(),
                    0,
                    false,
                    false
                )),
                "invoke_interface",
                "DeprecatedLegacy")
        )

        result should be(expectedResult)

    }
}