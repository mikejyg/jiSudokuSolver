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
 * this class uses the Solver class to perform higher functions, such as reduce, generate, etc. 
 *  
 */

package org.gu.junyang.solver.sudoku;

import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Logger;
import org.gu.junyang.solver.sudoku.Board.BoardException;
import org.gu.junyang.solver.sudoku.Solver.Action;
import org.gu.junyang.utilities.MessagePoster;
import org.gu.junyang.utilities.ProgramErrorException;

public class Sudoku {

	Logger logger = Logger.getLogger(this.getClass());
	
	public static enum SolveResult {
		SOLVED,
		SOLVED_MULTIPLE_SOLUTIONS,
		UNSOLVED,	// because of limited capability level
		UNSOLVABLE	// conflict in puzzle
	}
	
	boolean findAllSolutions = false;
	
	// the capability level limit for solving a board 
	int solvingCapabilityLevel = Solver.HIGHEST_CAPABILITY_LEVELS;
	
	int level;
	
	Solver solver;

	// for reduce
	ArrayList<CellPosition> cellList;
	int reductionCount;
	
	Board board;
	
	/**
	 * generate randomly
	 * when random is true, do not set seed and Java's random output is not deterministic
	 */
	boolean useRandomSeed=false;
	
	/**
	 * for solver's random search
	 */
	boolean randomSearch=true;
	
	long randomSeed = 1;

	int puzzleCount;

	String message;
	
    boolean recordTranscript = false;
    
    boolean recordBoardHistory = false;		// since this can be a performance drag, only record when required.

    SolverFactory solverFactory = SolverFactory.newInstance();
    
	//////////////////////////////////////////////
	
	/**
	 * generate a puzzle with specified capability
	 * upon completion, the following queries can be used
	 * 	getPuzzleCount()
	 * 	getHighestLevel()
	 * @param targetCapability
	 * @throws Exception
	 */
	public Board generatePuzzleWithCapability(int targetCapability, MessagePoster messagePoster) throws ProgramErrorException {
		System.err.println("searching generated puzzles with target capability of " + targetCapability);
		puzzleCount=0;
		
		// this must be true, otherwise, every puzzle generated would be the same
		randomSearch=true;
		
		do {
			board = generatePuzzle(messagePoster);
			puzzleCount++;
			
			if (level != targetCapability) {
				// try to reduce and check level again
				reduce(board.getPuzzle());
				if (reductionCount!=0) {
					level = getLevel(board);
					messagePoster.PostMessage("after reduction, puzzle level: " + level);
				}
			}
			
			if (useRandomSeed) {
				messagePoster.PostMessage("generated # " + randomSeed + " puzzles tried: " + puzzleCount);
				randomSeed++;				
			}
			else
				messagePoster.PostMessage("puzzles tried: " + puzzleCount);

		} while (level != targetCapability);

//		System.out.println(board.toSimpleString());
//		System.err.println("found.");

        return board;
	}

	private int getLevel(Board board) throws ProgramErrorException {
		SolveResult solveResult = isSolvable(board);
		
		if (solveResult!=SolveResult.SOLVED || solveResult==SolveResult.SOLVED_MULTIPLE_SOLUTIONS)
			throw new ProgramErrorException("should be solvable.");

		return level;
	}

	/**
	 * the following queries can be called to obtain more information,
	 * 	getBoard()	to get the solution
	 * 	getSolvedCapabilityLevel()
	 * 	isMultipleSolutions()
	 * @param puzzleBoard
	 * @return
	 */
	public SolveResult solve(Board puzzleBoard) {
		solver = solverFactory.newSolver();

		solver.setBoard(puzzleBoard);
		solver.setFindAllSolutions(findAllSolutions);
		solver.setRecordTranscript(recordTranscript);
		solver.setRecordBoardHistory(recordBoardHistory);
		solver.setRandomSearch(randomSearch);
		
		try {
			solver.Solve();
		} catch (BoardException e) {
			message = "conflict in puzzle detected - " + e.getMessage() + ", unsolvable";
			logger.info(message);
			board = solver.getBoard();
			return SolveResult.UNSOLVABLE;
		}

		level = solver.getHighestCapabilityUsed();
		
		board = solver.getBoard();
		
		if (!solver.isSolved() || level > solvingCapabilityLevel)
		{
			return SolveResult.UNSOLVED;
		}
		else
		{
			// check multiple puzzle
			if (solver.isMultipleSolutions()) {
				return SolveResult.SOLVED_MULTIPLE_SOLUTIONS;
			}
			return SolveResult.SOLVED;
		}
	}

	// does not take candidate list as input
	public SolveResult solve(Puzzle puzzle) {
		return solve(new Board(puzzle));
	}
	
