
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._

/**
 * @author Mirko Köhler
 */
object SparkStreamingBenchmark {



	def main(args: Array[String]): Unit = {
		val conf = new SparkConf().setMaster("local[8]").setAppName("WordCount")
		val ssc = new StreamingContext(conf, Seconds(1))

		val lines = ssc.socketTextStream("localhost",9999)

		//I. All words
		//val counts = lines.flatMap(line => line.replaceAll("\\p{Punct}"," ").split("\\s+"))
		//II. Unique words
		//val counts = lines.flatMap(line => line.replaceAll("\\p{Punct}"," ").split("\\s+")).distinct(8)
		//III. Occurences per word
		val counts = lines.flatMap(line => line.replaceAll("\\p{Punct}"," ").split("\\s+")).map(word => (word, 1)).reduceByKey((x,y) => x + y)

		val wordCounts = counts.count()

		wordCounts.print()

		ssc.start()             // Start the computation
		ssc.awaitTermination()  // Wait for the computation to terminate
	}

}
