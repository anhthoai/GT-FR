# Server Application Deployment Guide

## Deployment on CloudPanel with Nginx

### Prerequisites
- CloudPanel installed on VPS
- Tomcat 8.5+ installed and running
- MySQL/MariaDB database configured
- Java JDK 8+ installed

### Step 1: Deploy WAR File to Tomcat

1. **Build the WAR file:**
   ```bash
   # From Eclipse or using Maven/Ant
   # Export project as WAR file: FDRServer.war
   ```

2. **Deploy to Tomcat:**
   ```bash
   # Copy WAR file to Tomcat webapps directory
   cp FDRServer.war /opt/tomcat/webapps/
   
   # Or use Tomcat Manager (if enabled)
   # Upload via http://your-server:8080/manager/html
   ```

3. **Verify deployment:**
   ```bash
   # Check Tomcat logs
   tail -f /opt/tomcat/logs/catalina.out
   
   # Test direct access to Tomcat
   curl http://localhost:8080/FRServer/
   ```

### Step 2: Configure Nginx in CloudPanel

#### Option A: Using CloudPanel Web Interface

1. Log in to CloudPanel
2. Go to your site settings
3. Navigate to "Nginx Configuration" or "Advanced Settings"
4. Add the following configuration (see `nginx-config.conf` for full config):

```nginx
upstream tomcat_backend {
    server 127.0.0.1:8080;
    keepalive 32;
}

location /FRServer/ {
    proxy_pass http://tomcat_backend/FRServer/;
    proxy_http_version 1.1;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header Connection "";
    proxy_buffering off;
    proxy_redirect off;
}
```

#### Option B: Manual Configuration

1. **Find your site's Nginx config:**
   ```bash
   # CloudPanel typically stores configs in:
   /etc/nginx/sites-available/your-site-name
   # or
   /etc/nginx/conf.d/your-site-name.conf
   ```

2. **Edit the configuration:**
   ```bash
   sudo nano /etc/nginx/sites-available/your-site-name
   ```

3. **Add the upstream and location blocks** (see `nginx-config.conf`)

4. **Test Nginx configuration:**
   ```bash
   sudo nginx -t
   ```

5. **Reload Nginx:**
   ```bash
   sudo systemctl reload nginx
   # or
   sudo service nginx reload
   ```

### Step 3: Common Issues and Fixes

#### Issue 1: 404 Not Found

**Symptoms:** Getting 404 when accessing `/FRServer/LogIn`

**Causes:**
- Nginx not proxying to Tomcat correctly
- Case sensitivity (LogIn vs login)
- Context path mismatch

**Solutions:**

1. **Check Tomcat is running:**
   ```bash
   sudo systemctl status tomcat
   # or
   ps aux | grep tomcat
   ```

2. **Verify Tomcat port:**
   ```bash
   netstat -tlnp | grep 8080
   # Should show: tcp 0 0 127.0.0.1:8080
   ```

3. **Test direct Tomcat access:**
   ```bash
   curl -v http://localhost:8080/FRServer/
   curl -v http://localhost:8080/FRServer/LogIn
   ```

4. **Check Nginx error logs:**
   ```bash
   sudo tail -f /var/log/nginx/error.log
   ```

5. **Add case-insensitive rewrite rule:**
   ```nginx
   location ~* ^/FRServer/(login|Login|LOGIN)$ {
       rewrite ^/FRServer/(.*)$ /FRServer/LogIn permanent;
   }
   ```

#### Issue 2: 502 Bad Gateway

**Causes:**
- Tomcat not running
- Wrong port in upstream configuration
- Firewall blocking connection

**Solutions:**

1. **Start Tomcat:**
   ```bash
   sudo systemctl start tomcat
   ```

2. **Check upstream configuration:**
   ```nginx
   upstream tomcat_backend {
       server 127.0.0.1:8080;  # Verify this port matches Tomcat
   }
   ```

3. **Check firewall:**
   ```bash
   sudo ufw status
   # Ensure localhost connections are allowed
   ```

#### Issue 3: Static Files Not Loading

**Solution:**
```nginx
location ~* ^/FRServer/(assets|externalexec)/ {
    proxy_pass http://tomcat_backend;
    proxy_set_header Host $host;
    expires 30d;
}
```

### Step 4: Verify Deployment

1. **Test login page:**
   ```
   http://java.goldtek.vn/FRServer/
   ```

2. **Test servlet directly:**
   ```
   http://java.goldtek.vn/FRServer/LogIn
   ```

3. **Check browser console** for any 404 errors on static resources

### Step 5: Database Configuration

1. **Update `user_config.txt`:**
   ```bash
   # Location: /opt/tomcat/webapps/FRServer/user_config.txt
   host=localhost
   port=3306
   database=frdssdata
   username=your_db_user
   password=your_db_password
   ```

2. **Verify database connection:**
   ```bash
   # Check Tomcat logs for database connection errors
   tail -f /opt/tomcat/logs/catalina.out | grep -i "database\|mysql"
   ```

### Step 6: Security Considerations

1. **Restrict Tomcat access:**
   ```bash
   # Edit /opt/tomcat/conf/server.xml
   # Change:
   <Connector port="8080" ... />
   # To:
   <Connector address="127.0.0.1" port="8080" ... />
   ```

2. **Enable HTTPS in Nginx** (use CloudPanel SSL feature)

3. **Set proper file permissions:**
   ```bash
   sudo chown -R tomcat:tomcat /opt/tomcat/webapps/FRServer
   ```

### Troubleshooting Commands

```bash
# Check Tomcat status
sudo systemctl status tomcat

# Check Nginx status
sudo systemctl status nginx

# View Tomcat logs
tail -f /opt/tomcat/logs/catalina.out
tail -f /opt/tomcat/logs/localhost.YYYY-MM-DD.log

# View Nginx logs
sudo tail -f /var/log/nginx/error.log
sudo tail -f /var/log/nginx/access.log

# Test Nginx configuration
sudo nginx -t

# Reload services
sudo systemctl reload nginx
sudo systemctl restart tomcat
```

### CloudPanel Specific Notes

- CloudPanel may have its own Nginx configuration management
- Check CloudPanel documentation for adding custom Nginx directives
- Some CloudPanel versions use different config file locations
- CloudPanel may auto-generate SSL certificates - ensure they're configured

