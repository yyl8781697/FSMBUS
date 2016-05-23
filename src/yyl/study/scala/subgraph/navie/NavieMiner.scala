package yyl.study.scala.subgraph.navie

import yyl.study.scala.subgraph._
import org.apache.spark._
import org.apache.spark.SparkContext._
import org.apache.spark.graphx._
import org.apache.spark.rdd._
import org.apache.spark.storage._
import scala.collection.mutable.{ListBuffer,Map,Stack,Queue,HashSet}
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Vector
import yyl.study.copy.parmol.GraphEdge
import yyl.study.ffsm.extension._

/**
 * the navie alogithm compare fgbug(fsmbus)
 */
object NavieMiner {
	  val maxIterNum=1000;
	  var beforePurne=0
	  var afterPurne=0
	  def main(args: Array[String]): Unit = {
	    
	    val argMap=UtilHelper.getArgsMap(args);
	    if(!argMap.contains("input_vertex_path") || !argMap.contains("input_edge_path"))
	    {
	    	println("input vertex or edge path can't be null!");
	    	System.exit(0);
	    }
	    
	    
	    
	  	
	  	val df = new SimpleDateFormat("yyyy-MM-dd HHmmss");//set the time format
	  	
	  	val input_vertex_path=argMap("input_vertex_path");
	  	val input_edge_path=argMap("input_edge_path");
	  	val minSupport=if(argMap.contains("minSupport")) argMap("minSupport").toInt else 2;
	  	val parallelism=if(argMap.contains("parallelism")) argMap("parallelism").toInt else 2;
	  	val batchSize=if(argMap.contains("batchSize")) argMap("batchSize").toInt else 5000;
	  	val outputSb=new ListBuffer[String]()
	  	outputSb.append("last modify at 2015-05-04 12:26 navie miner algorithm\r\n")
	  	outputSb.append("user conf info:\r\n");
	  	outputSb.append("input_vertex_path:%s\r\n".format(input_vertex_path))
		outputSb.append("input_edge_path:%s\r\n".format(input_edge_path))
		outputSb.append("minSupport:%s\r\n".format(minSupport));
		outputSb.append("maxIterNum:%s\r\n".format(maxIterNum));
		outputSb.append("parallelism is %s\r\n".format(parallelism))
		outputSb.append("batchSize is %s\r\n".format(batchSize))
	  	outputSb.append("start to run at "+df.format(new Date())+"\r\n");
	  	
	  	println(outputSb.mkString(""))
	  	val sw=new StopWatch();
	  	sw.start  //start to count the time
	  	
	  	val conf=new SparkConf().setAppName("navie miner version1.0");
	    	conf.set("spark.default.parallelism", parallelism.toString)
	    	//conf.set("spark.storage.memoryFraction", "0.2")
	  	val sc=new SparkContext(conf);
	  	
	    val S=run(input_vertex_path,input_edge_path,minSupport,parallelism,batchSize,sc);//run the main program
	  	
	    val curTime=df.format(new Date())
	    val cost=sw.getElapsedTime;
	    println("complete at "+curTime)
	    println("total frequent subgraph count is %d:".format(S.size));
	    println("cost is %s and store the file in /yyl/test/ouput/%s".format(cost,curTime))
	    println("beforePurne:%s afterPurne:%s".format(beforePurne,afterPurne))

	    outputSb.append("complete at "+curTime+"\r\n")
	    outputSb.append("cost is %s and total %s frequent subgraph\r\n".format(cost,S.size))
	    outputSb.append("beforePurne:%s afterPurne:%s".format(beforePurne,afterPurne))
	    //save the result
	    S.foreach(x=>outputSb.append(getMatrixDesc(x)+"\r\n"))
	    sc.parallelize(outputSb, 1)
	    .saveAsTextFile("/yyl/test/ouput/%s-%s-sum".format(curTime,minSupport))
	    
	  }
	  
