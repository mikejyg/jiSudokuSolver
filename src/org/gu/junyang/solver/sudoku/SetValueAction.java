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


/**
 * this object represents an action of set value to a specific cell on a puzzle board
 * 
 */

package org.gu.junyang.solver.sudoku;

public class SetValueAction extends CellPosition {
	int value;
	
	public SetValueAction() {
	}
	
	public SetValueAction(int row, int col, int value) {
		this.row = row;
		this.col = col;
		this.value = value;
	}
	
}
