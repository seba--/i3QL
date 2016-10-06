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

import idb.demo.levenshtein.data.{Categories, Terminal}

/**
 *
 * @author Ralf Mitschke
 */
object WordnetPrologReader {


    /**
     * Reads lines of words in the wordnet prolog format and returns a terminal:
     * s(synset_id,w_num,’word’,ss_type,sense_number,tag_count).
     *
     * (cf. http://wordnetcode.princeton.edu/3.0/WNprolog-3.0.tar.gz)
     */
    def readLine(line: String): Terminal = {
        val entries = line.split(",")
        assert(entries.size >= 4)
        val word = entries(2).substring(1, entries(2).size - 1)
        val sort =
            if (entries.size == 6)
                entries(3)
            else if (entries.size == 4)
                entries(3).substring(0, 1)
            else
                throw new IllegalStateException("Illegal number of arguments to s(...) -- " + entries)
        sort match {
            case "n" => Terminal(word, Categories.Noun)
            case "v" => Terminal(word, Categories.Verb)
            case "a" => Terminal(word, Categories.Adjective)
            case "s" => Terminal(word, Categories.Adjective)
            case "r" => Terminal(word, Categories.Adverb)
            case _ => throw new IllegalStateException("Unknown word type: '" + sort + "'")
        }
    }


}
