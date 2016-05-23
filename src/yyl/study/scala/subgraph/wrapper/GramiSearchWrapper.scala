package yyl.study.scala.subgraph.wrapper

import yyl.study.scala.subgraph.AStarSearch;
import java.util.HashMap
import org.apache.spark.graphx.VertexId
import scala.collection.mutable.{HashSet,Map,ListBuffer}

/**
 * invoked by java
 */
class AStarSearchWrapper(javaDomainData:java.util.List[HashMap[java.lang.Long,HashMap[Integer,java.util.HashSet[java.lang.Long]]]],
    javaVIndexLinkMap:HashMap[Integer,java.util.ArrayList[Integer]],
    minSupport:java.lang.Integer,
    mid:java.lang.Integer,
    startIndex:java.lang.Integer) {
	
	/*println("javaDomainData"+javaDomainData)
	println("javaVIndexLinkMap"+javaVIndexLinkMap)*/
	
	private val domainLength=javaDomainData.size()
	private val domainData=new Array[Map[VertexId,Map[Int,HashSet[VertexId]]]](domainLength)
	private val vIndexLinkMap=Map[Int,List[Int]]()
	
	for(i<-0 until domainLength)//convert to scala object
	{
	  domainData(i)=Map[VertexId,Map[Int,HashSet[VertexId]]]() //init the array
	  val iter=javaDomainData.get(i).keySet().iterator()
	  while(iter.hasNext())
	  {
	    val vid=iter.next()
	    val indexMap=javaDomainData.get(i).get(vid)
	    val indexIter=indexMap.keySet().iterator()
	    
	    if(!domainData(i).contains(vid))
        {
          domainData(i).put(vid, Map[Int,HashSet[VertexId]]())
        }
	    
	    while(indexIter.hasNext())
	    {
	      val vIndex=indexIter.next()
	      val setIter=indexMap.get(vIndex).iterator()
	      
	      if(!domainData(i)(vid).contains(vIndex))
          {
            domainData(i)(vid).put(vIndex, HashSet[VertexId]())
          }
	      while(setIter.hasNext())
	      {
	        domainData(i)(vid)(vIndex).add(setIter.next())
	      }//while(setIter.hasNext())
	    }//while(indexIter.hasNext())
	  }// while(iter.hasNext())
	  
	  
	  val listBuffer=ListBuffer[Int]()
	  val listIter=javaVIndexLinkMap.get(i).iterator()
	  while(listIter.hasNext())
	  {
	    listBuffer.append(listIter.next())
	  }
	  vIndexLinkMap.put(i, listBuffer.toList)
	}//for
	
	def search():Boolean={
	  
	  val AStarSearch=new AStarSearch(domainData,vIndexLinkMap,minSupport,mid,startIndex)
	  var ret=AStarSearch.search
	  
	  /*if(ret)
	  {
	    for((assignIndex,invalidAssignVidList)<-AStarSearch.getNonCandidateMap;
	    invalidAssignVid<-invalidAssignVidList;
	    if javaDomainData.get(assignIndex).containsKey(invalidAssignVid))
	    {
	      javaDomainData.get(assignIndex).remove(invalidAssignVid)//remove the data with java object
	      
	    }
	  }*/
	  
	  
	  ret
	}
	  
	
	
}