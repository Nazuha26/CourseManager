$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$projectDirectory = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$PrivateKey = Join-Path $projectDirectory 'PrivateKeys\CourseManagerFX-release-private-key.pk8'
$appImage = Join-Path $projectDirectory 'build-output\app-image\CourseManagerFX'
$outputDirectory = Join-Path $projectDirectory 'build-output\release'
$updaterScript = Join-Path $PSScriptRoot 'internal\cman_updater.vbs'
$signerSource = Join-Path $PSScriptRoot 'internal\ReleaseSigner.java'
$publicKey = Join-Path $projectDirectory 'src\main\resources\com\coursemanagerfx\update-public-key.der'
$pomPath = Join-Path $projectDirectory 'pom.xml'

if (-not $env:JAVA_HOME) {
    throw 'JAVA_HOME is not set. Set it to Liberica JDK Standard 21.'
}

$java = Join-Path $env:JAVA_HOME 'bin\java.exe'
$requiredFiles = @(
    $java,
    $pomPath,
    $PrivateKey,
    $updaterScript,
    $signerSource,
    $publicKey,
    (Join-Path $appImage 'CourseManagerFX.exe'),
    (Join-Path $appImage 'app\CourseManagerFX.jar'),
    (Join-Path $appImage 'runtime\bin\java.exe'),
    (Join-Path $appImage '.coursemanagerfx-version')
)

foreach ($file in $requiredFiles) {
    if (-not (Test-Path -LiteralPath $file -PathType Leaf)) {
        throw "Required file is missing: $file"
    }
}

[xml]$pom = Get-Content -Raw -LiteralPath $pomPath
$version = [string]$pom.project.version
if ($version -notmatch '^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)$') {
    throw "pom.xml contains an invalid release version: $version"
}

$appImageVersion = (Get-Content -Raw -LiteralPath `
        (Join-Path $appImage '.coursemanagerfx-version')).Trim()
if ($appImageVersion -ne $version) {
    throw "pom.xml version is $version, but app-image version is $appImageVersion. Run 01-build-app-image.bat again."
}

[System.IO.Directory]::CreateDirectory($outputDirectory) | Out-Null

$archivePath = Join-Path $outputDirectory "CourseManagerFX_v$version.zip"
$signaturePath = "$archivePath.sig"
if ((Test-Path -LiteralPath $archivePath) -or
    (Test-Path -LiteralPath $signaturePath)) {
    throw "Release files already exist for v$version. Delete them or change the version in pom.xml."
}

$staging = Join-Path ([System.IO.Path]::GetTempPath()) `
        ("CourseManagerFX-release-" + [Guid]::NewGuid().ToString('N'))

try {
    Write-Host "[1/3] Preparing CourseManagerFX v$version update package..."

    $payload = Join-Path $staging 'payload'
    [System.IO.Directory]::CreateDirectory($payload) | Out-Null

    Get-ChildItem -LiteralPath $appImage -Force | ForEach-Object {
        Copy-Item -LiteralPath $_.FullName -Destination $payload -Recurse -Force
    }
    Copy-Item -LiteralPath $updaterScript `
            -Destination (Join-Path $staging 'cman_updater.vbs')

    $properties = "format=1`nversion=$version`n"
    [System.IO.File]::WriteAllText(
            (Join-Path $staging 'update.properties'),
            $properties,
            [System.Text.UTF8Encoding]::new($false))

    Write-Host '[2/3] Creating ZIP and Ed25519 signature...'
    Add-Type -AssemblyName System.IO.Compression
    Add-Type -AssemblyName System.IO.Compression.FileSystem

    $zip = [System.IO.Compression.ZipFile]::Open(
            $archivePath,
            [System.IO.Compression.ZipArchiveMode]::Create)

    try {
        $separator = [System.IO.Path]::DirectorySeparatorChar
        $prefix = $staging.TrimEnd($separator) + $separator

        Get-ChildItem -LiteralPath $staging -Recurse -Force |
                Sort-Object FullName |
                ForEach-Object {

            $entryName = $_.FullName.Substring($prefix.Length)
            $entryName = $entryName.Replace($separator, [char]'/')

            if ($_.PSIsContainer) {
                [void]$zip.CreateEntry("$entryName/")
            } else {
                [void][System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
                        $zip,
                        $_.FullName,
                        $entryName,
                        [System.IO.Compression.CompressionLevel]::Optimal)
            }
        }
    } finally {
        $zip.Dispose()
    }

    & $java $signerSource sign $PrivateKey $archivePath $signaturePath
    if ($LASTEXITCODE -ne 0) {
        throw "Release signing failed with exit code $LASTEXITCODE"
    }

    Write-Host '[3/3] Verifying the created signature...'
    & $java $signerSource verify $publicKey $archivePath $signaturePath
    if ($LASTEXITCODE -ne 0) {
        throw "Signature verification failed with exit code $LASTEXITCODE"
    }

    Write-Host ''
    Write-Host '[OK] Upload these two files to the GitHub release:'
    Write-Host "     $archivePath"
    Write-Host "     $signaturePath"
}
catch {
    Remove-Item -LiteralPath $archivePath -Force -ErrorAction SilentlyContinue
    Remove-Item -LiteralPath $signaturePath -Force -ErrorAction SilentlyContinue
    throw
}
finally {
    if (Test-Path -LiteralPath $staging) {
        Remove-Item -LiteralPath $staging -Recurse -Force
    }
}
