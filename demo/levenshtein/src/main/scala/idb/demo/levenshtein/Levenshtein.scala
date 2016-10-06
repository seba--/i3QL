/* License (BSD Style License):
 *  Copyright (c) 2009, 2011
 *  Software Technology Group
 *  Department of Computer Science
 *  Technische Universität Darmstadt
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  - Neither the name of the Software Technology Group or Technische
 *    Universität Darmstadt nor the names of its contributors may be used to
 *    endorse or promote products derived from this software without specific
 *    prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 *  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 *  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 *  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 *  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY OF SUCH DAMAGE.
 */
package idb.demo.levenshtein

import idb.algebra.print.RelationalAlgebraPrintPlan
import java.io.{BufferedReader, FileReader}
import idb.demo.levenshtein.data.{Categories, Terminal}
import idb.observer.Observer


/**
 *
 * @author Ralf Mitschke
 */
object Levenshtein  {


    def main(args: Array[String]) {
        if (args.length == 0)
            return

        val dictionaryFileName = args(0)

        println("reading file:  " + dictionaryFileName)

        val reader = new BufferedReader(new FileReader(dictionaryFileName))

        import Queries._

        characters.addObserver(
            new Observer[(Terminal, (String, Int))] {
                def updated(oldV: (Terminal, (String, Int)), newV: (Terminal, (String, Int))): Unit= {}


                def endTransaction(): Unit = {}

                def removed(v: (Terminal, (String, Int))): Unit = {}

                def added(v: (Terminal, (String, Int))): Unit = {println(v)}
            }
        )


        while(reader.ready()) {
            terminals += WordnetPrologReader.readLine(reader.readLine())
        }

        categoryCount.foreach(println)

    }
}
