import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, split}

object AlertSystem {

  def main(args: Array[String]) = {

    val spark = SparkSession.builder.getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    import spark.implicits._

    val topicName = "logs"

    val df = spark.readStream.format("kafka").
      option("kafka.bootstrap.servers", "b-3.logfilegeneratorkafkac.c9jlb9.c7.kafka.us-east-2.amazonaws.com:9092,b-2.logfilegeneratorkafkac.c9jlb9.c7.kafka.us-east-2.amazonaws.com:9092,b-1.logfilegeneratorkafkac.c9jlb9.c7.kafka.us-east-2.amazonaws.com:9092").
      option("subscribe", topicName).
      option("kafka.security.protocol", "PLAINTEXT").
      load()

    df.printSchema()

    val messages = df.selectExpr("CAST(value AS STRING)")

    val modified_messages = messages.select(
      split(col("value"), " ").getItem(0).as("Timestamp"),
      split(col("value"), " ").getItem(2).as("Level"),
      split(col("value"), " ").getItem(3).as("Source"),
      split(col("value"), " ").getItem(5).as("Message")
    )

    modified_messages.writeStream.outputMode("append").format("console").start.awaitTermination()

  }
}
