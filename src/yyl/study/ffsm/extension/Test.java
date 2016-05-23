package yyl.study.ffsm.extension;

import java.util.HashMap;
import java.util.Vector;

import yyl.study.copy.parmol.GraphEdge;



public class Test {
	public static void main(String[] args)
	{
		int a=44,b=33,x=2,y=1;
		int IE=5,DB=3,IR=1,DM=4;
		
		GraphEdge e1=new GraphEdge(a,b,x);
		GraphEdge e2=new GraphEdge(b,b,y);
		/*GraphEdge e3=new GraphEdge(IE,DB,12);
		GraphEdge e4=new GraphEdge(DM,DB,2);
		GraphEdge e5=new GraphEdge(DB,IR,4);
		GraphEdge e6=new GraphEdge(DM,IR,8);*/
		
		Matrix m1=new Matrix(e1);
		Matrix m2=new Matrix(e2);
		
		HashMap<Integer,Vector<Matrix>> matrixMap=new HashMap<Integer,Vector<Matrix>>();
		HashMap<Integer,Vector<Matrix>> tempMatrixMap=new HashMap<Integer,Vector<Matrix>>();
		JoinerAndExtender joiner=new JoinerAndExtender(new GraphEdge[]{e1,e2});
		
		
		Vector<Matrix> initMatrices=new Vector<Matrix>();
		initMatrices.add(m1);
		initMatrices.add(m2);
		/*initMatrices.add(new Matrix(e3));
		initMatrices.add(new Matrix(e4));
		initMatrices.add(new Matrix(e5));
		initMatrices.add(new Matrix(e6));*/
		matrixMap.put(-1, initMatrices);
		
		
		int num=2;
		
		while(num<=4)
		{
			num++;
			tempMatrixMap=(HashMap<Integer,Vector<Matrix>>)matrixMap.clone();
			matrixMap.clear();
			for(int pmid:tempMatrixMap.keySet())
			{
				for(Matrix ma:tempMatrixMap.get(pmid))
				{
					if(ma.isCAM()){

						Vector<Matrix> newMatrices=new Vector<Matrix>();
						for(Matrix mb:tempMatrixMap.get(pmid))
						{
							joiner.join(ma, mb, newMatrices);
						}
						joiner.extend(ma, newMatrices);
						matrixMap.put(ma.getID(), newMatrices);
					}
					
				}
			}
			
			int count=0;
			System.out.println("==================the node number is:"+num+"===================");
			for(int pmid:matrixMap.keySet())
			{
				System.out.println("###############generate by:"+pmid+"##############\r\n\r\n");
				for(Matrix m:matrixMap.get(pmid))
				{
					if(!m.isSuboptimalCAM())
						continue;
					
					System.out.println("matrix id:"+m.getID());
					System.out.println("cam:"+m.isCAM()+",is subcam:"+m.isSuboptimalCAM());
					System.out.println("extendEdge:"+m.getExtendEdge());
					System.out.println(String.format("parent:parentA=%d,parentB=%d",m.getParentMatrixAId(), m.getParentMatrixBId()));
					System.out.println(m.toString());
					System.out.println(m.getCspGraph());
					count++;
				}
			}
			System.out.println(count);
			
			/*Vector<Matrix>  aax=new Vector<Matrix>();
			for(int pmid:matrixMap.keySet())
			{
				for(Matrix mx:matrixMap.get(pmid))
				{
					aax.add(mx);
					
				}
			}
			
			for(Matrix mx1:aax)
			{
				for(Matrix mx2:aax)
				{
					
					if(mx1.getID()<mx2.getID())
					{
						//System.out.println("test for:"+mx1.getID()+","+mx2.getID());
						if(mx1.IsomorphismTo(mx2))
						{
							System.out.println("IsomorphismT:"+mx1.getID()+","+mx2.getID());
						}
						
					}
				}
			}*/
			
			
		}
		
		
		
	}
}
