package ru.korelin.ivan

import org.apache.spark.SparkConf
import org.apache.spark.sql.functions.{col, expr, sum}
import org.apache.spark.sql.types.StructField
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.storage.StorageLevel
import scala.language.postfixOps
import scala.util.Try

object Utils {

  def getSession(name: String, sparkConf: SparkConf = new SparkConf()): SparkSession = SparkSession
    .builder()
    .appName(name)
    .config(sparkConf)
    .getOrCreate()

  def analyzeFields(aFields: Array[StructField], bFields:Array[StructField], nameA: String = "data", nameB: String = "set") :(String, String) = {
    val onlyA = (aFields diff bFields)
      .map(x => s"$nameA.${x.name} ${x.name}")
    val onlyB = (bFields diff aFields)
      .map(x => s"$nameB.${x.name} ${x.name}")
    val bothAandB = (aFields intersect bFields)
      .map(x => s"nvl($nameB.${x.name},$nameA.${x.name}) ${x.name}")
    val allList = ((aFields union bFields) distinct)
      .map(x => s"last_value(${x.name}, true) over (partition by id order by ts) ${x.name}").mkString(",\n")
    val cteList = (onlyA ++ bothAandB ++ onlyB).mkString(",\n")
    (allList, cteList)
  }

  def makeCollapsedEntityView(dataFrame: DataFrame, name: String, nameA: String = "data", nameB: String = "set")
                             (implicit spark: SparkSession): Try[DataFrame] = Try {
    val (allList, cteList) = analyzeFields(dataFrame.select(s"$nameA.*").schema.fields, dataFrame.select(s"$nameB.*").schema.fields)
    val ret = makeSqlView(s"""
    with cte as (select id, ts, $cteList
    from $name)
    select id,
    ts tsb,
    nvl(lead(ts-1) over (partition by id order by ts),9999999999999999999999) tse,
    $allList
    from cte
    """, s"v_flat_$name")
    ret
  }

  def makeSqlView(script: String, name: String, needToShow: Boolean = true)
                 (implicit spark: SparkSession): DataFrame = {
    val sqlView = spark.sql(script)
    sqlView.createOrReplaceTempView(name)
    sqlView.persist(StorageLevel.MEMORY_AND_DISK)
    if (needToShow) sqlView.show()
    sqlView
  }

  def makeAggReport(df: DataFrame): Unit = {
    df.groupBy("account_id")
      .agg(
        sum("b").alias("saving_trn"),
        sum("c").alias("card_trn"),
        (sum("b") + sum("c")).alias("total_trn")
      ).show()
    df.select(col("account_id"),
      col("ts"),
      expr("stack(2, 'savings_account', save, 'card', card) as (transaction, amount)"))
      .where("amount != 0").show()
  }
}
