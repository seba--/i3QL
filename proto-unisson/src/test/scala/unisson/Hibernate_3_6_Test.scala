package unisson

import hibernate_3_6.{hibernate_3_6_ensemble_definitions, cache_sad, bytecode_sad, action_sad}
import org.junit.{Test, Ignore}
import org.junit.Assert._
import sae.bytecode.BytecodeDatabase
import sae.collections.QueryResult
/**
 *
 * Author: Ralf Mitschke
 * Created: 30.05.11 14:18
 *
 */

class Hibernate_3_6_Test
{
    @Test
    def test_class_with_members()
    {
        val database = new BytecodeDatabase
        val queries = new Queries(database)
        import queries._
        import sae.syntax.RelationalAlgebraSyntax._

        val SessionFactory : QueryResult[SourceElement[AnyRef]] = class_with_members("org.hibernate", "SessionFactory")

        val SessionFactoryImpl : QueryResult[SourceElement[AnyRef]] = class_with_members("org.hibernate.impl", "SessionFactoryImpl")

        val AbstractSessionImpl : QueryResult[SourceElement[AnyRef]] = class_with_members("org.hibernate.impl", "AbstractSessionImpl")

        val SessionFactoryStub : QueryResult[SourceElement[AnyRef]] = class_with_members("org.hibernate.jmx", "SessionFactoryStub")

        val SessionImpl : QueryResult[SourceElement[AnyRef]] = class_with_members("org.hibernate.impl", "SessionImpl")

        val Session : QueryResult[SourceElement[AnyRef]] = class_with_members("org.hibernate", "Session")

        val ClassicSession : QueryResult[SourceElement[AnyRef]] = class_with_members("org.hibernate.classic", "Session")

        val StatelessSessionImpl : QueryResult[SourceElement[AnyRef]] = class_with_members("org.hibernate.impl", "StatelessSessionImpl")

        val SessionImplementor : QueryResult[SourceElement[AnyRef]] = class_with_members("org.hibernate.engine", "SessionImplementor")

        val SessionFactoryImplementor : QueryResult[SourceElement[AnyRef]] = class_with_members("org.hibernate.engine", "SessionFactoryImplementor")

        val SessionFactoryObserver : QueryResult[SourceElement[AnyRef]] = class_with_members("org.hibernate", "SessionFactoryObserver")

        val StatelessSession : QueryResult[SourceElement[AnyRef]] = class_with_members("org.hibernate", "StatelessSession")


        database.addArchiveAsResource("hibernate-core-3.6.0.Final.jar")

        /*
        val impl = SessionFactoryImpl.asList.sortBy{
            case SourceElement( ObjectType(name) ) => "1" + name
            case SourceElement( Field(ObjectType(cls), name, _) ) => "2" + cls + name
            case SourceElement( Method(ObjectType(cls), name, _, _) ) => "3" + cls + name
        }
        impl.foreach(println)
        */
        assertEquals(29, SessionFactory.size)
        assertEquals(156, SessionFactoryImpl.size)
        assertEquals(17, AbstractSessionImpl.size)
        assertEquals(40, SessionFactoryStub.size)
        assertEquals(257, SessionImpl.size)
        assertEquals(92, Session.size)
        assertEquals(23, ClassicSession.size)
        assertEquals(92, StatelessSessionImpl.size)
        assertEquals(57, SessionImplementor.size)
        assertEquals(32, SessionFactoryImplementor.size)
        assertEquals(3, SessionFactoryObserver.size)
        assertEquals(26, StatelessSession.size)


    }

    @Test
    def count_all_ensemble_elements()
    {
        val database = new BytecodeDatabase

        val ensembles = new hibernate_3_6_ensemble_definitions(database){
            // access each lazy val once to initialize queries
            `org.hibernate.action`
            `org.hibernate.bytecode`
            `org.hibernate.cache`
            `org.hibernate.engine`
            `org.hibernate.event`
            `org.hibernate.intercept`
            `org.hibernate.loader`
            `org.hibernate.persister`
            `org.hibernate.stat`
            `org.hibernate.tool`
            `org.hibernate.tuple`
            GlobalSettings
            HQL
            lock
            Metamodel_Configurator
            Session
        }

        import ensembles._

        database.addArchiveAsResource("hibernate-core-3.6.0.Final.jar")

        assertEquals(186, `org.hibernate.action`.size) // findall :- 188
        assertEquals(410, `org.hibernate.bytecode`.size) // findall :- 416
        assertEquals(554, `org.hibernate.cache`.size) // findall :- 554
        assertEquals(1655, `org.hibernate.engine`.size) // findall :- 1688
        assertEquals(836, `org.hibernate.event`.size) // findall :- 839
        assertEquals(91, `org.hibernate.intercept`.size) // findall :- 92
        assertEquals(879, `org.hibernate.loader`.size) // findall :- 881
        assertEquals(1283, `org.hibernate.persister`.size) // findall :- 1289
        assertEquals(473, `org.hibernate.stat`.size) // findall :- 473
        assertEquals(275, `org.hibernate.tool`.size) // findall :- 275
        assertEquals(649, `org.hibernate.tuple`.size) // findall :- 650


        assertEquals(658, GlobalSettings.size) // findall :- 665
        assertEquals(75, lock.size) // findall :- 839
        assertEquals(2315, HQL.size) // findall :- 2329
        assertEquals(1864, Metamodel_Configurator.size) // findall :- 1908
        assertEquals(824, Session.size) // findall :- 834

    }

    @Test
    def find_action_sad_violation_elements()
    {
        val db = new BytecodeDatabase

        val ensembles = new action_sad(db)

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


    @Test
    def find_bytecode_sad_violation_elements()
    {
        val db = new BytecodeDatabase

        val ensembles = new bytecode_sad(db)

        import ensembles._

        db.addArchiveAsResource("hibernate-core-3.6.0.Final.jar")

        incoming_invoke_interface_to_bytecode_violation.foreach(println)
        assertEquals(0, incoming_invoke_interface_to_bytecode_violation.size)

    }

    @Test
    def find_cache_sad_violation_elements()
    {
        val db = new BytecodeDatabase

        val ensembles = new cache_sad(db)

        import ensembles._

        db.addArchiveAsResource("hibernate-core-3.6.0.Final.jar")

        incoming_invoke_interface_to_cache_violation.foreach(println)

        assertEquals(0, incoming_invoke_interface_to_cache_violation.size)

    }

}

