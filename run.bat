@echo off
setlocal

:: PATH DIRECTORIES
set LIB_DIR=lib
set SOURCE_DIR=src
set TARGET_DIR=target
set MAIN_CLASS=Main

echo Running Project...

rem EXECUTION
java -cp "%TARGET_DIR%;%LIB_DIR%\*" %MAIN_CLASS%

pause