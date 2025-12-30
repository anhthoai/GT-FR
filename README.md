# GT-FR - Face Recognition System

GT-FR is a comprehensive face recognition system consisting of three integrated components: a JSP-based server application, a Windows desktop application, and an Android mobile application. The system supports real-time face detection and recognition from multiple cameras, with push notification capabilities for mobile devices.

## System Architecture

```
┌─────────────────┐         ┌──────────────────┐         ┌─────────────────┐
│   Windows App   │────────▶│   Server App     │────────▶│   Mobile App    │
│  (4 Cameras)    │         │   (JSP/Java)     │         │   (Android)     │
│                 │         │                  │         │                 │
│ - Face Detect   │         │ - API Endpoints  │         │ - Notifications │
│ - Recognition   │         │ - Database       │         │ - View Results  │
│ - Send Results  │         │ - Push Notify    │         │ - Device Reg    │
└─────────────────┘         └──────────────────┘         └─────────────────┘
```

## Components

### 1. Server Application (`server-app/`)

A JSP-based web application that serves as the central hub for the face recognition system.

#### Features
- **User Management**: Admin, user, and agent account management
- **Person Registration**: Register and manage person profiles with face features
- **Real-time Monitoring**: View and search recognition events/alarms
- **Group Management**: Organize persons into groups (e.g., VIP, Unknown)
- **Camera Configuration**: Manage camera settings and URLs
- **Push Notifications**: Send Firebase Cloud Messaging notifications to mobile devices
- **Image Storage**: Store recognition images using Google Cloud Storage

#### Technology Stack
- **Backend**: Java Servlets, JSP
- **Database**: MySQL
- **Cloud Services**: 
  - Firebase Cloud Messaging (FCM) for push notifications
  - Google Cloud Storage for image storage
- **Web Server**: Apache Tomcat 11 (Jakarta Servlet)
- **Libraries**: 
  - MySQL Connector
  - Google Cloud Storage SDK
  - Firebase SDK
  - Apache Commons (Logging)
  - JSON processing libraries

#### Key API Endpoints

**Authentication & User Management:**
- `/LogIn` - User/Admin login
- `/AgentLogin` - Agent login
- `/UserCreate`, `/UserEdit`, `/UserDelete`, `/UserSearch` - User management
- `/AgentCreate`, `/AgentEdit`, `/AgentDelete`, `/AgentSearch` - Agent management

**Person Management:**
- `/Register` - Register new person
- `/RegisterFromUser` - User registration
- `/PhotoUpload` - Upload person photos
- `/PersonSearch`, `/PersonEdit`, `/PersonDelete` - Person management

**Recognition & Alarms:**
- `/SaveToDatabase` - Save recognition results
- `/AlarmSearch` - Search alarm/recognition history
- `/RealTimeAlarmSearch` - Real-time alarm search
- `/Search` - General search functionality

**Camera Management:**
- `/SaveCameraUrl` - Save camera configuration
- `/ReadCameraInfo` - Read camera settings

**Device Registration:**
- `/RegisterDevice` - Register mobile device token for push notifications

**Group Management:**
- `/GroupRegister`, `/GroupUpdate`, `/GroupDelete`, `/GroupSearch` - Group operations

#### Database Schema

The system uses MySQL with the following main tables:

- **`alarm_info`**: Stores face recognition events/alarms
  - Fields: alarm_id, access (timestamp), verify_state, score, name, adminid, group_name, etc.

- **`person_info`**: Stores registered person information
  - Fields: name, sex, birthday, email, phone, person_img (BLOB), feature (face encoding), group_name, etc.

- **`camera_info`**: Stores camera configurations
  - Fields: user_id, adminid, camera1_name, camera1_url, camera2_name, camera2_url, etc. (supports 4 cameras)

- **`device_lists`**: Stores mobile device tokens for push notifications
  - Fields: token, adminid

- **`group_info`**: Stores person groups
  - Fields: name, adminid

- **`users_info`**: Stores user account information
  - Fields: fullname, userid, password, member_type, etc.

