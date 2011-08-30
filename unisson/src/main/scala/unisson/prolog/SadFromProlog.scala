package unisson.prolog

import parser.UnissonPrologParser
import utils.ISOPrologStringConversion
import java.io._
import unisson.ast.{Ensemble, UnissonDefinition, UnissionQuery}

/**
 * 
 * Author: Ralf Mitschke
 * Created: 30.08.11 09:53
 *
 */

object SadFromProlog {

    private val ensembleFunctor = "ensemble"

    private val dependencyConstraintFunctors = List("incoming", "outgoing", "not_allowed", "inAndOut")

    def main (args: Array[String]) {

        args.foreach((s:String) => readSadFile(new FileInputStream(s)))

    }

    def readSadFile( stream : InputStream ) : Seq[UnissonDefinition] =
    {
        val in = new BufferedReader(new InputStreamReader(stream))
        while( in.ready )
        {

            val s = in.readLine();
            if( s.trim().length() > 0 && ! s.trim().startsWith("%") && ! s.trim().startsWith(":-") )
            {
                readPrologLine(s)
            }
        }

        Nil
    }

    def resourceAsStream(name : String) = {
        this.getClass.getClassLoader.getResourceAsStream(name)
    }


    def readPrologLine(s: String)
    {
        val functor = ISOPrologStringConversion.getFunctor(s)
        if( functor == ensembleFunctor )
        {
            readEnsemble(s)
        }
        else if( dependencyConstraintFunctors.contains(functor) )
        {
            val compounds = ISOPrologStringConversion.getArgs(s)
            readDependencyConstraint(functor, compounds)
        }

    }

    def readEnsemble(s : String) : Ensemble =
    {
        val parser = new UnissonPrologParser()
        val result = parser.parse(parser.ensemble, s)
        result.get

    }

    def readDependencyConstraint(functor:String, args: Array[String]) =
    {

    }


}