	  /**
      * run the program
   	  */
	  def run(input_vertex_path:String,input_edge_path:String,minSupport:Int,parallelism:Int,batchSize:Int,sc:SparkContext):List[Matrix]=
	  {
	    //load the source vertex data
	    val sourceVertexRdd:RDD[(VertexId,Int)]=sc.textFile(input_vertex_path).map(line=>{
	      val data=line.split(" ");
	      (data(0).toLong,data(1).toInt)
	    });
	    //load the source edge data
	    val sourceEdgeRdd:RDD[Edge[Int]]=sc.textFile(input_edge_path).map(line=>{
	      val data=line.split(" ");
	      Edge(data(0).toLong,data(1).toLong,data(2).toInt)
	    }).coalesce(parallelism, true)
	    
	    /*println("frequent vertex label")
	    sourceVertexRdd.groupBy(v=>v._2).map(x=>(x._1,x._2.size)).filter(_._2>=minSupport).collect.foreach(println(_))*/
	    
	    HdfsHelper.deletePath(HdfsHelper.TEMP_PATH)
	    println("delete the temp path %s on hdfs".format(HdfsHelper.TEMP_PATH))
	    //load the graph
	    val sourceGraph=Graph(sourceVertexRdd,sourceEdgeRdd)
	    
	    //sourceGraph.degrees.map(x=>(x._2,x._1)).groupByKey.map(x=>(x._1,x._2.size)).sortByKey(false, 1).saveAsTextFile(HdfsHelper.getPath("degree"))

	  	val vertexLabelMap=Map[Int,Int]()  //big number label map to samll num label,reduce the store space
	    val edgeLabelMap=Map[Int,Int]()
	    println("frequent edge label")
	    //check the frequent for edge 
	    val edgeFreqMap=sourceGraph.triplets.groupBy(t=>UtilHelper.getSortEdgeFromTrip(t))
	    .filter(_._2.size>=minSupport).map(x=>{
	        //check for MNI ,it will count double if will check all the vertex count after distinct
	    	var frequent:Int=0
	        if(x._1._1==x._1._3)
	        {
	          val vidSet=HashSet[Long]()
	          x._2.foreach(triplet=>{
	            vidSet.add(triplet.srcId)
	            vidSet.add(triplet.dstId)
	          })
	          
	          frequent=vidSet.size
	        }else{
	          val srdIdSet=HashSet[Long]()
	          val dstIdSet=HashSet[Long]()
	          x._2.foreach(triplet=>{
	            val (srcId,dstId)=if(triplet.srcAttr>triplet.dstAttr) (triplet.srcId,triplet.dstId) else (triplet.dstId,triplet.srcId)
	            srdIdSet.add(srcId)
	            dstIdSet.add(dstId)
	          })
	          frequent=Math.min(srdIdSet.size, dstIdSet.size)
	        }
	    	
	    	/*if(x._1==(3,1,3))
	    	  throw new Exception("3,2,3:"+frequent);*/
	    	
	    	(x._1,frequent)
	    }).filter(_._2>=minSupport).collect.toMap
	    
	    //record the vertex/edge label,using reset the label
	    //edgeFreqMap.keys.flatMap(x=>List(x._1,x._3)).toList.distinct.foreach(vertexLabelMap.put(_, vertexLabelMap.size)) 
	    //edgeFreqMap.keys.map(_._2).toList.distinct.foreach(edgeLabelMap.put(_, edgeLabelMap.size))
	    
	    val edgeFreqMapBR=sc.broadcast(edgeFreqMap)
	    println("edgeFreqMap:"+edgeFreqMap.size)
	    println(edgeFreqMap)
	    
	    
	    //generate the frequent graph for every edge is frequent
	    var GP=sourceGraph.subgraph(epred=e=>edgeFreqMapBR.value.contains(UtilHelper.getSortEdgeFromTrip(e))).cache//GP is compont by frequent edge
	    //sourceGraph.unpersistVertices(false)
	    //sourceGraph.vertices.unpersist(false)
	    
	    //println("after fileter",GP.triplets.count,GP.vertices.count)
	    
	    val S=minning(edgeFreqMap.keys.toArray,minSupport,parallelism,batchSize,GP,sc)
	    
	    println("to total result:"+S.size)
	    S.foreach(x=>println(getMatrixDesc(x)))
	    
	    S
	  }
	  
	  
	  
