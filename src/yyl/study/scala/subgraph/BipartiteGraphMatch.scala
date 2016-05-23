package yyl.study.scala.subgraph

import scala.collection.mutable.{Map,ListBuffer,HashSet}
import scala.reflect.ClassTag

class BipartiteGraphMatch[T:ClassTag](xn:Int,edgeMap:Map[Int,HashSet[T]])
{
  val xList=edgeMap.keys.toList
  val yList=HashSet[T]()
  edgeMap.foreach(_._2.foreach(yList.add(_)))
  val yn=yList.size
  val userif=new Array[Int](yn) //record whether y is using,1=using 0=none
  val link=new Array[Int](yn) //record which node in x is linked to y
  private var isDoMaxMatch=false
  
  def existAugmentPath(t:Int):Boolean={
	var ret=false
    var i=0
	val iter=yList.iterator
	while(iter.hasNext && !ret)
	{
	  val yv=iter.next
	  if(userif(i) == 0 && edgeMap(xList(t)).contains(yv))
	  {
	    userif(i)=1
	    if(link(i) == -1 || existAugmentPath(link(i)))
	    {
	       link(i)=t
	       ret=true
	    }
	  }
	  i +=1
	}
	
	ret
  }
  
  /**
   * max match in bipartite  graph using Hungarian Algorithm
   * reference http://blog.csdn.net/xuguangsoft/article/details/7861988
   * only get the max match num
   */
  def isMaxMatch:Boolean={
    var num=0;
    isDoMaxMatch=true
    for(i<-0 until link.length) link(i) = -1
    for(i<-0 until xn)
    {
      for(j<-0 until userif.length) userif(j) = 0
      if(existAugmentPath(i))
        num+=1
    }
    
    //link.foreach(println(_))
    
    num == xn
  }
}