	/**
	 * level will be available after the call
	 * 
	 * @param puzzleboard
	 * @return
	 * @throws ProgramErrorException
	 */
	private SolveResult isSolvable1(Board puzzleboard) throws ProgramErrorException {
		solver = solverFactory.newSolver();
		solver.setBoard(puzzleboard);

		try {
			solver.Solve();
		} catch (BoardException e) {
			return SolveResult.UNSOLVABLE;
		}

		level = solver.getHighestCapabilityUsed();
		
		if (!solver.isSolved())
		{
			throw new ProgramErrorException("solver report UNSOLVED - unexpected.");
		}
		else
		{
			// check multiple puzzle
			if (solver.isMultipleSolutions()) {
				return SolveResult.SOLVED_MULTIPLE_SOLUTIONS;
			}
			return SolveResult.SOLVED;
		}
	}

        public SolveResult isSolvable(Board puzzleboard) throws ProgramErrorException {
            return isSolvable1(new Board(puzzleboard));
        }

	// does not take candidate list as input
	public SolveResult isSolvable(Puzzle puzzle) throws ProgramErrorException {
		return isSolvable1(new Board(puzzle));
	}
	
	/**
	 * generate a puzzle
	 * to control the behavior, the following methods can be used in advance,
	 * 	setRandom(boolean random)	- whether to generate randomly
	 * 	setGenerateSeed(long generateSeed) - if not random, use this seed (effectively the ID of the generated puzzle)
	 * the following queried can be used to obtain more info about the generated puzzle
	 * 	getLevel()
	 * @return
	 * @throws Exception
	 */
	public Board generatePuzzle(MessagePoster messagePoster) throws ProgramErrorException {
		// when generate, solve an empty puzzle
		
		solver = solverFactory.newSolver();
		solver.setNoMultipleSolutionCheck(true);	// do not look for multiple solutions
		solver.setRandomSearch(randomSearch);
		solver.setRecordTranscript(recordTranscript);
		solver.setRecordBoardHistory(recordBoardHistory);
		
		if (useRandomSeed) {
			solver.setRandomSeed(randomSeed);
//			System.err.println("generate # " + generateSeed);
		} else {
//			System.err.println("generate random");
                }

		try {
			solver.Solve();
		} catch (BoardException e) {
                    // should never happen
                    throw new ProgramErrorException(e.getMessage());
//			e.printStackTrace();
//			System.exit(1);
		}
		
//		System.out.println("the guessed values is the puzzle.");

		// print out the generated puzzle
		board = solver.getGuessBoard();
//		System.out.println(board.toSimpleString());
//		System.out.println();
		
//		if (printPuzzle)
//			System.out.println(generatedPuzzleBoard.toSimpleString());
//		else
//			System.err.println(generatedPuzzleBoard.toSimpleString());
		
		level = getLevel(board);
//		System.out.println(board.toSimpleString());
//		System.out.println();
		
		messagePoster.PostMessage("generated puzzle level: " + level);

		return board;
	}

	/**
	 * IMPORTANT: the input puzzle must be solvable
	 * upon completion the following queries can be used to obtain more info
	 * 	getReduceCount() - if 0, no reduction is performed
	 *  
	 * @return
	 */
	public Board reduce(Puzzle puzzle) throws ProgramErrorException {
		reductionCount=0;
		
		// check initial condition
		SolveResult solveResult = isSolvable(new Board(puzzle));
		
		if (solveResult == SolveResult.SOLVED_MULTIPLE_SOLUTIONS) {
			return null;			// can not be reduced
		} else if (solveResult == SolveResult.UNSOLVABLE)
			return null;
		else if (solveResult == SolveResult.UNSOLVED) {
			throw new ProgramErrorException("solver returned UNSOLVED unexpectedly");
		}
		
		// get the cell position list from solver
		if (useRandomSeed)
			cellList = Solver.getRandomCellPositionList(randomSeed);
		else
			cellList = Solver.getRandomCellPositionList();
		
		board = new Board(puzzle);

		reduce1();
		
		return board;
	}
	
	private void reduce1() throws ProgramErrorException {

		// save the board
		byte [] generatedPuzzle = board.serializeToByteArray();
		
		for (CellPosition cellPosition : cellList) {
			if ( ! board.sudokuCells[cellPosition.row][cellPosition.col].known )
				continue;

			board.reset(cellPosition.row, cellPosition.col);

			// save the board, because solver mutates it
			byte [] saved = board.serializeToByteArray();
			
			// try solve it
			solver = solverFactory.newSolver();
			solver.setBoard(board);
			
			// we only want to know whether it is solvable or not, and whether has multiple solutions 
			solver.setRecordTranscript(false);
			solver.setRecordBoardHistory(false);
			
			try {
				solver.Solve();
			} catch (BoardException e) {
                            // should not happen
                            throw new ProgramErrorException(e.getMessage());
//				e.printStackTrace();
//				System.exit(1);
			}

			if ( ! solver.isMultipleSolutions() ) {
				// reduction of one cell sucessful
				reductionCount++;
				
				logger.debug("reset successfully " + cellPosition.row + ", " + cellPosition.col);

				// reduce further
				board.deserializeFromByteArray(saved);
				reduce1();
				
				return;	// don't need to try other cells at this level
			} 

			// back out
			board.deserializeFromByteArray(generatedPuzzle);
		}
		
		// once we have tried all the cells, done
		logger.info("reduce done.");
	}