	  def minning(frequentEdgeList:Array[(Int, Int, Int)],minSupport:Int,parallelism:Int,batchSize:Int,GP:Graph[Int,Int],sc:SparkContext):List[Matrix]=
	  {
	    val frequentConnectEdgeList=getConnectGraphEdgeList(frequentEdgeList)
	    val frequentMatrixs=ListBuffer[Matrix]() 
	    var matrixMap=Map[Int,Map[Int,Vector[Matrix]]]() //connect area,parent matrix id,matrix list
	    
	    //println("frequentConnectEdgeList"+frequentConnectEdgeList);
	    
	    //init the matrix map
	    for(i<-0 until frequentConnectEdgeList.length)
	    {
	      val initMatrices=new Vector[Matrix](frequentConnectEdgeList(i).length);
	      
	      for(ge<-frequentConnectEdgeList(i))
	      {
	        initMatrices.add(new Matrix(ge))
	      }
	      matrixMap.put(i, Map(-1->initMatrices))
	    }
	    
	    collectFrequentMatrix(frequentMatrixs,matrixMap)//collect the frequent one edge as the frequent graph
	    

	    //init the reverse index for one edge
	    val reverseEdgeIndexMap=Map[(Int,Int,Int),Int]()  //srcLabel,edgeLedge,dstLabel mid
	    for(areaId<- matrixMap.keys;
	    (pmid,ms)<-matrixMap(areaId);
	    i<-0 until ms.size();if ms.get(i).isCAM()
	    )
	    {
	        val extendEdge=ms.get(i).getExtendEdge
	        reverseEdgeIndexMap.put((extendEdge.nodeALabel,extendEdge.edgeLabel,extendEdge.nodeBLabel),ms.get(i).getID())
	    }
	    
	    //init the one-edge map to truth vertexid
	    val freqEdgeRdd=GP.triplets.flatMap(et=>{
	    	if(et.srcAttr==et.dstAttr)
	    	{
	    	  List(((et.srcAttr,et.attr,et.dstAttr),(et.srcId,et.dstId)),((et.srcAttr,et.attr,et.dstAttr),(et.dstId,et.srcId)))
	    	}else if(et.srcAttr>et.dstAttr)
	    	{
	    	  List(((et.srcAttr,et.attr,et.dstAttr),(et.srcId,et.dstId)))
	    	}else{
	    	  List(((et.dstAttr,et.attr,et.srcAttr),(et.dstId,et.srcId)))
	    	}
	    }).groupBy(_._1).filter(fm=>reverseEdgeIndexMap.contains(fm._1)).map(fm=>(fm._1,fm._2.unzip._2)).cache
	    
	    val freqEdgeSizeMap=freqEdgeRdd.map(x=>(x._1,x._2.size))//do action
	    .collectAsMap
	    //init the one-edge sub Grapp csp Map
	    //mid,domain data,vIndexLinkMap,vertexSize
	    var candidateFreqSubGRdd:RDD[(Int,ListBuffer[List[VertexId]])]=null
	    var preFreqSubGRdd:RDD[(Int,ListBuffer[List[VertexId]])]=null
	    var curFreqSubGRdd=freqEdgeRdd.map(fe=>{
	        val graphEdgeList=ListBuffer[List[VertexId]]()
	        fe._2.foreach(x=>{
	          graphEdgeList.append(List(x._1,x._2))
	        })
	        
	        
	        (reverseEdgeIndexMap(fe._1),graphEdgeList)//mid,(domain,edgeList,vIndexLinkMap:constraint link)
	    }).cache
	    
	    var freqSubGVertexSizeMap=curFreqSubGRdd.map(x=>(x._1,x._2.map(_.size).sum.toLong))//do action
	    .collectAsMap
	    
	    /*println("freqEdgeRdd")
	    freqEdgeRdd.collect.foreach(println(_))
	    println(reverseEdgeIndexMap)
	    println("curFreqSubG")
	    curFreqSubGRdd.collect.foreach(x=>{
	      println(x._1)
	      for(i<-0 until x._2.length)
	        println(i,x._2(i))
	    })*/
	    
	    val ffsmSeach=new FFSMSearch(frequentConnectEdgeList)
	    
	    println("==================one edge=================")
	    //matrixPrint(matrixMap)
	    val accumBeforePurne = sc.accumulator(0, "accumBeforePurne")
	    val accumAfterPurne = sc.accumulator(0, "accumAfterPurne")
	    
	    var iterNum=0
	    while(iterNum<maxIterNum)
	    {
	      val tempMatrixMap=matrixMap.clone
	      matrixMap=ffsmSeach.generate(tempMatrixMap)
	      println("============iterNum"+iterNum+"============")
	      //matrixPrint(matrixMap)
	      
	      val candidateSubGQueue=Queue[((Int,Int,Int),(Int,Int,Int,Boolean,(Int,Int)))]()
	      val candidateSubGList=ListBuffer[((Int,Int,Int),(Int,Int,Int,Boolean,(Int,Int)))]();//(srcLabel,eLabel,dstLabel),(areaId,pmid,mid,edgeIsReverse,(srcIndex,dstIndex))
	      for(areaId<- matrixMap.keys;
	    	(pmid,ms)<-matrixMap(areaId);
	    	i<-0 until ms.size();if ms.get(i).isCAM()
	      )
	      {
	        val extendEdge=ms.get(i).getExtendEdge
	        if(extendEdge.nodeALabel>extendEdge.nodeBLabel)
	        {
	          candidateSubGQueue.enqueue(((extendEdge.nodeALabel,extendEdge.edgeLabel,extendEdge.nodeBLabel),
	              (areaId,pmid,ms.get(i).getID(),false,(extendEdge.nodeAIndex,extendEdge.nodeBIndex))))
	        }else{
	          candidateSubGQueue.enqueue(((extendEdge.nodeBLabel,extendEdge.edgeLabel,extendEdge.nodeALabel),
	              (areaId,pmid,ms.get(i).getID(),true,(extendEdge.nodeBIndex,extendEdge.nodeAIndex))))
	        }
	      }
	      
	      val queueLength=candidateSubGQueue.length
	      println("candidate subgraph's queue size is:"+queueLength)
	      //println(candidateSubGQueue.mkString("\r\n"))
	      //get the candidate frequent subgraph rdd data
	      
	      preFreqSubGRdd=curFreqSubGRdd
	      curFreqSubGRdd=null
	      
	      val frequentMatrixIdList=ListBuffer[(Int,Long)]()
	      //val batchSize=5000
	      while(candidateSubGQueue.size>0)
	      {
	        
	        val freqSubGVertexSizeSortedMap=Map[Int,(Long,Int)]();
	        var _i=0;
	        val _a=Map[Int,Long]()
	        candidateSubGList.clear
	        for(i<-0 until batchSize;if candidateSubGQueue.size>0)
	        {
	          val _t=candidateSubGQueue.dequeue
	          candidateSubGList.append(_t)
	          if(!_a.contains(_t._2._2))
	          {
	            _a.put(_t._2._2,0)
	          }
	          
	          
	          _a.put(_t._2._2,_a(_t._2._2)+freqSubGVertexSizeMap(_t._2._2)+freqEdgeSizeMap(_t._1))
	          
	        }
	        
	        println(_a.mkString("##"))
	        _a.toArray.sortWith((xa,xb)=>xa._2<xb._2).foreach(a=>{
	          freqSubGVertexSizeSortedMap.put(a._1, (a._2,_i))
	          _i+=1
	        })
	        
	        //The greedy algorithm
	        val bucketMap=Map[Int,Int]();//pmid,bucketid
	        val bucketArray=new Array[Long](parallelism);
	        _a.toArray.sortWith((xa,xb)=>xa._2>xb._2).foreach(a=>{
	          var sIndex=0
	          var sValue=Long.MaxValue
	          for(i<-0 until bucketArray.length)
	          {
	        	  if(bucketArray(i)<sValue)
	        	  {
	        	    sIndex=i
	        	    sValue=bucketArray(i)
	        	  }
	          }
	          
	          bucketArray(sIndex)+=a._2
	          bucketMap.put(a._1, sIndex)
	        })
	        
	        
	        println("freqSubGVertexSizeSortedMap")
	        println(freqSubGVertexSizeSortedMap)
	        
	        
	        
	        println("pending subgraph:%s/%s/%s".format(candidateSubGList.size,candidateSubGQueue.size,queueLength))
	        
	        val batchFreqSubGRdd=sc.parallelize(candidateSubGList.toList,parallelism).join(freqEdgeRdd).map(x=>{
	          (x._2._1._2,//pmid
	           (x._2._1._1,//areaId
	            x._2._1._3,//mid,
	            //x._1,//(srcLabel,elabel,dstLabel)
	            x._2._1._4,//edgeIsReverse
	            x._2._1._5,//(srcIndex,dstIndex)
	            x._2._2//the truth vertexid
	            ))
	        }).join(preFreqSubGRdd,new greedyPartition(parallelism,bucketMap)).flatMap(x=>{
	          val ret=ListBuffer[(Int,ListBuffer[List[VertexId]])]()
	          val (areaId,pmid,mid,edgeIsReverse)=(x._2._1._1,x._1,x._2._1._2,x._2._1._3)
	          val graphVertexList=x._2._2
	          val srcIndex=if(edgeIsReverse) x._2._1._4._2 else x._2._1._4._1  //the truth srcIndex
	          val dstIndex=if(edgeIsReverse) x._2._1._4._1 else x._2._1._4._2
	          val isOuterEdge=(srcIndex == graphVertexList.head.size || dstIndex==graphVertexList.head.size)
	          
	          
	          val candGraphVertexList=ListBuffer[List[VertexId]]()
	          val srcDomainMap=Map[VertexId,Map[Int,HashSet[VertexId]]]()
	          val dstDomainMap=Map[VertexId,Map[Int,HashSet[VertexId]]]()
	        
	          val src2dstMap=Map[VertexId,HashSet[VertexId]]()
	          x._2._1._5.foreach(vertexids=>{
	              val (srcId,dstId)=if(edgeIsReverse) (vertexids._2,vertexids._1) else (vertexids._1,vertexids._2)
	              if(!src2dstMap.contains(srcId))
	                src2dstMap.put(srcId, HashSet[VertexId]())
	              src2dstMap(srcId).add(dstId)
	            })//foreach
	          
	          
	          if(isOuterEdge)
	          {
	             
	             //the extend edge is outer edge
	             graphVertexList.foreach(vList=>{
	               if(src2dstMap.contains(vList(srcIndex)))
	               {
	                 src2dstMap(vList(srcIndex)).foreach(dstId=>{
	                   if(!vList.contains(dstId))
	                     candGraphVertexList.append(vList:::List(dstId))
	                 })
	               }
	             })
	                
	          }else{//the extend edge is inner edge
		          graphVertexList.foreach(vList=>{
	               if(src2dstMap.contains(vList(srcIndex)) && src2dstMap(vList(srcIndex)).contains(vList(dstIndex)))
	               {
	                 candGraphVertexList.append(vList)
	               }
	             })
	          }
	          
//	          val output="extendEdgeList"+x._2._1._5.toString+"\r\n"+
//	        		  "graphVertexList"+graphVertexList.toString+"\r\n"+
//	        		  "candGraphVertexList"+candGraphVertexList.toString
//	          throw new Exception(output);
	          
	          
	          if(candGraphVertexList.size>=minSupport)//check the frequent
	          {
	              
	        	  val arr=new Array[HashSet[VertexId]](candGraphVertexList.head.size)
	        	  
	        	  try
	        	  {
	        	    candGraphVertexList.foreach(x=>{
	        	    for(i<-0 until x.size)
	        	    {
	        	      if(arr(i)==null)
	        	        arr(i)=HashSet[VertexId]()
	        	      arr(i).add(x(i))
	        	    }
	        	  })
	        	  }catch
	        	  {
	        	    case _=>throw new NullPointerException(candGraphVertexList.toString)
	        	  }
	        	  
	        	  
	        	  var frequent=Int.MaxValue
	        	  
	        	  arr.foreach(d=>frequent=Math.min(frequent,d.size))//calc the mni support
	              
	        	  
	        	  if(frequent>=minSupport)
	        	  {
	        	    ret.append((mid,candGraphVertexList))//success to check frequent
	        	  }
	              
	          }
	        
	          //MongodbHelper.insert("hello123",mid.toString+","+ret.size+sb.mkString("&&"))
	          ret.toList
	        }).persist(org.apache.spark.storage.StorageLevel.MEMORY_AND_DISK_SER)//parallelize
	        
	        val batchfrequentMatrixIdList=batchFreqSubGRdd.map(x=>(x._1,x._2.map(_.size).sum.toLong)).collect
	        if(batchfrequentMatrixIdList.size>0)
	        {
	          frequentMatrixIdList.appendAll(batchfrequentMatrixIdList)
	          //val preBatchFreqSubGRdd=batchFreqSubGRdd
	          if(curFreqSubGRdd==null)
	          {
	            curFreqSubGRdd=batchFreqSubGRdd
	          }else{
	            val preCandidateFreqSubGRdd=curFreqSubGRdd
	            curFreqSubGRdd=curFreqSubGRdd.union(batchFreqSubGRdd)  //update the new result
	            preCandidateFreqSubGRdd.unpersist(false)
	          }
	          //preBatchFreqSubGRdd.unpersist(false)
	        }
	      }//while queue
	      
	      if(frequentMatrixIdList.size>0)
	      {
	    	  println("frequentMatrixIdList")
	    	  println(frequentMatrixIdList.mkString(","))
	    	  freqSubGVertexSizeMap=frequentMatrixIdList.toMap
	         //remove the infrequent matrix/subgraph
	          for(areaId<- matrixMap.keys;
	    	   (pmid,ms)<-matrixMap(areaId)
		      )
		      {
		        var i=0;
		        while(i<ms.size())
		        {
		          if(ms.get(i).isCAM())
		          {
		            if(!freqSubGVertexSizeMap.contains(ms.get(i).getID()))
		            {
		              //check for infrequent subgraph's isomorphism and remove it
		              val isomorphismList=ListBuffer[Int]()
		              var j=0
		              while(j<ms.size())
		              {
		                if(!ms.get(j).isCAM() && ms.get(i).IsomorphismTo(ms.get(j)))
		                {
		                  //println("Isomorphism Test:"+ms.get(i).getID()+","+ms.get(j).getID())
		                  isomorphismList.append(j)
		                }
		                j+=1
		              }
		              
		              for(removeIndex<-isomorphismList.sortWith((a,b)=>a>b))
		              {
		                ms.remove(removeIndex)
		                if(removeIndex<i)
		                {
		                  i-=1
		                  
		                }
		              }
		              
		              ms.remove(i)
		            }else{
		              frequentMatrixs.append(ms.get(i))
		              i+=1
		            }
		          }else{
		            i+=1
		          }
		        }//while
		        var frequentCount=0
		        for(k<-0 until ms.size;if frequentCount==0)
		        {
		          if(ms.get(k).isCAM())
		            frequentCount+=1
		        }
		        if(frequentCount==0)
		          matrixMap(areaId).remove(pmid) //remove the empty element
		      }
	          
	          //println("after")
	          //matrixPrint(matrixMap)
	          
	          
	          //clear the cache
	          preFreqSubGRdd.unpersist(false)
	      }else{
	        //stop
	        println("stop at iterNum:",iterNum)
	        iterNum=maxIterNum
	      }
	      
	      
	      /*println("candFreqSubGRdd")
	      //candFreqSubGRdd.collect.foreach(println(_))
	      curFreqSubGRdd.collect.foreach(x=>{
	    	println(x._1)
	      	for(i<-0 until x._2._1.length)
	        println(i,x._2._1(i))
	        println(x._2._2)
	      })*/
	      
	      
	      
	      
	      iterNum+=1
	      //iterNum=maxIterNum
	      
	    }
	    println("ok")
	    beforePurne=accumBeforePurne.value
	    afterPurne=accumAfterPurne.value
	    frequentMatrixs.toList
	  }
	  
