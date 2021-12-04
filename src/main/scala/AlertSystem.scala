import org.apache.spark.sql.SparkSession
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.PublishRequest
import Email.Email
import com.amazonaws.regions.{Region, Regions}

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
      load().
      selectExpr("CAST(value AS STRING)")

    val mail = df.as[String].flatMap(i => {
      sendEmail(i)
      Array[String](i)
    })

    mail.writeStream.outputMode("append").format("console").start.awaitTermination()

    println("Terminated")
  }

  def sendEmail(m: String): String = {

    val msg = Email.createEmail(m)

    val topicArn = "arn:aws:sns:us-east-2:824124316412:logs"

    val snsClient = new AmazonSNSClient()
    snsClient.setRegion(Region.getRegion(Regions.US_EAST_2))

    val publishRequest = new PublishRequest(topicArn, msg)
    snsClient.publish(publishRequest)

    msg

  }
}
