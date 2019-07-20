package org.gu.junyang.solver.sudoku.gui;

import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.apache.log4j.Logger;
import org.gu.junyang.solver.sudoku.Board;
import org.gu.junyang.solver.sudoku.Puzzle;
import org.gu.junyang.solver.sudoku.Sudoku;
import org.gu.junyang.solver.sudoku.Sudoku.SolveResult;

public class IsSolvableWorker extends SwingWorker<SolveResult, String>  {

	Logger logger = Logger.getLogger(this.getClass());
	
	Puzzle puzzle;
	Board board;
	BoardPanel boardPanel;
	
	
	/**
	 * IMPORTANT: one of them has to be not null
	 * 
	 * if puzzle is not null, check puzzle
	 * else, check board
	 * 
	 * @param puzzle
	 * @param board
	 */
	public IsSolvableWorker(BoardPanel boardPanel, Puzzle puzzle, Board board) {
		this.boardPanel = boardPanel;
		this.puzzle = puzzle;
		this.board = board;
	}
	
	@Override
	protected SolveResult doInBackground() throws Exception {
		if (puzzle!=null)
			return new Sudoku().isSolvable(puzzle);
		else
			return new Sudoku().isSolvable(board);
	}

	@Override
	protected void done() {
		SolveResult solveResult;
		try {
			solveResult = get();
		} catch (InterruptedException e) {
			String message = "InterruptedException: " + e.getMessage();
			boardPanel.logMessage(message);
			logger.error(message);
			// TODO Auto-generated catch block
//			e.printStackTrace();
			boardPanel.setCurrentBoardSolvable(true);
			return;
		} catch (ExecutionException e) {
			String message = "ExecutionException: " + e.getMessage();
			boardPanel.logMessage(message);
			logger.error(message);
			// TODO Auto-generated catch block
//			e.printStackTrace();
			boardPanel.setCurrentBoardSolvable(true);
			return;
		}
		
		if (solveResult==SolveResult.UNSOLVABLE) {
			boardPanel.setCurrentBoardSolvable(false);
		}
		else {
			boardPanel.setCurrentBoardSolvable(true);
		}
		
	}
	
	
}
