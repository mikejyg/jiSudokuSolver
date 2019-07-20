rem a single puzzle
java -jar ..\jisudokusolver.jar --showprogress ../sample_puzzles/single

rem multiple puzzles
java -jar ..\jisudokusolver.jar --batch ../sample_puzzles/external

rem multiple puzzles, showing progress
java -jar ..\jisudokusolver.jar --batch --showprogress ../sample_puzzles/hard

rem multiple medium puzzles
java -jar ..\jisudokusolver.jar --batch -c --noguess < ../sample_puzzles/medium

rem bad puzzles
java -jar ..\jisudokusolver.jar --batch ../sample_puzzles/bad_puzzles

rem solve some hard puzzles from Wiki page

java -jar ..\jisudokusolver.jar --batch ../sample_puzzles/wikiHard.txt
