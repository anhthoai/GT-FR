# Server Application Deployment Guide (Tomcat 11 / Jakarta)

This project’s server application is designed to run on **Tomcat 11 (Jakarta Servlet)**.

## Prerequisites
- Tomcat 11 installed and running (Jakarta Servlet)
- Java JDK 17+ (recommended for Tomcat 11)
- MySQL/MariaDB configured

## Build WAR (Eclipse)
1. Configure Eclipse project for Tomcat 11:
   - Add Tomcat 11 Runtime: `Window → Preferences → Server → Runtime Environments`
   - Attach runtime: `server-app → Properties → Targeted Runtimes → Tomcat 11`
   - Facets: `server-app → Properties → Project Facets`
     - Dynamic Web Module: **6.0**
     - Java: **17**
2. Clean & build: `Project → Clean…` then `Build All`
3. Export WAR: `server-app → Export… → Web → WAR file`
   - Recommended WAR name: **`FRServer.war`**

## Deploy WAR to Tomcat 11 (example port 8088)
1. Remove old deployment (recommended when migrating):
   - Delete `webapps/FRServer/` and `webapps/FRServer.war`
2. Copy new WAR to `webapps/`:
   - `cp FRServer.war /opt/tomcat/webapps/`
3. Restart Tomcat
4. Verify:
   - `http://localhost:8088/FRServer/` (login page)
   - `http://localhost:8088/FRServer/LogIn` (should **not** be 404)

## Database configuration (`user_config.txt`)

The server reads DB connection settings from:
- `FRServer/user_config.txt` inside the deployed webapp (same folder as `index.jsp`)

Recommended format (matches the repo’s `server-app/WebContent/user_config.example.txt`):

```ini
db_url=jdbc:mysql://localhost:3306/
db_name=frdssdata
mysql_user=frdssdata
mysql_password=frdssdata
similarity threshold=50
enablelog=0
install=1
```

Notes:
- `mysql_user` / `mysql_password` are supported for backwards compatibility.
- Empty password values will **not** override the default password in code (to avoid `password: NO` issues).

## Common issue: 404 on servlet URLs (Tomcat 11)
If `index.jsp` works but `/FRServer/LogIn` is 404, the WAR was likely built against **`javax.servlet.*`**.
Tomcat 11 requires **`jakarta.servlet.*`**. Export the WAR from the Tomcat 11 compatible branch and redeploy.

