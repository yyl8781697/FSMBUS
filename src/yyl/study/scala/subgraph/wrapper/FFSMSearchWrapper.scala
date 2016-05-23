package yyl.study.scala.subgraph.wrapper

import yyl.study.copy.parmol.GraphEdge;
import scala.collection.mutable.{ListBuffer,Map}
import yyl.study.scala.subgraph.FFSMSearch
import java.util.{HashMap,Vector};
import yyl.study.ffsm.extension.Matrix

/**
 * invoked by java for FFSMSearch
 */
class FFSMSearchWrapper(javaFrequentConnectEdgeList:java.util.List[java.util.List[GraphEdge]]) {
	//println("hello,i am FFSMSearchWrapper")
  
	private val frequentConnectEdgeList=ListBuffer[List[GraphEdge]]()
	
	if(javaFrequentConnectEdgeList!=null)
	{
	  val iter=javaFrequentConnectEdgeList.iterator()
	  while(iter.hasNext())
	  {
	    val list=new ListBuffer[GraphEdge]()
	    
	    val iterGraphEdge=iter.next().iterator()
	    while(iterGraphEdge.hasNext())
	    {
	      list.append(iterGraphEdge.next())
	    }
	    frequentConnectEdgeList.append(list.toList)
	    
	  }
	}
	
	private val ffsmSearch=new FFSMSearch(frequentConnectEdgeList.toList)
	
	def generate(javaMatrixMap:HashMap[Integer,HashMap[Integer,Vector[Matrix]]]):HashMap[Integer,HashMap[Integer,Vector[Matrix]]]={
	  val matrixMap=Map[Int,Map[Int,Vector[Matrix]]]();
	  
	  val areaIter=javaMatrixMap.keySet().iterator()
	  while(areaIter.hasNext())
	  {
	    val areaId=areaIter.next()
	    matrixMap.put(areaId, Map[Int,Vector[Matrix]]());
	    
	    val _tMap=javaMatrixMap.get(areaId)
	    val iter=_tMap.keySet().iterator()
	    
	    while(iter.hasNext())
	    {
	      val pmid=iter.next()
	     matrixMap(areaId).put(pmid, _tMap.get(pmid))
	    }
	  }
	  
	  val ret=ffsmSearch.generate(matrixMap)
	  
	  val retMatrixMap=new HashMap[Integer,HashMap[Integer,Vector[Matrix]]]();
	  
	  for((areaId,_tMap)<-ret;
	  (pmid,vec)<-_tMap)
	  {
	    if(!retMatrixMap.containsKey(areaId))
	    {
	      retMatrixMap.put(areaId, new HashMap[Integer,Vector[Matrix]]())
	    }
	    
	    retMatrixMap.get(areaId).put(pmid, vec)
	  }
	  
	  return retMatrixMap
	  
	  
	}
}