#### Setup Instructions

1. **Prerequisites:**
   - Java JDK 17 or higher (recommended for Tomcat 11)
   - Apache Tomcat 11 (Jakarta Servlet)
   - MySQL 5.7 or higher
   - Eclipse IDE (for development)

2. **Database Setup:**
   ```sql
   -- Import the database schema
   mysql -u root -p < server-app/frdata.sql
   ```

3. **Configuration:**
   - Update `MySQLConfig.java` with your database credentials
   - Configure database connection via `server-app/WebContent/user_config.txt` (see `server-app/WebContent/user_config.example.txt`)
   - Place `FaceRecognition-966aee651648.json` (Google Cloud service account key) in the WebContent root
   - Configure Firebase API key in `PushNotifHelper.java`
   - Update `user_config.txt` with database connection settings (on the server: inside the deployed `/FRServer/` webapp folder)

4. **Tomcat 11 migration steps (Eclipse)**
   - **Install WTP**: Use an Eclipse package that includes *Enterprise Java / Web Tools (WTP)*. If Tomcat 11 is not available in the runtime list, install “Eclipse Enterprise Java and Web Developer Tools”.
   - **Add Tomcat 11 runtime**: `Window → Preferences → Server → Runtime Environments → Add… → Apache Tomcat v11.0`.
   - **Attach runtime to project**: Right-click `server-app` → `Properties → Targeted Runtimes` → select **Tomcat 11**.
   - **Update facets**: Right-click `server-app` → `Properties → Project Facets`:
     - **Dynamic Web Module**: **6.0**
     - **Java**: **17** (or whatever your Tomcat 11 JVM is)
   - **Clean build**: `Project → Clean…` then `Project → Build All`.
   - **Export WAR**: Right-click `server-app` → `Export… → Web → WAR file` (recommended WAR name: `FRServer.war`).

5. **Deployment (Tomcat 11)**
   - Copy the generated `FRServer.war` to Tomcat’s `webapps/` folder
   - Restart Tomcat
   - Test:
     - `http://localhost:8088/FRServer/` (login page)
     - `http://localhost:8088/FRServer/LogIn` (servlet mapping should not be 404)

6. **Tomcat 8 legacy branch (optional)**
   - If you need Tomcat 8 / Java 8 compatibility, use the `tomcat8-legacy` branch.
   - Tomcat 8 uses `javax.servlet.*`; Tomcat 11 uses `jakarta.servlet.*` and is not backwards compatible.

#### JSP Pages

- `index.jsp` - Login page
- `register.jsp` - Person registration
- `search.jsp` - Search interface
- `real_time_display.jsp` - Real-time recognition display
- `history_search.jsp` - Historical search
- `group_management.jsp` - Group management
- `agents.jsp` - Agent management
- `user_search.jsp`, `admin_search.jsp` - User search interfaces

---

### 2. Windows Application (`window-app/`)

A Microsoft Foundation Classes (MFC) desktop application that performs real-time face detection and recognition from up to 4 IP/USB cameras.

#### Features
- **Multi-Camera Support**: Simultaneously process up to 4 cameras (IP or USB)
- **Real-time Face Detection**: Detect faces in video streams using Intel Inference Engine
- **Face Recognition**: Match detected faces against registered persons in the database
- **Live Display**: Show recognized and unrecognized faces in real-time
- **Server Integration**: Send recognition results to the server application
- **Camera Configuration**: Configure camera URLs and settings per camera

#### Technology Stack
- **Framework**: Microsoft Foundation Classes (MFC)
- **Computer Vision**: 
  - OpenCV 4.1.0
  - Intel OpenVINO Inference Engine
- **Networking**: HTTP client for server communication
- **Build System**: Visual Studio 2015/2017

#### Key Components

**Main Classes:**
- `CFaceRecognitionMFCDlg` - Main dialog class managing camera threads
- `FaceDetectionClass` - Face detection using Intel Inference Engine
- `FaceRecognitionClass` - Face recognition and feature extraction
- `CameraURLSetting` - Camera configuration dialog
- `RegisterPerson` - Person registration interface
- `SettingDlg` - Application settings

