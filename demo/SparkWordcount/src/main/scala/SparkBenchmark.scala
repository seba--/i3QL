import org.apache.spark.{SparkContext, SparkConf}

/**
 * @author Mirko Köhler
 */
object SparkBenchmark {

	def main(args: Array[String]): Unit = {
		val conf = new SparkConf().setMaster("local[8]").setAppName("WordCount")
		val sc = new SparkContext(conf)

		var l : List[Long] = Nil
		for(i <- 1 to 1){
			System.gc()
			sc.clearCallSite()
			l = run(sc, 8) :: l
		}

		println(l.reverse)

	}

	def run(sc : SparkContext, partitions : Int): Long = {
		//Path to the ressource directory
		val dir = "C:/Users/Mirko/Documents/Projekte/git/i3QL/demo/wordcount/res/dta_kernkorpus_2014-03-10/*.xml"

		val lines = sc.textFile(dir, 1).coalesce(partitions)
		//I. All words
		val counts = lines.flatMap(line => line.replaceAll("\\p{Punct}"," ").split("\\s+"))
		//II. Unique words
		//val counts = lines.flatMap(line => line.replaceAll("\\p{Punct}"," ").split("\\s+")).distinct(8)
		//III. Occurences per word
		//val counts = lines.flatMap(line => line.replaceAll("\\p{Punct}"," ").split("\\s+")).map(word => (word, 1)).reduceByKey((x,y) => x + y)

		//Read (and cache) the textfiles
		lines.count()

		//Perform the measurement
		val before = System.currentTimeMillis()
		val c1 = counts.count()
		val after = System.currentTimeMillis()

		after - before
	}

}
