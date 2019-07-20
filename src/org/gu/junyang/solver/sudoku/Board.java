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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.log4j.Logger;

/**
 * a Sudoku board
 * It stores the state of each cell, and the valid candidates of each cell;
 * It also have data structure for valid sets (nine cells of values 1 to 9) of cells.
 * 
 */
public class Board implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3365635310615248119L;
    
    Logger logger = Logger.getLogger(this.getClass());
    
    public static final int TOTAL_CELLS = 81;
    public static final int TOTAL_CELL_SETS = 27;
    
    public static final String NL = System.getProperty("line.separator");
    
    Cell sudokuCells[][] = new Cell[9][9];
    int unknowns = TOTAL_CELLS;
    
    // a valid cell set in sudoku
    // 0-8: rows
    // 9-17: cols
    // 18-26: 3x3 blocks
    CellSet validCellSets[];
    
    // a list structure for easier addressing (than iterator)
    CellList validCellLists[];

    ////////////////////////////////////////
    public class BoardException extends Exception {

        /**
         *
         */
        private static final long serialVersionUID = 9124976961503823225L;

        public BoardException(String msg) {
            super(msg);
        }
    }

    /////////////////////////////////////
    
    public Board() {
        init();
    }

    // deep copy constructor
    public Board(Board o) {
        byte[] ba = o.serializeToByteArray();
        deserializeFromByteArray(ba);
    }

    // construct the board from a serialized byte array
    public Board(byte[] ba) {
        deserializeFromByteArray(ba);
    }

    // construct the board form a puzzle
    public Board(Puzzle puzzle) {
        init();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (puzzle.isSet(i, j)) {
                    sudokuCells[i][j].setValue(puzzle.getValue(i, j));
                    unknowns--;
                } else {
                    sudokuCells[i][j].reset();
                }
            }
        }
    }

    ///////////////////////////////////
    
    public void init() {
        int i, j, k;

        // init cell vector

        for (i = 0; i < 9; i++) {
            for (j = 0; j < 9; j++) {
                sudokuCells[i][j] = new Cell();
                sudokuCells[i][j].init(i, j);
            }
        }

        // init cell set vector
        validCellSets = new CellSet[TOTAL_CELL_SETS];

        validCellLists = new CellList[TOTAL_CELL_SETS];

        // row
        for (i = 0; i < 9; i++) {
            validCellSets[i] = new CellSet();
            validCellLists[i] = new CellList();
            for (j = 0; j < 9; j++) {
                validCellSets[i].add(sudokuCells[i][j]);
                validCellLists[i].add(sudokuCells[i][j]);
            }
        }

        // col
        for (j = 0; j < 9; j++) {
            validCellSets[9 + j] = new CellSet();
            validCellLists[9 + j] = new CellList();
            for (i = 0; i < 9; i++) {
                validCellSets[9 + j].add(sudokuCells[i][j]);
                validCellLists[9 + j].add(sudokuCells[i][j]);
            }
        }

        // block
        for (i = 0; i < 9; i += 3) {
            for (j = 0; j < 9; j += 3) {
                validCellSets[18 + i + j / 3] = new CellSet();
                validCellLists[18 + i + j / 3] = new CellList();
                for (k = 0; k < 9; k++) {
                    validCellSets[18 + i + j / 3].add(sudokuCells[i + k / 3][j + k % 3]);
                    validCellLists[18 + i + j / 3].add(sudokuCells[i + k / 3][j + k % 3]);
                }
            }
        }

    }

    // the duplicate cells, after sanity check duplicate cell failure
    ArrayList<Cell> duplicateCells = new ArrayList<Cell>();
    
    /**
     * check that each number only appear once in each cell set,
     * 	and that no missing candidates within each cell set.
     * 
     * if strictCandidatesCheck is true, no candidate shall co-exist with a know value for any cell set,
     * 	this is mainly used to verify that the eliminate candidate function did not miss any candidate.
     */
    protected void sanityCheck(boolean strictCandidatesCheck) throws BoardException {
    	duplicateCells.clear();
        for (CellSet cellSet : validCellSets) {

            // flag for a number that has been placed in a cell
            boolean cFlag[] = new boolean[9];

            // flag for a number that has been placed in a cell or exists as candidates
            boolean cFlag2[] = new boolean[9];

            for (Cell it : cellSet) {
                if (it.known) {
                    int value = it.value;

                    // whether this number is already set
                    if (cFlag[value - 1]) {
                    	// duplicate cell value detected
                    	
                    	duplicateCells.add(it);
                    	
                    	// find the other duplicate cell
                    	for (Cell it2 : cellSet) {
                    		if (it2==it)
                    			continue;
                    		if (!it2.known)
                    			continue;
                    		if (it2.value == value) {
                            	duplicateCells.add(it2);
                    			break;
                    		}
                    	}

                        throw new BoardException("sanity check failed: duplicate value of cells within a set - " 
                        		+ it.toPositionString() + ", " + duplicateCells.get(1).toPositionString() );
                    }

                    cFlag[value - 1] = true;

                    cFlag2[value - 1] = true;

                    // check candidates list of other cells
                    // the candidates should not contain the know value
                    if (strictCandidatesCheck) {
                        for (Cell otherCell : cellSet) {
                            if (otherCell.known) {
                                continue;
                            }

                            if (otherCell.candidates.contains(value)) {
                                //							Print();
                                throw new BoardException("sanity check failed: candidate list contain know cell value in a set");
                            }
                        }
                    }

                } else {
                    for (int i : it.candidates) {
                        cFlag2[i - 1] = true;
                    }
                }
            }

            for (int i = 0; i < 9; i++) {
                if (!cFlag2[i]) {
                    throw new BoardException("number " + (i + 1) + " is neither known or appear as a candidate within a set.");
                }
            }
        }

        // check unknown count
        int unknownCount = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!sudokuCells[i][j].known) {
                    unknownCount++;
                }
            }
        }

        if (unknownCount != unknowns) {
            throw new BoardException("unknown count record does not match board.");
        }

    }

    public void sanityCheck() throws BoardException {
        sanityCheck(false);
    }

    // this one does not check candidates
    public void sanityCheckPuzzle() throws BoardException {
    	duplicateCells.clear();
        for (CellSet cellSet : validCellSets) {

            // flag for a number that has been placed in a cell
            boolean cFlag[] = new boolean[9];

            for (Cell it : cellSet) {
                if (it.known) {
                    int value = it.value;

                    // whether this number is already set
                    if (cFlag[value - 1]) {
                    	// duplicate cell value detected
                    	
                    	duplicateCells.add(it);
                    	
                    	// find the other duplicate cell
                    	for (Cell it2 : cellSet) {
                    		if (it2==it)
                    			continue;
                    		if (!it2.known)
                    			continue;
                    		if (it2.value == value) {
                            	duplicateCells.add(it2);
                    			break;
                    		}
                    	}

                        throw new BoardException("sanity check failed: duplicate value of cells within a set - " 
                        		+ it.toPositionString() + ", " + duplicateCells.get(1).toPositionString() );
                    }

                    cFlag[value - 1] = true;

                }
            }

        }

        // check unknown count
        int unknownCount = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!sudokuCells[i][j].known) {
                    unknownCount++;
                }
            }
        }

        if (unknownCount != unknowns) {
            throw new BoardException("unknown count record does not match board.");
        }
    }
    
    // read in the puzzle
    public boolean read(Scanner scanner) {
        String readLine = "";
        int charIdx = 0;
        int unknownCnt=TOTAL_CELLS;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                while (true) {

                    if (charIdx >= readLine.length()) {
                        if (!scanner.hasNext()) {
                            if (i != 0 || j != 0) {
                                System.out.println("unexpected end of input.");
                            }
                            return false;
                        }
                        readLine = scanner.next();
                        charIdx = 0;
                    }

                    String token = readLine.substring(charIdx, charIdx + 1);
                    charIdx++;

                    // strings represent unknown cell: . 0
                    if (token.equals(".") || token.equals("0")) {
                    	sudokuCells[i][j].reset();
                        break;
                    }

                    int iTmp;
                    try {
                        iTmp = Integer.parseInt(token);
                        sudokuCells[i][j].setValue(iTmp);
                        unknownCnt--;
                        break;
                    } catch (NumberFormatException e) {
                        // ignore the token
                        continue;
                    }
                }
            }
        }
        unknowns = unknownCnt; 
        return true;
    }

    /**
     * get a puzzle object from the board. 
     * Note that a puzzle does not include all information a board has
     * @return
     */
    public Puzzle getPuzzle() {
        Puzzle puzzle = new Puzzle();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (sudokuCells[i][j].known) {
                    puzzle.set(i, j, sudokuCells[i][j].value);
                } else {
                    puzzle.reset(i, j);
                }
            }
        }
        return puzzle;
    }

    /**
     * when detail is true, this method is also used to serialize/de-serialize the object,
     * so there should be a one-to-one relationship between an object and the string
     */
    public String toString(boolean detail) {
        String str = "";
        boolean firstLine = true;
        for (int i = 0; i < 9; i++) {
            if (!firstLine) {
                str += NL;
            }

            if (i % 3 == 0 && !firstLine) {
                str += NL;
            }

            firstLine = false;

            boolean first = true;

            for (int j = 0; j < 9; j++) {
                if (!first) {
                    str += " ";
                }

                if (j % 3 == 0 && !first) {
                    str += " ";
                }

                if (detail) {
                    str += sudokuCells[i][j].toString();
                } else {
                    str += sudokuCells[i][j].toSimpleString();
                }

                first = false;
            }
        }
        return str;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toSimpleString() {
        return toString(false);
    }

    // save the current state to a byte array
    public byte[] serializeToByteArray() {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream;
        try {
            outputStream = new ObjectOutputStream(arrayOutputStream);
            
            outputStream.writeObject(sudokuCells);
            outputStream.writeObject(validCellSets);
            outputStream.writeObject(validCellLists);
            outputStream.writeObject((Integer) unknowns);
            
            outputStream.flush();
            outputStream.close();
            
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return arrayOutputStream.toByteArray();
    }

    // restore the current state from a byte array
    public void deserializeFromByteArray(byte[] ba) {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(ba);
        try {
            ObjectInputStream inputStream = new ObjectInputStream(arrayInputStream);
            
            sudokuCells = (Cell[][]) inputStream.readObject();
            validCellSets = (CellSet[]) inputStream.readObject();
            validCellLists = (CellList[]) inputStream.readObject();
            unknowns = (Integer) inputStream.readObject();
            
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    // shallow copy
    public void setBoard(Board o) {
        sudokuCells = o.sudokuCells;
        validCellSets = o.validCellSets;
        validCellLists = o.validCellLists;
        unknowns = o.unknowns;
    }

    public void setValue(int row, int col, int val) {
        if (!sudokuCells[row][col].known) {
            sudokuCells[row][col].setValue(val);
            unknowns--;
        } else {
            sudokuCells[row][col].setValue(val);
        }
    }

    boolean removeCandidate(int row, int col, int val) {
        if (sudokuCells[row][col].removeCandidate(val)) {
            if (sudokuCells[row][col].known) {
                unknowns--;
            }
            return true;
        } else {
            return false;
        }
    }

    public void reset(int row, int col) {
        if (sudokuCells[row][col].known) {
            sudokuCells[row][col].init(row, col);
            unknowns++;
        } else {
            sudokuCells[row][col].init(row, col);
        }
    }

    public CellSet getValidCellSet(int cellSetIdx) {
        return validCellSets[cellSetIdx];
    }

    public CellList getValidCellList(int cellListIdx) {
        return validCellLists[cellListIdx];
    }

    public ArrayList<CellPosition> diff(Board o, boolean compareCandidates) {
        ArrayList<CellPosition> cellList = new ArrayList<CellPosition>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Cell cell = sudokuCells[i][j];
                Cell cell2 = o.sudokuCells[i][j];
                if (cell.known) {
                    if (cell2.known && cell2.value == cell.value) {
                        continue;
                    }
                    cellList.add(new CellPosition(i, j));
                } else {
                    if (cell2.known) {
                        cellList.add(new CellPosition(i, j));
                    } else {
                        // both unknown
                        if (compareCandidates) {
                            if (cell.getCandidates().equals(cell2.getCandidates())) {
                                continue;
                            } else {
                                cellList.add(new CellPosition(i, j));
                            }
                        } else {
                            continue;
                        }
                    }
                }
            }
        }
        return cellList;
    }

	public int getUnknowns() {
		return unknowns;
	}

	public ArrayList<Cell> getDuplicateCells() {
		return duplicateCells;
	}
    
    //////////////////////////////////
    
    
}
