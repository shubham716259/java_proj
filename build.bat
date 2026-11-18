@echo off
if not exist "bin" mkdir bin
javac -encoding UTF-8 -cp "lib/mysql-connector-j.jar;src" -d bin src/com/notes/model/*.java src/com/notes/dao/*.java src/com/notes/helper/*.java src/com/notes/web/*.java src/com/notes/Main.java
if %ERRORLEVEL% EQU 0 (
    echo Compilation successful. Classes saved to bin.
) else (
    echo Compilation failed.
)
pause
