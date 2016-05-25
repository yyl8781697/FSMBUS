# FSMBUS

### The Algorithm about frequent subgraph mining in single graph using Spark

You can run the algorithm as follows:

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

Other args will work you may know:
- `parallelism`:the parallelism in the algorithm,default is 2
- `batchSize`:the batch size when the number of  candidate subgraph is large,default is 5000

The vertex dataset's format:

    vertexId vertexLabel
  
The edge dataset's format:

    vertexId1 vertexId2 edgeLabel
  

> The dataset you can use the data folder in project.
