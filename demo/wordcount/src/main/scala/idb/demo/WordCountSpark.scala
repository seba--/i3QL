package idb.demo

import java.io.{BufferedWriter, FileWriter}

import org.apache.spark
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

/**
	# Package a jar containing your application
	$ sbt package
	...
	[info] Packaging {..}/{..}/target/scala-2.10/simple-project_2.10-1.0.jar

	# Use spark-submit to run your application
	$ YOUR_SPARK_HOME/bin/spark-submit \
	  --class "SimpleApp" \
	  --master local[4] \
	  target/scala-2.10/simple-project_2.10-1.0.jar


 * @author Mirko Köhler
 */
object WordCountSpark {

//	def main(args: Array[String]) {
//
//		val conf = new SparkConf().setAppName("WordCountSpark")
//		val sc = new SparkContext(conf)
//
//
//val dir = "C:/Users/Mirko/Documents/Projekte/git/i3QL/demo/wordcount/res/dta_kernkorpus_2014-03-10/*.xml" // Should be some file on your system
//val file = sc.textFile(dir, 1)
//val counts = file.flatMap(line => line.replaceAll("\\p{Punct}"," ").split("\\s+")).map(word => (word, 1)).reduceByKey((x,y) => x + y)
//val before = System.currentTimeMillis()
//counts.count()
//val after = System.currentTimeMillis()
//println(s"Before: $before After $after total ${after - before}")
//
//		counts.saveAsTextFile("C:/Users/Mirko/Documents/Projekte/git/i3QL/demo/wordcount/sparkdata")
//
//		/*val fw = new BufferedWriter(new FileWriter("C:/Users/Mirko/Documents/Projekte/git/i3QL/demo/wordcount/sparkdata_2.txt"))
//		fw.write("Hello World!")
//		fw.close()    */
//
//	}
//
//def run(): Unit = {
//val dir = "C:/Users/Mirko/Documents/Projekte/git/i3QL/demo/wordcount/res/dta_kernkorpus_2014-03-10/*.xml" // Should be some file on your system
//val file = sc.textFile(dir, 1)
//val counts = file.flatMap(line => line.replaceAll("\\p{Punct}"," ").split("\\s+")).map(word => (word, 1)).reduceByKey((x,y) => x + y)
//val before = System.currentTimeMillis()
//counts.count()
//val after = System.currentTimeMillis()
//println(s"Before: $before After $after total ${after - before}")
//}

}



