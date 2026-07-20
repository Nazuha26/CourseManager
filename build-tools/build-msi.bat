@echo off
setlocal EnableExtensions

for %%I in ("%~dp0..") do set "PROJECT_DIR=%%~fI"

set "APP_NAME=CourseManagerFX"
set "APP_IMAGE=%PROJECT_DIR%\build-output\app-image\%APP_NAME%"
set "MSI_DIR=%PROJECT_DIR%\build-output\msi"
set "LICENSE_PATH=%PROJECT_DIR%\build-tools\internal\LICENSE.rtf"
set "UPGRADE_UUID=56e190e3-f9a7-4e2f-b2f5-8b04056c9fdb"

echo [1/3] Checking app-image and JDK...

if not defined JAVA_HOME (
    echo [ERROR] JAVA_HOME is not set.
    echo [ERROR] Set JAVA_HOME to Liberica JDK Standard 21.
    goto :failure
)

if not exist "%JAVA_HOME%\bin\jpackage.exe" (
    echo [ERROR] jpackage.exe was not found in JAVA_HOME:
    echo         %JAVA_HOME%
    goto :failure
)

set "JAVA_SPEC="
for /f "tokens=2 delims==" %%V in ('call "%JAVA_HOME%\bin\java.exe" -XshowSettings:properties -version 2^>^&1 ^| findstr /C:"java.specification.version"') do set "JAVA_SPEC=%%V"
set "JAVA_SPEC=%JAVA_SPEC: =%"
if not "%JAVA_SPEC%"=="21" (
    echo [ERROR] JAVA_HOME must point to JDK 21. Current version: %JAVA_SPEC%
    goto :failure
)

if not exist "%APP_IMAGE%\%APP_NAME%.exe" (
    echo [ERROR] app-image was not found:
    echo         %APP_IMAGE%
    echo [ERROR] Run build-tools\01-build-app-image.bat first.
    goto :failure
)

if not exist "%APP_IMAGE%\app\CourseManagerFX.jar" goto :invalid_image
if not exist "%APP_IMAGE%\runtime\bin\java.exe" goto :invalid_image
if not exist "%APP_IMAGE%\.coursemanagerfx-version" goto :invalid_image

if not exist "%LICENSE_PATH%" (
    echo [ERROR] License file was not found: %LICENSE_PATH%
    goto :failure
)

set "VERSION="
for /f "usebackq delims=" %%V in (`powershell -NoProfile -Command "$pom = [xml](Get-Content -Raw -LiteralPath '%PROJECT_DIR%\pom.xml'); $pom.project.version"`) do set "VERSION=%%V"
if not defined VERSION (
    echo [ERROR] Could not read the version from pom.xml.
    goto :failure
)

set /P IMAGE_VERSION=<"%APP_IMAGE%\.coursemanagerfx-version"
if not "%IMAGE_VERSION%"=="%VERSION%" (
    echo [ERROR] pom.xml version is %VERSION%, but app-image version is %IMAGE_VERSION%.
    echo [ERROR] Run build-tools\01-build-app-image.bat again.
    goto :failure
)

echo [2/3] Preparing output directory...
if exist "%MSI_DIR%" rmdir /S /Q "%MSI_DIR%"
mkdir "%MSI_DIR%" || goto :failure

echo [3/3] Creating MSI for CourseManagerFX v%VERSION%...
"%JAVA_HOME%\bin\jpackage.exe" ^
  --type msi ^
  --app-image "%APP_IMAGE%" ^
  --dest "%MSI_DIR%" ^
  --name "%APP_NAME%" ^
  --app-version "%VERSION%" ^
  --vendor "Nazuha26" ^
  --description "CourseManagerFX %VERSION% - course management application" ^
  --license-file "%LICENSE_PATH%" ^
  --win-upgrade-uuid "%UPGRADE_UUID%" ^
  --win-menu ^
  --win-shortcut ^
  --win-dir-chooser
if errorlevel 1 (
    echo [ERROR] MSI creation failed.
    goto :failure
)

dir /B "%MSI_DIR%\*.msi" >nul 2>&1
if errorlevel 1 (
    echo [ERROR] MSI validation failed: no .msi file was created.
    goto :failure
)

echo.
echo [OK] CourseManagerFX v%VERSION% MSI created:
echo      %MSI_DIR%
echo.
exit /b 0

:invalid_image
echo [ERROR] The app-image is incomplete. Rebuild it with 01-build-app-image.bat.
goto :failure

:failure
echo.
echo [ERROR] MSI build stopped.
exit /b 1