**Thread Functions:**
- `thread_func1`, `thread_func2`, `thread_func3`, `thread_func4` - Camera capture threads (one per camera)
- `thread_recognition` - Recognition processing thread

#### Build Instructions

1. **Prerequisites:**
   - Visual Studio 2015 or later
   - OpenCV 4.1.0 (pre-built or compiled)
   - Intel OpenVINO Toolkit
   - Windows SDK

2. **Build Configuration:**
   - **Debug**: Debug build with debug libraries
   - **Release**: Optimized release build
   - **Release_GPU**: GPU-accelerated build (requires compatible GPU)

3. **Setup:**
   - Configure camera URLs/IP addresses in the application settings
   - Set server URL and authentication credentials
   - Place face detection and recognition model files in `bin/Release/model/` directory
   - Ensure required DLLs are in the executable directory

4. **Model Files:**
   - Face detection model (XML + BIN)
   - Face recognition model (XML + BIN)
   - Model mapping file

#### Camera Configuration

The application supports:
- **IP Cameras**: Configure via RTSP URL (e.g., `rtsp://ip:port/stream`)
- **USB Cameras**: Select by camera index (0, 1, 2, 3)

Camera settings are stored per user and can be configured through the Camera URL Setting dialog.

---

### 3. Mobile Application (`mobile-app/`)

An Android application that receives push notifications when face recognition events occur.

#### Features
- **Device Registration**: Automatically registers device token with server on first launch
- **Push Notifications**: Receives Firebase Cloud Messaging notifications for recognition events
- **Notification Display**: Shows recognition results with person name, time, group, and image
- **Settings**: Configure server URL and admin ID
- **Offline Storage**: Local SQLite database for notification history

#### Technology Stack
- **Platform**: Android (minSdk 19, targetSdk 35)
- **Language**: Java
- **Firebase**: 
  - Firebase Cloud Messaging (FCM)
  - Firebase Analytics
- **UI**: AndroidX libraries
- **Image Loading**: Glide library

#### Key Components

**Activities:**
- `MainActivity` - Main activity displaying notifications
- `SettingActivity` - Settings configuration

**Services:**
- `MyFirebaseMessagingService` - Handles incoming FCM messages

**Models:**
- `NotificationModel` - Notification data model
- `SQLiteHelper` - Local database helper

#### Build Instructions

1. **Prerequisites:**
   - Android Studio
   - Android SDK (API 19+)
   - Firebase project with FCM enabled

2. **Setup:**
   - Place `google-services.json` in `app/` directory
   - Configure server URL in `app/src/main/assets/server_url.txt`
   - Configure admin ID in `app/src/main/assets/admin_config.txt`
   - Update `key.properties` with signing configuration (for release builds)

3. **Build:**
   ```bash
   ./gradlew assembleDebug    # Debug build
   ./gradlew assembleRelease # Release build
   ```

4. **Installation:**
   - Install APK on Android device (API 19+)
   - Grant necessary permissions (Internet, Storage)
   - Configure server URL and admin ID in settings

#### Configuration Files

- `server_url.txt`: Server base URL (e.g., `http://192.168.1.100:8080/FDRServer/`)
- `admin_config.txt`: Admin ID for device registration

#### Notification Flow

1. App launches and requests FCM token
2. Token is sent to server via `/RegisterDevice` endpoint
3. Server stores token in `device_lists` table
4. When recognition occurs, server sends FCM notification to all registered devices
5. App receives notification and displays recognition result

---

## System Workflow

### Face Recognition Flow

1. **Windows App** captures video from cameras
2. **Face Detection** identifies faces in video frames
3. **Face Recognition** extracts features and matches against database
4. **Results** are sent to server via HTTP POST to `/SaveToDatabase`
5. **Server** stores recognition event in database
6. **Push Notification** is sent to registered mobile devices (if enabled)
7. **Mobile App** receives and displays notification

