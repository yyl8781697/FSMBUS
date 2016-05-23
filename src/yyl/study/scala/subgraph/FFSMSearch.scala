package yyl.study.scala.subgraph

import yyl.study.ffsm.extension._;
import yyl.study.copy.parmol.GraphEdge;
import scala.collection.mutable.Map
import java.util.Vector

class FFSMSearch(frequentConnectEdgeList:List[List[GraphEdge]]) {
	
  val jeMap=Map[Int,JoinerAndExtender]()//joiner and extender map
  
  //init the je map
  for(areaId<-0 until frequentConnectEdgeList.length)
  {
    jeMap.put(areaId, new JoinerAndExtender(frequentConnectEdgeList(areaId).toArray))
  }
  
  def generate(matrixMap:Map[Int,Map[Int,Vector[Matrix]]]):Map[Int,Map[Int,Vector[Matrix]]]=
  {
    val newMatrixMap=Map[Int,Map[Int,Vector[Matrix]]]()
    
    for(areaId<-matrixMap.keys)
    {
      //check the areaid
      if(!jeMap.contains(areaId))
        throw new Exception("there is no areaid:"+areaId)
      
      newMatrixMap.put(areaId, Map[Int,Vector[Matrix]]())
      
      for((pmid,ms)<-matrixMap(areaId))
      {
		for(maIndex <-0 until ms.size())
		{
			if(ms.get(maIndex).isCAM()){//only the cam matrix can do something
					
				val newMatrices=new Vector[Matrix]();
				for(mbIndex <-0 until ms.size())
				{
					jeMap(areaId).join(ms.get(maIndex), ms.get(mbIndex), newMatrices);//do the joiner operate
				}
				
				jeMap(areaId).extend(ms.get(maIndex), newMatrices);//do the extender operate
				
				//println("newMatrices:"+newMatrices.size())
				newMatrixMap(areaId).put(ms.get(maIndex).getID(), newMatrices)
			}
				
		}
      }
    }
    
    //println(newMatrixMap)
    
    return newMatrixMap
  }
}