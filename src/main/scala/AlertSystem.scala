import org.apache.spark.sql.{DataFrame, SparkSession}
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
      load().
      selectExpr("CAST(value AS STRING)")
//      select(
//        split(col("value"), " ").getItem(0).as("Timestamp"),
//        split(col("value"), " ").getItem(2).as("Level"),
//        split(col("value"), " ").getItem(3).as("Source"),
//        split(col("value"), " ").getItem(5).as("Message")
//      )

    val mail = df.as[String].flatMap(i => {
      val msg = createHtmlEmailBody(i)
      println(msg)
      Array[String](i)
    })

    mail.writeStream.outputMode("append").format("console").start.awaitTermination(20000)

    println("Terminated")
  }

  def createHtmlEmailBody(df: String): String = {
//    val columnNames = df.columns.map(x => "<th>" + x.trim + "</th>").mkString
//    val data = df.collect.mkString
//    val data1 = data.split(",").map(x => "<td>".concat(x).concat("</td>"))
//    val data2 = data1.mkString.replaceAll("<td>\\[", "<tr><td>")
//    val data3 = data2.mkString.replaceAll("]\\[", "</td></tr><td>").replaceAll("]", "")

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
         |         <tr> Message </tr> $df
         |      </table>
         |   </body>
         |</html>""".stripMargin

    return msg
  }
}
