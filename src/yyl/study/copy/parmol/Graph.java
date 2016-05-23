package yyl.study.copy.parmol;

/*
 * Created on May 17, 2004
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

/*
 * update at 2015.1.10 by yyl
 * copy from de.parmol.graph.Graph
 */

/**
 * This interface describes a generic graph. Nodes and edges are simply accessed by numbers. The
 * interface does not specify if the graph is directed or undirected. If the graph is directed, i.e.
 * it also implements the interface DirectedGraph then "nodeA" stands for the source of an edge and
 * "nodeB" for the destination.
 * 
 * @author Thorsten Meinl <Thorsten.Meinl@informatik.uni-erlangen.de>
 */
public interface Graph {
	/**
	 * Constant for non-existing edges.
	 */
	public final static int NO_EDGE = -1;
	
	/**
	 * Constant for non-existing nodes.
	 */
	public final static int NO_NODE = -1;


	/**
	 * @return the number of nodes in this graph
	 */
	public int getNodeCount();


	/**
	 * @return the number of edges in this graph
	 */
	public int getEdgeCount();


	/**
	 * @return the name of this graph
	 */
	public String getName();


	/** 
	 * @return a unique ID for this graph
	 */
	public int getID();
	
	/**
	 * @return a complete copy if this graph (except for the ID)
	 */
	public Object clone();


	/**
	 * @param nodeA a node in this graph
	 * @param nodeB another node in this graph
	 * @return the edge between the two node, or <code>-1</code> if there is no edge
	 */
	public int getEdge(int nodeA, int nodeB);


	/**
	 * Returns the edge with the given index. index must be a value between 0 and (getEdgeCount() -
	 * 1).
	 * 
	 * @param index the index of the edge
	 * @return an edge
	 */
	public int getEdge(int index);


	/**
	 * Returns the node with the given index. index must be a value between 0 and (getNodeCount() -
	 * 1).
	 * 
	 * @param index the index of the node
	 * @return a node
	 */
	public int getNode(int index);


	/**
	 * Returns the label of a node in this graph.
	 * 
	 * @param node the node whose label shall be retrieved
	 * @return the label of the give node
	 */
	public int getNodeLabel(int node);


	/**
	 * Returns the label of an edge in this graph.
	 * 
	 * @param edge the edge whose label shall be retrieved
	 * @return the label of the give edge
	 */
	public int getEdgeLabel(int edge);


	/**
	 * @param node a node in this graph
	 * @return the degree of this node, i.e. the number of edges connected to it
	 */
	public int getDegree(int node);


	/**
	 * Returns the edge with the given index connected to the given node. index must be a value
	 * between 0 and (getDegree(node) - 1).
	 * 
	 * @param node the nodes whose edge should be returned
	 * @param index the index of the edge
	 * @return an edge
	 */
	public int getNodeEdge(int node, int index);


	/**
	 * Returns a unique index for this node in the range 0 to (nodeCount - 1)
	 * 
	 * @param node a node in this graph
	 * @return a unique index for this node
	 */
	public int getNodeIndex(int node);


	/**
	 * Returns a unique index for this edge in the range 0 to (edgeCount - 1)
	 * 
	 * @param edge an edge in this graph
	 * @return a unique index for this edge
	 */
	public int getEdgeIndex(int edge);


	/**
	 * @param edge an edge in this graph
	 * @return one of the two nodes adjacent to the given edge
	 */
	public int getNodeA(int edge);


	/**
	 * @param edge an edge in this graph
	 * @return the other node of the two nodes adjacent to the given edge
	 */
	public int getNodeB(int edge);


	/**
	 * @param edge an edge in this graph
	 * @param node the node whose neighbour should be retrieved
	 * @return the node that is connected to the given edge but not the given one
	 */
	public int getOtherNode(int edge, int node);
	
	/**
	 * Returns <code>true</code> if the given edge is a bridge, <code>false</code> otherwise.
	 * @param edge an edge in the graph
	 * @return if the edge is a brigde
	 */
	public boolean isBridge(int edge);
	
	/**
	 * Associates an arbitrary object with the given node. 
	 * @param node the node
	 * @param o the object
	 */
	public void setNodeObject(int node, Object o);
	
	/**
	 * Returns the object associated with the given node (if there is any of course)
	 * @param node the node
	 * @return the object associated with the given node or <code>null</code> if no object is associated with it
	 */
	public Object getNodeObject(int node);
	
	
	/**
	 * Associates an arbitrary object with the given edge. 
	 * @param edge the edge
	 * @param o the object
	 */
	public void setEdgeObject(int edge, Object o);
	
	/**
	 * Returns the object associated with the given edge (if there is any of course)
	 * @param edge the edge
	 * @return the object associated with the given node or <code>null</code> if no object is associated with it
	 */
	public Object getEdgeObject(int edge);
	
	/**
	 * Rearranges the internal structures so that fewer memory is needed. 
	 */
	public void saveMemory();
}
