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
 * this class generates Solver instances based on selectable classes
 */

package org.gu.junyang.solver.sudoku;

public class SolverFactory {

    public static final String SolverDefaultClassName = "org.gu.junyang.solver.sudoku.Solver";
    public static final String SolverNonGreedyClassName = "org.gu.junyang.solver.sudoku.SolverNonGreedy";
    
    static String solverClassName = SolverDefaultClassName;
//    static String solverClassName = SolverNonGreedyClassName; 	// for testing purposes
    
    Class<Solver> solverClass;
    
	@SuppressWarnings("unchecked")
	private SolverFactory() {
    	try {
			solverClass =  (Class<Solver>) Class.forName(solverClassName);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
		}
	}
    
	public static SolverFactory newInstance() {
		SolverFactory solverFactory = new SolverFactory();
		return solverFactory;
	}
	
	public static void setSolverClassName(String solverClassName) {
		SolverFactory.solverClassName = solverClassName;
	}

	public Solver newSolver() {
		Solver solver=null;
		try {
			solver = solverClass.newInstance();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(1);
		}
		return solver;
	}
	
	
}
