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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Scanner;

/**
 * a puzzle only contains know values, not candidate lists
 * 
 */
public class Puzzle implements Transferable {
    // 0 means not set
    int values[][] = new int[9][9];

    public static final String NL = System.getProperty("line.separator");
    
    public static final DataFlavor dataFlavor = DataFlavor.stringFlavor;

    //////////////////////////////////////////
    
    public Puzzle() {
    }

    public Puzzle(String data) throws UnexpectedEndOfInputException {
        if (! read(new Scanner(data)) )
        	throw new UnexpectedEndOfInputException();
    }
    
    // deep copy constructor
    public Puzzle(Puzzle puzzle) {
    	for (int i=0; i<9; i++)
    		for (int j=0; j<9; j++)
    			values[i][j] = puzzle.values[i][j];
	}

    //////////////////////////////////////////

    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor[] dataFlavors = {dataFlavor};
        return dataFlavors;
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (flavor.match(dataFlavor)) {
            return true;
        } else {
            return false;
        }
//        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.match(dataFlavor)) {
            return toString();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public class UnexpectedEndOfInputException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 5484073114199596082L;
    }

    public boolean isSet(int row, int col) {
        return values[row][col] != 0;
    }

    public Integer getValue(int row, int col) {
        if (values[row][col] == 0) {
            return null;
        } else {
            return values[row][col];
        }
    }

    public void set(int row, int col, int value) {
        this.values[row][col] = value;
    }

    public void reset(int row, int col) {
        this.values[row][col] = 0;
    }

    // read in the puzzle
    // return false if no data read
    // throw exception if data ends in the middle of input
    public boolean read(Scanner scanner) throws UnexpectedEndOfInputException {
        String readLine = "";
        int charIdx = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                while (true) {

                    if (charIdx >= readLine.length()) {
                        if (!scanner.hasNext()) {
                            if (i != 0 || j != 0) {
                                throw new UnexpectedEndOfInputException();
//                                System.out.println("unexpected end of input.");
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
                        reset(i, j);
                        break;
                    }

                    int iTmp;
                    try {
                        iTmp = Integer.parseInt(token);
                        set(i, j, iTmp);
                        break;
                    } catch (NumberFormatException e) {
                        // ignore the token
                        continue;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        String str = "";
        for (int i = 0; i < 9; i++) {

            boolean first = true;

            for (int j = 0; j < 9; j++) {
                if (!first) {
                    str += " ";
                }

                if (j % 3 == 0 && !first) {
                    str += " ";
                }

                if (isSet(i, j)) {
                    str += Integer.toString(values[i][j]);
                } else {
                    str += ".";
                }

                first = false;
            }

            str += NL;

            if (i % 3 == 2 && i != 8) {
                str += NL;
            }

        }
        return str;
    }

    // combine the known values from another puzzle
    // only non conflict values are added
    public void combine(Puzzle puzzle) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!isSet(i, j) && puzzle.isSet(i, j)) {
                    set(i, j, puzzle.getValue(i, j));
                }
            }
        }
    }
    
}
