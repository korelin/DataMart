# DataMart

## How to run
The project is published in github:
https://github.com/korelin/DataMart

To run make sure that `run_build.sh` is runnable file (chmod +x run_build.sh) and run it.
This prompts you to enter path to data on your local computer. First it builds Docker image from Dockerfile (this may take some time).
Second it would run Docker container based on previously built image.

## Solution

I chose Spark as my data processing framework. And the solution is implemented mainly in the Scala programming language. At first, I looked at other libraries such as `pandas`,` pyspark`. But in my opinion, the solution to this problem fits well into the SQL language, and I love Scala more than Python. For assembly, sbt is used. Unit testing is implemented and can be extended at `org.scalatest.funsuite`.

1. The first task is most well automated, and I was able to quickly come up with a Unit test for it. The results of collapsing the table are displayed on the screen using the `show()` function of the standard Spark DataFrame API.
2. I decided to do the joining of tables in SQL, because this approach is very convenient for analysts and is understandable for most bank employees. The results are displayed similarly to task 1.
3. It seems to me that the task is not very formalized. This is good because it gives room for creativity. But at the same time, it is not entirely clear what is required. For aggregation, I decided to use the Spark DataFrame API, firstly, to show its capabilities, and secondly, to show my competence in this approach.

## Discussion
The main question for me was how to withdraw the amounts of transactions from the card in task 3. I decided that we were talking about a credit card and the amount charged should be negative, and credited positive. This is the standard client point of view in European countries. From the point of view of the bank, everything should be the other way around.
