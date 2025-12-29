#!/bin/bash
# Bash script to download large binary files for GT-FR
# Update the URLs below with your actual download links

SERVER_APP_PATH="${1:-server-app}"
WINDOW_APP_PATH="${2:-window-app}"

echo "GT-FR Large Files Download Script"
echo "=================================="
echo ""

# Check if paths exist
if [ ! -d "$SERVER_APP_PATH" ]; then
    echo "Error: Server app path not found: $SERVER_APP_PATH"
    exit 1
fi

if [ ! -d "$WINDOW_APP_PATH" ]; then
    echo "Error: Window app path not found: $WINDOW_APP_PATH"
    exit 1
fi

# TODO: Update these URLs with your actual download links
MODEL_FILES_URL="YOUR_DOWNLOAD_URL_HERE/models.zip"
OPENCV_DLL_URL="YOUR_DOWNLOAD_URL_HERE/opencv_world410.dll"
EXTERNAL_EXEC_URL="YOUR_DOWNLOAD_URL_HERE/externalexec.zip"

# Server App - Model files
echo "Setting up directories for server app..."
mkdir -p "$SERVER_APP_PATH/WebContent/externalexec/model"
mkdir -p "$SERVER_APP_PATH/WebContent/externalexec"

# Example download (uncomment and update URLs when ready)
# echo "Downloading models..."
# wget "$MODEL_FILES_URL" -O "$SERVER_APP_PATH/WebContent/externalexec/model/models.zip"
# unzip "$SERVER_APP_PATH/WebContent/externalexec/model/models.zip" -d "$SERVER_APP_PATH/WebContent/externalexec/model"
# rm "$SERVER_APP_PATH/WebContent/externalexec/model/models.zip"
# echo "Models downloaded successfully!"

echo ""
echo "NOTE: Please update the download URLs in this script first!"
echo "See download-large-files.md for detailed instructions."
echo ""
echo "Required files:"
echo "  - Server App: server-app/WebContent/externalexec/model/*.bin"
echo "  - Server App: server-app/WebContent/externalexec/opencv_world410.dll"
echo "  - Windows App: window-app/bin/Release/model/*.bin"

