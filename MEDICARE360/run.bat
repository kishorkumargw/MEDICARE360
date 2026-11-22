@echo off
rem Compiles and starts MEDICARE360 (Windows).
cd /d "%~dp0"

if not exist lib\sqlite-jdbc*.jar (
    echo SQLite driver not found in lib\ - downloading it now...
    call setup-driver.bat
    if errorlevel 1 exit /b 1
)

if not exist out mkdir out
javac --release 17 -encoding UTF-8 -d out -sourcepath src src\com\medicare360\Main.java
if errorlevel 1 (
    echo Compilation failed.
    pause
    exit /b 1
)
java -cp "out;lib\*" com.medicare360.Main
