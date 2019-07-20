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
 * command line interface
 *  
 */

package org.gu.junyang.solver.sudoku;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.gu.junyang.solver.sudoku.Solver.Action;
import org.gu.junyang.solver.sudoku.Sudoku.SolveResult;
import org.gu.junyang.utilities.MessagePoster;
import org.gu.junyang.utilities.ProgramErrorException;

public class Cli implements MessagePoster {
	
	Logger logger = Logger.getLogger(this.getClass());

	String puzzleName;

	int capabilityLevel = Solver.HIGHEST_CAPABILITY_LEVELS;
	boolean showProgress;
	boolean showMoreProgress;
	boolean batchMode;
	
	boolean inputFromFile = true;
	String inputFile;
	
	int puzzleCount;
	int solvedCount;
	int nonUniquePuzzleCount;
	
	boolean generate = false;

	boolean randomSearch;
	boolean useRandomSeed;
	long randomSeed = 1;

	boolean allSolutions = false;
	
	Board board;
	
	Integer targetCapability = null;
	
	boolean reduce = false;
	
	Sudoku sudoku = new Sudoku();
	
	Scanner scanner;

	///////////////////////////////////

	public static void main(String [] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		Cli cli = new Cli();
		
		cli.execute(args);
	}

	void PrintHelp()
	{
		System.err.println("Java Intelligent Sudoku Solver, version 1.1.0");
		System.err.println("usage: [options] [input_file_name]");
		System.err.println("\t-h: print help");
		System.err.println("\t-c: read input from console (input_file_name not required)");
		System.err.println("\t--noguess: do not use what if ... method (the level 6 capability)");
		System.err.println("\t--showprogress: show solve progress");
		System.err.println("\t--showmorerogress: show more solve progress");
		System.err.println("\t--all: show all solutions");
		System.err.println("\t--batch: Solve multiple puzzles from input. Note that each puzzle must be preceeded by a puzzle ID (name).");
		System.err.println("\t-g: generate a board");
		System.err.println("\t-gr: generate a board randomly");
		System.err.println("\t-gc capability_level: generate a board with specified capability level (1-6)");
		System.err.println("\t--seed random_seed_number: set random seed number, default is 1");
		System.err.println("\t--reduce: try to reduce a puzzle");
		System.err.println("\t-r: use random search");
		System.exit(1);
	}

	void ParseArguments(String [] args)
	{	
		if (args.length==0)
			PrintHelp();
	
		for (int i=0; i<args.length; i++)
		{
			String s = args[i];
			if ( s.equals("--noguess") ) {
				capabilityLevel=Solver.HIGHEST_CAPABILITY_LEVELS-1;

			} else if ( s.equals("--showprogress") ) {
				showProgress=true;

			} else if ( s.equals("--showmoreprogress") ) {
				showProgress=true;
				showMoreProgress=true;

			} else if (s.equals("-h")) {
				PrintHelp();
				break;
				
			} else if (s.equals("-c")) {
				// input from a file
				inputFromFile = false;

			} else if (s.equals("--batch")) {
				batchMode = true;
			
			} else if (s.equals("-g")) {
				generate = true;
				
			} else if (s.equals("-gr")) {
				generate = true;
				randomSearch = true;
				
			} else if (s.equals("--all")) {
				allSolutions = true;
				
			} else if (s.equals("-gc")) {
				generate = true;
				i++;
				if (i<args.length)
					targetCapability = Integer.parseInt(args[i]);
				else {
					System.err.println("need to specify capability level - see help");
					System.exit(1);
				}
				
				if (targetCapability < 1 || targetCapability > Solver.HIGHEST_CAPABILITY_LEVELS) {
					System.err.println("error: target capability of " + targetCapability + " out of range.");
					System.exit(1);
				}
				
			} else if (s.equals("--seed")) {
				i++;
				randomSeed = Long.parseLong(args[i]);
				useRandomSeed = true;
				randomSearch = true;
				
			} else if (s.equals("--reduce")) {
				reduce = true;
				
			} else if (s.equals("-r")) {
				randomSearch = true;
				
			} else {
				inputFile = s;
				System.err.println("input file: " + s);
				break;
			}
		}
	}

	private void execute(String[] args) throws Exception {
		
		ParseArguments(args);
		
		Date start = new Date();
		
		if (generate) {
			
			sudoku.setUseRandomSeed(useRandomSeed);
			sudoku.setRandomSeed(randomSeed);
			sudoku.setRandomSearch(randomSearch);
			
			if (targetCapability != null) {
				sudoku.generatePuzzleWithCapability(targetCapability, this);
			}
			else
				sudoku.generatePuzzle(this);
			
			board = sudoku.getBoard();
			
			System.out.println(board.toSimpleString());
			
		} else {
			if (inputFromFile && inputFile==null) {
				System.err.printf("no input file specified.");
				System.exit(1);
			}
			
			getScanner();

			if (reduce) {
				reducePuzzles();
			} else
				solveReadPuzzles();
		}
		
		Date end = new Date();
		
		System.err.println("miliseconds used: " + (end.getTime() - start.getTime()) );
	}

