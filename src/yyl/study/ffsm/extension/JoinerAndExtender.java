package yyl.study.ffsm.extension;

/*
 * Created on 28.03.2005
 *  
 * Copyright 2005 Thorsten Meinl
 * 
 * This file is part of ParMol.
 * ParMol is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * ParMol is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with ParMol; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import yyl.study.copy.parmol.*;


/**
 * This class is the core of the FFSM algorithm, it joins end extends the matrices. Optionally it can be given an array
 * of all frequent node labels. This information is then used in the extension step so that only frequent nodes are
 * added. This may improve the performance a little bit if many unfrequent node labels exist.
 * 
 * @author Thorsten Meinl <Thorsten.Meinl@informatik.uni-erlangen.de>
 */
public class JoinerAndExtender {
	private final GraphEdge[] m_frequentEdges;
	private final GraphEdge m_edge = new GraphEdge(-1, -1, -1);
	/**
	 * restore the edge Map,the key is the labelA
	 */
	private final HashMap<Integer,ArrayList<GraphEdge>> m_frequentEdgeMap=new HashMap<Integer,ArrayList<GraphEdge>>();
	
	/**
	 * Creates a new joiner and extender.
	 */
	public JoinerAndExtender() {
		m_frequentEdges = null;
	}


	/**
	 * Creates a new joiner and extender with the given frequent node labels. This array is then used by during the
	 * extension process to filter out infrequent matrices before calculating the embedding lists.
	 * 
	 * @param frequentEdges an array of all frequent graph edges
	 */
	public JoinerAndExtender(GraphEdge[] frequentEdges) {
		m_frequentEdges = new GraphEdge[frequentEdges.length];
		System.arraycopy(frequentEdges, 0, m_frequentEdges, 0, frequentEdges.length);
		Arrays.sort(m_frequentEdges);
		this.initFrequentEdgeMap();
		
	}
	
	/***
	 * Init the FrequentEdgeMap,using in extend search the candidate edge
	 */
	private void initFrequentEdgeMap()
	{
		if(this.m_frequentEdges!=null)
		{
			for(GraphEdge e:m_frequentEdges)
			{
				if(!m_frequentEdgeMap.containsKey(e.nodeALabel))
				{
					m_frequentEdgeMap.put(e.nodeALabel, new ArrayList<GraphEdge>());
				}
				
				if(!m_frequentEdgeMap.containsKey(e.nodeBLabel))
				{
					m_frequentEdgeMap.put(e.nodeBLabel, new ArrayList<GraphEdge>());
				}
				
				m_frequentEdgeMap.get(e.nodeALabel).add(new GraphEdge(e));
				if(e.nodeALabel!=e.nodeBLabel)
				{
					m_frequentEdgeMap.get(e.nodeBLabel).add(new GraphEdge(e.nodeBLabel,e.nodeALabel,e.edgeLabel));
				}
			}
		}
	}