	  class greedyPartition(partitions: Int,bucketMap:Map[Int,Int]) extends Partitioner
	  {
	    def numPartitions = partitions
		  def getPartition(key: Any): Int = key match {
		    case null => 0
		    case pmid:Int => 
		      	if(bucketMap.contains(pmid))
		      	{
		      	  bucketMap(pmid)
		      	}else{
		      	  pmid%partitions
		      	}
		      	
		    	
		  }
		
		  override def equals(other: Any): Boolean = other match {
		    case h: greedyPartition =>
		      h.numPartitions == numPartitions
		    case _ =>
		      false
		  }
	  }
	  
	  def removeInfrequentAndgetFrequent(matrixMap:Map[Int,Map[Int,Vector[Matrix]]],frequentMatrixs:ListBuffer[Matrix])
	  {
	    
	  }
	  
	  
	  /**
	   * collect for frequent graph
	   */
	  def collectFrequentMatrix(frequentMatrixs:ListBuffer[Matrix],matrixMap:Map[Int,Map[Int,Vector[Matrix]]])
	  {
	    matrixMap.values.foreach(_.values.foreach(ms=>{
	      for(i<-0 until ms.size();if ms.get(i).isCAM())
	      {
	        frequentMatrixs.append(ms.get(i))
	      }
	    }))
	  }
	  
