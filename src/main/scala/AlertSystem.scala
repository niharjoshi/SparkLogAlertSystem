import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.{col, split}

import courier._, Defaults._
import scala.util._
import mail._

import com.spark.mail.Email

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
//      select(
//        split(col("value"), " ").getItem(0).as("Timestamp"),
//        split(col("value"), " ").getItem(2).as("Level"),
//        split(col("value"), " ").getItem(3).as("Source"),
//        split(col("value"), " ").getItem(5).as("Message")
//      )

    val mail = df.as[String].flatMap(i => {
      sendEmail(i)
      Array[String](i)
    })

    mail.writeStream.outputMode("append").format("console").start.awaitTermination(20000)

    println("Terminated")
  }

  def sendEmail(m: String): String = {

    val log_data = m.split(" ").toList

    val msg =
      s"""<!DOCTYPE html>
         |<html>
         |   <head>
         |      <style>
         |         table {
         |            border: 1px solid black;
         |         }
         |         th {
         |          border: 1px solid black;
         |          background-color: #FFA;
         |          }
         |         td {
         |          border: 1px solid black;
         |          background-color: #FFF;
         |          }
         |      </style>
         |   </head>
         |
         |   <body>
         |      <h1>Report</h1>
         |      <table>
         |         <tr> Message </tr> $m
         |      </table>
         |   </body>
         |</html>""".stripMargin

//    send a Mail (
//      from = ("nsj0596@gmail.com", "John Smith"),
//      to = "rajitsp@gmail.com",
//      cc = "valluruindra@gmail.com",
//      subject = "Eureka",
//      message = msg
//    )



    //    val mailer = Mailer("smtp.gmail.com", 587)
//      .auth(true)
//      .as("nsj0596@gmail.com", "vtn0cvd@bzy6KTX*vwp")
//      .startTls(true)()
//    mailer(Envelope.from("nsj0596@gmail.com")
//      .to("mom" `@` "gmail.com")
//      .cc("dad" `@` "gmail.com")
//      .subject("miss you")
//      .content(Text("hi mom"))).onComplete {
//      case Success(_) => println("message delivered")
//      case Failure(_) => println("delivery failed")
//    }

    val mail = new Email("/home/hadoop/SparkLogAlertSystem/src/main/resource/application.conf")

    mail.sendMail(msg, "appID", "", "F")

    msg

  }
}
