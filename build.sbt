name := "solution"

version := "0.1"

scalaVersion := "2.11.12"

idePackagePrefix := Some("ru.korelin.ivan")

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.5"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.5" % "provided"
