@echo off
setlocal

set "JDK_MODS=C:\Program Files\Java\jdk-24\jmods"
set "JAVAFX_MODS=C:\Program Files\Java\javafx-sdk-24\jmods"
set "OUTPUT_DIR=runtime"

echo [INFO] Building custom Java runtime...

jlink ^
  --module-path "%JDK_MODS%;%JAVAFX_MODS%" ^
  --add-modules java.base,java.desktop,java.logging,java.xml,javafx.base,javafx.controls,javafx.fxml,javafx.graphics ^
  --output "%OUTPUT_DIR%" ^
  --strip-debug ^
  --no-header-files ^
  --no-man-pages ^
  --compress=2

echo.
powershell -Command "Write-Host 'Runtime successfully created at: %CD%\%OUTPUT_DIR%' -ForegroundColor Green"
pause