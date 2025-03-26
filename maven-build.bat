@echo off
setlocal

REM === BUILD PROJECT WITH MAVEN ===
echo Building project with Maven...
cd /d %~dp0
mvn clean package
if %ERRORLEVEL% NEQ 0 (
	echo.
	echo [ERROR] Maven build failed. Aborting.
	powershell -Command "Write-Host '[ERROR] Maven build failed.' -ForegroundColor Red"
	echo.
	pause
	exit /b
)

echo.
powershell -Command "Write-Host '[OK] Maven build successful.' -ForegroundColor Green"
echo.