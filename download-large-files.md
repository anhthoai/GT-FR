# Download Large Binary Files

This repository excludes large binary files to avoid GitHub's file size limitations. Please download these files separately and place them in the correct locations.

## Server Application Files

### Face Recognition Model Files
**Location:** `server-app/WebContent/externalexec/model/`

1. **Face Recognition Model** (89.53 MB)
   - File: `20180402-114759.bin`
   - File: `20180402-114759.xml`
   - File: `20180402-114759.mapping`
   - **Download from:** [Add your download link here - e.g., Google Drive, Dropbox, or your hosting]
   - Place in: `server-app/WebContent/externalexec/model/`

### OpenCV DLL
**Location:** `server-app/WebContent/externalexec/`

1. **OpenCV World DLL** (70.86 MB)
   - File: `opencv_world410.dll`
   - **Download from:** [Add your download link here]
   - Place in: `server-app/WebContent/externalexec/`

### Other External Executables
**Location:** `server-app/WebContent/externalexec/`

The following files should be placed in `server-app/WebContent/externalexec/`:
- `OpenVinoFaceEngine.exe`
- `*.dll` files (Intel Inference Engine, OpenCV, etc.)
- `model/` directory with face detection and recognition models

**Download from:** [Add your download link here]

## Windows Application Files

### Model Files
**Location:** `window-app/bin/Release/model/` or `window-app/bin/Debug/model/`

1. **Face Detection Model**
   - File: `face-detection-retail-0004.bin`
   - File: `face-detection-retail-0004.xml`
   - **Download from:** [Add your download link here]

2. **Face Recognition Model**
   - File: `20180402-114759.bin`
   - File: `20180402-114759.xml`
   - File: `20180402-114759.mapping`
   - **Download from:** [Add your download link here]

### Required DLLs
**Location:** `window-app/bin/Release/` or `window-app/bin/Debug/`

The following DLLs are required (download from Intel OpenVINO and OpenCV distributions):
- `inference_engine.dll`
- `opencv_world410.dll`
- `tbb.dll`
- `tbbmalloc.dll`
- Other Intel Inference Engine plugin DLLs

**Download from:** 
- Intel OpenVINO Toolkit: https://software.intel.com/content/www/us/en/develop/tools/openvino-toolkit.html
- OpenCV: https://opencv.org/releases/

## Setup Instructions

1. **For Server Application:**
   ```bash
   cd server-app/WebContent/externalexec
   # Download and extract model files to model/ directory
   # Download opencv_world410.dll to this directory
   ```

2. **For Windows Application:**
   ```bash
   cd window-app/bin/Release
   # Download model files to model/ directory
   # Download required DLLs to this directory
   ```

## Alternative: Use Build Scripts

You can create download scripts to automate this process. Example:

### Windows (PowerShell)
```powershell
# download-models.ps1
$modelUrl = "YOUR_DOWNLOAD_URL"
$outputPath = "server-app/WebContent/externalexec/model"
New-Item -ItemType Directory -Force -Path $outputPath
Invoke-WebRequest -Uri $modelUrl -OutFile "$outputPath/models.zip"
Expand-Archive -Path "$outputPath/models.zip" -DestinationPath $outputPath
```

### Linux/Mac (Bash)
```bash
#!/bin/bash
# download-models.sh
MODEL_URL="YOUR_DOWNLOAD_URL"
OUTPUT_PATH="server-app/WebContent/externalexec/model"
mkdir -p "$OUTPUT_PATH"
wget "$MODEL_URL" -O "$OUTPUT_PATH/models.zip"
unzip "$OUTPUT_PATH/models.zip" -d "$OUTPUT_PATH"
```

## Notes

- These files are excluded from Git to keep the repository size manageable
- Always verify file integrity after downloading (use checksums if provided)
- Keep backup copies of these files in a secure location
- Update download links in this file when files are moved to new locations

