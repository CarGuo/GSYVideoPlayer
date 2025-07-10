# Check if base directory, version number, package name, and regex pattern are provided
param (
    [Parameter(Mandatory=$true)]
    [string]$BaseDir,
    [Parameter(Mandatory=$true)]
    [string]$Version,
    [Parameter(Mandatory=$true)]
    [string]$PackageName,
    [Parameter(Mandatory=$true)]
    [string]$RegexPattern
)

# Get all directories in BaseDir that match the regex pattern
$matchingDirs = Get-ChildItem -Path $BaseDir -Directory | Where-Object { $_.Name -match "^$RegexPattern" }

if ($matchingDirs.Count -eq 0) {
    Write-Error "No directories in $BaseDir match the pattern '$RegexPattern'"
    exit 1
}

# Process each matching directory
foreach ($dir in $matchingDirs) {
    $currentBaseDir = $dir.FullName
    $TargetDir = Join-Path $currentBaseDir $Version

    # Check if target directory exists
    if (-not (Test-Path $TargetDir -PathType Container)) {
        Write-Warning "Directory $TargetDir does not exist, skipping"
        continue
    }

    # Delete maven-metadata-local.xml from currentBaseDir if it exists
    $mavenFile = Join-Path $currentBaseDir "maven-metadata-local.xml"
    if (Test-Path $mavenFile) {
        Remove-Item $mavenFile
        Write-Host "Deleted $mavenFile"
    }

    # Change to target directory
    Set-Location $TargetDir

    # Process each file in the directory
    Get-ChildItem -File | Where-Object { $_.Extension -notin @('.asc', '.md5', '.sha1') } | ForEach-Object {
        $file = $_.Name

        # Generate GPG signature
        gpg -ab $file

        # Generate MD5 checksum (only the hash)
        $md5 = Get-FileHash -Algorithm MD5 $file
        $md5.Hash.ToLower() | Out-File "$file.md5" -Encoding ASCII

        # Generate SHA1 checksum (only the hash)
        $sha1 = Get-FileHash -Algorithm SHA1 $file
        $sha1.Hash.ToLower() | Out-File "$file.sha1" -Encoding ASCII

        Write-Host "Processed: $file in $TargetDir"
    }

    # Create output directory with timestamp and currentBaseDir's last part
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $baseDirLastPart = Split-Path $currentBaseDir -Leaf
    $outputDirName = "${timestamp}_${baseDirLastPart}"
    $parentDir = Split-Path $currentBaseDir -Parent
    $outputDir = Join-Path $parentDir $outputDirName

    # Convert package name to directory structure (e.g., io.github.carguo to io\github\carguo)
    $packagePath = $PackageName.Replace('.', '\')

    # Construct the final output path step-by-step
    $finalOutputPath = Join-Path $outputDir $packagePath
    $finalOutputPath = Join-Path $finalOutputPath $baseDirLastPart
    $finalOutputPath = Join-Path $finalOutputPath $Version

    # Validate finalOutputPath
    if (-not $finalOutputPath) {
        Write-Error "Failed to construct final output path for $currentBaseDir"
        continue
    }

    # Check if source and destination are the same
    if ($TargetDir -eq $finalOutputPath) {
        Write-Error "Source and destination directories are the same: $TargetDir"
        continue
    }

    # Create the output directory structure
    New-Item -ItemType Directory -Path $finalOutputPath -Force | Out-Null
    Write-Host "Created output directory: $finalOutputPath"

    # Copy all contents from TargetDir to the final output path
    Copy-Item -Path "$TargetDir\*" -Destination $finalOutputPath -Recurse -Force
    Write-Host "Copied contents to $finalOutputPath"

    # Compress the 'io' directory inside the output directory into a ZIP file
    $ioDir = Join-Path $outputDir "io"
    $zipFilePath = Join-Path $parentDir "${outputDirName}.zip"
    if (-not (Test-Path $ioDir -PathType Container)) {
        Write-Error "Directory $ioDir does not exist"
        continue
    }
    Compress-Archive -Path $ioDir -DestinationPath $zipFilePath -Force
    Write-Host "Compressed contents of $ioDir to $zipFilePath"

    # Delete the output directory after compression
    Remove-Item -Path $outputDir -Recurse -Force
    Write-Host "Deleted output directory: $outputDir"
}

Write-Host "Processing completed for all matching directories"
