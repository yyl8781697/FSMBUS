package yyl.study.scala.subgraph.wrapper

import java.util.{HashMap,HashSet}
import scala.collection.mutable.{Map,HashSet}
import yyl.study.scala.subgraph.BipartiteGraphMatch

/**
 * invoked by java
 */
class BipartiteGraphMatchWrapper(xn:java.lang.Integer,javaEdgeMap:HashMap[java.lang.Integer,java.util.HashSet[java.lang.Long]]) {
	private val edgeMap=Map[Int,scala.collection.mutable.HashSet[Long]]();
	
	if(javaEdgeMap!=null)
	{
		val iter=javaEdgeMap.keySet().iterator()
	
		while(iter.hasNext())
		{
		  val index=iter.next()
		  edgeMap.put(index, scala.collection.mutable.HashSet[Long]());
		  
		  val setIter=javaEdgeMap.get(index).iterator()
		  while(setIter.hasNext())
		  {
		    edgeMap(index).add(setIter.next())
		  }
		}
	}
	
	
	def isMaxMatch():Boolean=
	{
	  new BipartiteGraphMatch(xn,edgeMap).isMaxMatch
	}
	
	def calc(a:java.lang.Integer,b:java.lang.Integer):java.lang.Integer={
	  a+b
	}
	
	
}