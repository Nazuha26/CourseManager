@echo off
setlocal


set "OUTPUT_DIR=runtime"

echo [INFO] Building custom Java runtime...


jlink ^
  --module-path "C:\Program Files\BellSoft\LibericaJDK-24-Full\jmods;C:\javafx-sdk-21.0.2\jmods" ^
  --add-modules java.base,java.desktop,java.logging,java.scripting,java.xml,java.xml.crypto,java.datatransfer,jdk.unsupported,jdk.localedata,javafx.base,javafx.controls,javafx.fxml,javafx.graphics ^
  --output "%OUTPUT_DIR%" ^
  --strip-debug ^
  --no-header-files ^
  --no-man-pages ^
  --compress=2

echo.
powershell -Command "Write-Host 'Runtime successfully created at: %CD%\%OUTPUT_DIR%' -ForegroundColor Green"
pause


