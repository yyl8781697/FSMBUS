package yyl.study.copy.parmol;

/*
 * Created on Aug 5, 2004
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
 */

/*
 * update at 2015.1.10 by yyl
 * copy de.parmol.util.HalfIntMatrix
 * modify:delete Util.getDigits Invoke in method of toString,insert getDigits method 
 */

import java.text.DecimalFormat;

/**
 * This class represents a lower (or upper) triangle matrix that stores ints.
 * 
 * @author Thorsten Meinl <Thorsten.Meinl@informatik.uni-erlangen.de>
 *
 */
public final class HalfIntMatrix implements IntMatrix {
	/** the array holding the complete triangle matrix */
	private int[] m_matrix;
	
	private int m_size, m_initialValue;
	
	/**
	 * Creates a new HalfIntMatrix with the given size and initial values
	 * @param initialSize the size of the matrix in rows (or columns)
	 * @param initialValue the initial value of each matrix element
	 */
	public HalfIntMatrix(int initialSize, int initialValue) {
		m_matrix = new int[(initialSize*initialSize + initialSize) / 2];		
		for (int i = 0; i < m_matrix.length; i++) m_matrix[i] = initialValue;
		m_size = initialSize;
		m_initialValue = initialValue;
	}
	
	/**
	 * Creates a new HalfIntMatrix that is an exact copy of the given template
	 * @param template a HalfIntMatrix that should be copied
	 */
	public HalfIntMatrix(HalfIntMatrix template) {
		this (template, 0);
	}
	
	
	/**
	 * Creates a new HalfIntMatrix that is an exact copy of the given template
	 * @param template a HalfIntMatrix that should be copied
	 * @param reserveNewNodes the number of new nodes for which space should be reserved
	 */
	public HalfIntMatrix(HalfIntMatrix template, int reserveNewNodes) {
		int newSize = (template.m_size + reserveNewNodes);
		newSize = (newSize * newSize + newSize) / 2;
		
		m_matrix = new int[newSize];
		System.arraycopy(template.m_matrix, 0, m_matrix, 0, template.m_matrix.length);
		m_initialValue = template.m_initialValue;
		for (int i = template.m_matrix.length; i < m_matrix.length; i++) m_matrix[i] = m_initialValue;
		m_size = template.m_size;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see de.parmol.util.IntMatrix#getValue(int, int)
	 */
	public int getValue(int row, int col) {
		if ((row > m_size) || (col > m_size)) throw new IllegalArgumentException("row or column index too big: matrix has only " + m_size + " rows/cols");

		return getValueFast(row, col);
	}

	
	private final int getValueFast(int row, int col) {
		if (row < col) return m_matrix[col * (col + 1) / 2 + row];
		return m_matrix[row  * (row + 1) / 2 + col];
		
	}
	
	/*
	 *  (non-Javadoc)
	 * @see de.parmol.util.IntMatrix#setValue(int, int, int)
	 */
	public void setValue(int row, int col, int value) {
		if ((row > m_size) || (col > m_size)) throw new IllegalArgumentException("row or column index too big: matrix has only " + m_size + " rows/cols");
		
		setValueFast(row, col, value);
	}
	
	
	private final void setValueFast(int row, int col, int value) {
		if (row < col) 
			m_matrix[col * (col + 1) / 2 + row] = value;
		else
			m_matrix[row * (row + 1) / 2 + col] = value;		
	}
	
	/*
	 *  (non-Javadoc)
	 * @see de.parmol.util.IntMatrix#getSize()
	 */
	public int getSize() { return m_size; }
	
	/*
	 *  (non-Javadoc)
	 * @see de.parmol.util.IntMatrix#resize(int)
	 */
	public void resize(int newSize) {
		if (newSize == m_size) return;
		if ((newSize*newSize + newSize) / 2 == m_matrix.length) {
			m_size = newSize;
			return;
		}
		
		int[] temp = new int[(newSize*newSize + newSize) / 2];

		System.arraycopy(m_matrix, 0, temp, 0, java.lang.Math.min(m_matrix.length, temp.length));
		for (int i = m_matrix.length; i < temp.length; i++) temp[i] = m_initialValue;
		m_matrix = temp;
		m_size = newSize;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see de.parmol.util.IntMatrix#deleteRowAndCol(int)
	 */
	public void deleteRowAndCol(int row) {
		for (int i = row; i < m_size - 1; i++) {
			for (int k = 0; k <= i; k++) {
				if (k < row) { // eine Zeile nach oben schieben
					m_matrix[i * (i + 1) / 2 + k] = m_matrix[(i + 1) * (i + 2) / 2 + k]; 
				} else { // nach links oben schieben
					m_matrix[i * (i + 1) / 2 + k] = m_matrix[i * (i + 1) / 2 + k + i + 2];
				}
			}
		}		
		
		m_size--;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		int maxLength = 1;
		for (int i = 0; i < m_matrix.length; i++) {
			//maxLength = java.lang.Math.max(maxLength, Util.getDigits(m_matrix[i]));	//delete
			maxLength = java.lang.Math.max(maxLength, getDigits(m_matrix[i]));			//insert
		}
		
		DecimalFormat format = new DecimalFormat("000000000000000000000000000".substring(0, maxLength));
		
		StringBuffer buf = new StringBuffer((m_matrix.length + 1) * maxLength + 16);
		for (int row = 0; row < m_size; row++) {
			for (int col = 0; col <= row; col++) {
				buf.append(format.format(getValue(row, col)));
				if (col < row) buf.append(' ');
			}
			buf.append('\n');
		}
		
		return buf.toString();
	}
	
	//insert
	/**
	 * Calculates the number of digits a number has
	 * 
	 * @param number the number whose number of digits should be returned
	 * @return the number of digits
	 */
	public static int getDigits(int number) {
		int digits = 1;
		number /= 10;
		while (number > 0) {
			number /= 10;
			digits++;
		}
		return digits;
	}

	
	
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		HalfIntMatrix m = (HalfIntMatrix) o;
		

		int max = java.lang.Math.min(this.m_matrix.length, m.m_matrix.length);
		for (int i = 0; i < max; i++) {
			if (this.m_matrix[i] != m.m_matrix[i]) {
				return this.m_matrix[i] - m.m_matrix[i];
			}
		}
		
		return this.m_size - m.m_size;
	}
	
	
	/**
	 * Compares the given rows from this and the given matrix.
	 * @param matrix the matrix with which this matrix should be compared
	 * @param fromRow the row from which the comparison should start
	 * @param toRow the row at which the comparison should end (inclusive)
	 * @return 0 is both "sub"-matrices are equal, >0 if this matrix is greater than the given matrix, <0 otherwise
	 */
	public int compareTo(HalfIntMatrix matrix, int fromRow, int toRow) {	
		toRow++;
		int max = java.lang.Math.min(java.lang.Math.min(toRow*(toRow + 1)/2, this.m_size*(this.m_size + 1) / 2),
				matrix.m_size*(matrix.m_size + 1) / 2 ); 
		
		
		for (int i = fromRow * (fromRow + 1) / 2; i < max; i++) {
			if (this.m_matrix[i] != matrix.m_matrix[i]) {
				return this.m_matrix[i] - matrix.m_matrix[i];
			}
		}
		
		return 0;		
	}
	
	/*
	 *  (non-Javadoc)
	 * @see de.parmol.util.IntMatrix#exchangeRows(int, int)
	 */
	public void exchangeRows(int rowA, int rowB) {
		if (rowA == rowB) return;
		
		if (rowA > rowB) {
			int t = rowA;
			rowA = rowB;
			rowB = t;
		}
		
		for (int i = 0; i < m_size; i++) {
			if ((rowA != i) && (rowB != i)) {
				int tA = getValueFast(rowA, i);
				int tB = getValueFast(rowB, i);
				
				setValueFast(rowB, i, tA);
				setValueFast(rowA, i, tB);
			}
		}
		
		int tA = getValueFast(rowA, rowA);
		int tB = getValueFast(rowB, rowB);
		
		setValueFast(rowB, rowB, tA);
		setValueFast(rowA, rowA, tB);

		
		int tAB = getValueFast(rowA, rowB);
		int tBA = getValueFast(rowB, rowA);

		setValueFast(rowA, rowB, tBA);
		setValueFast(rowB, rowA, tAB);		
	}
}
