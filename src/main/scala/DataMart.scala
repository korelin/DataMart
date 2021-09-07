package ru.korelin.ivan

import org.apache.log4j.{Level, Logger, PropertyConfigurator}
import org.apache.spark.sql.SparkSession

object DataMart extends App {
  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)
  val logger: Logger = Logger.getLogger(this.getClass)
  //PropertyConfigurator.configure("log4j.properties")
  logger.trace("Start.")
  implicit val spark: SparkSession = Utils.getSession("Spark SQL basic data mart")
  logger.trace(s"Get session $spark")
  val entity = Seq("accounts", "cards", "savings_accounts")

  entity.foreach{ e =>
    logger.trace(s"read File file:///home/ubuntu/data/$e")
    val df = spark.read
      .option("multiline", value = true)
      .json(s"file:///home/ubuntu/data/$e")
    logger.trace(s"Create View for $e")
    df.createOrReplaceTempView(s"$e")
    logger.trace(s"Create collapced View for $e")
    Utils.makeCollapcedEntityView(df, s"$e")
  }
  logger.trace(s"Create v_flatHistory")
  Utils.makeSqlView(Scripts.flatHistoryScript,"v_flatHistory")
  logger.trace(s"Create preAgg")
  val preAgg = Utils.makeSqlView(Scripts.preAggScript,"preAgg", needToShow = false)
  logger.trace(s"Create report")
  Utils.makeAggReport(preAgg)

  spark.stop
  logger.trace("Finish.")
}