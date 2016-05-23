package yyl.study.copy.parmol;

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

/*
 * update at 2015.1.10 by yyl
 * copy from de.parmol.FFSM.GraphEdge
 */

/**
 * This class is a simple representation of an edge in a graph. It just stores the two node labels and the label of the
 * edge that connects the two nodes.
 * 
 * @author Thorsten Meinl <Thorsten.Meinl@informatik.uni-erlangen.de>
 *  
 */
public class GraphEdge implements Comparable {
	/** The label of the "first" node. */ 
	public int nodeALabel;
	/** The label of the "second" node. */
	public int nodeBLabel;
	/** The label of the edge node. */
	public int edgeLabel;
	
	/** The position index about the firse node */
	public int nodeAIndex=-1;
	/** The position index about the second node */
	public int nodeBIndex=-1;


	/**
	 * Creates a new edge with the given node and edge labels
	 * 
	 * @param nodeALabel the label of the first node
	 * @param nodeBLabel the label of the second node
	 * @param edgeLabel the label of the edge
	 */
	public GraphEdge(int nodeALabel, int nodeBLabel, int edgeLabel) {
		this.nodeALabel = nodeALabel;
		this.nodeBLabel = nodeBLabel;
		this.edgeLabel = edgeLabel;
	}


	/**
	 * Creates a copy of the given GraphEdge.
	 * @param ge a GraphEdge that should be copied
	 */
	public GraphEdge(GraphEdge ge) {
		this.nodeALabel = ge.nodeALabel;
		this.nodeBLabel = ge.nodeBLabel;
		this.edgeLabel = ge.edgeLabel;
	}
	
	/**
	 * Creates a copy of the given GraphEdge.
	 * @param ge
	 * @param nodeAIndex
	 * @param nodeBIndex
	 */
	public GraphEdge(int nodeALabel, int nodeBLabel, int edgeLabel,int nodeAIndex,int nodeBIndex)
	{
		this.nodeALabel = nodeALabel;
		this.nodeBLabel = nodeBLabel;
		this.edgeLabel = edgeLabel;
		this.nodeAIndex=nodeAIndex;
		this.nodeBIndex=nodeBIndex;
	}
	
	public GraphEdge(GraphEdge ge,int nodeAIndex,int nodeBIndex)
	{
		this(ge);
		this.nodeAIndex=nodeAIndex;
		this.nodeBIndex=nodeBIndex;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		final GraphEdge fe = (GraphEdge) obj;

		return (fe.edgeLabel == this.edgeLabel)
				&& (((fe.nodeALabel == this.nodeALabel) && (fe.nodeBLabel == this.nodeBLabel)) ||
						((fe.nodeALabel == this.nodeBLabel) && (fe.nodeBLabel == this.nodeALabel)));
	}

	

	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (nodeALabel > nodeBLabel) {
			return (nodeALabel << 22) ^ (nodeBLabel << 11) ^ edgeLabel;
		} else {
			return (nodeBLabel << 22) ^ (nodeALabel << 11) ^ edgeLabel;
		}
	}


	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		final GraphEdge e = (GraphEdge) o;
		
		int diff = this.edgeLabel - e.edgeLabel;
		if (diff != 0) return diff;
		
		if (this.nodeALabel <= this.nodeBLabel) {
			if (e.nodeALabel <= e.nodeBLabel) {
				diff = this.nodeALabel - e.nodeALabel;
				if (diff != 0) return diff;
				
				return this.nodeBLabel - e.nodeBLabel;
			} else {
				diff = this.nodeALabel - e.nodeBLabel;
				if (diff != 0) return diff;
				
				return this.nodeBLabel - e.nodeALabel;				
			}			
		} else {
			if (e.nodeALabel <= e.nodeBLabel) {
				diff = this.nodeBLabel - e.nodeALabel;
				if (diff != 0) return diff;
				
				return this.nodeALabel - e.nodeBLabel;
			} else {
				diff = this.nodeBLabel - e.nodeBLabel;
				if (diff != 0) return diff;
				
				return this.nodeALabel - e.nodeALabel;				
			}
		}
	}
	
	@Override
	public String toString()
	{
		return String.format("(%d,%d,%d)->(%d,%d)", this.nodeALabel,this.nodeBLabel,this.edgeLabel,this.nodeAIndex,this.nodeBIndex);
	}
}
