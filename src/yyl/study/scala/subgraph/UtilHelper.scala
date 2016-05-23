package yyl.study.scala.subgraph

import org.apache.spark.graphx._;
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Map;
import scala.collection.mutable.Stack
import scala.reflect.ClassTag

class UtilHelper {

}

object UtilHelper{
  
  /**
   * 获取根据Id排序之后的边
   */
  def getSortIdEdge[A:ClassTag](srcId:VertexId,attr:A,dstId:VertexId):(VertexId,A,VertexId)=
  {
    val (x,y)=if(srcId<dstId) (srcId,dstId) else (dstId,srcId)
    
    (x,attr,y)
  }
 
  /**
   * sort the label of edge,small label is after at big one
   */
  def getSortEdgeFromTrip(e:EdgeTriplet[Int,Int]):(Int,Int,Int)={
    val (x,y)=if(e.srcAttr>e.dstAttr) (e.srcAttr,e.dstAttr) else (e.dstAttr,e.srcAttr)   
    (x,e.attr,y)
  }
  
  
  def getSortEdgeFromTrip2(e:EdgeTriplet[(Int,Int),Int]):(Int,Int,Int)={
    val (x,y)=if(e.srcAttr._1>e.dstAttr._1) (e.srcAttr._1,e.dstAttr._1) else (e.dstAttr._1,e.srcAttr._1)
    
    (x,e.attr,y)
  }
  
  def checkListIntersect(listA:List[Int],listB:List[Int]):Boolean=
  {
    for(item<-listA.distinct)
    {
      if(listB.contains(item))
       return true
    }
    false
  }
  
  /**
   * generate the hashMap to a long code
   */
  def getMapCode(map:Map[Int,Int]):Long={
    var ret=0L;
    var kSum=1;
    var vSum=1;
    for((k,v)<-map)
    {
      kSum+=k
      vSum+=v
      ret+=((k+1)*(k+1)+(k+1)*v)*(k+1+v)
    }
    
    ret*=map.size*kSum*vSum;
    
    ret
  }
  
 
  def getCamCode(extendEdgeList:List[(Option[(Int, Int)], ((Int,Int), Int, (Int,Int)))],comStr:String):String={
    
    var ret=new StringBuilder();
    //val map=Map[Int,]
    //val list=for(edgeIndexStr<-comStr.split("#")) yield extendEdgeList(edgeIndexStr.toInt)
    val vertexMap=Map[(Int,Int),(Int,Int)]()//(edgeIndex,offset),(vid,vlabel)
    val edgeList=ListBuffer[((Int,Int),Int)]()//vid1,vid2,(vid1<vid2),elabel
    (for(edgeIndexStr<-comStr.split("#")) yield extendEdgeList(edgeIndexStr.toInt))
    .groupBy(x=>x._2._1).toList.sortWith((a,b)=>{
      var sum1=0
      var sum2=0
      a._2.unzip._1.filter(_!=None).foreach(v=>sum1+=Math.pow(v.get._2,2).toInt+v.get._2)
      b._2.unzip._1.filter(_!=None).foreach(v=>sum2+=Math.pow(v.get._2,2).toInt+v.get._2)
      sum1<sum2
    }).foreach(x=>{//builder the graph
      var vid1=0
      if(vertexMap.contains(x._1))
      {
        vid1=vertexMap(x._1)._1
      }else{
        vid1=vertexMap.size
        vertexMap.put(x._1, (vid1,x._1._1))
      }
      
      var tempIndex=0;
      x._2.foreach(y=>{
    	  var vid2=0
	      if(vertexMap.contains(y._2._3)){
	        vid2=vertexMap(y._2._1)._1
	      }else{
	        vid2=vertexMap.size
	        if(y._1 == None){//this is inner edge
	          vertexMap.put(y._2._3, (vid2,y._2._1._1))
	        }else{
	          
	          vertexMap.put(((Math.pow(y._2._1._1,3)*7+Math.pow(y._2._1._2+1,2)).toInt*13,Math.pow(+1,2).toInt*7+tempIndex*3), (vid2,y._1.get._2))
	        }
	      }
    	  tempIndex+=1;
    	  edgeList.append(((vid1,vid2),y._2._2))//add the edge
      })
    })
    
    
    val vertexList=vertexMap.values.toList.sortWith((a,b)=>if(a._2==b._2) a._1<b._1 else a._2<b._2)
	val camMatrix=Array.ofDim[Int](vertexList.size,vertexList.size)
	val vid2IndexMap=Map[Int,Int]()
	  
	//set the vertex label on diagonal line
	for(i<-0 until vertexList.size)
	{
	  val (vid,vlabel)=vertexList(i)
	  camMatrix(i)(i)=vlabel
	  vid2IndexMap.put(vid,i)
	}
    
	for(((srcId,dstId),elabel)<-edgeList)
	{
	  val (srcIndex,dstIndex)=if(vid2IndexMap(srcId)>vid2IndexMap(dstId)) (vid2IndexMap(srcId),vid2IndexMap(dstId)) else (vid2IndexMap(dstId),vid2IndexMap(srcId))
	  camMatrix(srcIndex)(dstIndex)=elabel
	}
	
	//builder the cam code
	for(i<-0 until vertexList.size)
	{
	  for(j<-0 to i)
	  {
	    ret.append(camMatrix(i)(j))
	  }
	}
	
	//MongodbHelper.insert("getCamCode",extendEdgeList.mkString("&&")+"_"+comStr+"_"+ret)
	
    ret.toString
  }
  
  
  
  
  
