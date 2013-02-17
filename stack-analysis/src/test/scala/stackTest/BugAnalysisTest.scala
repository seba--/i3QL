package stackTest

import sae.{Relation, QueryResult}
import org.junit.{Assert, Test, BeforeClass}
import java.util.Date
import sae.bytecode.bat.BATDatabaseFactory
import sandbox.findbugs._
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
  var methodTestRefComparison: QueryResult[(Int,BugType.Value)] = null
  var methodTestSelfAssignment: QueryResult[(Int,BugType.Value)] = null
  var methodTestSelfComparison: QueryResult[(Int,BugType.Value)] = null
  var methodTestArrayToString: QueryResult[(Int,BugType.Value)] = null
  var methodTestBadSQLAccess: QueryResult[(Int,BugType.Value)] = null
  var methodTestReturnValue: QueryResult[(Int,BugType.Value)] = null
  var methodTestSynchronized: QueryResult[(Int,BugType.Value)] = null


  @BeforeClass
  def start() {
    //Setup the database
    val database = BATDatabaseFactory.create()
    val analysis : Relation[BugInfo] = BugAnalysis.byCodeInfo(database)

    methodTestRefComparison = compile(SELECT ((bi: BugInfo) => (bi.pc, bi.bugType)) FROM analysis WHERE ((_: BugInfo).declaringMethod.name equals "testRefComparison"))
    methodTestSelfAssignment = compile(SELECT((bi: BugInfo) => (bi.pc, bi.bugType)) FROM analysis WHERE ((_: BugInfo).declaringMethod.name equals "testSelfAssignment"))
    methodTestSelfComparison = compile(SELECT((bi: BugInfo) => (bi.pc, bi.bugType)) FROM analysis WHERE ((_: BugInfo).declaringMethod.name equals "testSelfComparison"))
    methodTestArrayToString = compile(SELECT((bi: BugInfo) => (bi.pc, bi.bugType)) FROM analysis WHERE ((_: BugInfo).declaringMethod.name equals "testArrayToString"))
    methodTestBadSQLAccess = compile(SELECT((bi: BugInfo) => (bi.pc, bi.bugType)) FROM analysis WHERE ((_: BugInfo).declaringMethod.name equals "testBadSQLAccess"))
    methodTestReturnValue = compile(SELECT((bi: BugInfo) => (bi.pc, bi.bugType)) FROM analysis WHERE ((_: BugInfo).declaringMethod.name equals "testReturnValue"))
    methodTestSynchronized = compile(SELECT((bi: BugInfo) => (bi.pc, bi.bugType)) FROM analysis WHERE ((_: BugInfo).declaringMethod.name equals "testSynchronized"))

    database.addClassFile(new FileInputStream("stack-analysis" + File.separator + "target" + File.separator + "test-classes" + File.separator + "TestMethods.class"))
    println("Finish analysis: " + new Date())


  }
}


class BugAnalysisTest {
  @Test
  def testRefComparison() {
    val bugList : List[(Int,BugType.Value)] = BugAnalysisTest.methodTestRefComparison.asList

    Assert.assertEquals(2, bugList.size)
    Assert.assertTrue(bugList.contains((32, BugType.ES_COMPARING_STRINGS_WITH_EQ)))
    Assert.assertTrue(bugList.contains((64, BugType.RC_REF_COMPARISON_BAD_PRACTICE_BOOLEAN)))
  }

  @Test
  def testSelfAssignment() {
    val bugList = BugAnalysisTest.methodTestSelfAssignment.asList

    Assert.assertEquals(2, bugList.size)
    Assert.assertTrue(bugList.contains((11, BugType.SA_LOCAL_SELF_ASSIGNMENT)))
    Assert.assertTrue(bugList.contains((23, BugType.SA_LOCAL_SELF_ASSIGNMENT)))
  }

  @Test
  def testSelfComparison() {
    val bugList = BugAnalysisTest.methodTestSelfComparison.asList

    Assert.assertEquals(5, bugList.size)
    Assert.assertTrue(bugList.contains((10, BugType.SA_FIELD_SELF_COMPARISON)))
    Assert.assertTrue(bugList.contains((10, BugType.ES_COMPARING_STRINGS_WITH_EQ)))
    Assert.assertTrue(bugList.contains((22, BugType.ES_COMPARING_PARAMETER_STRING_WITH_EQ)))
    Assert.assertTrue(bugList.contains((50, BugType.SA_FIELD_SELF_COMPARISON)))
    Assert.assertTrue(bugList.contains((72, BugType.SA_FIELD_SELF_COMPARISON)))
  }

  @Test
  def testArrayToString() {
    val bugList = BugAnalysisTest.methodTestArrayToString.asList

    Assert.assertEquals(6, bugList.size)
    Assert.assertTrue(bugList.contains((8, BugType.DMI_INVOKING_TOSTRING_ON_ARRAY)))
    Assert.assertTrue(bugList.contains((12, BugType.DMI_INVOKING_TOSTRING_ON_ARRAY)))
    Assert.assertTrue(bugList.contains((26, BugType.DMI_INVOKING_TOSTRING_ON_ARRAY)))
    Assert.assertTrue(bugList.contains((34, BugType.RV_RETURN_VALUE_IGNORED)))
    Assert.assertTrue(bugList.contains((54, BugType.DMI_INVOKING_TOSTRING_ON_ARRAY)))
    Assert.assertTrue(bugList.contains((62, BugType.RV_RETURN_VALUE_IGNORED)))
  }

  @Test
  def testBadSQLAccess() {
    val bugList = BugAnalysisTest.methodTestBadSQLAccess.asList

    Assert.assertEquals(3, bugList.size)
    Assert.assertTrue(bugList.contains((16, BugType.SQL_BAD_PREPARED_STATEMENT_ACCESS)))
    Assert.assertTrue(bugList.contains((26, BugType.SQL_BAD_PREPARED_STATEMENT_ACCESS)))
    Assert.assertTrue(bugList.contains((49, BugType.SQL_BAD_PREPARED_STATEMENT_ACCESS)))

  }

  @Test
  def testReturnValue() {
    val bugList = BugAnalysisTest.methodTestReturnValue.asList

    Assert.assertEquals(2, bugList.size)
    Assert.assertTrue(bugList.contains((11, BugType.RV_RETURN_VALUE_IGNORED)))
    Assert.assertTrue(bugList.contains((23, BugType.RV_RETURN_VALUE_IGNORED)))
  }

  @Test
  def testSynchronized() {
    val bugList = BugAnalysisTest.methodTestSynchronized.asList

    Assert.assertEquals(1, bugList.size)
    Assert.assertTrue(bugList.contains((5, BugType.DL_SYNCHRONIZATION_ON_BOXED_PRIMITIVE)))
  }


}