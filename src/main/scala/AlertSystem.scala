import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.{col, split}

import javax.mail._
import javax.mail.internet._

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


    val props = System.getProperties
    props.setProperty("mail.smtp.host", "smtp.gmail.com")
    props.setProperty("mail.smtp.user", "user")
    props.setProperty("mail.smtp.host", "smtp.gmail.com")
    props.setProperty("mail.smtp.port", "587")
    props.setProperty("mail.debug", "true")
    props.setProperty("mail.smtp.auth", "true")
    props.setProperty("mail.smtp.starttls.enable", "true")
    props.setProperty("mail.smtp.EnableSSL.enable","true")

    val session = Session.getInstance(props)
    val message = new MimeMessage(session)

    message.setFrom(new InternetAddress("nsj0596@gmail.com"))
    message.setRecipients(Message.RecipientType.TO, "rajitsp@gmail.com")
    message.setSubject("Eureka")
    message.setText(msg)

    Transport.send(message)

    msg

  }
}
