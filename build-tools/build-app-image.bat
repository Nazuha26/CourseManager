@echo off
setlocal EnableExtensions

for %%I in ("%~dp0..") do set "PROJECT_DIR=%%~fI"

set "APP_NAME=CourseManagerFX"
set "MAIN_JAR=CourseManagerFX.jar"
set "MAIN_CLASS=com.coursemanagerfx.Bootstrap"
set "ICON_PATH=%PROJECT_DIR%\src\main\resources\com\coursemanagerfx\ui\cmfx_icon.ico"
set "SPLASH_PATH=%PROJECT_DIR%\src\main\resources\com\coursemanagerfx\ui\splash640x400.png"
set "OUTPUT_ROOT=%PROJECT_DIR%\build-output"
set "WORK_DIR=%OUTPUT_ROOT%\.work"
set "INPUT_DIR=%WORK_DIR%\input"
set "RUNTIME_DIR=%WORK_DIR%\runtime"
set "APP_IMAGE_ROOT=%OUTPUT_ROOT%\app-image"
set "APP_IMAGE=%APP_IMAGE_ROOT%\%APP_NAME%"
set "MODULES=java.base,java.datatransfer,java.desktop,java.logging,java.management,java.naming,java.net.http,java.prefs,java.scripting,java.sql,java.xml,java.xml.crypto,jdk.charsets,jdk.crypto.ec,jdk.localedata,jdk.unsupported,jdk.zipfs"

echo [1/4] Checking JDK and project files...

if not defined JAVA_HOME (
    echo [ERROR] JAVA_HOME is not set.
    echo [ERROR] Set JAVA_HOME to Liberica JDK Standard 21.
    goto :failure
)

if not exist "%JAVA_HOME%\bin\java.exe" goto :jdk_not_found
if not exist "%JAVA_HOME%\bin\jlink.exe" goto :jdk_not_found
if not exist "%JAVA_HOME%\bin\jpackage.exe" goto :jdk_not_found
if not exist "%JAVA_HOME%\jmods\java.base.jmod" goto :jdk_not_found

set "JAVA_SPEC="
for /f "tokens=2 delims==" %%V in ('call "%JAVA_HOME%\bin\java.exe" -XshowSettings:properties -version 2^>^&1 ^| findstr /C:"java.specification.version"') do set "JAVA_SPEC=%%V"
set "JAVA_SPEC=%JAVA_SPEC: =%"
if not "%JAVA_SPEC%"=="21" (
    echo [ERROR] JAVA_HOME must point to JDK 21. Current version: %JAVA_SPEC%
    goto :failure
)

if not exist "%PROJECT_DIR%\mvnw.cmd" (
    echo [ERROR] Maven Wrapper was not found: %PROJECT_DIR%\mvnw.cmd
    goto :failure
)

if not exist "%ICON_PATH%" (
    echo [ERROR] Application icon was not found:
    echo         %ICON_PATH%
    goto :failure
)

echo [2/4] Building and testing the project...
call "%PROJECT_DIR%\mvnw.cmd" clean verify
if errorlevel 1 (
    echo [ERROR] Maven build failed.
    goto :failure
)

set "VERSION="
for /f "usebackq delims=" %%V in (`powershell -NoProfile -Command "$pom = [xml](Get-Content -Raw -LiteralPath '%PROJECT_DIR%\pom.xml'); $pom.project.version"`) do set "VERSION=%%V"
if not defined VERSION (
    echo [ERROR] Could not read the version from pom.xml.
    goto :failure
)

if not exist "%PROJECT_DIR%\target\%MAIN_JAR%" (
    echo [ERROR] Fat JAR was not found:
    echo         %PROJECT_DIR%\target\%MAIN_JAR%
    goto :failure
)

echo [3/4] Creating Java 21 runtime...
if exist "%WORK_DIR%" rmdir /S /Q "%WORK_DIR%"
if exist "%APP_IMAGE_ROOT%" rmdir /S /Q "%APP_IMAGE_ROOT%"
mkdir "%INPUT_DIR%" || goto :failure
mkdir "%APP_IMAGE_ROOT%" || goto :failure

"%JAVA_HOME%\bin\jlink.exe" ^
  --module-path "%JAVA_HOME%\jmods" ^
  --add-modules "%MODULES%" ^
  --output "%RUNTIME_DIR%" ^
  --strip-debug ^
  --no-header-files ^
  --no-man-pages ^
  --compress=zip-6 ^
  --bind-services
if errorlevel 1 (
    echo [ERROR] Runtime creation failed.
    goto :failure
)

copy /Y "%PROJECT_DIR%\target\%MAIN_JAR%" "%INPUT_DIR%\%MAIN_JAR%" >nul
if errorlevel 1 goto :failure

set "HAS_SPLASH=0"
if exist "%SPLASH_PATH%" (
    copy /Y "%SPLASH_PATH%" "%INPUT_DIR%\splash640x400.png" >nul
    if errorlevel 1 goto :failure
    set "HAS_SPLASH=1"
) else (
    echo [INFO] splash640x400.png was not found. The app-image will be built without a splash screen.
)

echo [4/4] Creating app-image v%VERSION%...
if "%HAS_SPLASH%"=="1" (
    "%JAVA_HOME%\bin\jpackage.exe" ^
      --type app-image ^
      --input "%INPUT_DIR%" ^
      --dest "%APP_IMAGE_ROOT%" ^
      --name "%APP_NAME%" ^
      --main-jar "%MAIN_JAR%" ^
      --main-class "%MAIN_CLASS%" ^
      --app-version "%VERSION%" ^
      --vendor "Nazuha26" ^
      --description "CourseManagerFX %VERSION% - course management application" ^
      --icon "%ICON_PATH%" ^
      --runtime-image "%RUNTIME_DIR%" ^
      --java-options "-Xmx512m" ^
      --java-options "-splash:$APPDIR/splash640x400.png"
) else (
    "%JAVA_HOME%\bin\jpackage.exe" ^
      --type app-image ^
      --input "%INPUT_DIR%" ^
      --dest "%APP_IMAGE_ROOT%" ^
      --name "%APP_NAME%" ^
      --main-jar "%MAIN_JAR%" ^
      --main-class "%MAIN_CLASS%" ^
      --app-version "%VERSION%" ^
      --vendor "Nazuha26" ^
      --description "CourseManagerFX %VERSION% - course management application" ^
      --icon "%ICON_PATH%" ^
      --runtime-image "%RUNTIME_DIR%" ^
      --java-options "-Xmx512m"
)
if errorlevel 1 (
    echo [ERROR] app-image creation failed.
    goto :failure
)

if not exist "%APP_IMAGE%\%APP_NAME%.exe" goto :invalid_image
if not exist "%APP_IMAGE%\app\%MAIN_JAR%" goto :invalid_image
if not exist "%APP_IMAGE%\runtime\bin\java.exe" goto :invalid_image

> "%APP_IMAGE%\.coursemanagerfx-version" echo %VERSION%
if errorlevel 1 (
    echo [ERROR] Could not write the app-image version marker.
    goto :failure
)

if exist "%WORK_DIR%" rmdir /S /Q "%WORK_DIR%"

echo.
echo [OK] CourseManagerFX v%VERSION% app-image created:
echo      %APP_IMAGE%
echo.
exit /b 0

:jdk_not_found
echo [ERROR] JAVA_HOME does not point to a complete JDK 21:
echo         %JAVA_HOME%
goto :failure

:invalid_image
echo [ERROR] app-image validation failed.
goto :failure

:failure
echo.
echo [ERROR] App-image build stopped.
exit /b 1
