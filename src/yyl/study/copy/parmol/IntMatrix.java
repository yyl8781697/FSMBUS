package yyl.study.copy.parmol;

/*
 * Created on 12.12.2004
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
/**
 * @author Thorsten Meinl <Thorsten.Meinl@informatik.uni-erlangen.de>
 *
 */
public interface IntMatrix extends Comparable {
	/**
	 * Returns the value at the given position.
	 * @param row the row of the entry
	 * @param col the column of the entry
	 * @return the value at the given position in the matrix.
	 */
	public int getValue(int row, int col);


	/**
	 * Sets the value at the given position. 
	 * @param row the row on the entry
	 * @param col the column of the entry
	 * @param value the new value
	 */
	public void setValue(int row, int col, int value);


	/**
	 * @return the size of the matrix in rows (or columns)
	 */
	public int getSize();


	/**
	 * Resizes the matrix to the given size
	 * @param newSize the new size in rows (or columns)
	 */
	public void resize(int newSize);


	/**
	 * Deletes the given row (and column) from the matrix
	 * @param row
	 */
	public void deleteRowAndCol(int row);
	
	/**
	 * Exchanges the two rows (and the corresponding columns) in the matrix.
	 * @param rowA the first row
	 * @param rowB the second row
	 */
	public void exchangeRows(int rowA, int rowB);
}
