package idb.iql.lms.extensions

import org.junit.Assert._
import scala.virtualization.lms.common.Base

/**
 *
 * @author Ralf Mitschke
 */
trait LMSTestUtils extends Base
{

    def assertSameReified[A: Manifest, B: Manifest] (f1: Rep[A => B], f2: Rep[A => B]) {
        assertSame (f1, f2)
    }


}
