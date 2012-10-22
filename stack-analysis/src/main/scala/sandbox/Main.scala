package sandbox

import sae.bytecode.bat.BATDatabaseFactory
import java.io.FileInputStream
import sae.bytecode.instructions.InstructionInfo
import sae.{Relation, QueryResult}
import sae.syntax.sql._
import sae.bytecode.structure.CodeAttribute

/**
 * Created with IntelliJ IDEA.
 * User: mirko
 * Date: 22.10.12
 * Time: 15:59
 * To change this template use File | Settings | File Templates.
 */
object Main {

  def main(args: Array[String]) {
      val database = BATDatabaseFactory.create()

    val instr :QueryResult[InstructionInfo] = database.instructions

    val query : Relation[(Int, Array[Int])] = compile(SELECT ((codeAttribute:CodeAttribute) => (1, Array.ofDim[Int](codeAttribute.max_locals)) ) FROM database.codeAttributes)

    val query2 : QueryResult[(Int, Array[Int])] = compile(SELECT (* ) FROM query)

      database.addClassFile(new FileInputStream("/home/mirko/Dokumente/sae/test-data/target/classes/sae/test/code/innerclass/MyRootClass.class"))

      //database.addArchive(new FileInputStream("/home/mirko/Dokumente/sae/test-data/src/main/resources/demo.jar"))


      val instrAfter :QueryResult[InstructionInfo] = database.instructions // DONT DO THIS

      println(instr.size)
      println(instrAfter.size)




      query.foreach(
        println
      )


  }

}
