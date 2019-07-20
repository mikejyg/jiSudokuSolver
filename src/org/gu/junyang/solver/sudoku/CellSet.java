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
 * a set of cells.
 * 
 * TreeSet is used, to get a deterministric ordering of the cells within a set, 
 * so that the solver not only produces the same solutions, 
 * but also the same procedures to reach the solutions, every time.
 *     
 */
public class CellSet extends TreeSet<Cell> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2680073172159491119L;

	int CountCandidateRows(int val, Candidates rows) 
	{
		rows.clear();

		for (Cell i : this )
		{
			if ( i.known )
				continue;

			if ( i.candidates . contains(val) )
			{
				rows.add( i.row );
			}
		}

		return rows.size();
	}

	int CountCandidateCols(int val, Candidates cols) 
	{
		cols.clear();

		for (Cell i : this )
		{
			if ( i.known )
				continue;

			if ( i.candidates.contains(val) )
			{
				cols.add( i.col );
			}
		}

		return cols.size();
	}

}
