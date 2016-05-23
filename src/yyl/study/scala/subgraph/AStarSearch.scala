package yyl.study.scala.subgraph

import scala.collection.mutable.{ListBuffer,Map,Stack,HashSet,Queue}

import org.apache.spark.graphx.VertexId



class AStarSearch(domainData:Array[Map[VertexId,Map[Int,HashSet[VertexId]]]],
    vIndexLinkMap:Map[Int,List[Int]],//the vertex index link map about the constaint graph
    minSupport:Int,//the min support
    mid:Int,
    startIndex:Int) {
  
	
	private val domainLength=domainData.length
	
	private val instance=new Array[VertexId](domainLength) //the instance,store the assignment instance
	
	private val NON_VERTEXID:VertexId= -1L
	
	private val solDomain=new Array[HashSet[VertexId]](domainLength)  //store the solution for instance
	
	
	private val NOT_FOUND:Int= -1 //not fount
	private val FOUND_IT:Int=1 //success to fount it
	private val NO_MORE=0 //no more data in this branch
	private val STOP_IN_MAX_DEEPTH= 9999
	
	private val nonCandidateMap=Map[Int,ListBuffer[VertexId]]()  //the VertexId in this map is invalid
	def getNonCandidateMap=this.nonCandidateMap
	
	
	private val maxSearchDeepth=10000
	private var curSearchDeepth=0
	private val searchOrder=new Array[(Int,Int)](domainLength)  //assignIndex,parentIndex:-1 is none parent
	
	
	for(i<-0 until domainLength){
	  solDomain(i)=HashSet[VertexId]() //init the array
	  nonCandidateMap.put(i, ListBuffer[VertexId]())
	} 
	
	
	def search():Boolean=
	{
	  var ret=false
	  var assignIndex=domainLength-1
	  var isStop=false
	  val assignIndexQueue=Queue[Int]()
	  val xyz=ListBuffer[(Int,Int)]()
	  (for(i<-0 until domainLength)
	    xyz.append((i,domainData(i).size)))
	    xyz.sortWith((A,B)=>A._2<B._2).foreach(x=>assignIndexQueue.enqueue(x._1))
	  
	  val assignIndexQueue2=Queue[Int]()
	  while(assignIndex>=0)
	  {
	    assignIndexQueue2.enqueue(assignIndex)
	    assignIndex -=1
	  }

	  val visitedAssignIndexSet=HashSet[Int]()
	  //MongodbHelper.insert("xyz",mid.toString)
	  //println("assignIndexQueue",mid+","+xyz.mkString("&&")+","+assignIndexQueue.mkString("&&"));
	  //MongodbHelper.insert("assignIndexQueue",mid+","+xyz.mkString("&&")+","+assignIndexQueue.mkString("&&"))
	  //MongodbHelper.insert("assignIndexQueue2",mid+","+assignIndexQueue2.mkString("&&"))
	  while(assignIndexQueue.size>0 && !isStop)
	  {
	    assignIndex=assignIndexQueue.dequeue
	    
	    if(checkFrequent)
	    {
	      isStop=true
	      ret=true
	    }else{
	      //need to match
	      resetSearchOrder(assignIndex)
	      for((vid,vNextMap)<-domainData(assignIndex);
	      if solDomain(assignIndex).size<minSupport //only run to minSupport num in this assignIndex
	      && !solDomain(assignIndex).contains(vid)
	      && !isStop)  
	      {
        	  resetInstance() //reset the instance
	          instance(assignIndex)=vid
	          
	          resetOrderIndex
	          searchExist match
	          {
	            case NOT_FOUND=> //the vid can't find the valid instance 
	              nonCandidateMap(assignIndex).append(vid) //store the invalid the vertexid
	              
	              isStop=domainData(assignIndex).size-nonCandidateMap(assignIndex).size<minSupport   //unreachable to minSupport
	            case FOUND_IT=>//
	              for(i<-0 until domainLength) 
	              {
	                solDomain(i).add(instance(i)) //put the instance in the solution array  
	              }	                
	          }
	      }//for
	      
	      /*if(mid==16)
	      {
	        MongodbHelper.insert("solDomain",assignIndex+","+solDomain(assignIndex).size+","+nonCandidateMap(assignIndex).size)
	      }*/
	      
	      //clearInstanceCache
	      if(solDomain(assignIndex).size<minSupport)//can't find more in this index
	      {
	        //println("infrequent",mid,solDomain(assignIndex).size)
	        //MongodbHelper.insert("infrequent",(mid,solDomain(assignIndex).size).toString)
	        isStop=true
	      }else{
	        if(checkFrequent)
	        {
	          isStop=true
	          ret=true
	        }
	      }
	      
	    }//if
	  }
	  
	  if(ret)
	  {
	    //MongodbHelper.insert("frequent",mid.toString)
	    //purne
	    var removeIndex= -1
	    for((assignIndex,invalidAssignVidList)<-nonCandidateMap;
	    invalidAssignVid<-invalidAssignVidList;
	    if domainData(assignIndex).contains(invalidAssignVid))
	    {
	      //remove the self index
	      domainData(assignIndex).remove(invalidAssignVid)
	      
	    }
	  }
	  
	    
	  ret
	}
	
	/**
	 * search the valid instance
	 */
	private def searchExist:Int={
	  val (assignIndex,pIndex)=getNextOrder()
	  val curOrderIndex=orderIndex
	  if(assignIndex== -1)
	  {
	    return FOUND_IT
	  }
	  
	  if(pIndex== -1 || instance(pIndex)== -1)
	  {
	    return NOT_FOUND
	  }
	  
	  
	  if(!domainData(pIndex)(instance(pIndex)).contains(assignIndex))
	    return NOT_FOUND
	    
	  //cur vertex iter
	  val iter:Iterator[VertexId]=domainData(pIndex)(instance(pIndex))(assignIndex).iterator
	  
	  while(iter.hasNext)
	  {
	    val curVid=iter.next
	    if(domainData(assignIndex).contains(curVid))
	    {
	      if(!instanceContains(curVid))
	      {
	        val curNextMap=domainData(assignIndex)(curVid)
	        var matchFlag=true
	      
	        for(indexLink<-vIndexLinkMap(assignIndex).filter(_!=pIndex);if matchFlag)
	        {
	            if(curNextMap.contains(indexLink))
	            {
	              if(instance(indexLink)!=NON_VERTEXID)
	              {
	                if(!curNextMap(indexLink).contains(instance(indexLink)))
	                {
	                  matchFlag=false //vid not match
	                }
	              }
	            }else{
	              matchFlag=false //link not match
	            }
	        }
	      
	        if(matchFlag)
	        {
	          instance(assignIndex)=curVid
	        
	          searchExist match{
	            case NOT_FOUND=>
	              instance(assignIndex)=NON_VERTEXID
	            case FOUND_IT=>return FOUND_IT
	          }
	        }//matchFlag
	      }else{
	      }
	    }//if contains
	    
	  }
	  
	  setpBack
	  return NOT_FOUND
	}
	
	
	
	private var orderIndex=0
	def getNextOrder():(Int,Int)=
	{
	  if(0<=orderIndex && orderIndex<domainLength-1)
	  {
	    orderIndex+=1
	    searchOrder(orderIndex)
	  }else{
	    (-1,-1)
	  }
	}
	
	def getCurrentOrder():(Int,Int)=searchOrder(orderIndex)
	def setpBack()
	{
	  orderIndex -=1
	}
	
	private def resetOrderIndex{
	  orderIndex=0
	}
	
	
	
	def resetSearchOrder(firstIndex:Int)
	{
	  for(i<-0 until domainLength)
	    searchOrder(i) = (-1,-1)//-1 is none
	  
	  
	  val stack=Stack[(Int,Int)]()//vindex parentIndex
	  stack.push((firstIndex,-1))
	  
	  orderIndex=0
	  while(stack.size>0 && orderIndex<domainLength)
	  {
	     val (vIndex,pIndex)=stack.pop
	     if(!checkSearchOrderConatinsVIndex(vIndex))
	     {
	       searchOrder(orderIndex)=(vIndex,pIndex)
	       orderIndex+=1
	       vIndexLinkMap(vIndex).foreach(vi=>if(!checkSearchOrderConatinsVIndex(vi)) stack.push((vi,vIndex)))
	     }
	  }
	  orderIndex=0
	  //println(searchOrder.mkString("#"))
	}
	
	private def checkSearchOrderConatinsVIndex(vIndex:Int):Boolean=
	{
	  var flag=false
	  
	  for(i<-0 until domainLength;if !flag)
	  {
	     if(searchOrder(i)._1==vIndex)
	       flag=true
	  }
	  
	  flag
	}
	
	
	

	/**
	 * set the each value in instance to NON_VERTEXID
	 */
	private def resetInstance()
	{
	  for(i<-0 until domainLength)
	    instance(i)= NON_VERTEXID
	}
	
	/**
	 * check the elemnent whether is contianed in this instance
	 * @return :contains=true,not contain=false
	 */
	private def instanceContains(vid:VertexId):Boolean=
	{
	  var flag=false
	  
	  for(i<-0 until domainLength;if !flag)
	  {
	     if(instance(i)==vid)
	       flag=true
	  }
	  
	  flag
	}
	
	
	
	
	
	/**
	 * check current solution domain whether is frequent 
	 */
	private def checkFrequent():Boolean=
	{
	  var frequent=Int.MaxValue
	  for(assignIndex<-0 until solDomain.size;if frequent>=minSupport)
	  {
	      frequent= Math.min(frequent, solDomain(assignIndex).size)
	  }
	  
	  frequent>=minSupport
	}
	
	
}

object AStarSearch{
  def apply(domainData:Array[Map[VertexId,Map[Int,HashSet[VertexId]]]],vIndexLinkMap:Map[Int,List[Int]],minSupport:Int,mid:Int,startIndex:Int):Boolean={
    new AStarSearch(domainData,vIndexLinkMap,minSupport,mid,startIndex).search
  }
}