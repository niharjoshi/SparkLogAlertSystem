name := "SparkLogAlertSystem"

version := "0.1"

scalaVersion := "2.13.7"

val sparkVersion = "2.4.8"

libraryDependencies += "org.apache.spark" % "spark-core_2.11" % sparkVersion

libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % sparkVersion

libraryDependencies += "org.apache.spark" % "spark-streaming-kafka-0-10_2.11" % sparkVersion

libraryDependencies += "org.apache.kafka" % "kafka-clients" % "3.0.0"