	// fix conflict puzzle by removing known values
	public Puzzle fixConflictPuzzle(Puzzle puzzle) throws ProgramErrorException {
		Random random = new Random();
		
		// first step
		// use sanity check to remove duplicates
		Board board = new Board(puzzle);
		boolean sanityCheckPass = false;
		do {
			try {
				board.sanityCheckPuzzle();
				sanityCheckPass = true;
			} catch (BoardException e) {
				Cell cell = board.getDuplicateCells().get(random.nextInt(2));
				board.reset(cell.row, cell.col);
			}
		} while ( ! sanityCheckPass );
		
		// second step
		// randomly remove cells
		
		// create a known cell list
		ArrayList<CellPosition> positionList;
		if (useRandomSeed)
			positionList = Solver.getRandomCellPositionList(randomSeed);
		else
			positionList = Solver.getRandomCellPositionList();
		for (int i=positionList.size()-1; i>=0; i--) {
			int row = positionList.get(i).row;
			int col = positionList.get(i).col;
			if (!puzzle.isSet(row, col))
				positionList.remove(i);
		}
		
		// randomly remove cells, until it is solvable
		Puzzle workPuzzle = new Puzzle(puzzle);
		
		// keep a record
		ArrayList<CellPosition> removedPositions = new ArrayList<CellPosition>();
		ArrayList<Integer> removedValues = new ArrayList<Integer>(); 
		
		while ( isSolvable(workPuzzle) == SolveResult.UNSOLVABLE ) {
			
			int idx = random.nextInt(positionList.size());
			
			CellPosition cellPosition = positionList.get( idx );
			
			removedPositions.add(cellPosition);
			positionList.remove(idx);
			
			removedValues.add(workPuzzle.values[cellPosition.row][cellPosition.col]);
			workPuzzle.reset(cellPosition.row, cellPosition.col);
		}
		
		// the removed values may not contribute to the conflict
		// so put back these that does not cause conflict
		logger.debug("removed cells: " + removedPositions.size());
		for (int idx = 0; idx<removedPositions.size(); idx++)
		{
			CellPosition cellPosition = removedPositions.get(idx);
			workPuzzle.set(cellPosition.row, cellPosition.col, removedValues.get(idx));

			// if not solvable, back out
			if (isSolvable(workPuzzle) == SolveResult.UNSOLVABLE)
				workPuzzle.reset(cellPosition.row, cellPosition.col);
			else {
//				System.err.println( "added back: " + (cellPosition.row+1) + ", " 
//						+ (cellPosition.col+1) + " = " + removedValues.get(idx) );
				logger.debug( "added back: " + (cellPosition.row+1) + ", " 
						+ (cellPosition.col+1) + " = " + removedValues.get(idx) );
			}
		} 
		
		return workPuzzle;
	}
	
	/////////////////////////////////////////////////
	
	public ArrayList<String> getTranscript() {
		return solver.getTranscript();
	}
	
	public ArrayList<String> getSolutions() {
		return solver.getSolutions();
	}
	
	public ArrayList<Action> getActions() {
		return solver.getActions();
	}
	
	public ArrayList<String> getGuessBoards() {
		return solver.getGuessBoards();
	}

	public boolean isFindAllSolutionsLimitExceeded() {
		return solver.isFindAllSolutionsLimitExceeded();
	}

	public void setSolvingCapabilityLevel(int capabilityLevel) {
		this.solvingCapabilityLevel = capabilityLevel;
	}

	public int getLevel() {
		return level;
	}

	public boolean isFindAllSolutions() {
		return findAllSolutions;
	}

	public void setFindAllSolutions(boolean findAllSolutions) {
		this.findAllSolutions = findAllSolutions;
	}

	public Board getBoard() {
		return board;
	}

	public void setUseRandomSeed(boolean useRandomSeed) {
		this.useRandomSeed = useRandomSeed;
	}

	public long getRandomSeed() {
		return randomSeed;
	}

	public void setRandomSeed(long randomSeed) {
		this.randomSeed = randomSeed;
	}

	public int getPuzzleCount() {
		return puzzleCount;
	}

	public int getReductionCount() {
		return reductionCount;
	}

	public String getMessage() {
		return message;
	}

	public void setRecordTranscript(boolean recordTranscript) {
		this.recordTranscript = recordTranscript;
	}

	public void setRecordBoardHistory(boolean recordBoardHistory) {
		this.recordBoardHistory = recordBoardHistory;
	}

	public ArrayList<Board> getBoardHistory() {
		return solver.getBoardHistory();
	}

	public ArrayList<Integer> getSolutionBoardIndexes() {
		return solver.getSolutionBoardIndexes();
	}

	public ArrayList<Integer> getSolutionTranscriptIndexes() {
		return solver.getSolutionTranscriptIndexes();
	}

	public void setRandomSearch(boolean randomSearch) {
		this.randomSearch = randomSearch;
	}
	
	public ArrayList<Integer> getBoardHistoryTranscriptIdxs() {
		return solver.getBoardHistoryTranscriptIdxs();
	}

	public ArrayList<CellList> getPatternCellLists() {
		return solver.getPatternCellLists();
	}

}