	private void solveReadPuzzles() throws FileNotFoundException {
		do
		{
			// read puzzle name, if applicable
			if (batchMode) {
				if (!scanner.hasNext()) {
					break;
				}
				puzzleName = scanner.next();
				System.out.printf("%s\n", puzzleName);
			}

			if ( ! solveReadPuzzle() )
				break;
			
			System.err.println();
			
			puzzleCount++;

		} while (true);

//		System.out.println("end of input.");
		
		if (batchMode)
			System.err.println("total puzzles: " + puzzleCount + ", solved: " + solvedCount + 
					", non-unique solution puzzles: " + nonUniquePuzzleCount);

	}

	private void getScanner() throws FileNotFoundException {
		if ( inputFromFile ) {
			System.err.println("reading puzzle(s) from file: " + inputFile + "...");
			scanner = new Scanner(new FileInputStream(inputFile));
		}
		else {
			System.err.println("reading puzzle(s) from console...");
			scanner = new Scanner(System.in);
		}
	}

	boolean solveReadPuzzle() {
		board = new Board();
		
		// read puzzle
		if ( ! board.read(scanner) )
			return false;

		System.err.println(board.toSimpleString());

		sudoku.setFindAllSolutions(allSolutions);
		sudoku.setSolvingCapabilityLevel(capabilityLevel);
		sudoku.setRecordTranscript(true);
		sudoku.setRandomSearch(randomSearch);
		
		SolveResult solveResult = sudoku.solve(board);
	
		System.err.println();

		board = sudoku.getBoard();
		
		if ( solveResult == SolveResult.UNSOLVABLE ) {
			System.out.println(sudoku.getMessage());
			System.out.println(board.toSimpleString());
			
		} else if ( solveResult == SolveResult.UNSOLVED ) {
			System.out.printf("cells unknown: %d\n", board.unknowns);
			System.out.println();
			
			System.out.println(board.toSimpleString());
			
			if (board.unknowns!=0) {
				System.out.println();
				System.out.println(board.toString());
			}

			System.out.printf("unsolvable with capability level %d.\n", capabilityLevel);
		}
		else	// solved
		{
			if (showProgress) {
				for (int i=0; i<sudoku.getTranscript().size(); i++) {
					if ( sudoku.getActions().get(i) != Action.SET_VALUE )
						if (!showMoreProgress)
							continue;
					
					System.out.println(sudoku.getTranscript().get(i));
				}
			}
			
			System.err.println( "solved: capability level " + sudoku.getLevel() );
			solvedCount++;
			
			System.out.println(sudoku.getSolutions().get(0));
			
			// check multiple puzzle
			if ( solveResult == SolveResult.SOLVED_MULTIPLE_SOLUTIONS ) {
				nonUniquePuzzleCount++;
				System.out.println("WARNING: multiple solutions found - if you add to the original puzzle the guessed values, the puzzle will have a unique solution.");
				System.out.println("guessed values:");
				System.out.println(sudoku.getGuessBoards().get(0));
			}
			
			if (allSolutions) {
				for (int i=1; i<sudoku.getSolutions().size(); i++) {
					System.out.println("solution:");
					System.out.println(sudoku.getSolutions().get(i));
					System.out.println("guessed values:");
					System.out.println(sudoku.getGuessBoards().get(i));
					System.out.println();
				}
			}
			
			if (sudoku.isFindAllSolutionsLimitExceeded())
				System.out.println("WARNING: number of solutions exeeded limit of " + Solver.FIND_ALL_SOLUTIONS_LIMIT + ", terminated.");
		}
		
		return true;
	}

	private void reducePuzzles() throws ProgramErrorException {
		do
		{
			// read puzzle name, if applicable
			if (batchMode) {
				if (!scanner.hasNext()) {
					break;
				}
				puzzleName = scanner.next();
				System.out.println(puzzleName);
			}

			board = new Board();
			
			if ( ! board.read(scanner) )
				break;
			
			System.err.println(board.toSimpleString());
			
			sudoku.setRandomSearch(randomSearch);
			sudoku.setUseRandomSeed(useRandomSeed);
			sudoku.setRandomSeed(randomSeed);
			
			board = sudoku.reduce(board.getPuzzle());
			
			System.err.println();
			
			if (sudoku.reductionCount>0) {
				System.err.println("reduced: " + sudoku.reductionCount + " known value(s)");
				System.out.println(board.toSimpleString());
			} else
				System.err.println("not reduceable");

			System.out.println();
			
			puzzleCount++;

		} while (true);

//		System.out.println("end of input.");
		
		if (batchMode)
			System.err.println("total puzzles: " + puzzleCount);
	}

    public void PostMessage(String message) {
        System.err.println(message);
    }


}
