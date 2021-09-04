package ru.korelin.ivan

import org.apache.log4j.Logger
import org.apache.log4j.Level
import org.apache.spark.sql.SparkSession

object DataMart extends App {
  Logger.getLogger("org").setLevel(Level.OFF)
  Logger.getLogger("akka").setLevel(Level.OFF)

  implicit val spark: SparkSession = Utils.getSession("Spark SQL basic data mart")

  val entity = List("accounts", "cards", "savings_accounts")

  entity.foreach{ e =>
    val df = spark.read
      .option("multiline", value = true)
      .json(s"file:///D:/work/dwh-coding-challenge/data/$e")
    df.createOrReplaceTempView(s"$e")
    Utils.makeCollapcedEntityView(df, s"$e")
  }

  Utils.makeSqlView(Scripts.flatHistoryScript,"v_flatHistory")
  val preAgg = Utils.makeSqlView(Scripts.preAggScript,"preAgg", needToShow = false)
  Utils.makeAggReport(preAgg)

  spark.stop

}