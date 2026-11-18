@echo off
title Building UniVibe - College Event and Ticket Management System
echo ======================================================================
echo   COMPILING UNIVIBE CORE JAVA APPLICATION
echo ======================================================================

if not exist bin mkdir bin

echo Compiling Java source files...
javac -encoding UTF-8 -cp "lib/*" -d bin src\com\univibe\interfaces\*.java src\com\univibe\exception\*.java src\com\univibe\model\*.java src\com\univibe\util\*.java src\com\univibe\database\*.java src\com\univibe\dao\*.java src\com\univibe\service\*.java src\com\univibe\ui\*.java src\com\univibe\test\*.java src\com\univibe\Main.java

if %ERRORLEVEL% EQU 0 (
    echo [SUCCESS] Compilation completed successfully!
    echo Output directory: bin\
) else (
    echo [ERROR] Compilation failed. Please inspect the error logs above.
)
pause