	/**
	 * Joins the two given matrices and puts all resulting matrices in the given collection. The number of created
	 * matrices is returned. Please keep in mind, that the order of the matrices is important for join case 3b.
	 * 
	 * @param matrixA the first matrix
	 * @param matrixB the second matrix
	 * @param newMatrices a collection into which the new matrices should be put
	 * @return the number of created matrices (1 or 2)
	 */
	public int join(Matrix matrixA, Matrix matrixB, Vector<Matrix> newMatrices) {
		if ((matrixA.getEdgeCount() == 0) || (matrixB.getEdgeCount() == 0)) return 0;

		assert (matrixA.compareMaximalProperSubmatrix(matrixB) == 0);

		final boolean innerA = matrixA.isInnerMatrix();
		final boolean innerB = matrixB.isInnerMatrix();

		final int lastEdgeA = matrixA.getLastEdge();
		final int lastEdgeB = matrixB.getLastEdge();

		if ((lastEdgeA == Graph.NO_EDGE) || (lastEdgeB == Graph.NO_EDGE)) return 0;

		if (innerA && innerB) { // join case 1
			if (matrixA.getNodeCount() != matrixB.getNodeCount()) return 0;
			if (matrixA.compareTo(matrixB) < 0) return 0;

			if (lastEdgeA != lastEdgeB) {
				if (m_frequentEdges != null) {
					m_edge.nodeALabel = matrixB.getNodeLabel(matrixB.getNodeA(lastEdgeB));
					m_edge.nodeBLabel = matrixB.getNodeLabel(matrixB.getNodeB(lastEdgeB));
					m_edge.edgeLabel = matrixB.getEdgeLabel(lastEdgeB);
	
					if (Arrays.binarySearch(m_frequentEdges, m_edge) < 0) {
						return 0;
					}
				}
				
				// here we make an assumption about the encoding of the edges which is not very clean from the
				// software engineering point of view...
				final Matrix newMatrix = new Matrix(matrixA, 0);
				int nodeAIndex=newMatrix.getNode(matrixB.getNodeIndex(matrixB.getNodeA(lastEdgeB)));
				int nodeBIndex=newMatrix.getNode(matrixB.getNodeIndex(matrixB.getNodeB(lastEdgeB)));
				newMatrix.addEdge(nodeAIndex,nodeBIndex , matrixB.getEdgeLabel(lastEdgeB));
				newMatrix.setParents(matrixA.getID(), matrixB.getID());
				newMatrix.setExtendEdge(new GraphEdge(m_edge,nodeAIndex,nodeBIndex));
				this.addCamMatrix(newMatrices, newMatrix);
				
				
				return 1;
			} else {
				return 0;
			}
		} else if (innerA && !innerB) { // join case 2
			if (matrixA.getNodeCount() + 1 != matrixB.getNodeCount()) return 0;
			if (matrixA.compareTo(matrixB) < 0) return 0;

			if (m_frequentEdges != null) {
				m_edge.nodeALabel = matrixA.getNodeLabel(matrixA.getNodeA(lastEdgeA));
				m_edge.nodeBLabel = matrixA.getNodeLabel(matrixA.getNodeB(lastEdgeA));
				m_edge.edgeLabel = matrixA.getEdgeLabel(lastEdgeA);

				if (Arrays.binarySearch(m_frequentEdges, m_edge) < 0) {
					return 0;
				}
			}

			
			final Matrix newMatrix = new Matrix(matrixB,  0);
			int nodeAIndex=newMatrix.getNode(matrixA.getNodeIndex(matrixA.getNodeA(lastEdgeA)));
			int nodeBIndex=newMatrix.getNode(matrixA.getNodeIndex(matrixA.getNodeB(lastEdgeA)));
			newMatrix.addEdge(nodeAIndex, nodeBIndex, matrixA.getEdgeLabel(lastEdgeA));
			
			//generate the new edge
			final int newLastEdge=newMatrix.getLastEdge();
			int nA = newMatrix.getNodeA(newLastEdge);
			int nB = newMatrix.getNodeB(newLastEdge);
			if(nA>nB)
			{
				int temp=nA;
				nA=nB;
				nB=temp;
			}
			newMatrix.setExtendEdge(new GraphEdge(
					newMatrix.getNodeLabel(nA),
					newMatrix.getNodeLabel(nB),
					newMatrix.getEdgeLabel(newLastEdge),
					nA,nB));
			//newMatrix.setExtendEdge(new GraphEdge(m_edge,nodeAIndex,nodeBIndex));
			
			newMatrix.setParents(matrixA.getID(), matrixB.getID());
			
			
			this.addCamMatrix(newMatrices, newMatrix);

			return 1;
		} else if (!innerA && !innerB) { // join case 3
			if (matrixA.getNodeCount() != matrixB.getNodeCount()) return 0;

			final int nA = matrixB.getNodeA(lastEdgeB);
			final int nB = matrixB.getNodeB(lastEdgeB);
			
			boolean ok = true;
			if (m_frequentEdges != null) {
				m_edge.nodeALabel = matrixA.getNodeLabel((nA > nB) ? nB : nA);
				m_edge.nodeBLabel = matrixB.getNodeLabel((nA > nB) ? nA : nB);
				m_edge.edgeLabel = matrixB.getEdgeLabel(lastEdgeB);

				if (Arrays.binarySearch(m_frequentEdges, m_edge) < 0) {
					ok = false;
				}
			}
			
			// join case 3b
			if (ok) {
				Matrix newMatrix = new Matrix(matrixA,  1);
				final int newNode = newMatrix.addNode(matrixB.getNodeLabel(matrixB.getNodeCount() - 1));
	
				newMatrix.addEdge((nA > nB) ? nB : nA, newNode, matrixB.getEdgeLabel(lastEdgeB));
				//newMatrix.addNodeAndEdge((nA > nB) ? nB : nA, m_edge.nodeBLabel, m_edge.edgeLabel);
				newMatrix.setParents(matrixA.getID(), matrixB.getID());
				newMatrix.setExtendEdge(new GraphEdge(m_edge,(nA > nB) ? nB : nA,newNode));
				this.addCamMatrix(newMatrices, newMatrix);
				
				/*System.out.println("join case 3b");
				System.out.println(newMatrix);
				System.out.println("is cam:"+newMatrix.isCAM());
				System.out.println("is sub cam:"+newMatrix.isSuboptimalCAM()+"\r\n\r\n");*/
			}

			// join case 3a
			if ((lastEdgeA != lastEdgeB)
					&& (matrixA.getNodeLabel(matrixA.getNodeCount() - 1) == matrixB.getNodeLabel(matrixB.getNodeCount() - 1))) {
				if (matrixA.compareTo(matrixB) < 0) return 1;
				
				
				if (m_frequentEdges != null) {
					m_edge.nodeALabel = matrixB.getNodeLabel((nA > nB) ? nB : nA);
					m_edge.nodeBLabel = matrixB.getNodeLabel((nA > nB) ? nA : nB);
					m_edge.edgeLabel = matrixB.getEdgeLabel(lastEdgeB);

					if (Arrays.binarySearch(m_frequentEdges, m_edge) < 0) {
						return 1;
					}
				}				
				
				Matrix newMatrix = new Matrix(matrixA,  0);
				newMatrix.addEdge(nA, nB, matrixB.getEdgeLabel(lastEdgeB));
				newMatrix.setParents(matrixA.getID(), matrixB.getID());
				newMatrix.setExtendEdge(new GraphEdge(m_edge,(nA > nB) ? nB : nA,(nA > nB) ? nA : nB));
				//newMatrix.setExtendEdge(new GraphEdge(m_edge,nA , nB));
				this.addCamMatrix(newMatrices, newMatrix);

				/*System.out.println("join case 3a");
				System.out.println(newMatrix);
				System.out.println("is cam:"+newMatrix.isCAM());
				System.out.println("is sub cam:"+newMatrix.isSuboptimalCAM()+"\r\n\r\n");*/
				
				return 2;
			} else {
				return 1;
			}
		}

		return 0;
	}


