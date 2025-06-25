@echo off
setlocal


set "OUTPUT_DIR=runtime"

echo [INFO] Building custom Java runtime...


jlink ^
  --module-path "C:\Program Files\BellSoft\LibericaJDK-24-Full\jmods;C:\javafx-sdk-21.0.2\jmods" ^
  --add-modules java.base,java.logging,java.desktop,jdk.localedata,javafx.controls,javafx.graphics,javafx.fxml ^
  --output "%OUTPUT_DIR%" ^
  --strip-debug ^
  --no-header-files ^
  --no-man-pages ^
  --compress=2

echo.
powershell -Command "Write-Host 'Runtime successfully created at: %CD%\%OUTPUT_DIR%' -ForegroundColor Green"
pause