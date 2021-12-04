import org.apache.spark.sql.SparkSession
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.PublishRequest
import Email.Email
import com.amazonaws.regions.{Region, Regions}
import com.typesafe.config.ConfigFactory


object AlertSystem {

  // Driver function for the alert system
  def main(args: Array[String]) = {

    // Creating/getting Spark session (if available)
    val spark = SparkSession.builder.getOrCreate()

    // Setting logging level for Spark
    spark.sparkContext.setLogLevel("ERROR")

    // Importing implicits for dataframe manipulation
    import spark.implicits._

    // Getting Kafka broker to connect to
    val brokerUrl = ConfigFactory.load().getString("kafka.broker")

    // Getting Kafka topic name to consume logs from
    val topicName = ConfigFactory.load().getString("kafka.topic")

    // Getting Kafka protocol for logs
    val protocol = ConfigFactory.load().getString("kafka.protocol")

    // Starting Kafka consumer and reading logs in plaintext from the topic on the broker
    val df = spark.readStream.format("kafka").
      option("kafka.bootstrap.servers", brokerUrl).
      option("subscribe", topicName).
      option("kafka.security.protocol", protocol).
      load().
      selectExpr("CAST(value AS STRING)")

    // Calling the email notification function on log batches
    val mail = df.as[String].flatMap(i => {
      sendEmail(i)
      Array[String](i)
    })

    // Writing consumed logs to console
    mail.writeStream.outputMode("append").format("console").start.awaitTermination()

  }

  // Function to send email notification
  def sendEmail(m: String) = {

    // Getting email text body
    val msg = Email.createEmail(m)

    // Getting SNS topic ARN
    val topicArn = ConfigFactory.load().getString("sns.arn")

    // Initializing SNS client
    val snsClient = new AmazonSNSClient()
    snsClient.setRegion(Region.getRegion(Regions.US_EAST_2))

    // Publishing our email to SNS topic
    val publishRequest = new PublishRequest(topicArn, msg)
    snsClient.publish(publishRequest)

  }
}
