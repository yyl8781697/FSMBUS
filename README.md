# FSMBUS

### The Algorithm about frequent subgraph mining in single graph using Spark

you can run the algorithm as follows:

```shell
$SPARK_HOME/bin/spark-submit --class yyl.study.scala.subgraph.FSMBUSAlgo \
--master yarn-cluster \
--num-executors 1 \
--driver-memory 6G \
--executor-memory 6G \
--executor-cores 1 \
$SPARK_HOME/[your program's jar] \
input_vertex_path=[the vertex path on hdfs] \
input_edge_path=[the edge path on hdfs] \
minSupport=[the minimal support the frequent] \
```

> the dataset you can use the data folder in project.
