package sandbox

import findbugs.StackBugAnalysis
import sae.bytecode.bat.BATDatabaseFactory
import java.io.FileInputStream
import stackAnalysis.StackAnalysis


/**
 * Main class
 *
 * Created with IntelliJ IDEA.
 * User: mirko
 * Date: 22.10.12
 * Time: 15:59
 * To change this template use File | Settings | File Templates.
 */
object Main {

  def main(args: Array[String]) {


    val database = BATDatabaseFactory.create()

    StackAnalysis.printResults = true
    StackBugAnalysis.printResults = true
    StackBugAnalysis(database)


    //  def getStream = this.getClass.getClassLoader.getResourceAsStream ("jdk1.7.0-win-64-rt.jar")
    //  database.addArchive(new FileInputStream("test-data\\src\\main\\resources\\jdk1.7.0-win-64-rt.jar"))

    database.addClassFile(new FileInputStream("stack-analysis\\target\\test-classes\\TestMethods.class"))
  }


}
