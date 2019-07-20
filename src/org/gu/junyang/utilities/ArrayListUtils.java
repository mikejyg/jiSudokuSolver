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

package org.gu.junyang.utilities;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 */
public class ArrayListUtils {

    // remove all elements starting from a specified index (including the specified index)
    static public <T> void removeTailStartingAt(ArrayList<T> arrayList, int tailStartIdx) {
        for (int i = arrayList.size() - 1; i >= tailStartIdx; i--) {
            arrayList.remove(i);
        }
    }

    // remove n elements from the end
    static public <T> void removeTailLength(ArrayList<T> arrayList, int length) {
    	int idx = arrayList.size()-1;
        for (int i = 0; i < length; i++) {
            arrayList.remove(idx--);
            if (idx<0)
            	break;
        }
    }

    // randomize a arrayList
    public static <T> void randomize(ArrayList<T> list, Random random) {
        int len = list.size();
        for (int i = 0; i < len; i++) {
            int idx1 = random.nextInt(len);
            int idx2 = random.nextInt(len);
            T c = list.get(idx1);
            list.set(idx1, list.get(idx2));
            list.set(idx2, c);
        }
    }

    
}
