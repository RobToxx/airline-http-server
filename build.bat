@echo off
setlocal

:: PATH DIRECTORIES
set LIB_DIR=lib
set SOURCE_DIR=src
set TARGET_DIR=target
set MAIN_CLASS=Main

echo Building Project...

javac -cp "%SOURCE_DIR%;%LIB_DIR%\*" -d %TARGET_DIR% "%SOURCE_DIR%\%MAIN_CLASS%.java

:: RESULT
if %errorlevel% equ 0 (
    echo Compilation succeeded.
) else (
    echo Compiliaton failed.
    exit /b 1
)