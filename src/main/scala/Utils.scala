package ru.korelin.ivan

import org.apache.spark.sql.functions.{col, expr, sum}
import org.apache.spark.sql.{DataFrame, SparkSession}

object Utils {

  def getSession(name: String): SparkSession = SparkSession
    .builder()
    .appName(name)
    .getOrCreate()

  def makeCollapcedEntityView(dataFrame: DataFrame, name: String)
                            (implicit spark: SparkSession): DataFrame = {
    val dataFields = dataFrame.select("data.*").schema.fields
    val setFields = dataFrame.select("set.*").schema.fields
    val onlyData = (dataFields diff setFields)
      .map(x => s"data.${x.name} ${x.name}")
    val onlySet = (setFields diff dataFields)
      .map(x => s"set.${x.name} ${x.name}")
    val dataAndSet = (dataFields intersect setFields)
      .map(x => s"nvl(set.${x.name},data.${x.name}) ${x.name}")
    val allList = ((dataFields union setFields) distinct)
      .map(x => s"last_value(${x.name}, true) over (partition by id order by ts) ${x.name}").mkString(",\n")
    val cteList = (onlyData ++ dataAndSet ++ onlySet).mkString(",\n")
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
    if (needToShow) sqlView.show()
    sqlView
  }

  def makeAggReport(df:DataFrame): Unit ={
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
