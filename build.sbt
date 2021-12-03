name := "SparkLogAlertSystem"

version := "0.1"

scalaVersion := "2.13.7"

val sparkVersion = "2.4.8"

libraryDependencies += "org.apache.spark" % "spark-core_2.11" % sparkVersion

libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % sparkVersion

libraryDependencies += "org.apache.spark" % "spark-sql-kafka-0-10_2.11" % sparkVersion

libraryDependencies += "org.apache.spark" % "spark-streaming-kafka-0-10_2.11" % sparkVersion

libraryDependencies += "org.apache.kafka" % "kafka-clients" % "3.0.0"

// META-INF discarding
assemblyMergeStrategy in assembly := {
  case PathList("META-INF","services",xs @ _*) => MergeStrategy.filterDistinctLines
  case PathList("META-INF",xs @ _*) => MergeStrategy.discard
  case "application.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}
