package idb.demo.chartparser

import kilbury.Parser

/**
 * @author Mirko Köhler
 */
object SentenceParser extends Parser {
	def topLevelCategory = "S"

	productions +=("S", Seq ("NP", "VP"))
	productions +=("NP", Seq ("Noun"))
	productions +=("NP", Seq ("AdjP", "Noun"))
	productions +=("VP", Seq ("Verb"))
	productions +=("VP", Seq ("Verb", "Adv"))
	productions +=("AdjP", Seq ("Adj"))
	productions +=("AdjP", Seq ("Adj", "AdjP"))


	terminals +=("green", "Noun")
	terminals +=("ideas", "Noun")
	terminals +=("sleep", "Noun")
	terminals +=("green", "Verb")
	terminals +=("sleep", "Verb")
	terminals +=("colorless", "Adj")
	terminals +=("green", "Adj")
	terminals +=("furiously", "Adv")




}
