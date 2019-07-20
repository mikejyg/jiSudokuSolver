jiSudokuSolver is a masterSudoku program.
It solves any puzzle, good or bad, even an empty one (no cell known), usually in an instant, and it shows you how, step by step.
It also let you play puzzles with assist, generates, fixes, tests, reduces... 

Start GUI:
double click or run jisudokusolver.bat.

copy and paste in GUI:
You can copy and paste puzzles between tabs, and to and from other programs in text form.

sample use cases (CLI/command line interface):

Solve a puzzle: just give it the puzzle.

Fix a puzzle: if a puzzle has multiple solutions, it will provide set of values to make it unique solution.

Generate a puzzle: it can generate a random one, one with specified difficulty, or one with a specified ID.

Reduce a puzzle: try to remove unnecessary values from the puzzle (makes it harder).

Puzzle Design tool: Design the puzzle the way you want; ask the program to solve the puzzle, and it will tell you whether there are too many or too few values set. Once the puzzle is good, ask the program to reduce it to make it harder (without changing the solution). 

examples:

See sample_scripts/ for usage examples. 

----

jiSudokuSolver is the Java version of the c++ iSudokuSolver. The c++ iSudokuSolver is no longer being maintained.

It is a command line program that reads in one or more (in batch mode) puzzles.

Use -h for help.

See the sample_scripts directory for usage samples.

The format of puzzle this program expects are numbers separated by whitespace characters (space, tab, end-of-line...).

The number of whitespace characters do not matter.

The order is from left to right, from top to bottom.
 
If a cell is known, it should be a non-zero number, otherwise it should be 0. 

---- from the c++ iSudokuSolver

iSudokuSolver is an intelligent Sudoku puzzle solver

It is intlligent, because it uses several logical deduction methods, just like a person would use, in addition to a DFS algorithm to solve Sudoku puzzles. 

The solver uses 5 logical deduction methods. It will try to exhaust all available logical deductions, before resorting to brute force search. If the "--showprogress" switch is used, it will show which method it used for each determination, and at the end, it will give a "level" figure to indicate the difficulty of the puzzle.

It is written in standard c++.

