import org.scalatest.flatspec.AnyFlatSpec
import com.typesafe.config.ConfigFactory

class AlertSystemTest extends AnyFlatSpec {

  // Checking for app name in config
  "config" should "contain app name" in {

    // Getting name
    val name = ConfigFactory.load().getString("spark.appName")

    assert(name == "SparkLogAlterSystem")

  }

  // Checking for kafka broker in config
  "config" should "contain kafka broker" in {

    // Getting broker
    val broker = ConfigFactory.load().getString("kafka.broker")

    assert(broker == "b-3.logfilegeneratorkafkac.c9jlb9.c7.kafka.us-east-2.amazonaws.com:9092,b-2.logfilegeneratorkafkac.c9jlb9.c7.kafka.us-east-2.amazonaws.com:9092,b-1.logfilegeneratorkafkac.c9jlb9.c7.kafka.us-east-2.amazonaws.com:9092")

  }

  // Checking for kafka topic in config
  "config" should "contain kafka topic" in {

    // Getting topic
    val topic = ConfigFactory.load().getString("kafka.topic")

    assert(topic == "logs")

  }

  // Checking for kafka protocol in config
  "config" should "contain kafka protocol" in {

    // Getting protocol
    val protocol = ConfigFactory.load().getString("kafka.protocol")

    assert(protocol == "PLAINTEXT")

  }

  // Checking for sns arn in config
  "config" should "contain sns arn" in {

    // Getting arn
    val arn = ConfigFactory.load().getString("sns.arn")

    assert(arn == "arn:aws:sns:us-east-2:824124316412:logs")

  }

}
