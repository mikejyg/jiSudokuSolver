/**
 * Copyright (C) 2011 Junyang Gu <mikejyg@gmail.com>
 * 
 * This file is part of iSudokuSolver.
 *
 * iSudokuSolver is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * iSudokuSolver is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with iSudokuSolver.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gu.junyang.solver.sudoku;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * a sudoku cell, its position within the board and status (value if known)
 * It also contains a list of valid candidates.
 *
 */
public class Cell implements Serializable, Comparable<Cell> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1632526450183704470L;
	
	int row;

	int col;

	boolean known;
	int value;

	Candidates candidates;

        //////////////////////////
        
        public Cell() {
            super();
        }

        // deep copy constructor
        public Cell(Cell o) {
            row = o.row;
            col = o.col;
            known = o.known;
            value = o.value;
            candidates = new Candidates(o.candidates);
        }

	@Override
	public int compareTo(Cell o) {
		// the cell's position uniquely determines the cell

		if ( row < o.row )
			return -1;
		else if ( row > o.row)
			return 1;
		else {
			if (col < o.col)
				return -1;
			else if (col > o.col)
				return 1;
			else
				return 0;
		}
	}
	
	//////////////////////////////

	boolean removeCandidate(int val)
	{
		if ( candidates.contains(val) )
		{
			candidates.remove(val);

			if ( candidates.size() == 1 )
			{
				known = true;
				value = candidates.iterator().next();
				candidates.clear();
			}

			return true;
		}
		else
			return false;
	}

	public void init(int row, int col)
	{
		this.row=row;
		this.col=col;
		candidates = new Candidates();

                reset();
        }

        public void reset() {
		known = false;
                candidates.clear();
		for (int i=1; i<=9; i++)
			candidates.add(i);
        }
        
	public void setValue(int val)
	{
		known = true;
		value = val;
		candidates.clear();
	}

	public String toPositionString() {
		return "[" + ( row+1 ) + "]["+ ( col+1 ) + "]";
	}
	
	/**
	 * this method is also used to serialize/de-serialize the object,
	 * so there should be a one-to-one relationship between an object and the string
	 * format: it is either a number, if known, or a list of candidates in ()
	 */
    @Override
	public String toString() {
		String str;
		if (known)
			str=Integer.toString(value);
		else {
			str="(" + candidates.toString() + ")";
		}
		return str;
	}

	public String toSimpleString() {
		if (known)
			return Integer.toString(value);
		else
			return ".";
	}

        public void setCandidates(ArrayList<Integer> candidates) {
            this.candidates.clear();
            for (int i: candidates) {
                this.candidates.add(i);
            }
        }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

	public boolean isKnown() {
		return known;
	}

	public int getValue() {
		return value;
	}

	public Candidates getCandidates() {
		return candidates;
	}

}
