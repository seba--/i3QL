package unisson

import hibernate_3_6.{bytecode_sad, action_sad}
import org.junit.Test
import org.junit.Assert._
import sae.bytecode.BytecodeDatabase
/**
 *
 * Author: Ralf Mitschke
 * Created: 30.05.11 14:18
 *
 */

class Hibernate_3_6_Test
{

    @Test
    def count_action_sad_ensemble_elements()
    {
        val db = new BytecodeDatabase

        val ensembles = new action_sad(db)

        import ensembles._

        db.addArchiveAsResource("hibernate-core-3.6.0.Final.jar")

        assertEquals(186, `org.hibernate.action`.size) // findall :- 188
        assertEquals(836, `org.hibernate.event`.size) // findall :- 839
        assertEquals(75, lock.size) // findall :- 839
        assertEquals(2315, HQL.size) // findall :- 2329
        assertEquals(1655, `org.hibernate.engine`.size) // findall :- 1688

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
    def count_bytecode_sad_ensemble_elements()
    {
        val db = new BytecodeDatabase

        val ensembles = new bytecode_sad(db)

        import ensembles._

        db.addArchiveAsResource("hibernate-core-3.6.0.Final.jar")


        assertEquals(410, `org.hibernate.bytecode`.size) // findall :- 416
        assertEquals(91, `org.hibernate.intercept`.size) // findall :- 92
        assertEquals(275, `org.hibernate.tool`.size) // findall :- 275
        assertEquals(649, `org.hibernate.tuple`.size) // findall :- 650

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
}