### Person Registration Flow

1. **Admin/User** uploads person photo via web interface or Windows app
2. **Server** extracts face features and stores in `person_info` table
3. **Person** is assigned to a group (VIP, Unknown, etc.)
4. **Recognition** can now match against this person

---

## Configuration

### Server Configuration

**Database Connection** (`user_config.txt`):
```
host=localhost
port=3306
database=frdssdata
username=root
password=your_password
```

**Firebase Configuration** (`PushNotifHelper.java`):
- Update `FIREBASE_API_KEY` with your Firebase Server Key

**Google Cloud Storage**:
- Place service account JSON key file in WebContent root
- Update bucket name in `SaveToDatabase.java`

### Windows App Configuration

**Server Connection**:
- Set server URL in application settings
- Configure admin/user credentials

**Camera Settings**:
- Configure up to 4 camera URLs/IP addresses
- Settings are saved per user account

### Mobile App Configuration

**Server URL**: Set in `server_url.txt` or app settings
**Admin ID**: Set in `admin_config.txt` or app settings

---

## Dependencies

### Server App
- Java 8+
- MySQL Connector 5.1.43
- Google Cloud Storage SDK
- Firebase SDK
- Apache Commons libraries
- JSON processing libraries

### Windows App
- Visual C++ Runtime
- OpenCV 4.1.0
- Intel OpenVINO Inference Engine
- MFC libraries

### Mobile App
- Android 4.4+ (API 19+)
- Firebase Cloud Messaging
- AndroidX libraries
- Glide image library

---

## Large Binary Files

**Important:** Large binary files (model files, DLLs) are excluded from this repository to avoid GitHub's file size limitations. These files must be downloaded separately.

### Quick Setup

1. **Server Application:**
   - Download face recognition model files and place in `server-app/WebContent/externalexec/model/`
   - Download `opencv_world410.dll` and place in `server-app/WebContent/externalexec/`

2. **Windows Application:**
   - Download model files and place in `window-app/bin/Release/model/` or `window-app/bin/Debug/model/`
   - Download required DLLs (OpenCV, Intel Inference Engine) to the appropriate `bin/` directory

### Detailed Instructions

See **[download-large-files.md](download-large-files.md)** for:
- Complete list of required files
- Download links (to be configured)
- Setup scripts
- File integrity verification

**Note:** You'll need to host these files separately (Google Drive, Dropbox, your own server, etc.) and update the download links in `download-large-files.md`.

---

## Database Updates

See `server-app/DB_Updates.txt` for database schema updates:
- Added `group_name` column to `person_info` and `alarm_info` tables
- Added `adminid` column to `device_lists` table

---

## Security Considerations

1. **Authentication**: All API endpoints require authentication
2. **HTTPS**: Use HTTPS in production for secure communication
3. **Firebase API Key**: Keep Firebase API key secure on server
4. **Database Credentials**: Store database credentials securely
5. **Google Cloud Storage**: Use service account with minimal required permissions

---

## Troubleshooting

### Server Issues
- Check MySQL connection settings
- Verify Firebase API key is correct
- Ensure Google Cloud Storage credentials are valid
- Check Tomcat logs for errors

### Windows App Issues
- Verify camera URLs are accessible
- Check server connectivity
- Ensure model files are present
- Check OpenCV and OpenVINO DLLs are in executable directory

### Mobile App Issues
- Verify server URL is correct and accessible
- Check admin ID matches server configuration
- Ensure Firebase is properly configured
- Check device has internet connectivity

---

## License

[Specify your license here]

---

## Contact & Support

[Add contact information]

---

## Version History

- **Server App**: See `server-app/WebContent/version.txt`
- **Mobile App**: Version 1.3 (versionCode 4)
- **Windows App**: See project files for version information

---

## Additional Notes

- The system supports multiple admin accounts with separate person databases
- Recognition results can be filtered by group
- Push notifications can be enabled/disabled per recognition event
- Historical recognition data is stored with timestamps for search and analysis

