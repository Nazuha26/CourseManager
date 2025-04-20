@echo off
setlocal

call maven-build.bat
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Maven build script failed. Aborting installer build.
    pause
    exit /b
)

REM === ASK FOR VERSION === %NAME%-jar-with-dependencies.jar
set /p VERSION=Enter version number (e.g. 1.0.2): 
set NAME=CourseManagerFX
set MAIN_JAR=%NAME%.jar	
set MAIN_CLASS=com.coursemanagerfx.Launcher
set ICON_PATH=D:\Projects\IntelliJ_IDEA\CourseManagerFX\src\main\resources\com\coursemanagerfx\ui\AppIcon.ico

set INPUT_DIR=build_temp
set OUTPUT_DIR=VERSIONS\cmanfx-%VERSION%
set FINAL_NAME=%NAME%-%VERSION%

REM === TERMINATE old process if running ===
echo Terminating old instances...
taskkill /F /IM %FINAL_NAME%.exe >nul 2>&1

REM === PREPARE TEMP FOLDER ===
echo Preparing temp build folder...
rmdir /S /Q %INPUT_DIR% >nul 2>&1
mkdir %INPUT_DIR%

REM === COPY JAR ===
copy target\%MAIN_JAR% %INPUT_DIR%\

REM === CREATE OUTPUT FOLDER (don't delete old ones) ===
mkdir %OUTPUT_DIR% >nul 2>&1

REM === BUILD MSI INSTALLER ===
echo Building MSI installer for version %VERSION%...
jpackage ^
  --type msi ^
  --input %INPUT_DIR% ^
  --dest %OUTPUT_DIR% ^
  --name %NAME% ^
  --main-jar %MAIN_JAR% ^
  --main-class %MAIN_CLASS% ^
  --app-version %VERSION% ^
  --vendor "Ha3yxa" ^
  --description "CourseManagerFX %VERSION% - course management application" ^
  --icon "%ICON_PATH%" ^
  --runtime-image runtime ^
  --win-menu ^
  --win-shortcut ^
  --win-dir-chooser ^
  --java-options "-Xmx512m"


REM === CLEANUP TEMP FILES ===
rmdir /S /Q %INPUT_DIR%

REM === DONE ===

echo.
echo Done! MSI for version %VERSION% is located in: %OUTPUT_DIR%
powershell -Command "Write-Host '[DONE] MSI installer created.' -ForegroundColor Green"
explorer %OUTPUT_DIR%
echo.

pause