  private val combinationMap=Map[Int,List[String]]();//the combination cache
  /**
   * the invertedIndex for the combination element  count
   * the first int is combination item num/length
   * the second int is the element count
   */
  private val invertedElemCountMap=Map[Int,Map[Int,List[String]]]();
  /**
   * generate the complete combination for 0 until len
   */
  def genCom(len:Int):List[String]={
    if(!combinationMap.contains(len))
    {
    	val list=ListBuffer[String]();
	    for(i<- 0 until len)
	    {
	      list.append(i.toString);
	    }
	    //println("genCom:"+len)
	    combinationMap.put(len,genCom(list.toList));//store in the cache
    }
    
    combinationMap(len);
  }
  
  /**
   * generate the complete combination by the gived combination element count in len range
   */
  def getComByItem(m:Int,n:Int):List[String]=
  {
    
    if(!invertedElemCountMap.contains(m))
    {
      invertedElemCountMap.put(m, Map[Int,List[String]]());
    }
    
    if(!invertedElemCountMap(m).contains(n))
    {
      //println("getComByItem:%s,%s".format(m,n))
    	invertedElemCountMap(m).put(n,genCom(m).filter(_.split("#").length==n));//get the com
    }
    
    invertedElemCountMap(m)(n);
  }
  
  /**
   * 0-1 convert to 1-0,check the equal edge
   */
  def inverseEdgeId(edgeId:String):String=
  {
    val vertex=edgeId.split("-");
    vertex(1)+"-"+vertex(0)
  }
  
  def getArgsMap(args:Array[String]):Map[String,String]=
  {
    val ret=Map[String,String]();
    for(keyValuePair<-args)
    {
      val key=keyValuePair.split("=")(0);
      val value=keyValuePair.split("=")(1);
      ret.put(key, value)
    }
    
    ret
  }
  
  
  /**
   * generate the complete combination
   * like:List("1","2","3")=>List(1, 2, 1#2, 3, 1#3, 2#3, 1#2#3)
   */
  def genCom(list:List[String],split:String="#"):List[String]={
	  val nCnt = list.length;	 
	  val nBit = (0xFFFFFFFF >>> (32 - nCnt));
	  
	  val ret=ListBuffer[String]();
	  val com=ListBuffer[String]();
	  for(i<- 1 to nBit)
	  {
	    for(j<-0 to nCnt)
	    {
	      if ((i << (31 - j)) >> 31 == -1) {
	    	  com.append(list(j));
		  }
	    }
	    
	    ret.append(com.mkString(split));
	    com.clear;
	  }
	  
	  ret.toList;
	}
  
  
}