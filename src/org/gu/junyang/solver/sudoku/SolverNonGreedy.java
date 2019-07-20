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
 * a non-greedy version of the Solver class
 * so that cross eliminate are not carried out depth first, nor immediately after a value is set.
 * this implementation is slower than the Solver
 * 
 */

package org.gu.junyang.solver.sudoku;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.gu.junyang.utilities.ArrayListUtils;

public class SolverNonGreedy extends Solver {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2857339775918949315L;

    // the list of cells used by cross eliminate
    // this is an optimization so that cross eliminate don't iterate through all known cells
    CellList newlySetCells = null;
    
    ////////////////////////////////////////////////

    ////////////////////////////////////////
    // capability level
    // 1: basic: eliminate candidate (single candidate)
    // 2. single destination (within a cell set)
    // 3. limited candidates
    // 4. limited destination
    // 5. dual row/column limited destination
    // 6. X-wing
    // 7. trial and error (brute force search)
    /////////////////////////////////////////

    /**
     * possible outcome:
     * true: solved, please check multiple solutions flag
     * false: not solved (with limited capability level
     * BoardException is thrown, the puzzle is not solvable;
     * 
     * IMPORTANT: do not reuse a solver class
     *
     * @return
     * @throws BoardException
     */
    @Override
    public boolean Solve() throws BoardException {
        if (cellPositionList==null)
        	makeCellList();	

        addToBoardHistory();

        sanityCheck();
        
        Solve1();

        if (guessed) {
            logger.debug("end search.");
        }

        if (solutions.size() > 1) {
            logger.info("multiple solutions found: " + solutions.size());
        }

        // find the capability level
        if (solved) {
        	for (int i=HIGHEST_CAPABILITY_LEVELS-1; i>=0; i--) {
        		if (capabilitiesUsed[i]) {
        			highestCapabilityUsed = i +1;
        			break;
        		}
        	}
        }
        
        return (solved);
    }
	
    @Override
    protected void Solve1() throws BoardException {

        // cross eliminate candidate propagates itself completely, and has been done before this method
        // so it doesn't appear here
        
        while (unknowns != 0) {
    		
        	if (CrossEliminateAll()) {
        		capabilitiesUsed[0] = true;
            	addToBoardHistory();
        		continue;
        	}
        	
            // if we have reached here, cross eliminate have completed all set cells
            if (newlySetCells==null) {
            	newlySetCells = new CellList();
            }
            
            if (singleDestinationAll()) {
            	capabilitiesUsed[1] = true;
            	addToBoardHistory();
            	continue;
            }

            if ( limitedCandidates2Search() || limitedCandidates3Search() ) {
            	capabilitiesUsed[2] = true;
            	addToBoardHistory();
            	continue;
            }

            if ( limitedDestination() ) {
            	capabilitiesUsed[3] = true;
            	addToBoardHistory();
            	continue;
            }

            if ( dualRowLimitedDestination() || dualColLimitedDestination() ) {
            	capabilitiesUsed[4] = true;
            	addToBoardHistory();
            	continue;
            }
            
            if (xwingRow() || xwingCol()) {
            	capabilitiesUsed[5] = true;
            	addToBoardHistory();
            	continue;
            }
            
            // no progress can be made
            break;
        }
        
        if (unknowns == 0) {
            sanityCheck();

            solved = true;

//				System.out.println();
//				System.out.printf("success - %d guess(es).\n", guessCount);
//				System.out.println(toString());
            logger.info("success - guess count: " + guessCount);

            // print guessStack
//				printGuessStack();

            if (guessed) {
//					System.out.println("guessed values:");
                guessBoards.add(guessBoard.toSimpleString());
            } else {
                guessBoards.add("");
            }

            if (solutions.size() == FIND_ALL_SOLUTIONS_LIMIT) {
                findAllSolutionsLimitExceeded = true;
            } else {
                solutions.add(toString());
                if (recordBoardHistory) {
                    solutionBoardIndexes.add(boardHistory.size() - 1);
                }
                if (recordTranscript) {
                    solutionTranscriptIndexes.add(transcript.size() - 1);
                }
            }
        } else {
        	guessed = true;
        	SolveRecursive();
        	capabilitiesUsed[HIGHEST_CAPABILITY_LEVELS-1] = true;
        	if (!solved) {
        		throw new BoardException("Trial and error can not find a solution.");
        	}
        }
    }

