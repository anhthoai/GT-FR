# Troubleshooting Guide - Tomcat 11 (Jakarta) Migration

## Problem: 404 Not Found when accessing `/FRServer/LogIn`

### Quick Diagnosis Steps

1. **Test Tomcat directly:**
   ```bash
   curl -v http://localhost:8088/FRServer/
   curl -v http://localhost:8088/FRServer/LogIn
   ```
   If `/FRServer/` works but `/FRServer/LogIn` returns 404, that strongly indicates a servlet registration issue.

2. **Check Tomcat is running:**
   ```bash
   sudo systemctl status tomcat
   # or
   ps aux | grep tomcat
   ```

3. **Verify context path:**
   - WAR file should be named `FRServer.war` or deployed to `/FRServer` context
   - Check: `/opt/tomcat/webapps/FRServer/` exists

### Common Solutions

#### Tomcat 11 / Jakarta note (very common 404 root cause)

If you are running **Tomcat 10.1 / 11** and you see **404** on servlet URLs (e.g. `/FRServer/LogIn`) while `index.jsp` works, the most common cause is a **Jakarta mismatch**:

- **Tomcat 8/9** expects servlets compiled against `javax.servlet.*`
- **Tomcat 10.1/11** expects servlets compiled against `jakarta.servlet.*`

If the WAR was built with `javax.servlet.*` but deployed to Tomcat 11, the servlet annotations are not registered and Tomcat returns **404**.

Fix:
- Use the Tomcat 11 compatible branch (`main`) and export a fresh WAR from Eclipse after setting **Dynamic Web Module 6.0**.

#### Solution 1: Context Path Mismatch

Verify your WAR file name matches the context:
```bash
# Check deployed context
ls -la /opt/tomcat/webapps/

# Should see: FRServer/ or FRServer.war
# If you see: FDRServer/ or ROOT/, that's the problem
```

#### Solution 2: Tomcat Not Binding to Localhost

Edit `/opt/tomcat/conf/server.xml`:
```xml
<Connector port="8088" address="127.0.0.1" ... />
```

Then restart:
```bash
sudo systemctl restart tomcat
```

### Testing Commands

```bash
# Test 1: Direct Tomcat access
curl -I http://localhost:8088/FRServer/

# Test 2: Check if servlet responds
curl -X POST http://localhost:8088/FRServer/LogIn \
  -d "userid=admin&password=test"
```

### Expected Results

- **Working:** Should return 200 OK or redirect to login page
- **Not Working:** Returns 404 Not Found

### Additional Checks

1. **Firewall:**
   ```bash
   sudo ufw status
   # Ensure Tomcat port (e.g. 8088) is reachable from where you are testing (or only localhost if behind a proxy)
   ```

2. **SELinux (if enabled):**
   ```bash
   sudo setsebool -P httpd_can_network_connect 1
   ```

3. **Tomcat logs:**
   ```bash
   tail -f /opt/tomcat/logs/catalina.out
   # Look for deployment errors
   ```

4. **File permissions:**
   ```bash
   sudo chown -R tomcat:tomcat /opt/tomcat/webapps/FRServer
   ```

### Still Not Working?

1. Verify you exported the WAR from the **Tomcat 11 compatible codebase** (Jakarta) and redeployed.
2. Check Tomcat logs for deployment/classloading errors.
3. Verify your servlet mappings exist (Tomcat should list them at startup if debug logging enabled).

