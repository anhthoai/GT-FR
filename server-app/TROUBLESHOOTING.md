# Troubleshooting Guide - 404 Error on CloudPanel/Nginx

## Problem: 404 Not Found when accessing `/FRServer/LogIn` on VPS

### Quick Diagnosis Steps

1. **Test Tomcat directly:**
   ```bash
   curl -v http://localhost:8080/FRServer/
   curl -v http://localhost:8080/FRServer/LogIn
   ```
   If this works, the issue is with Nginx configuration.

2. **Check Tomcat is running:**
   ```bash
   sudo systemctl status tomcat
   # or
   ps aux | grep tomcat
   ```

3. **Check Nginx error logs:**
   ```bash
   sudo tail -f /var/log/nginx/error.log
   ```

4. **Verify context path:**
   - WAR file should be named `FRServer.war` or deployed to `/FRServer` context
   - Check: `/opt/tomcat/webapps/FRServer/` exists

### Common Solutions

#### Solution 1: Fix Nginx Proxy Configuration

Add to your CloudPanel Nginx config:

```nginx
upstream tomcat_backend {
    server 127.0.0.1:8080;
}

location /FRServer/ {
    proxy_pass http://tomcat_backend/FRServer/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_http_version 1.1;
    proxy_set_header Connection "";
    proxy_buffering off;
}
```

#### Solution 2: Case Sensitivity Issue

The servlet is mapped as `/LogIn` (capital L, I). Nginx might be case-sensitive.

**Fix A:** Add case-insensitive rewrite:
```nginx
location ~* ^/FRServer/(login|Login|LOGIN)$ {
    rewrite ^/FRServer/(.*)$ /FRServer/LogIn permanent;
}
```

**Fix B:** Code has been updated to accept both `/LogIn` and `/login`

#### Solution 3: Context Path Mismatch

Verify your WAR file name matches the context:
```bash
# Check deployed context
ls -la /opt/tomcat/webapps/

# Should see: FRServer/ or FRServer.war
# If you see: FDRServer/ or ROOT/, that's the problem
```

#### Solution 4: Tomcat Not Binding to Localhost

Edit `/opt/tomcat/conf/server.xml`:
```xml
<Connector port="8080" address="127.0.0.1" ... />
```

Then restart:
```bash
sudo systemctl restart tomcat
```

### Step-by-Step Fix for CloudPanel

1. **Access CloudPanel:**
   - Log in to CloudPanel web interface
   - Navigate to your site (java.goldtek.vn)

2. **Add Nginx Configuration:**
   - Go to "Nginx Configuration" or "Advanced Settings"
   - Add the configuration from `cloudpanel-nginx-config.txt`
   - Save changes

3. **Test Configuration:**
   ```bash
   sudo nginx -t
   ```

4. **Reload Nginx:**
   ```bash
   sudo systemctl reload nginx
   ```

5. **Verify:**
   - Access: `http://java.goldtek.vn/FRServer/`
   - Should show login page
   - Try: `http://java.goldtek.vn/FRServer/LogIn` (POST request)

### Testing Commands

```bash
# Test 1: Direct Tomcat access
curl -I http://localhost:8080/FRServer/

# Test 2: Through Nginx
curl -I http://java.goldtek.vn/FRServer/

# Test 3: Check if servlet responds
curl -X POST http://localhost:8080/FRServer/LogIn \
  -d "userid=admin&password=test"

# Test 4: Check Nginx proxy
curl -v http://java.goldtek.vn/FRServer/LogIn
```

### Expected Results

- **Working:** Should return 200 OK or redirect to login page
- **Not Working:** Returns 404 Not Found

### Additional Checks

1. **Firewall:**
   ```bash
   sudo ufw status
   # Ensure port 80, 443, and 8080 (if needed) are open
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

1. Check if CloudPanel has specific requirements
2. Verify Tomcat version compatibility
3. Check if there's a reverse proxy already configured
4. Review CloudPanel documentation for custom Nginx configs