	  /**
	   * print for test
	   */
	  def matrixPrint(matrixMap:Map[Int,Map[Int,Vector[Matrix]]],isCam:Boolean=true)
	  {
	    var count=0
	    for(areaId<- matrixMap.keys)
	    {
	      println("===================areaid:"+areaId+"====================")
	      for((pmid,ms)<-matrixMap(areaId))
	      {
	        println("===================parent matrix id:"+pmid+"====================")
	        for(i<-0 until ms.size())
	        {
	          if(ms.get(i).isCAM() || !isCam)
	          {
	            println(getMatrixDesc(ms.get(i)))
			    count+=1
	          }
	          
	        }
	      }
	    }
	    
	    println("the matrix count is:"+count)
	  }
	  
	  
	  
	  def getMatrixDesc(m:Matrix):String={
	    val sb=new StringBuilder()
	    sb.append("matrix id:"+m.getID()+"\r\n");
        sb.append("cam:"+m.isCAM()+",is subcam:"+m.isSuboptimalCAM()+"\r\n");
		sb.append("extendEdge:"+m.getExtendEdge()+"\r\n");
		sb.append("parent:parentA=%d,parentB=%d\r\n".format(m.getParentMatrixAId(), m.getParentMatrixBId()));
		sb.append(m.toString());
		sb.append(m.getCspGraph());
	    
	    sb.toString
	  }
	  
