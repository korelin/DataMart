FROM ubuntu:rolling

RUN apt-get update -y && apt-get install -y \
openjdk-8-jdk \
curl \
gnupg \
wget
RUN echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list
RUN echo "deb https://repo.scala-sbt.org/scalasbt/debian /" | tee /etc/apt/sources.list.d/sbt_old.list
RUN curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | apt-key add
RUN apt-get update -y && apt-get install -y sbt && \
apt-get clean && \
rm -rf /var/lib/apt/lists/*

ENV HOME /home/ubuntu
ENV SPARK_VERSION 2.4.5
ENV HADOOP_VERSION 2.7
ENV SCALA_VERSION 2.11

RUN useradd --create-home --shell /bin/bash ubuntu

WORKDIR ${HOME}

ENV SPARK_HOME ${HOME}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}
ENV PATH ${PATH}:${SPARK_HOME}/bin

COPY . /home/ubuntu/
RUN chown -R ubuntu:ubuntu /home/ubuntu/*
USER ubuntu
# build package
#RUN sbt clean
#RUN sbt package

# get spark spark-2.4.5-bin-without-hadoop.tgz
RUN wget http://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz && \
tar xvf spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz
RUN rm -fv spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz

CMD $SPARK_HOME/bin/spark-submit --class ru.korelin.ivan.DataMart target/scala-2.11/solution_2.11-0.1.jar