    // for row, col and block
    @Override
    protected boolean CrossEliminate(int row, int col, int iVal) {
        boolean bSet = false;

		// eliminate row
        bSet |= EliminateRow(row, iVal);

        // eliminate col
        bSet |= EliminateCol(col, iVal);

        // eliminate 3x3 cell
        bSet |= EliminateBlock(row, col, iVal);

        if (bSet)
    		patternCellList.add(sudokuCells[row][col]);
        
        return bSet;
    }

    @Override
    public boolean CrossEliminateAll() {
        boolean bSet = false;

        if (newlySetCells!=null) {
        	CellList cellList = new CellList();
        	cellList.addAll(newlySetCells);
        	newlySetCells.clear();
        	while (cellList.size() != 0) {
        		Cell cell = cellList.get(0);
        		bSet |= CrossEliminate(cell.row, cell.col, cell.value);
        		cellList.remove(0);
        		if (bSet)
        			break;
        	}
        	
        	return bSet;
        } 
        
        Board originalBoard = new Board(this);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (originalBoard.sudokuCells[i][j].known) {
                    int iTmp = sudokuCells[i][j].value;

                    bSet |= CrossEliminate(i, j, iTmp);
                    
                    if (bSet)
                    	return bSet;
                }
            }
        }

        return bSet;
    }

    // single destination search for a given cell set
    @Override
    protected boolean singleDestination(int cellSetIdx) {
        boolean bSet = false;

        // for each number, count the cells that may contain the number.
        int count[] = new int[9];

        // a mapping from number to cell
        Cell cLastCell[] = new Cell[9];

        for (Cell pCell : validCellSets[cellSetIdx]) {
            if (!pCell.known) {
                for (Integer value : pCell.candidates) {
                    count[value - 1]++;
                    cLastCell[value - 1] = pCell;
                }
            } else {
                count[pCell.value - 1]++;
                cLastCell[pCell.value - 1] = pCell;
            }
        }

        for (int value = 0; value < 9; value++) {
            if (count[value] == 1) {

                // during the call to CrossEliminate(...), the cell may be set,
                // so check it here

                // also, the known value is also counted
                if (cLastCell[value].known) {
                    continue;
                }

                int iRow = cLastCell[value].row;
                int iCol = cLastCell[value].col;

                setValue(iRow, iCol, value + 1);
                if (newlySetCells!=null)
                	newlySetCells.add(sudokuCells[iRow][iCol]);
                
                bSet = true;
                
                if (recordTranscript) {
                	transcriptTemp.add("SingleDestination (2): " + toPositionString(iRow, iCol) + "=" + (value + 1));
                	actions.add(Action.SET_VALUE);
                }

                patternCellList.addAll(validCellSets[cellSetIdx]);
                
                return bSet;

//				sanityCheck();
            }
        }

        return bSet;
    }

    // search each row, col and 3x3 block to find
    // a number that has only one possible cell
    // and set that cell
    @Override
    public boolean singleDestinationAll() {
        boolean bSet = false;

        // search all sets
//        for (int i = 0; i < TOTAL_CELL_SETS; i++) {
        for (int i = TOTAL_CELL_SETS-1; i >=0; i--) {		// give priority to block sets
            bSet |= singleDestination(i);
            
            if (bSet)
            	return bSet;
        }

        return bSet;
    }

    // return true if candidates eliminated
    @Override
    protected boolean EliminateCandidates(int row, int col, Candidates candidates) {
        boolean bSet = false;

        if (sudokuCells[row][col].known) {
            return false;
        }

        for (Integer it : candidates) {
            bSet |= removeCandidate(row, col, it);
        }

        if (sudokuCells[row][col].known) {
        	
        	if (newlySetCells!=null)
        		newlySetCells.add(sudokuCells[row][col]);
        	
            int iTmp = sudokuCells[row][col].value;

            if (recordTranscript) {
            	transcriptTemp.add("EliminateCandidates (1): " + toPositionString(row, col) + "=" + iTmp);
            	actions.add(Action.SET_VALUE);
            }
//            addToBoardHistory();

            if (debug) {
                logger.debug(toString());
            }
        }

        return bSet;
    }

    // for within a given set, test whether cell_list meets limitedCandidates,
    // and if so, eliminate the corresponding candidates within the set
    @Override
    protected boolean limitedCandidatesTest(int cellSetIndex, ArrayList<Cell> cell_list) {
        boolean bSet = false;

        // important: cell_list must not contain known cell

        Candidates cCandTmp = new Candidates();
        for (Cell it : cell_list) {
//			System.out.printf("add candidates from %s\n", it.toPositionString());
            if (it.known) {
                throw new Error("error: cell is known.");
            }
            cCandTmp.addAll(it.candidates);
        }

        if (cCandTmp.size() > cell_list.size()) {
            return false;
        }

        int cellCnt = cell_list.size();
    	patternCellList.addAll(cell_list);
//    	System.out.println("pattern cell list: " + patternCellList.toString());
    	
        for (Cell it : validCellSets[cellSetIndex]) {
            if (cell_list.contains(it)) {
                continue;
            }

            if (it.known) {
                continue;
            }
            
            boolean transcriptAdded = false;
            
        	if (recordRemoveCandidate && recordTranscript) {
        		transcriptTemp.add("LimitedCandidates (3): " + it.toPositionString() + " remove " + cCandTmp.toString());
        		actions.add(Action.REMOVE_CANDIDATE);
        		transcriptAdded = true;
        	}
        	
            boolean changed = EliminateCandidates(it.row, it.col, cCandTmp);
           
            if (!changed && transcriptAdded) {
            	transcriptTemp.remove(transcriptTemp.size()-1);
            	actions.remove(actions.size()-1);
            }

            bSet |= changed;
        }
        
        if (!bSet) {
        	ArrayListUtils.removeTailLength(patternCellList, cellCnt);
        }
        	
        return bSet;
    }

    // limited candidates
    // if 2 cells of a set contain only 2 candidates in total, than these candidates can not
    // appear in other cells within the set
    // same applies to 3 cells;
    @Override
    protected boolean limitedCandidates2Search() {
        boolean bSet = false;

        CellList cCellPair = new CellList();

        for (int i = 0; i < TOTAL_CELL_SETS; i++) {
            CellList cellList = validCellLists[i];

            for (int j = 0; j < 9; j++) {
                Cell it1Cell = cellList.get(j);

                if (it1Cell.known) {
                    continue;
                }

                if (it1Cell.candidates.size() > 2) {
                    continue;
                }

                cCellPair.add(it1Cell);

                for (int k = j + 1; k < 9; k++) {
                    Cell it2Cell = cellList.get(k);

                    if (it2Cell.known) {
                        continue;
                    }

                    if (it2Cell.candidates.size() > 2) {
                        continue;
                    }

                    cCellPair.add(it2Cell);

                    bSet |= limitedCandidatesTest(i, cCellPair);

                    if (bSet)
                    	return bSet;
                    
                    cCellPair.remove(1);

                    // it1Cell can be set during the operation
                    if (it1Cell.known) {
                        break;
                    }
                }

                cCellPair.remove(0);
            }
        }
        return bSet;
    }

    @Override
    protected boolean limitedCandidates3Search() {
        boolean bSet = false;

        CellList cThreeCell = new CellList();

        for (int i = 0; i < TOTAL_CELL_SETS; i++) {
            CellList cellList = validCellLists[i];

            for (int j = 0; j < 9; j++) {
                Cell it1Cell = cellList.get(j);

                if (it1Cell.known) {
                    continue;
                }

                if (it1Cell.candidates.size() > 3) {
                    continue;
                }

                cThreeCell.add(it1Cell);

                for (int k = j + 1; k < 9; k++) {

                    Cell it2Cell = cellList.get(k);

                    if (it2Cell.known) {
                        continue;
                    }

                    if (it2Cell.candidates.size() > 3) {
                        continue;
                    }

                    cThreeCell.add(it2Cell);

                    for (int l = k + 1; l < 9; l++) {
                        Cell it3Cell = cellList.get(l);

                        if (it3Cell.known) {
                            continue;
                        }

                        if (it3Cell.candidates.size() > 3) {
                            continue;
                        }

                        cThreeCell.add(it3Cell);

                        bSet |= limitedCandidatesTest(i, cThreeCell);
                        
                        if (bSet)
                        	return bSet;
                        
                        cThreeCell.remove(2);

                        // it1Cell or it2Cell can be set during the operation
                        if (it2Cell.known || it1Cell.known) {
                            break;
                        }
                    }

                    cThreeCell.remove(1);

                    if (it1Cell.known) {
                        break;
                    }
                }

                cThreeCell.remove(0);
            }
        }
        return bSet;
    }

    // for a cell set, if a candidate only appear in 2 cells, then all other sets that contain these 2 cells
    // can not have that candidate in all other cells
    // this can be generalized to 3 cells also
    @Override
    protected boolean limitedDestination() {
        boolean bSet = false;

        // for all 3x3 cell sets
        for (int i = 18; i < TOTAL_CELL_SETS; i++) {
            // create reverse map

            // a map of candidate to cells
            CellSet cCandToCells[] = new CellSet[10];
            for (int j = 0; j < 10; j++) {
                cCandToCells[j] = new CellSet();
            }

            for (Cell cell : validCellSets[i]) {
                if (cell.known) {
                    continue;
                }

                for (Integer candidate : cell.candidates) {
                    cCandToCells[candidate].add(cell);
                }
            }

            // check number of cells for each candidate
            for (int j = 1; j <= 9; j++) {
                if (cCandToCells[j].size() == 2 || cCandToCells[j].size() == 3) {
                    // check whether the cells are on the same row or col
                    Iterator<Cell> it = cCandToCells[j].iterator();

                    // the row and col of the first cell
                    Cell cell = it.next();
                    int row = cell.row;
                    boolean rowSame = true;
                    int col = cell.col;
                    boolean colSame = true;

                    // check the rest of the cells
                    while (it.hasNext()) {
                        cell = it.next();
                        if (cell.row != row) {
                            rowSame = false;
                        }

                        if (cell.col != col) {
                            colSame = false;
                        }

                        if (!rowSame && !colSame) {
                            break;
                        }
                    }

                    // there is only one row set or col set that contains all the cells
                    int k;
                    if (rowSame) {
                        k = row;
                    } else if (colSame) {
                        k = 9 + col;
                    } else {
                        continue;
                    }

                    int cellCnt = cCandToCells[j].size();
               		patternCellList.addAll(cCandToCells[j]);

               		// for all other sets

                    // eliminate candidates
                    for (Cell cell2 : validCellSets[k]) {
                        if (cell2.known || cCandToCells[j].contains(cell2)) {
                            continue;
                        }

                        bSet |= RemoveCandidateWithCheck(cell2.row, cell2.col, j, "LimitedDestination (4)");
                    }
                    
                    if (bSet) {
                    	return bSet;
                    } else {
                    	ArrayListUtils.removeTailLength(patternCellList, cellCnt);
                    }
                    
                }
            }
        }

        return bSet;
    }

    // for 2 square sets in a row,
    // if a candidate only appear on any 2 common rows of the 2 sets,
    // then on the 3rd square set of the row, the candidate can be eliminated from the 2 common rows.
    // same applies to columns.
    @Override
    protected boolean dualRowLimitedDestination() {
        boolean bSet = false;

        // do all the block rows
        Candidates cRows1 = new Candidates();
        Candidates cRows2 = new Candidates();

        Iterator<Integer> pRow;

        for (int i = 18; i < TOTAL_CELL_SETS; i += 3) // for all the values
        {
            for (int iVal = 1; iVal <= 9; iVal++) // for each block pair
            {
                for (int j = i; j < i + 3; j++) {
                    for (int k = j + 1; k < i + 3; k++) {
                        if (validCellSets[j].CountCandidateRows(iVal, cRows1) == 2
                                && validCellSets[k].CountCandidateRows(iVal, cRows2) == 2) {
                            // see if they are on the same 2 rows
                            int iRow11, iRow12, iRow21, iRow22;
                            pRow = cRows1.iterator();
                            iRow11 = pRow.next();
                            iRow12 = pRow.next();

                            pRow = cRows2.iterator();
                            iRow21 = pRow.next();
                            iRow22 = pRow.next();

                            if ((iRow11 == iRow21 && iRow12 == iRow22)
                                    || (iRow11 == iRow22 && iRow12 == iRow21)) {
                                // if so, eliminate the value from 2 rows of the 3rd block
                                int l;
                                for (l = i; l < i + 3; l++) {
                                    if (l != j && l != k) {
                                        break;
                                    }
                                }

                                // add to pattern cell list
                                int cellCnt=0;
                            	for (Cell cell : validCellLists[j])
                            		if (cell.row == iRow11 || cell.row == iRow12) {
                            			patternCellList.add(cell);
                            			cellCnt++;
                            		}
                            	for (Cell cell : validCellLists[k])
                            		if (cell.row == iRow21 || cell.row == iRow22) {
                            			patternCellList.add(cell);
                            			cellCnt++;
                            		}
                            	
                            	for (Cell pCell : validCellSets[l]) {
                                    if (!pCell.known
                                            && (pCell.row == iRow11 || pCell.row == iRow12)) {
                                        bSet |= RemoveCandidateWithCheck(pCell.row, pCell.col, iVal, "DualRowLimitedDestination (5)");
                                    }
                                }
                                
                                if (bSet) {
                                	return bSet;
                                } else {
                                	ArrayListUtils.removeTailLength(patternCellList, cellCnt);
                                }
                                
                            }
                        }
                    }
                }
            }
        }
        return bSet;
    }

    @Override
    protected boolean dualColLimitedDestination() {
        boolean bSet = false;

        // do all the block cols
        Candidates cCols1 = new Candidates();
        Candidates cCols2 = new Candidates();

        Iterator<Integer> pCol;

        for (int i = 18; i < 21; i++) // for all the values
        {
            for (int iVal = 1; iVal <= 9; iVal++) // for each block pair
            {
                for (int j = i; j < i + 9; j += 3) {
                    for (int k = j + 3; k < i + 9; k += 3) {
                        if (validCellSets[j].CountCandidateCols(iVal, cCols1) == 2
                                && validCellSets[k].CountCandidateCols(iVal, cCols2) == 2) {
                            // see if they are on the same 2 cols
                            int iCol11, iCol12, iCol21, iCol22;
                            pCol = cCols1.iterator();
                            iCol11 = pCol.next();
                            iCol12 = pCol.next();

                            pCol = cCols2.iterator();
                            iCol21 = pCol.next();
                            iCol22 = pCol.next();

                            if ((iCol11 == iCol21 && iCol12 == iCol22)
                                    || (iCol11 == iCol22 && iCol12 == iCol21)) {
                                // if so, eliminate the value from 2 rows of the 3rd block
                                int l;
                                for (l = i; l < i + 9; l += 3) {
                                    if (l != j && l != k) {
                                        break;
                                    }
                                }

                                // add to pattern
                                int cellCnt = 0;
                            	for (Cell cell : validCellLists[j])
                            		if (cell.col == iCol11 || cell.col == iCol12) {
                            			patternCellList.add(cell);
                            			cellCnt++;
                            		}
                            	for (Cell cell : validCellLists[k])
                            		if (cell.col == iCol21 || cell.col == iCol22) {
                            			patternCellList.add(cell);
                            			cellCnt++;
                            		}

                            	for (Cell pCell : validCellSets[l]) {
                                    if (!pCell.known
                                            && (pCell.col == iCol11 || pCell.col == iCol12)) {
                                        bSet |= RemoveCandidateWithCheck(pCell.row, pCell.col, iVal, "DualColLimitedDestination (5)");
                                    }
                                }
                                
                                if (bSet) {
                                	return bSet;
                                } else {
                                	ArrayListUtils.removeTailLength(patternCellList, cellCnt);                                	
                                }
                            }
                        }
                    }
                }
            }
        }

        return bSet;
    }

    @Override
    protected boolean RemoveCandidateWithCheck(int row, int col, int val, String prompt) {
        if (!removeCandidate(row, col, val)) {
            return false;
        }

        if (recordRemoveCandidate && recordTranscript) {
        	transcriptTemp.add(prompt + ": " + toPositionString(row, col) + " remove " + val);
            actions.add(Action.REMOVE_CANDIDATE);
        }

        if (sudokuCells[row][col].known) {

        	if (newlySetCells!=null)
        		newlySetCells.add(sudokuCells[row][col]);
        	
            int iTmp = sudokuCells[row][col].value;

            if (recordTranscript) {
            	transcriptTemp.add(prompt + ": " + toPositionString(row, col) + "=" + iTmp);
            actions.add(Action.SET_VALUE);
            }

            if (debug) {
                logger.debug(toString());
            }

        }

        return true;
    }

    @Override
    protected void SolveRecursive() {
        // forward to the first unknow cell
        boolean foundUnknown = false;
        int i = 0, j = 0;
//		for (i=0; i<9; i++)
//		{
//			for (j=0; j<9; j++)
//			{
//				// only need to try the first unknown cell
//				// the rest are tried by recursive calls
//
//				if ( ! cSudokuCells[i][j].known ) {
//					foundUnknown = true;
//					break;
//				}
//			}
//			if (foundUnknown)
//				break;
//		}

        int savedIdx = cellIdx;
        while (cellIdx < cellPositionList.size()) {
            CellPosition locaterCell = cellPositionList.get(cellIdx++);
            i = locaterCell.row;
            j = locaterCell.col;
            if (!sudokuCells[i][j].known) {
                foundUnknown = true;
                break;
            }
        }

        if (!foundUnknown) {
            cellIdx = savedIdx;
            return;
        }

        // save the current state
        byte[] ba = serializeToByteArray();

        ArrayList<Integer> candidates = makeCandidateList(i, j);

        for (Integer candidate : candidates) {
//			cSudokuCells[i][j].setValue( candidate );
            setValue(i, j, candidate);

            if (recordTranscript) {
            	transcriptTemp.add("TrialAndError (7): trying " + toPositionString(i, j) + "=" + candidate);
            	actions.add(Action.SET_VALUE);
            }
//            addToBoardHistory();

            guessCount++;
            guessStack.push(new SetValueAction(i, j, candidate));
            guessBoard.setValue(i, j, candidate);

            newlySetCells.add(sudokuCells[i][j]);
            
            if (debug) {
                logger.debug(toString());
            }

            try {
                sanityCheck();

                Solve1();

                if (solved) {
                    if (solutions.size() > 1 && !findAllSolutions) {
                        return;
                    }

                    if (noMultipleSolutionCheck) {
                        return;
                    }

                    if (findAllSolutionsLimitExceeded) {
                        return;
                    }
                }

            } catch (BoardException e) {
                // do nothing
            }
            
            newlySetCells.clear();
            
            //	      printf("failed.\n");
            transcriptTemp.add("TrialAndError: back out " + toPositionString(i, j) + "=" + candidate);
            actions.add(Action.SET_VALUE);

            guessCount--;
            guessStack.pop();
            guessBoard.reset(i, j);

            // restore original board
            deserializeFromByteArray(ba);

            addToBoardHistory();

            if (debug) {
                logger.debug(toString());
            }

        }

        cellIdx = savedIdx;
    }

    /**
     * the xwing - 2 rows
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
	protected boolean xwingRow() {
    	// for each row, construct candidates to columns map
    	// idx1: row
    	// idx2: candidate
    	Set<Integer> [][] colMaps = new Set[9][9];
    	for (int row=0; row<9; row++) {
    		// init data structures
    		for (int c=0; c<9; c++)
    			colMaps[row][c] = new HashSet<Integer>();
    		
    		for (int col=0; col<9; col++) {
    			for (int candidate : sudokuCells[row][col].getCandidates()) {
    				colMaps[row][candidate-1].add(col);
    			}
    		}
    	}

    	boolean bset=false;
    	
    	// compare every 2 rows
    	for (int row1=0; row1<8; row1++) {
    		for (int c=0; c<9; c++) {
    			if (colMaps[row1][c].size() !=2)
    				continue;

    			for (int row2=row1+1; row2<9; row2++) {
        			if (colMaps[row2][c].size() !=2)
        				continue;
    				
        			if (colMaps[row1][c].equals(colMaps[row2][c])) {
        				// match

        				// add to pattern cell list
    					Iterator<Integer> it = colMaps[row1][c].iterator();
    					while (it.hasNext()) {
    						int col = it.next();
    						patternCellList.add(sudokuCells[row1][col]);
    						patternCellList.add(sudokuCells[row2][col]);
    					}
//       				System.out.println("pattern cell list: " + patternCellList.toPositionString());

    					// remove candidates from the two columns
        				for (int col : colMaps[row1][c])
        					bset |= xwingRowRemoveCandidateWithCheck(row1, row2, col, c+1);

        				if (bset) { 
        					return bset;
        				} else {
        					// back out pattern cell list
        					ArrayListUtils.removeTailLength(patternCellList, 4);
        				}
        				
        			}
        		}	
    		}
    	}
    	
    	return bset;
    }
    
    /**
     * the xwing - 2 rows
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
	protected boolean xwingCol() {
    	// for each col, construct candidates to columns map
    	// idx1: col
    	// idx2: candidate
    	Set<Integer> [][] rowMaps = new Set[9][9];
    	for (int col=0; col<9; col++) {
    		// init data structures
    		for (int c=0; c<9; c++)
    			rowMaps[col][c] = new HashSet<Integer>();
    		
    		for (int row=0; row<9; row++) {
    			for (int candidate : sudokuCells[row][col].getCandidates()) {
    				rowMaps[col][candidate-1].add(row);
    			}
    		}
    	}

    	boolean bset=false;
    	
    	// compare every 2 rows
    	for (int col1=0; col1<8; col1++) {
    		for (int c=0; c<9; c++) {
    			if (rowMaps[col1][c].size() !=2)
    				continue;

    			for (int col2=col1+1; col2<9; col2++) {
        			if (rowMaps[col2][c].size() !=2)
        				continue;
    				
        			if (rowMaps[col1][c].equals(rowMaps[col2][c])) {
        				// match

        				// add to pattern cell list
    					Iterator<Integer> it = rowMaps[col1][c].iterator();
    					while (it.hasNext()) {
    						int row = it.next();
        					patternCellList.add(sudokuCells[row][col1]);
        					patternCellList.add(sudokuCells[row][col2]);
    					}
//    		        	System.out.println("pattern cell list: " + patternCellList.toPositionString());

    					// remove candidates from the two columns
        				for (int row : rowMaps[col1][c])
        					bset |= xwingColRemoveCandidateWithCheck(col1, col2, row, c+1);

        				if (bset) {
        					return bset;
        				} else {
        					// back out pattern cell list
        					ArrayListUtils.removeTailLength(patternCellList, 4);
        				}
        				
        			}
        		}	
    		}
    	}
    	
    	return bset;
    }
    
    
}
