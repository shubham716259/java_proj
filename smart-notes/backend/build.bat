@echo off
echo Compiling Smart Notes Backend...
if not exist "bin" mkdir bin
javac -encoding UTF-8 -cp "lib/mysql-connector-j.jar;lib/gson.jar;src" -d bin src/model/*.java src/util/*.java src/dao/*.java src/service/*.java src/handler/*.java src/Main.java
if %ERRORLEVEL% EQU 0 (
    echo Compilation successful! Classes stored in bin/
) else (
    echo Compilation failed.
)
pause