	/**
	 * Extends the given matrix in all possible ways and puts the resulting matrices into the given collection.
	 * 
	 * @param matrix the matrix
	 * @param newMatrices a collection into which the new matrices should be put
	 * @return the number of created matrices
	 */
	public int extend(Matrix matrix, Vector<Matrix> newMatrices) {
		int count = 0;
		if (!matrix.isInnerMatrix()) {
			final int lastNodeLabel=matrix.getNodeLabel(matrix.getNodeCount()-1);
			if(this.m_frequentEdgeMap.containsKey(lastNodeLabel))
			{
				//get the candidate extend edge
				for(GraphEdge e:this.m_frequentEdgeMap.get(lastNodeLabel))
				{
					Matrix newMatrix=new Matrix(matrix,1);
					newMatrix.addNodeAndEdge(matrix.getNodeCount()-1, e.nodeBLabel, e.edgeLabel);
					newMatrix.setParent(matrix.getID());
					newMatrix.setExtendEdge(new GraphEdge(e,matrix.getNodeCount()-1,newMatrix.getNodeCount()-1));
					this.addCamMatrix(newMatrices, newMatrix);
					
					/*System.out.println("extend");
					System.out.println(newMatrix);
					System.out.println("is cam:"+newMatrix.isCAM());
					System.out.println("is sub cam:"+newMatrix.isSuboptimalCAM()+"\r\n\r\n");*/
					count++;
				}
			}
		}
		return count;
	}
	
	private void addCamMatrix(Vector<Matrix> newMatrices,Matrix newMatrix)
	{
		if(newMatrix.isCAM() || newMatrix.isSuboptimalCAM())
			newMatrices.add(newMatrix);
	}
}
