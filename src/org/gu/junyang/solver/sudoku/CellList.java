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

import java.util.ArrayList;

public class CellList extends ArrayList<Cell> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6255384942868485051L;

	public String toPositionString() {
		String str="[ ";
		for (int i=0; i<size(); i++) {
			if (i!=0)
				str += ", ";
			str += get(i).toPositionString();
		}
		str += " ]";
		return str;
	}

}
