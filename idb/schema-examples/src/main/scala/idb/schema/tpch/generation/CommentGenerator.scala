package idb.schema.tpch.generation

import scala.util.Random

/**
  * This class generates comments according to the TPC-H specification.
  *
  * @author mirko
  */
object CommentGenerator {

	private val random = new Random()

	private val NOUNS = List("foxes", "ideas", "theodolites", "pinto beans",
		"instructions", "dependencies", "excuses", "platelets",
		"asymptotes", "courts", "dolphins", "multipliers",
		"sauternes", "warthogs", "frets dinos",
		"attainments", "somas", "Tiresias'", "patterns",
		"forges", "braids", "hockey players", "frays",
		"warhorses", "dugouts", "notornis", "epitaphs",
		"pearls", "tithes", "waters", "orbits",
		"gifts", "sheaves", "depth", "sentiments",
		"decoys", "realms", "pains", "grouches", "escapades")
	private val TERMINATORS = List(".", ";", ":", "?", "!", "--")


	//Only the first line of the tables are implemented here.
	private val VERBS = List("sleep", "wake", "are", "cajole")
	private val ADJECTIVES = List("furious", "sly", "careful", "blithe")
	private val ADVERBS = List("sometimes", "always", "never", "furiously")
	private val PREPOSITIONS = List("about", "above", "according to", "across")
	private val AUXILIARIES = List("do", "may", "might", "shall")

	def generateText(min : Int, max : Int) : String = {
		var result = sentence

		while (true) {
			val s = sentence
			random.nextInt(3) match {
				case 0 if result.length >= min && result.length <= max =>
					return result
				case 0 if result.length > max =>
					result = s
				case _ =>
					if (result.length + s.length + 1 <= max)
						result = result + " " + s
			}
		}

		return ""
	}

	private def sentence : String = random.nextInt(5) match {
		case 0 => nounPhrase + " " + verbPhrase + terminator
		case 1 => nounPhrase + " " + verbPhrase + " " + prepositionalPhrase + terminator
		case 2 => nounPhrase + " " + verbPhrase + " " + nounPhrase + terminator
		case 3 => nounPhrase + " " + prepositionalPhrase + " " + verbPhrase + " " +
			nounPhrase + terminator
		case 4 => nounPhrase + " " + prepositionalPhrase + " " + verbPhrase + " " +
			prepositionalPhrase + terminator
	}

	private def nounPhrase : String = random.nextInt(4) match {
		case 0 => noun
		case 1 => adjective + " " + noun
		case 2 => adjective + ", " + adjective + " " + noun
		case 3 => adverb + " " + adjective + " " + noun
	}

	private def verbPhrase : String = random.nextInt(4) match {
		case 0 => verb
		case 1 => auxiliary + " " + verb
		case 2 => verb + " " + adverb
		case 3 => auxiliary + " " + verb + " " + adverb
	}

	private def prepositionalPhrase : String =
		preposition + " the " + nounPhrase

	private def noun : String =
		randomFromList(NOUNS)

	private def verb : String =
		randomFromList(VERBS)

	private def adjective : String =
		randomFromList(ADJECTIVES)

	private def adverb : String =
		randomFromList(ADVERBS)

	private def preposition : String =
		randomFromList(PREPOSITIONS)

	private def terminator : String =
		randomFromList(TERMINATORS)

	private def auxiliary : String =
		randomFromList(AUXILIARIES)

	private def randomFromList[A](list : List[A]) : A = {
		list(random.nextInt(list.length))
	}

}
