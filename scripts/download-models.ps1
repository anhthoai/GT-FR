# PowerShell script to download large binary files for GT-FR
# Update the URLs below with your actual download links

param(
    [string]$ServerAppPath = "server-app",
    [string]$WindowAppPath = "window-app"
)

Write-Host "GT-FR Large Files Download Script" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green
Write-Host ""

# Check if paths exist
if (-not (Test-Path $ServerAppPath)) {
    Write-Host "Error: Server app path not found: $ServerAppPath" -ForegroundColor Red
    exit 1
}

if (-not (Test-Path $WindowAppPath)) {
    Write-Host "Error: Window app path not found: $WindowAppPath" -ForegroundColor Red
    exit 1
}

# TODO: Update these URLs with your actual download links
$ModelFilesUrl = "YOUR_DOWNLOAD_URL_HERE/models.zip"
$OpenCVDllUrl = "YOUR_DOWNLOAD_URL_HERE/opencv_world410.dll"
$ExternalExecUrl = "YOUR_DOWNLOAD_URL_HERE/externalexec.zip"

# Server App - Model files
Write-Host "Downloading model files for server app..." -ForegroundColor Yellow
$modelPath = Join-Path $ServerAppPath "WebContent\externalexec\model"
New-Item -ItemType Directory -Force -Path $modelPath | Out-Null

# Server App - OpenCV DLL
Write-Host "Downloading OpenCV DLL for server app..." -ForegroundColor Yellow
$externalExecPath = Join-Path $ServerAppPath "WebContent\externalexec"
New-Item -ItemType Directory -Force -Path $externalExecPath | Out-Null

# Example download (uncomment and update URLs when ready)
# try {
#     Write-Host "Downloading models..." -ForegroundColor Cyan
#     Invoke-WebRequest -Uri $ModelFilesUrl -OutFile "$modelPath\models.zip" -UseBasicParsing
#     Expand-Archive -Path "$modelPath\models.zip" -DestinationPath $modelPath -Force
#     Remove-Item "$modelPath\models.zip"
#     Write-Host "Models downloaded successfully!" -ForegroundColor Green
# } catch {
#     Write-Host "Error downloading models: $_" -ForegroundColor Red
# }

Write-Host ""
Write-Host "NOTE: Please update the download URLs in this script first!" -ForegroundColor Yellow
Write-Host "See download-large-files.md for detailed instructions." -ForegroundColor Yellow
Write-Host ""
Write-Host "Required files:" -ForegroundColor Cyan
Write-Host "  - Server App: server-app/WebContent/externalexec/model/*.bin" -ForegroundColor White
Write-Host "  - Server App: server-app/WebContent/externalexec/opencv_world410.dll" -ForegroundColor White
Write-Host "  - Windows App: window-app/bin/Release/model/*.bin" -ForegroundColor White

