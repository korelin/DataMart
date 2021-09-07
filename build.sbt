name := "solution"

version := "0.2"

scalaVersion := "2.11.12"

idePackagePrefix := Some("ru.korelin.ivan")

libraryDependencies ++= Seq("org.apache.spark" %% "spark-core" % "2.4.5",
                            "org.apache.spark" %% "spark-sql" % "2.4.5" % "provided")
