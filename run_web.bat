@echo off
title UniVibe - Local Web Server
echo ======================================================================
echo   STARTING UNIVIBE LOCAL WEBSITE SERVER (PORT 8080)
echo ======================================================================

start "" http://localhost:8080

java -cp "bin;lib/*" com.univibe.web.UniVibeWebServer 8080

pause
