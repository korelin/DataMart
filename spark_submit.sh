JAR=`ls target/scala-${SCALA_VERSION}/*.jar | head -n 1`
cp src/resources/log4j.properties ./
$SPARK_HOME/bin/spark-submit \
  --class ru.korelin.ivan.DataMart \
  --conf "spark.driver.extraJavaOptions=-Dlog4j.configuration=log4j.properties" \
  ${JAR}