@echo off
rem Downloads the SQLite JDBC driver (and its small logging helper) into the lib folder.
rem You only need to run this once. Requires an internet connection.
cd /d "%~dp0"
if not exist lib mkdir lib
set BASE=https://repo1.maven.org/maven2
powershell -NoProfile -ExecutionPolicy Bypass -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri '%BASE%/org/xerial/sqlite-jdbc/3.45.3.0/sqlite-jdbc-3.45.3.0.jar' -OutFile 'lib\sqlite-jdbc-3.45.3.0.jar'; Invoke-WebRequest -Uri '%BASE%/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar' -OutFile 'lib\slf4j-api-1.7.36.jar'"
if errorlevel 1 (
    echo Download failed. Please download the jars manually - see README.md.
    exit /b 1
)
echo Done. Driver jars are in the lib folder.
