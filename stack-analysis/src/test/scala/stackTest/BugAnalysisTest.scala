package stackTest

import sae.QueryResult
import org.junit.{Assert, Test, BeforeClass}
import java.util.Date
import sae.bytecode.bat.BATDatabaseFactory
import sandbox.findbugs.{IIStackBugAnalysis, BugType, BugEntry, CIStackBugAnalysis}
import sae.syntax.sql._
import java.io.{File, FileInputStream}

/**
 * Created with IntelliJ IDEA.
 * User: Mirko
 * Date: 16.12.12
 * Time: 17:55
 * To change this template use File | Settings | File Templates.
 */
object BugAnalysisTest extends org.scalatest.junit.JUnitSuite {
  var methodTestRefComparison: QueryResult[BugEntry] = null
  var methodTestSelfAssignment: QueryResult[BugEntry] = null
  var methodTestSelfComparison: QueryResult[BugEntry] = null
  var methodTestArrayToString: QueryResult[BugEntry] = null
  var methodTestBadSQLAccess: QueryResult[BugEntry] = null
  var methodTestReturnValue: QueryResult[BugEntry] = null
  var methodTestSynchronized: QueryResult[BugEntry] = null


  @BeforeClass
  def start() {
    //Setup the database
    val database = BATDatabaseFactory.create()
    val analysis = CIStackBugAnalysis(database)
    CIStackBugAnalysis.printResults = true

    methodTestRefComparison = compile(SELECT(*) FROM analysis WHERE ((_: BugEntry).declaringMethod.name equals "testRefComparison"))
    methodTestSelfAssignment = compile(SELECT(*) FROM analysis WHERE ((_: BugEntry).declaringMethod.name equals "testSelfAssignment"))
    methodTestSelfComparison = compile(SELECT(*) FROM analysis WHERE ((_: BugEntry).declaringMethod.name equals "testSelfComparison"))
    methodTestArrayToString = compile(SELECT(*) FROM analysis WHERE ((_: BugEntry).declaringMethod.name equals "testArrayToString"))
    methodTestBadSQLAccess = compile(SELECT(*) FROM analysis WHERE ((_: BugEntry).declaringMethod.name equals "testBadSQLAccess"))
    methodTestReturnValue = compile(SELECT(*) FROM analysis WHERE ((_: BugEntry).declaringMethod.name equals "testReturnValue"))
    methodTestSynchronized = compile(SELECT(*) FROM analysis WHERE ((_: BugEntry).declaringMethod.name equals "testSynchronized"))

    database.addClassFile(new FileInputStream("stack-analysis" + File.separator + "target" + File.separator + "test-classes" + File.separator + "TestMethods.class"))
    println("Finish analysis: " + new Date())


  }
}


class BugAnalysisTest {
  @Test
  def testRefComparison() {
    val logList = BugAnalysisTest.methodTestRefComparison.asList(0).log.getLog

    Assert.assertEquals(2, logList.size)
    Assert.assertTrue(logList.contains((32, BugType.ES_COMPARING_STRINGS_WITH_EQ)))
    Assert.assertTrue(logList.contains((64, BugType.RC_REF_COMPARISON_BAD_PRACTICE_BOOLEAN)))
  }

  @Test
  def testSelfAssignment() {
    val logList = BugAnalysisTest.methodTestSelfAssignment.asList(0).log.getLog

    Assert.assertEquals(2, logList.size)
    Assert.assertTrue(logList.contains((11, BugType.SA_LOCAL_SELF_ASSIGNMENT)))
    Assert.assertTrue(logList.contains((23, BugType.SA_LOCAL_SELF_ASSIGNMENT)))
  }

  @Test
  def testSelfComparison() {
    val logList = BugAnalysisTest.methodTestSelfComparison.asList(0).log.getLog

    Assert.assertEquals(5, logList.size)
    Assert.assertTrue(logList.contains((10, BugType.SA_FIELD_SELF_COMPARISON)))
    Assert.assertTrue(logList.contains((10, BugType.ES_COMPARING_STRINGS_WITH_EQ)))
    Assert.assertTrue(logList.contains((22, BugType.ES_COMPARING_PARAMETER_STRING_WITH_EQ)))
    Assert.assertTrue(logList.contains((50, BugType.SA_FIELD_SELF_COMPARISON)))
    Assert.assertTrue(logList.contains((72, BugType.SA_FIELD_SELF_COMPARISON)))
  }

  @Test
  def testArrayToString() {
    val logList = BugAnalysisTest.methodTestArrayToString.asList(0).log.getLog

    Assert.assertEquals(6, logList.size)
    Assert.assertTrue(logList.contains((8, BugType.DMI_INVOKING_TOSTRING_ON_ARRAY)))
    Assert.assertTrue(logList.contains((12, BugType.DMI_INVOKING_TOSTRING_ON_ARRAY)))
    Assert.assertTrue(logList.contains((26, BugType.DMI_INVOKING_TOSTRING_ON_ARRAY)))
    Assert.assertTrue(logList.contains((34, BugType.RV_RETURN_VALUE_IGNORED)))
    Assert.assertTrue(logList.contains((54, BugType.DMI_INVOKING_TOSTRING_ON_ARRAY)))
    Assert.assertTrue(logList.contains((62, BugType.RV_RETURN_VALUE_IGNORED)))
  }

  @Test
  def testBadSQLAccess() {
    val logList = BugAnalysisTest.methodTestBadSQLAccess.asList(0).log.getLog

    Assert.assertEquals(3, logList.size)
    Assert.assertTrue(logList.contains((16, BugType.SQL_BAD_PREPARED_STATEMENT_ACCESS)))
    Assert.assertTrue(logList.contains((26, BugType.SQL_BAD_PREPARED_STATEMENT_ACCESS)))
    Assert.assertTrue(logList.contains((49, BugType.SQL_BAD_PREPARED_STATEMENT_ACCESS)))

  }

  @Test
  def testReturnValue() {
    val logList = BugAnalysisTest.methodTestReturnValue.asList(0).log.getLog

    Assert.assertEquals(2, logList.size)
    Assert.assertTrue(logList.contains((11, BugType.RV_RETURN_VALUE_IGNORED)))
    Assert.assertTrue(logList.contains((23, BugType.RV_RETURN_VALUE_IGNORED)))
  }

  @Test
  def testSynchronized() {
    val logList = BugAnalysisTest.methodTestSynchronized.asList(0).log.getLog

    Assert.assertEquals(1, logList.size)
    Assert.assertTrue(logList.contains((5, BugType.DL_SYNCHRONIZATION_ON_BOXED_PRIMITIVE)))
  }


}