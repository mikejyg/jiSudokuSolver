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

package org.gu.junyang.solver.sudoku.gui;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import org.gu.junyang.solver.sudoku.Board;
import org.gu.junyang.solver.sudoku.Sudoku;
import org.gu.junyang.utilities.ExceptionUtils;
import org.gu.junyang.utilities.MessagePoster;
import org.gu.junyang.utilities.ProgramErrorException;

/**
 *
 */
public class SudokuWorker extends SwingWorker<Board, String> implements MessagePoster {

    boolean random;
    long seed;
    Integer level;
    MessagePoster messagePoster;
    MainWindow mainWindow;

    public SudokuWorker(boolean random, long seed, Integer level, MessagePoster messagePoster, MainWindow mainWindow) {
        this.random = random;
        this.seed = seed;
        this.level = level;
        this.messagePoster = messagePoster;
        this.mainWindow = mainWindow;
    }

    @Override
    protected Board doInBackground() {
        Sudoku sudoku = new Sudoku();
        sudoku.setRandomSearch(true);	// when generate, always use randomSearch
        sudoku.setUseRandomSeed(!random);
        if (!random) {
        	sudoku.setRandomSeed(seed);
        }

        Board board;

        if (level != null) {
            try {
                board = sudoku.generatePuzzleWithCapability(level, this);
            } catch (ProgramErrorException ex) {
//                Logger.getLogger(SudokuWorker.class.getName()).log(Level.SEVERE, null, ex);
                PostMessage((ex.getMessage() + MainWindow.NL + ExceptionUtils.getStackTraceString(ex)));
                return null;
            }
        } else {
            try {
                board = sudoku.generatePuzzle(this);
            } catch (ProgramErrorException ex) {
//                Logger.getLogger(SudokuWorker.class.getName()).log(Level.SEVERE, null, ex);
                PostMessage((ex.getMessage() + MainWindow.NL + ExceptionUtils.getStackTraceString(ex)));
                return null;
            }
        }

        return board;
    }

    @Override
    public void PostMessage(String message) {
        publish(message);
        if (isCancelled()) {
            publish("Cancelled.");
            throw new Error("cancelled");   // just to stop the thread
        }
    }

    @Override
    protected void process(List<String> chunks) {
        for (String msg : chunks) {
            messagePoster.PostMessage(msg);
        }
    }

    @Override
    protected void done() {
        if (isCancelled()) {
            System.err.println("done: cancelled.");
            return;
        }

        try {
            super.done();
            Board board = get();
            if (board != null)
                mainWindow.updateInputPanel(get());
        } catch (InterruptedException ex) {
            Logger.getLogger(SudokuWorker.class.getName()).log(Level.SEVERE, null, ex);
            PostMessage("InterruptedException: " + ex.getMessage());
        } catch (ExecutionException ex) {
            Logger.getLogger(SudokuWorker.class.getName()).log(Level.SEVERE, null, ex);
            PostMessage("ExecutionException :" + ex.getMessage());
        }

        mainWindow.waitDialog.dispose();
        mainWindow.restoreCursor();
        
    }

    
}
