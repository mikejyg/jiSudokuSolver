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

import java.util.TreeSet;

/**
 * TreeSet is used to force a sorted set, so the iteration through the set is unique.
 *
 */
public class Candidates extends TreeSet<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9145716133409779479L;

    public Candidates() {
        super();
    }

    // deep copy constructor
    public Candidates(Candidates o) {
        addAll(o);
    }
    
	/**
	 * this method is also used to serialize/de-serialize the object,
	 * so there should be a one-to-one relationship between an object and the string
	 */
    @Override
	public String toString() {
		String str = "";
		boolean first = true;
		for (Integer i : this) {
			if (!first)
				str += " ";
			else
				first = false;
			str += Integer.toString(i);
		}
		return str;
	}

}
