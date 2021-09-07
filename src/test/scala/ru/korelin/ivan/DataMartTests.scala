package ru.korelin.ivan

import org.apache.spark.sql.types.{StructField, StringType}
import org.scalatest.funsuite.AnyFunSuite

class DataMartTests extends AnyFunSuite {
  test("Test.analyzeFields.001") {
    val a = List(StructField("aId", StringType, nullable = true),StructField("key", StringType, nullable = true))
    val b = List(StructField("bId", StringType, nullable = true),StructField("key", StringType, nullable = true))
    val (allList, cteList) = Utils.analyzeFields(a.toArray,b.toArray)
    val standardCTE = "data.aId aId,\nnvl(set.key,data.key) key,\nset.bId bId"
    val standardALL = "last_value(aId, true) over (partition by id order by ts) aId,\nlast_value(key, true) over (partition by id order by ts) key,\nlast_value(bId, true) over (partition by id order by ts) bId"
    assert( cteList === standardCTE && allList === standardALL )
  }
}