	  /**
	   * get the connect graph in the edge list
	   */
	  def getConnectGraphEdgeList(frequentEdgeList:Array[(Int,Int,Int)]):List[List[GraphEdge]]={
	    val edgeList=ListBuffer[(ListBuffer[GraphEdge],ListBuffer[Int])]();
	    for(edge<-frequentEdgeList)
	    {
	      edgeList.append((ListBuffer(new GraphEdge(edge._1,edge._3,edge._2)),ListBuffer(edge._1,edge._3)));
	    }
	    
	    var isIntersect=false
	    var i=1
	    while(i<edgeList.length)
	    {
	      isIntersect=false
	      for(j<-0 until i;if !isIntersect)
	      {
	        if(UtilHelper.checkListIntersect(edgeList(i)._2.toList, edgeList(j)._2.toList))
	        {
	          edgeList(j)._1.appendAll(edgeList(i)._1)
	          edgeList(j)._2.appendAll(edgeList(i)._2)
	          edgeList.remove(i)
	          isIntersect=true
	        }
	      }
	      
	      
	      
	      if(isIntersect)
	      {
	        i=1
	      }else{
	        i+=1
	      }
	    }
	    
	    val ret=ListBuffer[List[GraphEdge]]()
	    edgeList.foreach(x=>ret.append(x._1.toList))
	    
	    println("ret.toList:"+ret.toList)
	    ret.toList
	  }
}