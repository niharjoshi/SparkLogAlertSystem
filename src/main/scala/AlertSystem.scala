import org.apache.spark.sql.SparkSession

object AlertSystem {

  def main(args: Array[String]) = {
    val spark = SparkSession.builder.getOrCreate()

    val topicName = "logs"

    val df = spark.readStream.format("kafka").
      option("kafka.bootstrap.servers", "b-3.logfilegeneratorkafkac.c9jlb9.c7.kafka.us-east-2.amazonaws.com:9092,b-2.logfilegeneratorkafkac.c9jlb9.c7.kafka.us-east-2.amazonaws.com:9092,b-1.logfilegeneratorkafkac.c9jlb9.c7.kafka.us-east-2.amazonaws.com:9092").
      option("kafka.security.protocol", "PLAINTEXT").
      option("subscribe", topicName).load()

    df.printSchema()

    val messages = df.selectExpr("CAST(value AS STRING)")

    val query = messages.writeStream.
      option("path", "data.txt").
      option("checkpointLocation", "checkpoint.txt").
      format("text").start()

    query.awaitTermination()
    query.stop()

    println(query)
  }

}
