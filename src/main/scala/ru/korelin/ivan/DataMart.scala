package ru.korelin.ivan

import org.apache.log4j.{Level, Logger, PropertyConfigurator}
import org.apache.spark.sql.SparkSession
import scala.util.Try

object DataMart extends App {
  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)
  val logger: Logger = Logger.getLogger(this.getClass)
  Try(PropertyConfigurator.configure("log4j.properties"))
  logger.trace("Start.")
  implicit val spark: SparkSession = Utils.getSession("Spark SQL basic data mart")
  try {
    logger.trace(s"Get session $spark")
    val entity = Seq("accounts", "cards", "savings_accounts")

    val results = entity.map { e =>
      logger.trace(s"read File file:///home/ubuntu/data/$e")
      val df = spark.read
        .option("multiline", value = true)
        .json(s"file:///home/ubuntu/data/$e")
      logger.trace(s"Create View for $e")
      df.createOrReplaceTempView(s"$e")
      logger.trace(s"Create collapsed View for $e")
      Utils.makeCollapsedEntityView(df, s"$e")
    }

    if (!results.exists(x => x.isFailure)) {
      logger.trace(s"Create v_flatHistory")
      Utils.makeSqlView(Scripts.flatHistoryScript, "v_flatHistory")
      logger.trace(s"Create preAgg")
      val preAgg = Utils.makeSqlView(Scripts.preAggScript, "preAgg", needToShow = false)
      logger.trace(s"Create report")
      Utils.makeAggReport(preAgg)
    }
  }
  catch {
    case e: Exception =>
      val msg = s"Something bad had happened: ${e.getMessage}"
      logger.error(msg)
  } finally {
    spark.stop
    logger.trace("Finish.")
  }
}
