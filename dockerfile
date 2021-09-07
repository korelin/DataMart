FROM ubuntu:rolling
# get apps
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
# set env
ENV HOME /home/ubuntu
ENV SPARK_VERSION 2.4.5
ENV HADOOP_VERSION 2.7
ENV SCALA_VERSION 2.11
ENV SPARK_HOME ${HOME}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}
ENV PATH ${PATH}:${SPARK_HOME}/bin

RUN useradd --create-home --shell /bin/bash ubuntu

WORKDIR ${HOME}

COPY . /home/ubuntu/
RUN mkdir /home/ubuntu/data
ADD ../dwh-coding-challenge/data/* /home/ubuntu/data/
RUN chown -R ubuntu:ubuntu /home/ubuntu/*
USER ubuntu

# build package
RUN sbt clean
RUN sbt test
RUN sbt package

# get spark
RUN wget http://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz && \
tar xvf spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz
RUN rm -fv spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz

# set executable to spark_submit.sh
RUN chmod +x /home/ubuntu/spark_submit.sh

CMD /home/ubuntu/spark_submit.sh